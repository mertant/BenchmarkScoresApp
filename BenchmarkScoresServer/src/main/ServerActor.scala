package main

/*
 *  This class defines how we respond to incoming requests at the inner interface. If we wish to extend the
 *  server with new types of queries, we simply add new cases here. The ! operator represents 'tell'
 *  i.e. fire a message and return immediately, since we do not need to wait for a reply from the client.
 * 
 * @author Teo Mertanen
 */

import akka.actor.Actor
import spray.http._
import spray.can.Http

class ServerActor extends Actor {
  
  def receive = {
    // Handle new connection
    case _: Http.Connected => sender ! Http.Register(self)

    // Handle requests
    case req: HttpRequest => {
      val response = req.uri.query.get("type") match {
        case Some("help") => "Enter a country code to see average test scores for that country, " +
          "'list' for a list of countries, or 'stats' for statistics."

        case Some("stats") => s"${ResultList.size} results in ${ResultList.testCount} benchmarks " +
          "from ${resultList.countryCount} countries."

        case Some("list") => "Available countries: " + ResultList.countryCodes.mkString(", ")

        case Some(s) => ResultList.countryAveragesString(s.toUpperCase)

        case _ => "Empty query."
      }
      sender ! HttpResponse(entity = response)
    }

  }
}