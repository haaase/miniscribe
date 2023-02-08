package miniscribe.databackend

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

def getArmies(): Future[Seq[(String, String)]] =
  // get index
  val backend = FetchBackend()
  val corsProxy =
    uri"https://miniscribe-cors.fly.dev"
  val mesbgRoot =
    uri"$corsProxy/https://battlescribedata.appspot.com/repos/middle-earth"
  val mesbgIndex =
    uri"$mesbgRoot/index.bsi"
  val indexRequest: Future[Response[Either[String, Array[Byte]]]] = basicRequest
    .get(mesbgIndex)
    .response(asByteArray)
    .send(backend)

  val xmlDoc: Future[xml.Document] =
    for
      response <- indexRequest
      zipFile = response.body match
        case Left(error) =>
          throw Exception(s"Failed to reach data repo: $error")
        case Right(zipFile) =>
          zipFile
      xmlString <- unzipTxtFile(zipFile)
    yield parseXML(xmlString)

  val dataIndex: Future[Seq[(String, String)]] =
    for
      doc <- xmlDoc
      index = for
        entry <- doc \\ "dataIndexEntry"
        // filter by army catalogues
        if entry \@ "dataType" == "catalogue"
        name = entry \@ "dataName"
        filePath = entry \@ "filePath"
      yield (name, filePath)
    yield index

  return dataIndex
//   dataIndex.onComplete {
//     case Success(value)     => println(value)
//     case Failure(exception) => println(s"Failed to acquire index: $exception")
//   }
