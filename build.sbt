import com.typesafe.tools.mima.core._

val projectName = "parsley-cats"
val Scala213 = "2.13.12"
val Scala212 = "2.12.18"
val Scala3 = "3.3.0"
val Java8 = JavaSpec.temurin("8")
val JavaLTS = JavaSpec.temurin("11")
val JavaLatest = JavaSpec.temurin("17")

val mainBranch = "master"

Global / onChangedBuildSource := ReloadOnSourceChanges

val releaseFlags = Seq("-Xdisable-assertions", "-opt:l:method,inline", "-opt-inline-from", "parsley.**", "-opt-warnings:at-inline-failed")
val noReleaseFlagsScala3 = true // maybe some day this can be turned off...

inThisBuild(List(
  tlBaseVersion := "1.3",
  organization := "com.github.j-mie6",
  organizationName := "Parsley Contributors <https://github.com/j-mie6/Parsley/graphs/contributors>",
  startYear := Some(2022),
  homepage := Some(url("https://github.com/j-mie6/parsley-cats")),
  licenses := List("BSD-3-Clause" -> url("https://opensource.org/licenses/BSD-3-Clause")),
  developers := List(
    tlGitHubDev("j-mie6", "Jamie Willis")
  ),
  versionScheme := Some("early-semver"),
  crossScalaVersions := Seq(Scala213, Scala212, Scala3),
  scalaVersion := Scala213,
  mimaBinaryIssueFilters ++= Seq(
    // Until 2.0 (these are all misreported package private members)
    ProblemFilters.exclude[MissingClassProblem]("parsley.ApplicativeForParsley"),
    ProblemFilters.exclude[MissingClassProblem]("parsley.DeferForParsley"),
    ProblemFilters.exclude[MissingClassProblem]("parsley.FunctorFilterForParsley"),
    ProblemFilters.exclude[MissingClassProblem]("parsley.FunctorForParsley"),
    ProblemFilters.exclude[MissingClassProblem]("parsley.MonadForParsley"),
    ProblemFilters.exclude[MissingClassProblem]("parsley.MonoidKForParsley"),
  ),
  // CI Configuration
  tlCiReleaseBranches := Seq(mainBranch),
  tlSonatypeUseLegacyHost := false,
  tlCiScalafmtCheck := false,
  tlCiHeaderCheck := true,
  githubWorkflowJavaVersions := Seq(Java8, JavaLTS, JavaLatest),
))

lazy val root = tlCrossRootProject.aggregate(`parsley-cats`)

lazy val `parsley-cats` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("parsley-cats"))
  .settings(
    name := projectName,

    headerLicenseStyle := HeaderLicenseStyle.SpdxSyntax,
    headerEmptyLine := false,

    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.8.0",
      "com.github.j-mie6" %%% "parsley" % "4.0.0",
      "org.scalatest" %%% "scalatest" % "3.2.17" % Test,
      "org.typelevel" %%% "cats-laws" % "2.8.0" % Test,
    ),

    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oI"),

    scalacOptions ++= {
        if (!isSnapshot.value && !(noReleaseFlagsScala3 && scalaBinaryVersion.value == "3")) releaseFlags else Seq.empty
    },
  )
  .jsSettings(
      // Test / scalaJSLinkerConfig := scalaJSLinkerConfig.value.withESFeatures(_.withESVersion(ESVersion.ES2018))
  )
