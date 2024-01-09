import org.scalajs.linker.interface.ESVersion
import com.typesafe.tools.mima.core._

val projectName = "parsley-cats"
val Scala213 = "2.13.12"
val Scala212 = "2.12.18"
val Scala3 = "3.3.1"

Global / onChangedBuildSource := ReloadOnSourceChanges

inThisBuild(List(
  tlBaseVersion := "1.3",
  organization := "com.github.j-mie6",
  organizationName := "Parsley-Cats Contributors <https://github.com/j-mie6/parsley-cats/graphs/contributors>",
  startYear := Some(2022),
  homepage := Some(url("https://github.com/j-mie6/parsley-cats")),
  licenses := List("BSD-3-Clause" -> url("https://opensource.org/licenses/BSD-3-Clause")),
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
  tlCiReleaseBranches := Seq("master"),
  tlCiScalafmtCheck := false,
  tlCiHeaderCheck := true,
  githubWorkflowJavaVersions := Seq(JavaSpec.temurin("8"), JavaSpec.temurin("11"), JavaSpec.temurin("17")),
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

    resolvers ++= Opts.resolver.sonatypeOssReleases, // Will speed up MiMA during fast back-to-back releases
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.8.0",
      "com.github.j-mie6" %%% "parsley" % "4.5.0",
      "org.scalatest" %%% "scalatest" % "3.2.17" % Test,
      "org.typelevel" %%% "cats-laws" % "2.8.0" % Test,
    ),

    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oI"),
  )
