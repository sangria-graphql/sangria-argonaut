name := "sangria-argonaut"
organization := "org.sangria-graphql"
version := "1.0.1-SNAPSHOT"
mimaPreviousArtifacts := Set("org.sangria-graphql" %% "sangria-argonaut" % "1.0.0")

description := "Sangria argonaut marshalling"
homepage := Some(url("http://sangria-graphql.org"))
licenses := Seq("Apache License, ASL Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

scalaVersion := "2.13.0"
crossScalaVersions := Seq("2.11.11", "2.12.8", scalaVersion.value)

scalacOptions ++= Seq("-deprecation", "-feature")

scalacOptions ++= {
  if (scalaVersion.value startsWith "2.11")
    Seq("-target:jvm-1.7")
  else
    Seq.empty
}

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria-marshalling-api" % "1.0.4-SNAPSHOT",
  "io.argonaut" %% "argonaut" % "6.2.3",

  "org.sangria-graphql" %% "sangria-marshalling-testkit" % "1.0.2-SNAPSHOT" % Test,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test
)

git.remoteRepo := "git@github.com:sangria-graphql/sangria-argonaut.git"

// Publishing

publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := (_ => false)
publishTo := Some(
  if (version.value.trim.endsWith("SNAPSHOT"))
    "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")

// Site and docs

enablePlugins(SiteScaladocPlugin)
enablePlugins(GhpagesPlugin)

// nice *magenta* prompt!

shellPrompt in ThisBuild := { state =>
  scala.Console.MAGENTA + Project.extract(state).currentRef.project + "> " + scala.Console.RESET
}

// Additional meta-info

startYear := Some(2016)
organizationHomepage := Some(url("https://github.com/sangria-graphql"))
developers := Developer("OlegIlyenko", "Oleg Ilyenko", "", url("https://github.com/OlegIlyenko")) :: Nil
scmInfo := Some(ScmInfo(
  browseUrl = url("https://github.com/sangria-graphql/sangria-argonaut.git"),
  connection = "scm:git:git@github.com:sangria-graphql/sangria-argonaut.git"
))
