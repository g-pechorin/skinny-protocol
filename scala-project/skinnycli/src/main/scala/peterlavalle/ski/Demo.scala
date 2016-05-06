package peterlavalle.ski

import java.io.File

object Demo extends App {

	Main.main(
		Array(
			"-i", new File("C:\\Users\\peter\\Desktop\\SceneHost\\Assets").getAbsolutePath,
			"-o:cs", new File("C:\\Users\\peter\\Desktop\\SceneHost\\Assets").getAbsolutePath
		)
	)
}
