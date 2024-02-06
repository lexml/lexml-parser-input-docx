package br.gov.lexml.parser.input.driver.docx

import br.gov.lexml.parser.input.docx.DOCXReader

import java.io.OutputStreamWriter
import scala.xml.{MinimizeMode, XML}
import java.nio.file.{Files, Paths}
import scala.annotation.static
import scala.xml.dtd.{DocType, PublicID}

class DOCXInputConverterMain

object DOCXInputConverterMain:
  private val xhtmlDocType = DocType.apply("html", PublicID("-//W3C//DTD XHTML 1.1//EN","http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"),Seq())
  @static
  def main(args : Array[String]) : Unit =
    try {
      val data =
        if args.isEmpty then
          System.in
        else if args.length > 1 then
          sys.error("Uso: DOCXInputConverterMain [path to file...]")
        else
          Files.newInputStream(Paths.get(args.head))
      DOCXReader.readDOCX(data) match {
        case None =>
          sys.error("Não foi possível realizar a conversão.")
        case Some(el) =>
          XML.write(new OutputStreamWriter(System.out), el, "UTF-8", true, xhtmlDocType, MinimizeMode.Never)
          System.out.close()
      }
    } catch {
      case ex : Exception =>
        System.err.println("Erro: ")
        ex.printStackTrace(System.err)
    }


