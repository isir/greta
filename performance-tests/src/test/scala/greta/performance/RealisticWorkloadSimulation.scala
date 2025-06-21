package greta.performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.util.Random

/**
 * Realistic workload simulation based on production usage patterns
 */
class RealisticWorkloadSimulation extends Simulation {

  // Configuration based on real-world usage patterns
  val baseUrl = System.getProperty("greta.test.url", "http://localhost:8080")
  val concurrentEducators = Integer.getInteger("greta.educators", 20)
  val concurrentResearchers = Integer.getInteger("greta.researchers", 15)
  val concurrentStudents = Integer.getInteger("greta.students", 100)
  val testDuration = Integer.getInteger("greta.test.duration", 1800) // 30 minutes

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Greta-Client/1.0")
    .shareConnections

  // Realistic data feeders based on actual usage
  val educatorAnimations = csv("educator_animations.csv").circular
  val researcherBehaviors = csv("researcher_behaviors.csv").circular
  val studentInteractions = csv("student_interactions.csv").circular

  // User personas with realistic behavior patterns
  val educatorScenario = scenario("Educator Creating Lessons")
    .feed(educatorAnimations)
    .exec(session => {
      println(s"Educator ${session.userId} starting lesson creation")
      session
    })
    .exec(
      http("Login as Educator")
        .post("/api/auth/login")
        .body(StringBody("""{"username": "${username}", "password": "educator123", "role": "educator"}"""))
        .check(status.is(200))
        .check(jsonPath("$.token").saveAs("authToken"))
    )
    .pause(2, 5) // Think time
    .repeat(3, "lessonIndex") {
      exec(
        http("Create Lesson Animation")
          .post("/api/animation/create")
          .header("Authorization", "Bearer ${authToken}")
          .body(StringBody("""
            {
              "type": "${animationType}",
              "name": "${animationName}_lesson_${lessonIndex}",
              "duration": ${duration},
              "difficulty": "intermediate",
              "subject": "${subject}",
              "ageGroup": "${ageGroup}"
            }
          """))
          .check(status.is(201))
          .check(jsonPath("$.id").saveAs("animationId"))
      )
      .pause(5, 15)
      .exec(
        http("Preview Animation")
          .get("/api/animation/${animationId}/preview")
          .header("Authorization", "Bearer ${authToken}")
          .check(status.is(200))
      )
      .pause(3, 8)
      .exec(
        http("Adjust Animation Parameters")
          .patch("/api/animation/${animationId}")
          .header("Authorization", "Bearer ${authToken}")
          .body(StringBody("""{"intensity": ${adjustedIntensity}, "speed": ${adjustedSpeed}}"""))
          .check(status.is(200))
      )
      .pause(1, 3)
    }
    .exec(
      http("Save Lesson Plan")
        .post("/api/lessons/save")
        .header("Authorization", "Bearer ${authToken}")
        .body(StringBody("""
          {
            "title": "Lesson ${lessonTitle}",
            "animations": ["${animationId}"],
            "metadata": {
              "subject": "${subject}",
              "duration": ${totalDuration}
            }
          }
        """))
        .check(status.is(201))
    )
    .pause(10, 30) // Break between lessons

  val researcherScenario = scenario("Researcher Conducting Studies")
    .feed(researcherBehaviors)
    .exec(
      http("Login as Researcher")
        .post("/api/auth/login")
        .body(StringBody("""{"username": "researcher_${userId}", "password": "research123", "role": "researcher"}"""))
        .check(status.is(200))
        .check(jsonPath("$.token").saveAs("authToken"))
    )
    .pause(1, 3)
    .exec(
      http("Start Research Session")
        .post("/api/research/session/start")
        .header("Authorization", "Bearer ${authToken}")
        .body(StringBody("""
          {
            "study": "${studyName}",
            "participant": "${participantId}",
            "condition": "${experimentalCondition}"
          }
        """))
        .check(status.is(200))
        .check(jsonPath("$.sessionId").saveAs("sessionId"))
    )
    .repeat(10, "trialIndex") {
      exec(
        http("Plan Behavior for Trial")
          .post("/api/behavior/plan")
          .header("Authorization", "Bearer ${authToken}")
          .body(StringBody("""
            {
              "intention": "${intention}",
              "context": {
                "emotion": "${emotion}",
                "formality": "${formality}",
                "trial": ${trialIndex}
              },
              "sessionId": "${sessionId}"
            }
          """))
          .check(status.is(200))
          .check(jsonPath("$.planId").saveAs("planId"))
      )
      .pause(2, 5)
      .exec(
        http("Execute Behavior")
          .post("/api/behavior/execute")
          .header("Authorization", "Bearer ${authToken}")
          .body(StringBody("""{"planId": "${planId}", "sessionId": "${sessionId}"}"""))
          .check(status.is(202))
      )
      .pause(3, 8) // Behavior execution time
      .exec(
        http("Record Trial Data")
          .post("/api/research/data/record")
          .header("Authorization", "Bearer ${authToken}")
          .body(StringBody("""
            {
              "sessionId": "${sessionId}",
              "trial": ${trialIndex},
              "planId": "${planId}",
              "metrics": {
                "responseTime": ${responseTime},
                "accuracy": ${accuracy},
                "engagement": ${engagement}
              }
            }
          """))
          .check(status.is(201))
      )
      .pause(1, 2)
    }
    .exec(
      http("End Research Session")
        .post("/api/research/session/${sessionId}/end")
        .header("Authorization", "Bearer ${authToken}")
        .check(status.is(200))
    )

  val studentScenario = scenario("Student Interactive Learning")
    .feed(studentInteractions)
    .exec(
      http("Anonymous Student Access")
        .get("/api/session/guest")
        .check(status.is(200))
        .check(jsonPath("$.sessionId").saveAs("guestSession"))
    )
    .pause(1, 2)
    .exec(
      http("Browse Available Lessons")
        .get("/api/lessons/browse")
        .queryParam("grade", "${gradeLevel}")
        .queryParam("subject", "${preferredSubject}")
        .check(status.is(200))
        .check(jsonPath("$.lessons[*].id").findAll.saveAs("availableLessons"))
    )
    .pause(3, 10) // Browse and select
    .exec(session => {
      val lessons = session("availableLessons").as[Vector[String]]
      if (lessons.nonEmpty) {
        val selectedLesson = lessons(Random.nextInt(lessons.length))
        session.set("selectedLesson", selectedLesson)
      } else {
        session.set("selectedLesson", "default_lesson")
      }
    })
    .exec(
      http("Start Lesson")
        .post("/api/lessons/${selectedLesson}/start")
        .body(StringBody("""{"sessionId": "${guestSession}", "learnerProfile": {"level": "${skillLevel}"}}"""))
        .check(status.is(200))
        .check(jsonPath("$.lessonSession").saveAs("lessonSession"))
    )
    .repeat(5, "activityIndex") {
      exec(
        http("Get Next Activity")
          .get("/api/lessons/${selectedLesson}/activity/${activityIndex}")
          .queryParam("session", "${lessonSession}")
          .check(status.is(200))
          .check(jsonPath("$.animation").saveAs("currentAnimation"))
      )
      .pause(2, 5)
      .exec(
        http("Submit Student Response")
          .post("/api/lessons/response")
          .body(StringBody("""
            {
              "lessonSession": "${lessonSession}",
              "activity": ${activityIndex},
              "response": "${studentResponse}",
              "confidence": ${confidenceLevel}
            }
          """))
          .check(status.is(200))
      )
      .pause(10, 30) // Time to watch animation and respond
    }
    .exec(
      http("Complete Lesson")
        .post("/api/lessons/${selectedLesson}/complete")
        .body(StringBody("""{"sessionId": "${lessonSession}"}"""))
        .check(status.is(200))
    )

  // Peak hours simulation (9 AM - 5 PM academic schedule)
  val morningRush = educatorScenario.inject(
    rampUsers(concurrentEducators) during (10 minutes),
    constantUsersPerSec(2) during (20 minutes)
  )

  val researchPeriod = researcherScenario.inject(
    rampUsers(concurrentResearchers) during (5 minutes),
    constantUsersPerSec(1) during (25 minutes)
  )

  val studentLearning = studentScenario.inject(
    rampUsers(concurrentStudents) during (15 minutes),
    constantUsersPerSec(8) during (15 minutes)
  )

  // Realistic usage patterns
  val realisticPattern = educatorScenario.inject(
    // Morning preparation (7-9 AM)
    rampUsersPerSec(0.5) to 3 during (30 minutes),
    constantUsersPerSec(3) during (60 minutes),
    
    // Peak teaching hours (9 AM - 12 PM)
    rampUsersPerSec(3) to 8 during (20 minutes),
    constantUsersPerSec(8) during (160 minutes),
    
    // Lunch break (12-1 PM)
    rampUsersPerSec(8) to 2 during (10 minutes),
    constantUsersPerSec(2) during (50 minutes),
    
    // Afternoon sessions (1-5 PM)
    rampUsersPerSec(2) to 6 during (15 minutes),
    constantUsersPerSec(6) during (225 minutes),
    
    // Evening wind-down (5-6 PM)
    rampUsersPerSec(6) to 1 during (60 minutes)
  )

  // Geographic distribution simulation
  val globalUsagePattern = educatorScenario.inject(
    // Asia-Pacific (GMT+8)
    atOnceUsers(20),
    nothingFor(8 hours),
    
    // Europe (GMT+1)
    rampUsers(30) during (2 hours),
    constantUsersPerSec(2) during (6 hours),
    
    // Americas (GMT-5)
    rampUsers(40) during (3 hours),
    constantUsersPerSec(3) during (8 hours)
  )

  // Weekend/holiday light usage
  val lightUsagePattern = scenario("Light Weekend Usage").inject(
    constantUsersPerSec(0.5) during (12 hours)
  )

  // Performance degradation test
  val stressPattern = educatorScenario.inject(
    rampUsersPerSec(1) to 20 during (300 seconds),
    constantUsersPerSec(20) during (300 seconds),
    rampUsersPerSec(20) to 50 during (180 seconds), // Stress point
    constantUsersPerSec(50) during (120 seconds),
    rampUsersPerSec(50) to 5 during (300 seconds)  // Recovery
  )

  setUp(
    morningRush.protocols(httpProtocol),
    researchPeriod.protocols(httpProtocol),
    studentLearning.protocols(httpProtocol)
  ).assertions(
    // Response time requirements
    global.responseTime.max.lt(10000),
    global.responseTime.mean.lt(2000),
    global.responseTime.percentile3.lt(5000),
    
    // Success rate requirements
    global.successfulRequests.percent.gt(99),
    
    // Specific endpoint requirements
    details("Educator Creating Lessons" / "Create Lesson Animation").responseTime.mean.lt(1500),
    details("Researcher Conducting Studies" / "Plan Behavior for Trial").responseTime.mean.lt(2000),
    details("Student Interactive Learning" / "Get Next Activity").responseTime.mean.lt(800),
    
    // Throughput requirements
    global.requestsPerSec.gt(10)
  )
}