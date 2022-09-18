val scalaVer = "2.13.8"

val zioVer = "2.0.2"

lazy val compileDependencies = Seq(
  "dev.zio" %% "zio" % zioVer
) map (_ % Compile)

lazy val settings = Seq(
  name := "zio-hangman",
  version := "1.0.0",
  scalaVersion := scalaVer,
  libraryDependencies ++= compileDependencies
)

lazy val root = (project in file(".")).settings(settings)
