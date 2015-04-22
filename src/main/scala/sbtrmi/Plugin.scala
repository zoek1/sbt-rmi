package sbtrmi

import sbt._
import Keys._


object RMIServer extends sbt.Plugin {
  val rmiPort = settingKey[Int]("Port runnning rmiserver.")
  val rmiPath = settingKey[String]("Path where rmiServer will be executed.")
  val rmiClass = settingKey[String]("Classes to compile with rmiserver")
  val rmiStart = taskKey[Unit]("really wake up the rmi server")
  val rmiStop = taskKey[Unit]("stop the rmi server")
  val rmiCompile = taskKey[Unit]("Compile with rmic")


  val rmiSettings = Seq(
     rmiPort := 8081,
     rmiPath := "/target/scala-2.10/",
     rmiClass := "Server",
     rmiStart := {
       println("despertando a rmi!" )
      sys.process.Process(Seq("rmiregistry", rmiPort.value.toString ), new java.io.File("/home/zoek/workspace/scala/sbt-rmi/target/scala-2.10/sbt-0.13/classes/sbtrmi")).!
    },
     rmiStop := {
         import scala.sys.process._
        "killall rmiregistry".!!
	       println("matando a todos los rmi >.<!")
     },
     rmiCompile := {
       val scalaVer = "scala-" + scalaVersion.value.toString.replaceAll("\\.\\d+$", "")
       val sbtVer = "sbt-" + sbtVersion.value.toString.replaceAll("\\.\\d+$", "")
       val classes = file(s"./target/${scalaVer}/${sbtVer}") ** "*.class"
       sys.process.Process(Seq("rmic", rmiClass.value.toString  + ".class"), new java.io.File(s"./target/${scalaVer}/${sbtVer}/classes/sbtrmi")).!
     }
  )

  override lazy val settings = rmiSettings ++ Seq(commands += rmiServer)

  lazy val rmiServer = Command.command("rmiServer") { (state: State) =>
    val extracted: Extracted = Project.extract(state)
    import extracted._

    //val rmiStartKey = Keys.rmiStop
    val thread = new Thread {
      override def run {
        val result: Option[Result[Unit]] = Project.evaluateTask(rmiStart, state)
       // handle the result
       result match {
          case None => println("key no fue definida")// Key wasn't defined.
          case Some(Inc(inc)) => println("hacer algo con " + inc)
          case Some(Value(v)) => println("todo ok")// do something with v: inc.Analysis
       }
      }
    }

    thread.start

    // get name of current project
    val nameOpt: Option[String] = name in currentRef get structure.data




    println("wake up! " + nameOpt)
    state
  }
}
