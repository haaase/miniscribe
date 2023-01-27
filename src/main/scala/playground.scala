import scala.util.Using
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import sttp.client3._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import js.annotation._
import scala.collection.mutable
// import js.annotation._

// @js.native
// @JSImport("https://deno.land/x/zipjs/index.js", "TextWriter")
// object TextWriter = js.Dynamic

// @js.native
// @JSImport("https://deno.land/x/zipjs/index.js", "BlobReader")
// object BlobReader = js.Dynamic

@main
def main() =
  // get index
  // val backend = FetchBackend()

  // val mesbgRoot =
  //   uri"http://localhost:8080/http://battlescribedata.appspot.com/repos/middle-earth"
  // val mesbgIndex =
  //   uri"$mesbgRoot/index.bsi"

  // val indexZipped = basicRequest
  //   .get(mesbgIndex)
  //   .response(asByteArray)
  //   .send(backend)
// import {
//   BlobReader,
//   BlobWriter,
//   TextReader,
//   TextWriter,
//   ZipReader,
//   ZipWriter,
// } from "https://deno.land/x/zipjs/index.js";
  // unzip with nodejs module

  object Zip {
    @js.native
    @JSImport("./ziptest.js", "doStuff")
    def doStuff(): Unit = js.native
  }

  Zip.doStuff()
  println("End of playground")
  // for {
  //   r <- indexZipped
  // } {
  //   r.body match
  //     case Left(error) => println(error)
  //     case Right(zip) =>
  //       val mutableSeq: mutable.Seq[Byte] = mutable.Seq(zip.toSeq: _*)
  //       val zipFileReader =
  //         js.Dynamic.newInstance(js.Dynamic.global.BlobReader)(
  //           mutableSeq.toJSArray
  //         )
  //       val helloWorldWriter =
  //         js.Dynamic.newInstance(js.Dynamic.global.TextWriter)()
  //       val zipReader = js.Dynamic.newInstance(js.Dynamic.global.ZipReader)(
  //         zipFileReader
  //       )
  //       for {
  //         firstEntry <- zipReader.getEntries().shift()
  //         text <- firstEntry.getData(helloWorldWriter)
  //         _ <- zipReader.close()
  //       } {
  //         println(text)
  //       }
  // }

  // indexZipped.body match
  //   case Left(error) => println(s"Cannot reach BS data repo: $error")
  //   case Right(content) =>
  //     Files.write(Path.of("index.zip"), content)
  //     zipDecompress("index.zip", ".")

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
