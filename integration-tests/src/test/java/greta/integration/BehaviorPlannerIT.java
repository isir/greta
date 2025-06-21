package greta.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Behavior Planning System
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.CONCURRENT)
public class BehaviorPlannerIT extends BaseIntegrationTest {
    
    @BeforeAll
    @Override
    public void setupContainers() {
        super.setupContainers();
        gretaApp.start();
        RestAssured.baseURI = getGretaBaseUrl();
    }
    
    @Test
    @Order(1)
    @DisplayName("Should plan simple communicative intention")
    public void testSimpleCommunicativeIntention() {
        String intentionRequest = """
            {
                "intention": "greeting",
                "target": "user",
                "context": {
                    "formality": "informal",
                    "emotion": "happy",
                    "distance": "close"
                }
            }
            """;
        
        Map<String, Object> plan = given()
            .contentType(ContentType.JSON)
            .body(intentionRequest)
        .when()
            .post("/api/behavior/plan")
        .then()
            .statusCode(200)
            .body("behaviors", notNullValue())
            .body("behaviors", hasSize(greaterThan(0)))
            .extract()
            .as(Map.class);
        
        List<Map<String, Object>> behaviors = (List<Map<String, Object>>) plan.get("behaviors");
        
        // Verify greeting behaviors are appropriate
        boolean hasWave = behaviors.stream()
            .anyMatch(b -> "gesture".equals(b.get("type")) && "wave".equals(b.get("name")));
        boolean hasSmile = behaviors.stream()
            .anyMatch(b -> "facial".equals(b.get("type")) && "smile".equals(b.get("expression")));
        
        assertTrue(hasWave || hasSmile, "Greeting should include wave or smile");
    }
    
    @Test
    @Order(2)
    @DisplayName("Should plan complex multimodal behavior")
    public void testComplexMultimodalBehavior() {
        String complexRequest = """
            {
                "intention": "explain",
                "content": "The weather today is sunny with a chance of rain later",
                "context": {
                    "audience": "general",
                    "emphasis": ["weather", "sunny", "rain"],
                    "emotion": "neutral"
                }
            }
            """;
        
        Map<String, Object> plan = given()
            .contentType(ContentType.JSON)
            .body(complexRequest)
        .when()
            .post("/api/behavior/plan")
        .then()
            .statusCode(200)
            .body("behaviors", hasSize(greaterThan(3)))
            .body("duration", notNullValue())
            .extract()
            .as(Map.class);
        
        List<Map<String, Object>> behaviors = (List<Map<String, Object>>) plan.get("behaviors");
        
        // Verify multimodal behaviors
        long gestureCount = behaviors.stream()
            .filter(b -> "gesture".equals(b.get("type")))
            .count();
        long gazeCount = behaviors.stream()
            .filter(b -> "gaze".equals(b.get("type")))
            .count();
        long facialCount = behaviors.stream()
            .filter(b -> "facial".equals(b.get("type")))
            .count();
        
        assertTrue(gestureCount > 0, "Should include gestures");
        assertTrue(gazeCount > 0, "Should include gaze behaviors");
        assertTrue(facialCount > 0, "Should include facial expressions");
        
        // Verify emphasis timing
        List<Map<String, Object>> emphasisBehaviors = behaviors.stream()
            .filter(b -> b.containsKey("emphasis") && (boolean) b.get("emphasis"))
            .toList();
        
        assertEquals(3, emphasisBehaviors.size(), "Should have behaviors for each emphasis word");
    }
    
    @Test
    @Order(3)
    @DisplayName("Should adapt behavior based on emotional context")
    public void testEmotionalAdaptation() {
        String[] emotions = {"happy", "sad", "angry", "surprised", "fearful"};
        
        for (String emotion : emotions) {
            String request = String.format("""
                {
                    "intention": "statement",
                    "content": "I have something to tell you",
                    "context": {
                        "emotion": "%s",
                        "intensity": 0.8
                    }
                }
                """, emotion);
            
            Map<String, Object> plan = given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/behavior/plan")
            .then()
                .statusCode(200)
                .extract()
                .as(Map.class);
            
            List<Map<String, Object>> behaviors = (List<Map<String, Object>>) plan.get("behaviors");
            
            // Verify emotion-appropriate behaviors
            boolean hasEmotionalExpression = behaviors.stream()
                .anyMatch(b -> "facial".equals(b.get("type")) && 
                              emotion.equals(b.get("expression")));
            
            assertTrue(hasEmotionalExpression, 
                "Should include " + emotion + " facial expression");
            
            // Verify intensity mapping
            behaviors.stream()
                .filter(b -> "facial".equals(b.get("type")))
                .forEach(b -> {
                    assertTrue((Double) b.get("intensity") >= 0.7,
                        "High emotional intensity should map to high behavior intensity");
                });
        }
    }
    
    @Test
    @Order(4)
    @DisplayName("Should handle real-time behavior replanning")
    public void testRealTimeReplanning() throws InterruptedException {
        // Start with initial plan
        String initialRequest = """
            {
                "intention": "presentation",
                "content": "Let me show you our new product features",
                "context": {
                    "audience": "formal",
                    "duration": 30
                }
            }
            """;
        
        String sessionId = given()
            .contentType(ContentType.JSON)
            .body(initialRequest)
        .when()
            .post("/api/behavior/session/start")
        .then()
            .statusCode(200)
            .body("sessionId", notNullValue())
            .body("status", equalTo("active"))
            .extract()
            .path("sessionId");
        
        // Simulate interruption and replanning
        Thread.sleep(2000);
        
        String interruptRequest = """
            {
                "event": "user_question",
                "data": {
                    "question": "Can you explain that again?",
                    "timestamp": 2.5
                }
            }
            """;
        
        Map<String, Object> replan = given()
            .contentType(ContentType.JSON)
            .pathParam("sessionId", sessionId)
            .body(interruptRequest)
        .when()
            .post("/api/behavior/session/{sessionId}/interrupt")
        .then()
            .statusCode(200)
            .body("replanned", equalTo(true))
            .body("newBehaviors", notNullValue())
            .extract()
            .as(Map.class);
        
        List<Map<String, Object>> newBehaviors = 
            (List<Map<String, Object>>) replan.get("newBehaviors");
        
        // Verify replanning includes acknowledgment
        boolean hasAcknowledgment = newBehaviors.stream()
            .anyMatch(b -> "gesture".equals(b.get("type")) && 
                          ("nod".equals(b.get("name")) || "acknowledgment".equals(b.get("name"))));
        
        assertTrue(hasAcknowledgment, "Replanning should include acknowledgment");
    }
    
    @Test
    @Order(5)
    @DisplayName("Should coordinate with animation system")
    public void testAnimationCoordination() {
        String coordinatedRequest = """
            {
                "intention": "demonstrate",
                "content": "First, you click here, then drag to there",
                "context": {
                    "includePointing": true,
                    "screenCoordinates": [
                        {"x": 100, "y": 200, "time": 1.0},
                        {"x": 400, "y": 300, "time": 3.0}
                    ]
                }
            }
            """;
        
        String planId = given()
            .contentType(ContentType.JSON)
            .body(coordinatedRequest)
        .when()
            .post("/api/behavior/plan-and-execute")
        .then()
            .statusCode(202)
            .body("planId", notNullValue())
            .body("animationIds", hasSize(greaterThan(0)))
            .extract()
            .path("planId");
        
        // Monitor execution progress
        await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                Map<String, Object> status = given()
                    .pathParam("planId", planId)
                .when()
                    .get("/api/behavior/execution/{planId}/status")
                .then()
                    .statusCode(200)
                    .extract()
                    .as(Map.class);
                
                assertEquals("completed", status.get("status"));
                assertEquals(100, status.get("progress"));
                
                // Verify timing coordination
                List<Map<String, Object>> timeline = 
                    (List<Map<String, Object>>) status.get("timeline");
                
                // Check that pointing gestures align with content timing
                timeline.stream()
                    .filter(event -> "pointing".equals(event.get("type")))
                    .forEach(event -> {
                        double eventTime = (Double) event.get("time");
                        assertTrue(eventTime == 1.0 || eventTime == 3.0,
                            "Pointing should occur at specified times");
                    });
            });
    }
    
    @Test
    @Order(6)
    @DisplayName("Should handle behavior conflicts and priorities")
    public void testBehaviorConflictResolution() {
        String conflictingRequest = """
            {
                "intentions": [
                    {
                        "type": "greeting",
                        "priority": 5,
                        "startTime": 0
                    },
                    {
                        "type": "pointing",
                        "target": {"x": 200, "y": 300},
                        "priority": 8,
                        "startTime": 0.5
                    },
                    {
                        "type": "gesture",
                        "name": "thinking",
                        "priority": 3,
                        "startTime": 0.3
                    }
                ]
            }
            """;
        
        Map<String, Object> resolution = given()
            .contentType(ContentType.JSON)
            .body(conflictingRequest)
        .when()
            .post("/api/behavior/resolve-conflicts")
        .then()
            .statusCode(200)
            .body("resolved", notNullValue())
            .body("conflicts", notNullValue())
            .extract()
            .as(Map.class);
        
        List<Map<String, Object>> resolved = 
            (List<Map<String, Object>>) resolution.get("resolved");
        List<Map<String, Object>> conflicts = 
            (List<Map<String, Object>>) resolution.get("conflicts");
        
        // Verify high priority behavior wins conflicts
        Map<String, Object> pointingBehavior = resolved.stream()
            .filter(b -> "pointing".equals(b.get("type")))
            .findFirst()
            .orElse(null);
        
        assertNotNull(pointingBehavior, "High priority pointing should be preserved");
        
        // Verify conflict detection
        assertTrue(conflicts.size() > 0, "Should detect conflicts");
        
        // Verify temporal adjustment
        resolved.forEach(behavior -> {
            if ("thinking".equals(behavior.get("name"))) {
                double adjustedTime = (Double) behavior.get("startTime");
                assertTrue(adjustedTime > 0.5, 
                    "Lower priority gesture should be delayed to avoid conflict");
            }
        });
    }
    
    @Test
    @Order(7)
    @DisplayName("Should provide behavior analytics")
    public void testBehaviorAnalytics() {
        // Generate some behavior data
        for (int i = 0; i < 10; i++) {
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                        "intention": "statement",
                        "content": "Test statement %d",
                        "context": {
                            "emotion": "%s"
                        }
                    }
                    """, i, i % 2 == 0 ? "happy" : "neutral"))
            .when()
                .post("/api/behavior/plan")
            .then()
                .statusCode(200);
        }
        
        // Get analytics
        Map<String, Object> analytics = given()
        .when()
            .get("/api/behavior/analytics")
        .then()
            .statusCode(200)
            .body("totalPlans", greaterThan(10))
            .body("behaviorFrequency", notNullValue())
            .body("averagePlanComplexity", notNullValue())
            .body("emotionDistribution", notNullValue())
            .extract()
            .as(Map.class);
        
        Map<String, Integer> behaviorFreq = 
            (Map<String, Integer>) analytics.get("behaviorFrequency");
        Map<String, Double> emotionDist = 
            (Map<String, Double>) analytics.get("emotionDistribution");
        
        // Verify analytics data
        assertTrue(behaviorFreq.size() > 0, "Should track behavior frequencies");
        assertTrue(emotionDist.containsKey("happy"), "Should track emotion distribution");
        assertTrue(emotionDist.containsKey("neutral"), "Should track emotion distribution");
        
        double happyPercentage = emotionDist.get("happy");
        double neutralPercentage = emotionDist.get("neutral");
        assertTrue(Math.abs(happyPercentage - neutralPercentage) < 10, 
            "Emotion distribution should be roughly equal");
    }
}