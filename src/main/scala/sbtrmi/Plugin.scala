package sbtrmi

import sbt._
import Keys._
import complete.DefaultParsers._


object RMIServer extends sbt.Plugin {
  val rmiPort = settingKey[Int]("Port runnning rmiserver.")
  val rmiPath = settingKey[String]("Path where rmiServer will be executed.")
  val rmiClass = settingKey[String]("Classes to compile with rmiserver")
  val rmiStart = taskKey[Unit]("really wake up the rmi server")
  val rmiStop = taskKey[Unit]("stop the rmi server")
  val rmiCompile = inputKey[Unit]("Compile with rmic")


  (compile in Compile) <<=  (compile in Compile).dependsOn(rmiCompile.toTask(""))

  // Move this to utils
  def getPathFor(target: String): Option[File]  = {
    val localFile = file(s"./target/") ** s"$target.class"

    if (localFile.get.length == 1 ) {
      var tmp = file((localFile.get)(0).getParent)
      Some(tmp)
    }
    else None
  }

  val rmiSettings = Seq(
     rmiPort := 8081,
     // TODO: Remove this option, is more easy using the discovery facilities of File
     // REF-1: http://www.scala-sbt.org/0.12.4/docs/Detailed-Topics/Paths.html
     rmiPath := "/target/scala-2.10/",
     rmiClass := "Server",
     rmiStart := {
       println("despertando a rmi!" )
       // TODO: adds validation to the class
       getPathFor(rmiClass.value) match {
         case Some(path) => sys.process.Process(Seq("rmiregistry", rmiPort.value.toString ), path).!
         case None => println("Clase no encontrada")

       }
       Unit
    },
     rmiStop := {
         import scala.sys.process._
        "killall rmiregistry".!!
	       println("matando a todos los rmi >.<!")
     },
     rmiCompile := {
       val cp = (fullClasspath in Runtime).value.files.map((a) => a.toString).reduce((a: String, b:String ) => s"$a:$b")

       val args: Seq[String] = spaceDelimited("<arg>").parsed
       var _classes: Seq[String] = Seq("")
       if (args.length >= 1)
         _classes = args
       else
         rmiClass.value match { 
            case sing: String => _classes = (rmiClass.value).split("\\s+")
            case seq: Seq[String] => _classes = seq
         }

       // Delete this. See REF-1
       // val scalaVer = "scala-" + scalaVersion.value.toString.replaceAll("\\.\\d+$", "")
       // val sbtVer = "sbt-" + sbtVersion.value.toString.replaceAll("\\.\\d+$", "")
       // val classes = file(s"./target/${scalaVer}/${sbtVer}") ** "*.class"
       for (_class <- _classes) {
         getPathFor(_class) match {
           case Some(path) => sys.process.Process(Seq("rmic", "-classpath", cp, rmiClass.value.toString), path).!
           case None => println(s"Clase no encontrada ${_class}")
         }
       }
       Unit
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
