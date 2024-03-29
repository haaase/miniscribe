package miniscribe

import cats.syntax.all._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.typedarray.Uint8Array
import scala.scalajs.js.JSConverters._
import org.scalajs.dom.Blob
import typings.zipJsZipJs.mod.{BlobReader, TextWriter, ZipReader}
import sttp.client3._
import fs2.{Fallible, Stream}
import fs2.data.xml._
import fs2.data.xml.dom._
import fs2.data.xml.scalaXml._
import sttp.model.Uri
import typings.idbKeyval.{mod => idbKeyval}

object DataBackend:
  // urls
  private val fetchBackend = FetchBackend()
  private val corsProxy =
    uri"https://miniscribe-cors.fly.dev"
    // uri"http://localhost:8080"
  // see https://gallery.bsdata.net/?repo=middle-earth and https://github.com/BSData/gallery for better alternative
  private val mesbgRoot =
    uri"$corsProxy/https://github.com/BSData/middle-earth/releases/latest/download/"

  // unzip index file and retrieve text
  private def unzipTxtFile(zipFile: Array[Byte]): Future[String] =
    val zipFileJs = Uint8Array.from(zipFile.map(_.toShort).toJSArray)
    val zipFileBlob = new Blob(js.Array(zipFileJs))
    val zipFileReader = new BlobReader(zipFileBlob)
    val zipReader = new ZipReader(zipFileReader)
    val textWriter = new TextWriter()
    for
      entries <- zipReader.getEntries().toFuture
      firstEntry = entries.shift()
      // result <- firstEntry.getData_MEntry(helloWorldWriter).toFuture
      result <- firstEntry.getData
        // necessary fix because scalablytyped could not infer type
        .asInstanceOf[js.Function1[TextWriter, Promise[String]]](textWriter)
        .toFuture
      _ <- zipReader.close().toFuture
    yield result

  // adapted from https://fs2-data.gnieh.org/documentation/xml/libraries/
  private def parseXML(xmlString: String): xml.Document =
    Stream
      .emits(xmlString)
      .through(events[Fallible, Char]())
      .through(documents)
      .compile
      .toList
      .flatMap(docs => Right(docs.head))
      .toTry
      .get

  // fetch a zipped BS data file and return the content as string
  private def fetchFile(url: Uri): Future[String] =
    val request = basicRequest.get(url).response(asByteArray).send(fetchBackend)
    for
      response <- request
      zipFile = response.body match
        case Left(error) =>
          throw Exception(s"Failed to reach data repo: $error")
        case Right(zipFile) =>
          zipFile
      xmlString <- unzipTxtFile(zipFile)
    yield xmlString

  // returns the specified catalogue as xmlDoc. If the specified revision is already cached, use this one.
  def getArmyCatalogue(filename: String, revision: Int): Future[xml.Document] =
    val key = s"$filename$revision"
    val dbResult = idbKeyval.get[String](key).toFuture.map(_.toOption)
    for
      db <- dbResult
      string <- db match
        case Some(value) =>
          // println(s"Cache hit for $key")
          Future.successful(value)
        case None =>
          val file = fetchFile(uri"$mesbgRoot/$filename")
          file.onComplete(f => if f.isSuccess then idbKeyval.set(key, f.get))
          file
    yield parseXML(string)

  def buildArmyIndex(): Future[Map[String, xml.Document]] =
    // fetch index
    val indexName = "middle-earth.latest.bsi"
    val fetchedIndex = fetchFile(uri"$mesbgRoot/$indexName")

    // store index in cache if we succeed
    fetchedIndex.onComplete(i =>
      if i.isSuccess then idbKeyval.set(indexName, i.get)
    )

    val xmlDoc = fetchedIndex
      // recover from cache if fetching the index failed
      .recoverWith { case _ =>
        println("trying to fetch from cache")
        idbKeyval.get[String](indexName).toFuture.map(_.get)
      }
      .map(parseXML)

    for
      doc <- xmlDoc
      index = {
        for
          entry <- doc \\ "dataIndexEntry"
          // filter by army catalogues
          if entry \@ "dataType" == "catalogue"
          name = entry \@ "dataName"
          filePath = entry \@ "filePath"
          revision = (entry \@ "dataRevision").toInt
        yield (name, revision, filePath)
      }
      allArmies <- index.map { (name, revision, path) =>
        (Future.successful(name), getArmyCatalogue(path, revision)).tupled
      }.sequence
    yield allArmies.toMap

  final case class HeroOption(name: String, unique: Boolean = false)
  def getHeroOptions(armyCatalogue: xml.Document): Seq[HeroOption] =
    for
      entries <- armyCatalogue \ "selectionEntries"
      entry <- entries \ "selectionEntry"
      name = entry \@ "name"
      if entry \@ "hidden" == "false" // hide hidden entries
      isUnique = !(for // check if this is a unique hero
        constraintsSec <- entry \ "constraints"
        constraints <- constraintsSec \ "constraint"
      yield constraints)
        .filter(c => c \@ "type" == "max" && c \@ "value" == "1.0")
        .isEmpty
    yield HeroOption(name = name, unique = isUnique)

  //   dataIndex.onComplete {
  //     case Success(value)     => println(value)
  //     case Failure(exception) => println(s"Failed to acquire index: $exception")
  //   }
