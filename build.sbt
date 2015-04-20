import AssemblyKeys._

assemblySettings

sbtPlugin := true

name := "sbt-rmi"

organization := "org.kaltia"

version := "0.1"

sbtVersion in Global := "0.13.8"

scalaVersion in Global := "2.10.3"

description := "sbt plugin for compile and run with rmi dependencies"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-actor" % "2.3.9"

jarName in assembly := "sbt-rmi.jar"
