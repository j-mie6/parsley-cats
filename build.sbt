val projectName = "parsley-cats"
val Scala213 = "2.13.10"
val Scala212 = "2.12.17"
val Scala3 = "3.2.1"

Global / onChangedBuildSource := ReloadOnSourceChanges

inThisBuild(List(
  tlBaseVersion := "0.1",
  organization := "com.github.j-mie6",
  startYear := Some(2022),
  homepage := Some(url("https://github.com/j-mie6/parsley-cats")),
  licenses := List("BSD-3-Clause" -> url("https://opensource.org/licenses/BSD-3-Clause")),
  developers := List(
    tlGitHubDev("j-mie6", "Jamie Willis")
  ),
  tlSonatypeUseLegacyHost := false,
  //tlFatalWarningsInCi := false,
  versionScheme := Some("early-semver"),
  crossScalaVersions := Seq(Scala213, Scala212, Scala3),
  scalaVersion := Scala213,
  // CI Configuration
  tlCiReleaseBranches := Seq("master"),
  githubWorkflowJavaVersions := Seq(JavaSpec.temurin("8"), JavaSpec.temurin("11"), JavaSpec.temurin("17")),
))

lazy val root = tlCrossRootProject.aggregate(core)

lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := projectName,
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.8.0" % Provided,
      "com.github.j-mie6" %%% "parsley" % "4.0.0" % Provided,
      "org.scalatest" %%% "scalatest" % "3.2.12" % Test,
    )
  )
