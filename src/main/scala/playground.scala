package miniscribe

import org.scalajs.dom.document
@main
def main() =

  val controller = Controller()
  val view = View(controller)
  val ui = view.getContent()
  document.body.replaceWith(ui.render)

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
