package br.gov.lexml.parser.input.driver.docx

import br.gov.lexml.parser.input.docx.DOCXReader
import br.gov.lexml.parser.input.driver.{InputConversionError, InputConversionResult, InputConverter, InputDriver, MediaType, UnsuportedMediaType}
import zio.*

import java.io.ByteArrayInputStream
import scala.util.matching.Regex

class DOCXInputDriver extends InputDriver:
  import DOCXInputDriver.DOCX_MEDIA_TYPE
  override def name: String = "LexML Parser DOCX input driver"

  override def description: String = "Driver para importação de arquivos DOCX"

  override def acceptMediaTypeRegexps: Set[Regex] = Set(
    DOCX_MEDIA_TYPE.replaceAll("\\.","\\.").r
  )

  override def converterFor(mediaType: Option[MediaType]): IO[InputConversionError, Option[InputConverter]] =
    if mediaType.contains(DOCX_MEDIA_TYPE) then
      ZIO.succeed(Some(DOCXInputConverter))
    else
      ZIO.succeed(None)

object DOCXInputDriver:
  val DOCX_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

object DOCXInputConverter extends InputConverter:
  import DOCXInputDriver.DOCX_MEDIA_TYPE

  class DOCXConversionError(cause : Option[Exception]) extends InputConversionError:
    override def userMessage: String =
      s"""Erro durante a conversão de arquivo DOCX. Para maiores informações, contacte a
         | equipe de desenvolvimento.""".stripMargin

    override def systemMessage: String =
      cause match {
        case None => s"""Erro durante a conversão de arquivo DOCX."""
        case Some(ex) => s"""Erro durante a conversão de arquivo DOCX. Causa: ${ex.getMessage}"""
      }

    override def underlyingException: Option[Throwable] = cause

  def convertFromDOCX(input: Array[Byte]) : IO[InputConversionError, InputConversionResult] =
    try {
      DOCXReader.readDOCX(new ByteArrayInputStream(input)) match {
        case Some(res) => ZIO.succeed(
          InputConversionResult.ScalaXmlResult(Seq(res))
        )
        case None => ZIO.fail(DOCXConversionError(None))
      }
    } catch {
      case ex : Exception => ZIO.fail(DOCXConversionError(Some(ex)))
    }

  override def convert(input: Array[Byte], mediaType: Option[MediaType]): IO[InputConversionError, InputConversionResult] =
    if mediaType.contains(DOCX_MEDIA_TYPE) then
      convertFromDOCX(input)
    else
      ZIO.fail[InputConversionError](UnsuportedMediaType(mediaType, classOf[DOCXInputDriver]))