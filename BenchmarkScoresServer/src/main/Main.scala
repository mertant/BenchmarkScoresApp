package main

/*
 * This class consists of two HTTP interfaces, referred to as the "client" (requests out) and "server" (requests in)
 * parts, for lack of better nomenclature. The outer interface or client runs in its own thread, collecting data
 * at constant intervals from the external API located at http://www.3dmark.com/proxycon/resultevent/. The data
 * is unmarshalled into Result type and stored in the ResultList  object. The inner interface or "server" hosts
 * an interface service at http://localhost:8080/ which can be queried for stats like country-based average scores.
 * 
 * Spray is used to handle both HTTP interfaces. Instead of being a Java library in a wrapper, it allows us to
 * leverage Scala's benefits such as asynchronous execution with Futures. Another reason to choose spray is the
 * related but seprate spray-json library, since the data from the online API is in JSON form. 
 * 
 * Akka (required by spray) provides a  high-level abstraction for concurrency with its Actors, so we do not
 * need to worry about manual thread handling. We use Actors for the update function scheduler,
 * and the "server" side request handler.
 * 
 * 
 * @author Teo Mertanen
 */

import scala.concurrent.Future
import scala.concurrent.duration._

import spray.http._
import spray.json._
import spray.httpx.encoding.{ Gzip, Deflate }
import spray.httpx.SprayJsonSupport._
import spray.can.Http
import spray.client.pipelining._

import akka.io.IO
import akka.actor.ActorSystem
import akka.actor.Props

object Main extends App {
  def log(msg: String) = println(DateTime.now + " " + msg)

  implicit val system = ActorSystem() // system for our dispatcher, scheduler and handler actors 

  /**
   *
   * SERVER SIDE INTERFACE
   *
   * Where we handle outgoing requests and incoming responses.
   *
   */

  /* 
   * Define the JSON protocol for the format of results expected from the external API.
   * The number of fields is defined here; see the Result case class for the content of each field. 
   */

  object JsonProtocol extends DefaultJsonProtocol {
    implicit val resultFormat = jsonFormat4(Result)
  }

  import JsonProtocol._

  // execution context for HTTP sendreceive futures
  import system.dispatcher

  /*
   *  Define our pipeline for talking to the external API. Formats the request, sends and receives,
   *  then unmarshals the JSON using the protocol we defined above. 
   *  The val keyword means additional calls do not incur additional overhead.
   */

  val pipeline: HttpRequest => Future[List[Result]] = (
    addHeader("Host", "www.3dmark.com")
    ~> addHeader("Connection", "keep-alive")
    ~> addHeader("Accept", "application/json")
    ~> addHeader("Accept-Encoding", "deflate")
    ~> logRequest
    ~> sendReceive
    ~> decode(Deflate)
    ~> unmarshal[List[Result]])

  // custom piece of pipe that logs the request and returns it unchanged
  def logRequest: HttpRequest => HttpRequest = { r => log("Sending request" + r.uri.toString()); r }

  // Define the size and frequency of our requests to the API
  private val EventCount = 100
  private var currentId = 0
  private val initialDelay = Duration(1, SECONDS)
  private val updateInterval = Duration(20, SECONDS)

  // Set our data to update in real-time at constant intervals with an akka scheduler
  system.scheduler.schedule(initialDelay, updateInterval)(update)

  // Send a request to the API and update our data on response 
  def update = {
    val uri = s"/proxycon/resultevent/?eventIdStart=${currentId}&eventCountMax=${currentId + EventCount}"
    val response: Future[List[Result]] = pipeline(Get(uri))

    // Handle the response on its completion
    response.onSuccess {
      case newResults => {
        val n = newResults.length
        log(s"Request succesful, $n new results for a total of ${ResultList.size + n}")
        ResultList ++= newResults
        if (newResults.nonEmpty)
          currentId = newResults.map { _.id }.max + 1 // so we do not receive the same result twice
      }
    }

    response.onFailure { case r => log("Request failed: " + r) }
  }

  /**
   *
   * SERVER SIDE INTERFACE
   *
   * Where we handle incoming requests and outgoing responses.
   *
   */

  // Create the handler actor which will reply to incoming HttpRequests according to behavior defined in ServerActor
  private val handler = system.actorOf(Props[ServerActor], name = "handler")

  // Bind our handler to an address and port to listen to.
  // ! represents 'tell' i.e. fire a message and return immediately
  IO(Http) ! Http.Bind(handler, interface = "localhost", port = 8080)

}