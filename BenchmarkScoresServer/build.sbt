name := "BenchmarkScores"
version := "1.0"
assemblyJarName in assembly := "BenchmarkScoresServer.jar"

scalaVersion := "2.10.5"


// set the main Scala source directory
scalaSource in Compile := baseDirectory.value / "src"

// add dependencies
resolvers += "spray repo" at "http://repo.spray.io"
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
	"io.spray" %% "spray-httpx" % "1.3.1",
	"io.spray" %% "spray-client" % "1.3.1",
	"com.typesafe.akka" %% "akka-osgi" % "2.3.14" exclude("org.osgi", "org.osgi.compendium"),
	"io.spray" %%  "spray-json" % "1.2.5"
	)

    
    