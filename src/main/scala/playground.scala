package miniscribe

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

@main
def main() =
  // get index
  val backend = FetchBackend()
  val mesbgRoot =
    uri"http://localhost:8080/http://battlescribedata.appspot.com/repos/middle-earth"
  val mesbgIndex =
    uri"$mesbgRoot/index.bsi"
  val indexRequest: Future[Response[Either[String, Array[Byte]]]] = basicRequest
    .get(mesbgIndex)
    .response(asByteArray)
    .send(backend)

  // unzip index file and retrieve text
  def unzipTxtFile(zipFile: Array[Byte]): Future[String] =
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

  val xmlString: Future[String] =
    for
      response <- indexRequest
      zipFile = response.body match
        case Left(error) =>
          throw Exception(s"Failed to reach data repo: $error")
        case Right(zipFile) =>
          zipFile
      result <- unzipTxtFile(zipFile)
    yield result

  xmlString.onComplete {
    case Success(value)     => println(value)
    case Failure(exception) => println(s"Failed to acquire index: $exception")
  }

  val controller = Controller()
  val ui = view.getContent(controller.state)
  document.body.replaceChild(ui.render, document.body.firstElementChild)

// // read xml from string
// Using[InputStream, Unit](Files.newInputStream(Path.of("index.xml"))) {
//   xmlStream =>
//     val xmlDoc = scala.xml.XML.load(xmlStream)
//     println(xmlDoc \ "dataIndexEntries")
//     val names =
//       for
//         entry <- xmlDoc \\ "dataIndexEntry"
//         name = entry \@ "dataName"
//         filePath = entry \@ "filePath"
//       yield (name, filePath)
//     println(names)
// }
