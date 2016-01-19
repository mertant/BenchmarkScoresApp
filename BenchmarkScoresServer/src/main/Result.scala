package main

/*
 * A case class representing a single result parsed from a JSON. All four fields of the JSON are collected.
 * Even though for the current specification we don't require the events and latestTimestampMilliseconds fields
 * and only a few fields from benchmarkRunModel, we save all the data to allow for future extensions.
 * 
 * @author Teo Mertanen
 */

import spray.json._

case class Result(id: Int, benchmarkRunModel: JsObject, events: List[JsObject], latestTimestampMilliseconds: Int) {
  // Methods for obtaining select values as formatted strings/number values
  def countryCode: String = getBenchmarkValue("countryCode").filter { _ != '"' }
  def testName: String = getBenchmarkValue("testName").filter { _ != '"' }
  def overallScore: Double = getBenchmarkValue("overallScore").toDouble

  private def getBenchmarkValue(fieldName: String): String = this.benchmarkRunModel.fields.get(fieldName).get.toString
}

