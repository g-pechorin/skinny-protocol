package peterlavalle.ski

sealed trait IR {
	val source: String
}

object IR {

	sealed trait Named extends IR {
		val name: String
	}

	sealed trait Type extends IR

	object Type {

		sealed trait PrimitiveType extends Type {
			val name = toString
			lazy val source = name.toLowerCase()
		}

		sealed trait IntegralType extends PrimitiveType

		sealed trait RealType extends PrimitiveType

		object PrimitiveType {

			case object Bool8 extends PrimitiveType

			case object Int8 extends IntegralType

			case object Int16 extends IntegralType

			case object Int32 extends IntegralType

			case object Real16 extends RealType

			case object Real32 extends RealType

			case object Real64 extends RealType

			case object UTF8String extends PrimitiveType

			case object Adler32Hash extends PrimitiveType

		}

		case class Union(tTypes: Set[Type]) extends Type {
			override lazy val source: String = tTypes.toList.map(_.source).sorted.reduce(_ + "|\n" + _)
		}

		case class Chain(size: IntegralType, eType: Type) extends Type {
			override lazy val source: String = "[" + size.name + ":" + eType.source + "]"
		}

		case class Chunk(eType: Type, size: Int) extends Type {
			override lazy val source: String = "[" + eType.source + " x" + size + "]"
		}

		case class Packet(members: Map[String, Type]) extends Type {
			override lazy val source: String = members.toList.sortBy(_._1).foldLeft("{\n") { case (l, (n, t)) => l + "\t" + n + ": " + t.source + "\n" } + "}"
		}

		case class Enumeration(values: Set[String]) extends Type {
			override lazy val source: String = "{" + values.reduce(_ + "," + _) + "}"
		}

	}

	case class Argument(name: String, tType: Type) extends Named {
		override lazy val source: String = name + ": " + tType.source
	}

	case class Procedure(name: String, arguments: List[Argument]) extends Named {
		override lazy val source: String =
			"def " + name + "(" + arguments.map(a => a.source).reduce(_ + ", " + _) + ")"
	}

	private val regex: String = "^(.*)/(.*?)$"

	case class Protocol(name: String, contents: Set[Named]) extends Named {

		lazy val simpleName = name.replaceAll(regex, "$2")

		lazy val packageName =
			if (simpleName != name)
				name.replaceAll(regex, "$1").replace("/", ".")
			else
				"ski"

		require({
			val map = contents.foldLeft(Map[String, Named]()) { case (m, e) => m ++ Map(e.name -> e) }

			contents.map(c => map(c.name) eq c).reduce(_ && _)
		})
		override lazy val source: String =
			"protocol " + name + "{" + contents.map {
				case procedure: Procedure =>
					procedure.source + "\n"
			}.reduce(_ + _) + "}"

		lazy val indexedProcedures =
			contents.filter(_.isInstanceOf[IR.Procedure]).toList.sortBy(_.name).zipWithIndex.map {
				case (procedure: IR.Procedure, index: Int) =>
					(index + 1) -> procedure
			}.toMap
	}

}
