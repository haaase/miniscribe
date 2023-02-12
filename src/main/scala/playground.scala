package miniscribe

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

@main
def main() =

  val controller = Controller()
  val view = View(controller)
  val ui = view.getContent()
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
