package peterlavalle.ski

import fastparse.core.Parsed
import junit.framework.TestCase
import org.junit.Assert._

class ParserTest extends TestCase {
	def testInt8(): Unit = {
		val expected =
			IR.Type.PrimitiveType.Int8

		Parser(expected.source) match {
			case Parsed.Success(tType, _) =>
				assertEquals(expected, tType)
		}
	}

	def testInt8x3(): Unit = {
		val expected =
			IR.Type.Chunk(IR.Type.PrimitiveType.Int8, 3)

		Parser(expected.source) match {
			case Parsed.Success(tType, _) =>
				assertEquals(expected, tType)
		}

	}

	def testInt8UInt32(): Unit = {
		val expected =
			IR.Type.Union(Set(IR.Type.PrimitiveType.Int8, IR.Type.PrimitiveType.Int8, IR.Type.PrimitiveType.Int16))

		Parser(expected.source) match {
			case Parsed.Success(tType, _) =>
				assertEquals(expected, tType)
		}
	}

	def testEnum(): Unit = {
		val expected = IR.Type.Enumeration(Set("Foo", "Bar"))

		Parser(expected.source) match {
			case Parsed.Success(tType, _) =>
				assertEquals(expected, tType)
		}
	}

	val littleSource =
		"""
			|protocol UpDoot {
			| def spawn(id: int32)
			| def move(id: int32, x: single, y: single, z: single)
			| def close(id: int32)
			|}
		""".stripMargin

	def testSpawn = {
		val expected = IR.Procedure("spawn", List(IR.Argument("id", IR.Type.PrimitiveType.Int32)))
		Parser.procedure.parse(expected.source) match {
			case Parsed.Success(tType, _) =>
				assertEquals(expected, tType)
		}
	}

	def testMove = {
		val expected = IR.Procedure("move", List(IR.Argument("id", IR.Type.PrimitiveType.Int32), IR.Argument("x", IR.Type.PrimitiveType.Real32), IR.Argument("y", IR.Type.PrimitiveType.Real32), IR.Argument("z", IR.Type.PrimitiveType.Real32)))
		Parser.procedure.parse(expected.source) match {
			case Parsed.Success(tType, _) =>
				assertEquals(expected, tType)
		}
	}

	def testClose = {
		val expected = IR.Procedure("close", List(IR.Argument("id", IR.Type.PrimitiveType.Int32)))
		Parser.procedure.parse(expected.source) match {
			case Parsed.Success(tType, _) =>
				assertEquals(expected, tType)
		}
	}

	def testLittle(): Unit = {
		val expected =
			IR.Protocol(
				"UpDoot",
				Set(
					IR.Procedure("spawn", List(IR.Argument("id", IR.Type.PrimitiveType.Int32))),
					IR.Procedure("move", List(IR.Argument("id", IR.Type.PrimitiveType.Int32), IR.Argument("x", IR.Type.PrimitiveType.Real32), IR.Argument("y", IR.Type.PrimitiveType.Real32), IR.Argument("z", IR.Type.PrimitiveType.Real32))),
					IR.Procedure("close", List(IR.Argument("id", IR.Type.PrimitiveType.Int32)))
				)
			)

		Parser(littleSource) match {
			case Parsed.Success(tType, _) =>
				assertEquals(expected, tType)
		}
		Parser(expected.source) match {
			case Parsed.Success(tType, _) =>
				assertEquals(expected, tType)
		}
	}

	val bigSource =
		"""
			|protocol UpDoot {
			|
			| // calling spawn returns an interface
			| def spawn(): {
			|
			|   // oh hai! you can do things to me
			|
			|   // move me!
			|   def move(x: single, y: single, z: single)
			|
			|   // shut me down
			|   // the tilda marks this as a destructor
			|   def ~close
			| }
			|}
		""".stripMargin
}
