package peterlavalle

import java.io.File

import language.implicitConversions

package object ski {

	def ?? : Nothing = {
		val notImplementedError: NotImplementedError = new NotImplementedError
		notImplementedError.setStackTrace(notImplementedError.getStackTrace.tail)
		throw notImplementedError
	}

	sealed trait TWrapFile {
		val file: File

		def AbsolutePath: String =
			file.getAbsolutePath.replace("\\", "/")

		def makeParents() =
			require(file.getParentFile.exists() || file.getParentFile.mkdirs())

		def contents: Stream[(String, File)] = {
			def recu(todo: List[String]): Stream[(String, File)] = {
				todo match {
					case Nil => Stream.Empty

					case name :: tail =>
						val child = new File(file, name)

						child.list() match {
							case null => (name, child) #:: recu(tail)

							case list => recu(list.foldRight(tail)(name + "/" + _ :: _))
						}
				}
			}

			recu(file.list().toList)
		}
	}

	implicit def wrapFile(value: File): TWrapFile =
		new TWrapFile {
			override val file: File = value.getAbsoluteFile
		}
}
