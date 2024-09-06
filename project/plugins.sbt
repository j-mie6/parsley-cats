val sbtTypelevelVersion = "0.7.3"

libraryDependencySchemes ++= Seq(
  "org.scala-native" % "sbt-scala-native" % VersionScheme.Always,
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
)

addSbtPlugin("org.typelevel" % "sbt-typelevel" % sbtTypelevelVersion)

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.13.2")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.4.15")

addSbtPlugin("com.beautiful-scala" % "sbt-scalastyle" % "1.5.1")
addSbtPlugin("org.jmotor.sbt" % "sbt-dependency-updates" % "1.2.7")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.9")
addSbtPlugin("com.timushev.sbt" % "sbt-rewarn" % "0.1.3")
