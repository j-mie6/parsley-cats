import org.scalajs.linker.interface.ESVersion

val projectName = "parsley-cats"
val Scala213 = "2.13.10"
val Scala212 = "2.12.17"
val Scala3 = "3.2.1"

Global / onChangedBuildSource := ReloadOnSourceChanges

inThisBuild(List(
  tlBaseVersion := "1.0",
  organization := "com.github.j-mie6",
  startYear := Some(2022),
  homepage := Some(url("https://github.com/j-mie6/parsley-cats")),
  licenses := List("BSD-3-Clause" -> url("https://opensource.org/licenses/BSD-3-Clause")),
  developers := List(
    tlGitHubDev("j-mie6", "Jamie Willis")
  ),
  versionScheme := Some("early-semver"),
  crossScalaVersions := Seq(Scala213, Scala212, Scala3),
  scalaVersion := Scala213,
  // CI Configuration
  tlCiReleaseBranches := Seq("master", "new-host"),
  tlSonatypeUseLegacyHost := false, // this needs to be switched off when we migrate parsley to the other server too
  githubWorkflowJavaVersions := Seq(JavaSpec.temurin("8"), JavaSpec.temurin("11"), JavaSpec.temurin("17")),
))

lazy val root = tlCrossRootProject.aggregate(`parsley-cats`)

lazy val `parsley-cats` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("parsley-cats"))
  .settings(
    name := projectName,
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.8.0" % Provided,
      "com.github.j-mie6" %%% "parsley" % "4.0.0" % Provided,
      "org.scalatest" %%% "scalatest" % "3.2.12" % Test,
      "org.typelevel" %%% "cats-laws" % "2.8.0" % Test,
    ),
  )
  .jsSettings(
      Test / scalaJSLinkerConfig := scalaJSLinkerConfig.value.withESFeatures(_.withESVersion(ESVersion.ES2018))
  )
