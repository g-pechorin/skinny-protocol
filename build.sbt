import sbt.Keys._

lazy val commonSettings =
	Seq(
		organization := "com.peterlavalle.skinny",
		version := "0.0.0-SNAPSHOT",
		scalaVersion := "2.10.6",

		javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),

		libraryDependencies ++= Seq(
			"junit" % "junit" % "4.12" % Test,
			"org.easymock" % "easymock" % "3.4" % Test,

			"com.novocode" % "junit-interface" % "0.11" % Test
				exclude("junit", "junit-dep")
		),

		publishTo := Some(
			Resolver.file(
				"Dropbox",
				new File(Path.userHome.absolutePath + (version.value match {
					case tag if tag matches "\\d+(\\.\\d+)+\\a*" =>
						"/Dropbox/Public/release"
					case tag if tag matches "\\d+(\\.\\d+)+\\a*\\-SNAPSHOT" =>
						"/Dropbox/Public/develop"
				}))
			)
		)
	)

lazy val root = (project in file("."))
	.settings(commonSettings: _*)
	.aggregate(
		skinnyLib,
		skinnyCli,
		skinnySbt
	)

lazy val skinnyLib =
	(project in file("skinnyLib"))
		.settings(commonSettings: _*)
		.enablePlugins(SamonPlugin)
		.settings(
			libraryDependencies += "com.lihaoyi" %% "fastparse" % "0.3.7"
		)

lazy val skinnyCli =
	(project in file("skinnyCli"))
		.settings(commonSettings: _*)
		.dependsOn(skinnyLib)

lazy val skinnySbt =
	(project in file("skinnySbt"))
		.settings(commonSettings: _*)
		.settings(
			sbtPlugin := true
		)
		.dependsOn(skinnyLib)