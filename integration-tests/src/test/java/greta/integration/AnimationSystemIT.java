package greta.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Animation System
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.CONCURRENT)
public class AnimationSystemIT extends BaseIntegrationTest {
    
    @BeforeAll
    @Override
    public void setupContainers() {
        super.setupContainers();
        gretaApp.start();
        RestAssured.baseURI = getGretaBaseUrl();
    }
    
    @Test
    @Order(1)
    @DisplayName("Should successfully start animation system")
    public void testAnimationSystemStartup() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/animation/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("ready"))
            .body("version", notNullValue())
            .body("fps", equalTo(30));
    }
    
    @Test
    @Order(2)
    @DisplayName("Should process simple animation request")
    public void testSimpleAnimationRequest() {
        String animationRequest = """
            {
                "type": "gesture",
                "name": "wave",
                "duration": 2.0,
                "parameters": {
                    "hand": "right",
                    "amplitude": 0.8
                }
            }
            """;
        
        String animationId = given()
            .contentType(ContentType.JSON)
            .body(animationRequest)
        .when()
            .post("/api/animation/play")
        .then()
            .statusCode(202)
            .body("id", notNullValue())
            .body("status", equalTo("queued"))
            .extract()
            .path("id");
        
        // Wait for animation to complete
        await()
            .atMost(5, TimeUnit.SECONDS)
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted(() -> {
                given()
                    .pathParam("id", animationId)
                .when()
                    .get("/api/animation/{id}/status")
                .then()
                    .statusCode(200)
                    .body("status", equalTo("completed"));
            });
    }
    
    @Test
    @Order(3)
    @DisplayName("Should handle complex animation sequence")
    public void testComplexAnimationSequence() {
        String sequenceRequest = """
            {
                "type": "sequence",
                "animations": [
                    {
                        "type": "facial",
                        "expression": "smile",
                        "intensity": 0.7,
                        "duration": 1.5
                    },
                    {
                        "type": "gesture",
                        "name": "nod",
                        "repetitions": 2,
                        "duration": 1.0
                    },
                    {
                        "type": "posture",
                        "position": "lean_forward",
                        "duration": 2.0
                    }
                ]
            }
            """;
        
        Map<String, Object> response = given()
            .contentType(ContentType.JSON)
            .body(sequenceRequest)
        .when()
            .post("/api/animation/sequence")
        .then()
            .statusCode(202)
            .body("id", notNullValue())
            .body("totalDuration", equalTo(4.5f))
            .body("animations", hasSize(3))
            .extract()
            .as(Map.class);
        
        String sequenceId = (String) response.get("id");
        
        // Monitor sequence progress
        await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                Map<String, Object> status = given()
                    .pathParam("id", sequenceId)
                .when()
                    .get("/api/animation/sequence/{id}/progress")
                .then()
                    .statusCode(200)
                    .extract()
                    .as(Map.class);
                
                assertEquals("completed", status.get("status"));
                assertEquals(3, status.get("completedAnimations"));
            });
    }
    
    @Test
    @Order(4)
    @DisplayName("Should handle concurrent animation requests")
    public void testConcurrentAnimations() throws InterruptedException {
        int concurrentRequests = 10;
        String[] animationIds = new String[concurrentRequests];
        
        // Send concurrent animation requests
        for (int i = 0; i < concurrentRequests; i++) {
            final int index = i;
            String request = String.format("""
                {
                    "type": "gesture",
                    "name": "gesture_%d",
                    "duration": 1.0
                }
                """, i);
            
            animationIds[index] = given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/animation/play")
            .then()
                .statusCode(202)
                .extract()
                .path("id");
        }
        
        // Verify all animations complete successfully
        await()
            .atMost(15, TimeUnit.SECONDS)
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted(() -> {
                for (String animationId : animationIds) {
                    given()
                        .pathParam("id", animationId)
                    .when()
                        .get("/api/animation/{id}/status")
                    .then()
                        .statusCode(200)
                        .body("status", equalTo("completed"));
                }
            });
    }
    
    @Test
    @Order(5)
    @DisplayName("Should properly handle animation interruption")
    public void testAnimationInterruption() {
        // Start a long animation
        String longAnimationRequest = """
            {
                "type": "gesture",
                "name": "long_wave",
                "duration": 10.0
            }
            """;
        
        String animationId = given()
            .contentType(ContentType.JSON)
            .body(longAnimationRequest)
        .when()
            .post("/api/animation/play")
        .then()
            .statusCode(202)
            .extract()
            .path("id");
        
        // Wait for animation to start
        await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                given()
                    .pathParam("id", animationId)
                .when()
                    .get("/api/animation/{id}/status")
                .then()
                    .body("status", equalTo("playing"));
            });
        
        // Interrupt the animation
        given()
            .pathParam("id", animationId)
        .when()
            .post("/api/animation/{id}/interrupt")
        .then()
            .statusCode(200)
            .body("status", equalTo("interrupted"));
        
        // Verify animation was interrupted
        given()
            .pathParam("id", animationId)
        .when()
            .get("/api/animation/{id}/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("interrupted"))
            .body("completionPercentage", lessThan(100));
    }
    
    @Test
    @Order(6)
    @DisplayName("Should validate animation parameters")
    public void testAnimationParameterValidation() {
        // Test invalid animation type
        String invalidRequest = """
            {
                "type": "invalid_type",
                "name": "test"
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(invalidRequest)
        .when()
            .post("/api/animation/play")
        .then()
            .statusCode(400)
            .body("error", containsString("Invalid animation type"));
        
        // Test missing required parameters
        String incompleteRequest = """
            {
                "type": "gesture"
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(incompleteRequest)
        .when()
            .post("/api/animation/play")
        .then()
            .statusCode(400)
            .body("error", containsString("Missing required parameter: name"));
        
        // Test invalid duration
        String invalidDurationRequest = """
            {
                "type": "gesture",
                "name": "wave",
                "duration": -1.0
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(invalidDurationRequest)
        .when()
            .post("/api/animation/play")
        .then()
            .statusCode(400)
            .body("error", containsString("Duration must be positive"));
    }
    
    @Test
    @Order(7)
    @DisplayName("Should track animation metrics")
    public void testAnimationMetrics() {
        // Get initial metrics
        Map<String, Object> initialMetrics = given()
        .when()
            .get(getGretaManagementUrl() + "/metrics/animation")
        .then()
            .statusCode(200)
            .extract()
            .as(Map.class);
        
        int initialCount = (int) initialMetrics.getOrDefault("totalAnimationsPlayed", 0);
        
        // Play a few animations
        for (int i = 0; i < 5; i++) {
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                        "type": "gesture",
                        "name": "test_%d",
                        "duration": 0.5
                    }
                    """, i))
            .when()
                .post("/api/animation/play")
            .then()
                .statusCode(202);
        }
        
        // Wait for animations to complete
        await().atMost(5, TimeUnit.SECONDS).pollDelay(3, TimeUnit.SECONDS).until(() -> true);
        
        // Check updated metrics
        given()
        .when()
            .get(getGretaManagementUrl() + "/metrics/animation")
        .then()
            .statusCode(200)
            .body("totalAnimationsPlayed", greaterThan(initialCount))
            .body("averageAnimationDuration", notNullValue())
            .body("animationSuccessRate", greaterThan(0.0f));
    }
}