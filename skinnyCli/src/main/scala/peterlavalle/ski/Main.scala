package peterlavalle.ski

import java.io.{File, FileWriter, Writer}

import fastparse.core.Parsed

import scala.io.Source

object Main extends App {

	run(args.toList, Nil, Nil)

	case class Output
	(
		format: IR.Protocol => String,
		target: File
	) {
		def apply(protocol: IR.Protocol): Unit = {
			new FileWriter({

				val file = new File(target, protocol.name.replace("/", ".") + ".cs")

				require(file.getParentFile.exists() || file.getParentFile.mkdirs())

				file
			})
				.append(format(protocol))
				.close()
		}
	}

	def run(args: List[String], incs: List[File], outs: List[Output]): Unit =
		args match {
			case "-i" :: path :: tail =>
				run(tail, new File(path).getCanonicalFile :: incs, outs)

			case "-o:cs" :: path :: tail =>
				val done: File = new File(path).getAbsoluteFile

				run(tail, incs, Output(
					(protocol: IR.Protocol) => {
						ForgeCS(protocol)
					},
					done) :: outs)

			case Nil =>
				incs.reverse.foldLeft(Map[String, File]()) {
					case (done, root) =>

						def recu(todo: List[String], done: Map[String, File]): Map[String, File] = {

							todo match {
								case name :: tail =>
									val file = new File(root, name).getAbsoluteFile

									if (file.isDirectory)
										recu(tail ++ file.list().map(name + "/" + _), done)
									else if (!name.endsWith(".sp"))
										recu(tail, done)
									else
										recu(tail, done ++ Map(name -> file))

								case Nil =>
									done
							}
						}

						recu(root.list().toList, done)
				}.foreach {
					case (path, file) =>
						Parser(Source.fromFile(file).mkString) match {
							case Parsed.Success(IR.Protocol(name, contents), _) =>
								outs.foreach(out => out(IR.Protocol(path.replaceAll("\\.sp$", "." + name).replace(".", "/"), contents)))
						}
				}
		}
}
