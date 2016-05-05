package peterlavalle.ski

import fastparse.ParserApi
import fastparse.all._
import peterlavalle.ski.IR.Type

import scala.language.implicitConversions

object Parser {


	// from : https://github.com/lihaoyi/fastparse/issues/72
	implicit def wrap2_10_6(s: String): ParserApi[Unit] = parserApi(s)

	// from : https://github.com/lihaoyi/fastparse/issues/72
	implicit def wrap2_10_6[T](s: Parser[T]): ParserApi[T] = parserApi(s)

	import fastparse.all._

	val ws = " " | "\r" | "\n" | "\t"
	val WS = ws.rep
	val digit = CharIn('0' to '9')
	val upper = CharIn('A' to 'Z')
	val lower = CharIn('a' to 'z')

	val nameBig = (upper ~ (upper | lower | digit).rep).!
	val nameLow = (lower ~ (upper | lower | digit).rep).!

	val enum: P[Type.Enumeration] =
		("{" ~ nameBig.rep(min = 1, sep = WS ~ "," ~ WS) ~ "}")
			.map {
				case all =>
					IR.Type.Enumeration(all.toSet)
			}

	val integral: P[Type] =
		WS ~ ("int" ~ ("8" | "16" | "32" | "64")).!.map {
			case "int8" => IR.Type.PrimitiveType.Int8
			case "int16" => IR.Type.PrimitiveType.Int16
			case "int32" => IR.Type.PrimitiveType.Int32
		}

	val real: P[IR.Type.RealType] =
		WS ~ ("single" | ("real" ~ ("16" | "32" | "64"))).!.map {
			case "single" | "real32" => IR.Type.PrimitiveType.Real32
		}

	val arrayMember = integral | enum | real

	val array =
		(WS ~ "[" ~ WS ~ arrayMember ~ WS ~ "x" ~ WS ~ digit.rep.! ~ WS ~ "]").map {
			case (eType: Type, size: String) =>
				IR.Type.Chunk(eType, size.toInt)
		}

	val unionMember = array | arrayMember

	val union: P[IR.Type.Union] =
		WS ~ unionMember.rep(min = 2, sep = WS ~ "|" ~ WS).map {
			case (contents) =>
				IR.Type.Union(contents.toSet)
		}

	val argument =
		(WS ~ nameLow ~ WS ~ ":" ~ WS ~ (union | unionMember))
			.map {
				case (aName: String, aType: IR.Type) =>
					IR.Argument(aName, aType)
			}

	val procedure =
		(WS ~ "def" ~ WS ~ nameLow ~ WS ~ "(" ~ WS ~ argument.rep(sep = WS ~ ",") ~ WS ~ ")")
			.map {
				case (pName, pArgs) =>
					IR.Procedure(pName, pArgs.toList)
			}

	val protocolMember =
		procedure | union | unionMember

	val protocol =
		(WS ~ "protocol" ~ WS ~ nameBig ~ WS ~ "{" ~ WS ~ protocolMember.rep(min = 1) ~ WS ~ "}")
			.map {
				case (name, content) =>
					IR.Protocol(
						name,
						content.map(_.asInstanceOf[IR.Named]).toSet
					)
			}

	def apply(source: String): Parsed[IR] =
		(protocol | protocolMember).parse(source, 0)
}
