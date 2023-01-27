//> using scala "3"
//> using lib "com.softwaremill.sttp.client3::core:3.8.9"
//> using lib "com.github.gekomad::scala-compress:1.0.1"
//> using lib "org.scala-lang.modules::scala-xml:2.1.0"
import scala.util.Using
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import sttp.client3._
import com.github.gekomad.scalacompress.Compressors._
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import sys.process._

// get index
// val backend = HttpClientSyncBackend()

// val mesbgRoot =
//   uri"https://battlescribedata.appspot.com/repos/middle-earth"
// val mesbgIndex =
//   uri"$mesbgRoot/index.bsi"

// val indexZipped = basicRequest
//   .get(mesbgIndex)
//   .response(asByteArray)
//   .send(backend)

// println(indexZipped)

// indexZipped.body match
//   case Left(error) => println(s"Cannot reach BS data repo: $error")
//   case Right(content) =>
//     Files.write(Path.of("index.zip"), content)
//     zipDecompress("index.zip", ".")

// read xml from string
Using[InputStream, Unit](Files.newInputStream(Path.of("index.xml"))) {
  xmlStream =>
    val xmlDoc = scala.xml.XML.load(xmlStream)
    println(xmlDoc \ "dataIndexEntries")
    val names =
      for
        entry <- xmlDoc \\ "dataIndexEntry"
        name = entry \@ "dataName"
        filePath = entry \@ "filePath"
      yield (name, filePath)
    println(names)
}
