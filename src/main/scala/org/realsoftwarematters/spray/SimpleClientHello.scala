package org.realsoftwarematters.spray

import cc.spray.can.client.HttpClient
import java.net.InetSocketAddress
import akka.util.duration._
import cc.spray.can.model.{HttpResponse, HttpRequest}
import akka.actor._
import cc.spray.io.IoWorker.SendCompleted
import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}
import cc.spray.io.{CleanClose, Handle, IoWorker}
import util.Random

object SimpleClientHello extends App {
  implicit val system = ActorSystem()
  def log = system.log

  val ioWorker = new IoWorker(system).start()

  system.registerOnTermination {
    log.info("Shutting down...")
    ioWorker.stop()
  }

  val httpClient = system.actorOf(
    props = Props(new HttpClient(ioWorker)),
    name = "http-client"
  )

  system.actorOf(Props(new SimpleConnActor(httpClient)))
}

object SimpleConnActor {
  val ServerAddress = new InetSocketAddress("127.0.0.1", 8080)
  val MaxConns = 50000
  val Delay = 4900.millis // 5 seconds
  val Ping = new {}
  val HoldFor = 10 // requests
}

class SimpleConnActor(httpClient: ActorRef,
                connCount: AtomicInteger = new AtomicInteger,
                closing: AtomicBoolean = new AtomicBoolean) extends Actor with ActorLogging {
  import ConnActor._
  var handle: Handle = _
  var reqCounter = 0
  val connNr = connCount.incrementAndGet()
  val doLog = connNr % 100 == 0

  log.info("Opening connection {}", connNr)
  httpClient ! HttpClient.Connect(ServerAddress)

  log.info("After the actor")
  
  def unconnected = ({
    case HttpClient.Connected(h) =>
      log.info("Connection {} established", connNr)
      handle = h
      context.become(connected)
      self ! Ping
    case _ => log.info("Something Went Wrong")
  }: Receive)

  val connected: Receive = ({
    case Ping =>
      log.info("Connection {}: sending Hello", connNr)
      reqCounter += 1
      handle.handler ! HttpRequest(uri = "/Hello")
      context.become(responsePending)
  }: Receive)

  val responsePending: Receive = ({
    case _: SendCompleted => // ignore
    case HttpResponse(200, _, body, _) =>
      log.info("Connection {}: Hi!", connNr)
      onResponse(new String(body, "ASCII"))
  }: Receive)

  def receive = unconnected
  
  def onResponse(response: String) {
	log.info("Message Received {}",response)
	shutdown()
  }
  
  def shutdown() {
    if (doLog) log.info("Shutting down connection {}", connNr)
    handle.handler ! HttpClient.Close(CleanClose)
    context.stop(self)
    if (connCount.decrementAndGet() == 0) context.system.shutdown()
  }
}
