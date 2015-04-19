package sbtrmi

import sbt._
import Keys._


object RMIServer extends sbt.Plugin {
  val rmiPort = settingKey[Int]("Port runnning rmiserver.")
  val rmiPath = settingKey[String]("Path where rmiServer will be executed.")

  val rmiStart = taskKey[Unit]("really wake up the rmi server")
  val rmiStop = taskKey[Unit]("stop the rmi server")

  val rmiSettings = Seq(
     rmiPort := 8081,
     rmiPath := "/target/scala-2.10/",
     rmiStart := { 
import scala.sys.process._
        ("rmiregistry " + rmiPort.value).!!
	println("despertando a rmi!" )
     },
     rmiStop := { 
import scala.sys.process._
        "killall rmiregistry".!!
	println("matando a todos los rmi >.<!")
     }
  )
  
  override lazy val settings = rmiSettings ++ Seq(commands += rmiServer)

  lazy val rmiServer = Command.command("rmiServer") { (state: State) =>
    val extracted: Extracted = Project.extract(state)
    import extracted._

    //val rmiStartKey = Keys.rmiStop

    val result: Option[Result[Unit]] = Project.evaluateTask(rmiStart, state)
    // handle the result
    result match {
        case None => println("key no fue definida")// Key wasn't defined.
        case Some(Inc(inc)) => println("hacer algo con " + inc)
        case Some(Value(v)) => println("todo ok")// do something with v: inc.Analysis
    }

    // get name of current project
    val nameOpt: Option[String] = name in currentRef get structure.data
    
    println("wake up! " + nameOpt)
    state
  }
}
