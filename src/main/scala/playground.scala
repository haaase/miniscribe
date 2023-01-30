import scala.util.Failure
import scala.util.Success
import sttp.client3._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import js.annotation._
import typings.zipJsZipJs.mod.{BlobReader, Entry, TextWriter, ZipReader}
import scala.concurrent.Future
import org.scalajs.dom.Blob
import js.JSConverters._
import scala.scalajs.js.typedarray._

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
