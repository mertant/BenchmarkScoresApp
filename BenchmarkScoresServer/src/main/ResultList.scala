package main

/*
 * This object is used to collect the received Results and provide methods for calculating aggregate data.
 * The internal representation of the list is a mutable ListBuffer, for which both prepend and apply operations
 * run in constant time. Though mutable, we only expose the add (++=) method to the outside since we do not
 * want to remove any existing data.
 * 
 * 
 * @author Teo Mertanen
 */

import scala.collection.mutable.ListBuffer

object ResultList {
  private val list = ListBuffer[Result]()

  // Convenience methods
  def countryCodes = list.toVector.map { _.countryCode }.distinct.sorted
  def countryCount = countryCodes.size
  def testNames = list.toVector.map { _.testName }.distinct.sorted
  def testCount = testNames.size
  def overallScores = list.toVector.map { _.overallScore }

  // Returns a map from a country code to a list of results from that country.
  def countryMap: Map[String, Vector[Result]] = {
    list.toVector.groupBy { _.countryCode }
  }

  // Returns a list of all the results for a given country
  def resultsFromCountry(country: String) = countryMap(country)

  // For a given country, returns a list of all benchmarks posted from that country, each paired with
  // their average score in that country 
  def countryAverages(country: String): Seq[(String, Double)] = {
    if (!countryCodes.contains(country)) {
      Seq() // return an empty sequence instead of trying to divide by zero
    } else {
      val byTest = resultsFromCountry(country).groupBy { _.testName }
      val scoresByTest = byTest.mapValues { v => v.map { _.overallScore } }
      def average(vector: Vector[Double]): Double = (vector.sum / vector.length)
      scoresByTest.mapValues { results => average(results) }.toVector
    }
  }

  // For a given country, returns a formatted string of the benchmarks and their average score in that country
  def countryAveragesString(country: String): String = countryAverages(country) match {
    case Seq() => "Country not found."
    case seq => seq.map { pair => pair._1 + ": " + pair._2.round.toString }.mkString(" - ")
  }

  // Add results to the list
  def ++=(results: Seq[Result]) = {
    list ++= results
  }

  // The number of results in the list
  def size = list.size

}