import org.scalajs.linker.interface.ESVersion
import com.typesafe.tools.mima.core._

val projectName = "parsley-cats"
val Scala213 = "2.13.14"
val Scala212 = "2.12.18"
val Scala3 = "3.3.3"
val Java11 = JavaSpec.temurin("11")
val Java17 = JavaSpec.temurin("17")
val Java21 = JavaSpec.temurin("21")

val mainBranch = "master"

Global / onChangedBuildSource := ReloadOnSourceChanges

inThisBuild(List(
  tlBaseVersion := "1.5",
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
  tlCiReleaseBranches := Seq(mainBranch),
  tlCiScalafmtCheck := false,
  tlCiHeaderCheck := true,
  githubWorkflowJavaVersions := Seq(Java11, Java17, Java21),
  githubWorkflowAddedJobs += testCoverageJob(githubWorkflowGeneratedCacheSteps.value.toList),
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
      "org.typelevel" %%% "cats-core" % "2.12.0",
      "com.github.j-mie6" %%% "parsley" % "4.6.0",
      "org.scalatest" %%% "scalatest" % "3.2.19" % Test,
      "org.typelevel" %%% "cats-laws" % "2.12.0" % Test,
    ),

    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oI"),
  )
  // 1.5.0 bumped to 0.5, which means the old versions are unfindable (remove at 2.0.0)
  .nativeSettings(
    tlVersionIntroduced := Map(
      "2.13" -> "1.5.0",
      "2.12" -> "1.5.0",
      "3"    -> "1.5.0",
    ),
  )


def testCoverageJob(cacheSteps: List[WorkflowStep]) = WorkflowJob(
    id = "coverage",
    name = "Run Test Coverage and Upload",
    cond = Some(s"github.ref == 'refs/heads/$mainBranch' || (github.event_name == 'pull_request' && github.base_ref == '$mainBranch')"),
    steps =
        WorkflowStep.Checkout ::
        WorkflowStep.SetupSbt ::
        WorkflowStep.SetupJava(List(Java11)) :::
        cacheSteps ::: List(
            WorkflowStep.Sbt(name = Some("Generate coverage report"), commands = List("coverage", "parsley-cats / test", "coverageReport")),
            WorkflowStep.Use(
                name = Some("Upload coverage to Code Climate"),
                ref = UseRef.Public(owner = "paambaati", repo = "codeclimate-action", ref = "v3.2.0"),
                env = Map("CC_TEST_REPORTER_ID" -> "9eaba49e557ae0578e3c7c022038a3641f3524b429ab2379ea6f83ca38c5440e"),
                params = Map("coverageLocations" -> Seq(
                    coverageReport("parsley-cats"),
                ).mkString("\n")),
            )
        )
)

def coverageReport(project: String) = s"$${{github.workspace}}/$project/jvm/target/scala-2.13/coverage-report/cobertura.xml:cobertura"
