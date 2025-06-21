package greta.performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.util.Random

/**
 * Gatling load test simulation for Greta platform
 */
class GretaLoadSimulation extends Simulation {

  // Configuration
  val baseUrl = System.getProperty("greta.test.url", "http://localhost:8080")
  val users = Integer.getInteger("greta.test.users", 100)
  val rampDuration = Integer.getInteger("greta.test.ramp", 30)
  val testDuration = Integer.getInteger("greta.test.duration", 300)

  // HTTP Configuration
  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling/3.0")
    .shareConnections

  // Feeder for random data
  val animationFeeder = Iterator.continually(Map(
    "animationType" -> Random.shuffle(List("gesture", "facial", "posture")).head,
    "animationName" -> s"animation_${Random.alphanumeric.take(10).mkString}",
    "duration" -> (Random.nextDouble() * 5 + 1),
    "intensity" -> Random.nextDouble()
  ))

  val behaviorFeeder = Iterator.continually(Map(
    "intention" -> Random.shuffle(List("greeting", "explain", "question", "statement")).head,
    "emotion" -> Random.shuffle(List("happy", "neutral", "sad", "excited")).head,
    "content" -> s"Test content ${Random.alphanumeric.take(20).mkString}"
  ))

  // Scenarios
  val animationScenario = scenario("Animation System Load Test")
    .feed(animationFeeder)
    .exec(
      http("Create Animation")
        .post("/api/animation/play")
        .body(StringBody(
          """{
            |  "type": "${animationType}",
            |  "name": "${animationName}",
            |  "duration": ${duration},
            |  "parameters": {
            |    "intensity": ${intensity}
            |  }
            |}""".stripMargin
        ))
        .check(status.is(202))
        .check(jsonPath("$.id").saveAs("animationId"))
    )
    .pause(1, 3)
    .exec(
      http("Check Animation Status")
        .get("/api/animation/${animationId}/status")
        .check(status.is(200))
        .check(jsonPath("$.status").in("queued", "playing", "completed"))
    )

  val behaviorScenario = scenario("Behavior Planning Load Test")
    .feed(behaviorFeeder)
    .exec(
      http("Plan Behavior")
        .post("/api/behavior/plan")
        .body(StringBody(
          """{
            |  "intention": "${intention}",
            |  "content": "${content}",
            |  "context": {
            |    "emotion": "${emotion}",
            |    "formality": "informal"
            |  }
            |}""".stripMargin
        ))
        .check(status.is(200))
        .check(jsonPath("$.behaviors").exists)
    )
    .pause(2, 5)

  val complexScenario = scenario("Complex Workflow Load Test")
    .exec(
      http("Start Session")
        .post("/api/session/start")
        .body(StringBody("""{"type": "interactive", "mode": "conversation"}"""))
        .check(status.is(200))
        .check(jsonPath("$.sessionId").saveAs("sessionId"))
    )
    .repeat(5) {
      feed(behaviorFeeder)
      .exec(
        http("Send Message")
          .post("/api/session/${sessionId}/message")
          .body(StringBody(
            """{
              |  "message": "${content}",
              |  "emotion": "${emotion}"
              |}""".stripMargin
          ))
          .check(status.is(200))
      )
      .pause(1, 2)
    }
    .exec(
      http("End Session")
        .post("/api/session/${sessionId}/end")
        .check(status.is(200))
    )

  val healthCheckScenario = scenario("Health Check")
    .exec(
      http("Health Check")
        .get("/health")
        .check(status.is(200))
    )

  // Load profiles
  val normalLoad = animationScenario.inject(
    rampUsers(users) during (rampDuration seconds),
    constantUsersPerSec(5) during (testDuration seconds)
  )

  val behaviorLoad = behaviorScenario.inject(
    rampUsers(users / 2) during (rampDuration seconds),
    constantUsersPerSec(3) during (testDuration seconds)
  )

  val complexLoad = complexScenario.inject(
    rampUsers(users / 4) during (rampDuration seconds),
    constantUsersPerSec(1) during (testDuration seconds)
  )

  val healthLoad = healthCheckScenario.inject(
    constantUsersPerSec(1) during ((testDuration + rampDuration) seconds)
  )

  // Spike test profile
  val spikeProfile = animationScenario.inject(
    constantUsersPerSec(2) during (60 seconds),
    nothingFor(10 seconds),
    atOnceUsers(50), // Spike
    nothingFor(30 seconds),
    constantUsersPerSec(2) during (60 seconds)
  )

  // Stress test profile
  val stressProfile = animationScenario.inject(
    rampUsersPerSec(1) to 20 during (300 seconds),
    constantUsersPerSec(20) during (120 seconds),
    rampUsersPerSec(20) to 1 during (60 seconds)
  )

  // Setup simulation
  setUp(
    normalLoad.protocols(httpProtocol),
    behaviorLoad.protocols(httpProtocol),
    complexLoad.protocols(httpProtocol),
    healthLoad.protocols(httpProtocol)
  ).assertions(
    global.responseTime.max.lt(5000),
    global.responseTime.mean.lt(1000),
    global.successfulRequests.percent.gt(95),
    details("Animation System Load Test" / "Create Animation").responseTime.mean.lt(500),
    details("Behavior Planning Load Test" / "Plan Behavior").responseTime.mean.lt(1000)
  )
}