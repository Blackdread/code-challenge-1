import _root_.io.gatling.core.scenario.Simulation
import ch.qos.logback.classic.{Level, LoggerContext}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

/**
 * Performance test for the Transaction entity.
 */
class TransactionGatlingTest extends Simulation {

    val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    // Log all HTTP requests
    //context.getLogger("io.gatling.http").setLevel(Level.valueOf("TRACE"))
    // Log failed HTTP requests
    //context.getLogger("io.gatling.http").setLevel(Level.valueOf("DEBUG"))

    val baseURL = Option(System.getProperty("baseURL")) getOrElse """http://localhost:8081"""

    val httpConf = http
        .baseURL(baseURL)
        .inferHtmlResources()
        .acceptHeader("*/*")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
        .connectionHeader("keep-alive")
        .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:33.0) Gecko/20100101 Firefox/33.0")

    val headers_http = Map(
        "Accept" -> """application/json"""
    )

    val headers_http_authentication = Map(
        "Content-Type" -> """application/json""",
        "Accept" -> """application/json"""
    )

    val headers_http_authenticated = Map(
        "Accept" -> """application/json""",
        "Authorization" -> "${access_token}"
    )

    val timestamp: Long = System.currentTimeMillis

    val scn = scenario("Test the Transaction")
        .repeat(2) {
            exec(http("Create new current time transaction")
            .post("/transactions")
            .body(StringBody(s"""{"amount":20.009693, "timestamp":"${System.currentTimeMillis}"}""")).asJSON
            .check(status.in(201, 204)))
            .pause(1)
        }
        .repeat(2) {
            exec(http("Create new future transaction")
            .post("/transactions")
            .body(StringBody("""{"amount":1.0051, "timestamp":"2020-01-01T00:00:00.000Z"}""")).asJSON
            .check(status.is(201)))
            .pause(10)
            .repeat(5) {
                exec(http("Get statistics")
                .get("/statistics")
                .check(status.is(200)))
                .pause(10)
            }
            .pause(10)
        }
        .exec(http("Create new transaction too old")
        .post("/transactions")
        .body(StringBody("""{"amount":20.009693, "timestamp":"0"}""")).asJSON
        .check(status.is(204)))
        .pause(1)


    val users = scenario("Users").exec(scn)

    setUp(
        users.inject(rampUsers(Integer.getInteger("users", 1000)) over (Integer.getInteger("ramp", 1) minutes))
    ).protocols(httpConf)
}
