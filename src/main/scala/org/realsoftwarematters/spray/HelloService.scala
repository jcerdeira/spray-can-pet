package org.realsoftwarematters.spray

import akka.util.duration._
import cc.spray.can.model.{HttpResponse, HttpRequest}
import cc.spray.can.model.HttpMethods.GET
import cc.spray.io.pipelines.MessageHandlerDispatch
import cc.spray.io.IoWorker
import cc.spray.can.server.HttpServer
import akka.actor._

object HelloService extends App {

  val system = ActorSystem("server")

  val handler = system.actorOf {
    Props {
      new Actor with ActorLogging {
        val Delay = 60.seconds
        def receive = {
        	case HttpRequest(GET, "/Hello", _, _, _) =>
        		sender ! response("Hi!")
        	}
        def response(msg: String) = HttpResponse(200, body = msg.getBytes("ISO-8859-1"))
      }
    }
  }

  val ioWorker = new IoWorker(system).start()

  val server = system.actorOf(
    props = Props(new HttpServer(ioWorker, MessageHandlerDispatch.SingletonHandler(handler))),
    name = "http-server"
  )
  
  server ! HttpServer.Bind("localhost", 8080)

  system.registerOnTermination {
    ioWorker.stop()
  }
}