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

				println("Writing `" + file.AbsolutePath + "`")

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
				val file: File = new File(path).getAbsoluteFile

				println(s"including `${file.AbsolutePath}`")

				run(tail, file :: incs, outs)

			case "-o:cs" :: path :: tail =>
				val done: File = new File(path).getAbsoluteFile

				println(s"CS to `${done.AbsolutePath}`")

				run(tail, incs, Output(
					(protocol: IR.Protocol) => {
						ForgeCS(protocol)
					},
					done) :: outs)

			case Nil =>
				incs.reverse.foldLeft(Map[String, File]()) {
					case (done, root) =>
						root.contents.foldLeft(done) {
							case (l, (n, f)) if n.endsWith(".sp") && !l.contains(n) => l ++ Map(n -> f)
							case (l, _) => l
						}

				}.foreach {
					case (path, file) =>
						Parser(Source.fromFile(file).mkString) match {
							case Parsed.Success(IR.Protocol(name, contents), _) =>
								outs.foreach(out => {
									val namePackaged: String = path.replaceAll("\\.sp$", "." + name).replace(".", "/")
									println(s"preparing `$namePackaged`")
									out(IR.Protocol(namePackaged, contents))
								})
						}
				}
		}
}
