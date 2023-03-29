package miniscribe

import cats.syntax.all._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array
import scala.scalajs.js.JSConverters._
import org.scalajs.dom.document
import org.scalajs.dom.Blob
import typings.zipJsZipJs.mod.{BlobReader, Entry, TextWriter, ZipReader}
import sttp.client3._
import scalatags.JsDom.all._
import fs2.{Fallible, Stream}
import fs2.data.xml._
import fs2.data.xml.dom._
import fs2.data.xml.scalaXml._
import sttp.model.Uri

object DataBackend:
  // urls
  val backend = FetchBackend()
  val corsProxy =
    uri"https://miniscribe-cors.fly.dev"
  // see https://gallery.bsdata.net/?repo=middle-earth and https://github.com/BSData/gallery for better alternative
  val mesbgRoot =
    uri"$corsProxy/https://battlescribedata.appspot.com/repos/middle-earth"

  // unzip index file and retrieve text
  private def unzipTxtFile(zipFile: Array[Byte]): Future[String] =
    val zipFileJs = Uint8Array.from(zipFile.map(_.toShort).toJSArray)
    val zipFileBlob = new Blob(js.Array(zipFileJs))
    val zipFileReader = new BlobReader(zipFileBlob)
    val zipReader = new ZipReader(zipFileReader)
    val helloWorldWriter = new TextWriter()
    for
      entries <- zipReader.getEntries().toFuture
      firstEntry = entries.shift()
      result <- firstEntry.getData_MEntry(helloWorldWriter).toFuture
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

  private def fetchXMLFile(url: Uri): Future[xml.Document] =
    val request = basicRequest.get(url).response(asByteArray).send(backend)
    for
      response <- request
      zipFile = response.body match
        case Left(error) =>
          throw Exception(s"Failed to reach data repo: $error")
        case Right(zipFile) =>
          zipFile
      xmlString <- unzipTxtFile(zipFile)
    yield parseXML(xmlString)

  def getArmyOptions(filename: String): Future[xml.Document] =
    fetchXMLFile(uri"$mesbgRoot/$filename")

  def buildArmyIndex(): Future[Map[String, xml.Document]] =
    // get index
    val xmlDoc = fetchXMLFile(uri"$mesbgRoot/index.bsi")

      for
        doc <- xmlDoc
        index = for
          entry <- doc \\ "dataIndexEntry"
          // filter by army catalogues
          if entry \@ "dataType" == "catalogue"
          name = entry \@ "dataName"
          filePath = entry \@ "filePath"
        yield (name, filePath)
      allArmies <- index.map { (name, path) =>
        (Future(name), getArmyOptions(path)).tupled
      }.sequence
    yield allArmies.toMap

  //   dataIndex.onComplete {
  //     case Success(value)     => println(value)
  //     case Failure(exception) => println(s"Failed to acquire index: $exception")
  //   }
