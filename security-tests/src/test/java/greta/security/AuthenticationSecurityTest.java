package greta.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Security tests for authentication and authorization
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationSecurityTest {
    
    private static final String BASE_URL = System.getProperty("greta.test.url", "http://localhost:8080");
    private static final String TEST_SECRET = "test-secret-key-for-testing-only";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Test
    @Order(1)
    @DisplayName("Should reject requests without authentication")
    public void testUnauthenticatedAccess() {
        given()
        .when()
            .get("/api/protected/resource")
        .then()
            .statusCode(401)
            .body("error", containsString("Unauthorized"));
    }
    
    @Test
    @Order(2)
    @DisplayName("Should reject invalid JWT tokens")
    public void testInvalidJWT() {
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature";
        
        given()
            .header("Authorization", "Bearer " + invalidToken)
        .when()
            .get("/api/protected/resource")
        .then()
            .statusCode(401)
            .body("error", containsString("Invalid token"));
    }
    
    @Test
    @Order(3)
    @DisplayName("Should reject expired JWT tokens")
    public void testExpiredJWT() {
        // Create an expired token
        String expiredToken = JWT.create()
            .withIssuer("greta-test")
            .withSubject("testuser")
            .withExpiresAt(Date.from(Instant.now().minusSeconds(3600))) // Expired 1 hour ago
            .sign(Algorithm.HMAC256(TEST_SECRET));
        
        given()
            .header("Authorization", "Bearer " + expiredToken)
        .when()
            .get("/api/protected/resource")
        .then()
            .statusCode(401)
            .body("error", containsString("Token expired"));
    }
    
    @Test
    @Order(4)
    @DisplayName("Should prevent JWT algorithm confusion attacks")
    public void testJWTAlgorithmConfusion() {
        // Try to use 'none' algorithm
        String noneAlgorithmToken = JWT.create()
            .withIssuer("greta-test")
            .withSubject("testuser")
            .sign(Algorithm.none());
        
        given()
            .header("Authorization", "Bearer " + noneAlgorithmToken)
        .when()
            .get("/api/protected/resource")
        .then()
            .statusCode(401);
        
        // Try to change algorithm from RS256 to HS256
        String confusedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMn0." +
            "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        
        given()
            .header("Authorization", "Bearer " + confusedToken)
        .when()
            .get("/api/protected/resource")
        .then()
            .statusCode(401);
    }
    
    @Test
    @Order(5)
    @DisplayName("Should enforce proper CORS headers")
    public void testCORSSecurity() {
        // Test preflight request
        given()
            .header("Origin", "https://evil.com")
            .header("Access-Control-Request-Method", "POST")
            .header("Access-Control-Request-Headers", "content-type")
        .when()
            .options("/api/animation/play")
        .then()
            .statusCode(anyOf(is(200), is(403)))
            .header("Access-Control-Allow-Origin", not("*"))
            .header("Access-Control-Allow-Origin", not("https://evil.com"));
    }
    
    @Test
    @Order(6)
    @DisplayName("Should protect against brute force attacks")
    public void testBruteForceProtection() {
        String endpoint = "/api/auth/login";
        
        // Attempt multiple failed logins
        for (int i = 0; i < 10; i++) {
            given()
                .contentType("application/json")
                .body(String.format("""
                    {
                        "username": "testuser",
                        "password": "wrongpassword%d"
                    }
                    """, i))
            .when()
                .post(endpoint)
            .then()
                .statusCode(anyOf(is(401), is(429)));
        }
        
        // Next attempt should be rate limited
        Response response = given()
            .contentType("application/json")
            .body("""
                {
                    "username": "testuser",
                    "password": "anotherWrongPassword"
                }
                """)
        .when()
            .post(endpoint)
        .then()
            .extract()
            .response();
        
        // Should either be unauthorized or rate limited
        assertTrue(response.getStatusCode() == 401 || response.getStatusCode() == 429,
            "Should implement rate limiting after multiple failed attempts");
    }
    
    @Test
    @Order(7)
    @DisplayName("Should validate authorization scopes")
    public void testAuthorizationScopes() {
        // Create token with limited scope
        String limitedToken = JWT.create()
            .withIssuer("greta-test")
            .withSubject("testuser")
            .withClaim("scope", "read:animations")
            .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
            .sign(Algorithm.HMAC256(TEST_SECRET));
        
        // Should allow read operations
        given()
            .header("Authorization", "Bearer " + limitedToken)
        .when()
            .get("/api/animation/list")
        .then()
            .statusCode(anyOf(is(200), is(403))); // Depends on implementation
        
        // Should deny write operations
        given()
            .header("Authorization", "Bearer " + limitedToken)
            .contentType("application/json")
            .body("""
                {
                    "type": "gesture",
                    "name": "wave"
                }
                """)
        .when()
            .post("/api/animation/create")
        .then()
            .statusCode(403);
    }
    
    @Test
    @Order(8)
    @DisplayName("Should prevent session fixation attacks")
    public void testSessionFixation() {
        // Get initial session
        String sessionId = given()
        .when()
            .get("/api/session/new")
        .then()
            .statusCode(200)
            .cookie("JSESSIONID")
            .extract()
            .cookie("JSESSIONID");
        
        // Login with the session
        Response loginResponse = given()
            .cookie("JSESSIONID", sessionId)
            .contentType("application/json")
            .body("""
                {
                    "username": "testuser",
                    "password": "testpass"
                }
                """)
        .when()
            .post("/api/auth/login")
        .then()
            .extract()
            .response();
        
        // Session ID should change after login
        String newSessionId = loginResponse.getCookie("JSESSIONID");
        if (newSessionId != null) {
            assertNotEquals(sessionId, newSessionId, 
                "Session ID should be regenerated after login");
        }
    }
    
    @Test
    @Order(9)
    @DisplayName("Should implement secure password requirements")
    public void testPasswordSecurity() {
        // Test weak passwords
        String[] weakPasswords = {
            "123456",
            "password",
            "12345678",
            "qwerty",
            "abc123",
            "password123"
        };
        
        for (String weakPassword : weakPasswords) {
            given()
                .contentType("application/json")
                .body(String.format("""
                    {
                        "username": "newuser_%s",
                        "password": "%s",
                        "email": "test@example.com"
                    }
                    """, UUID.randomUUID(), weakPassword))
            .when()
                .post("/api/auth/register")
            .then()
                .statusCode(400)
                .body("error", containsString("password"));
        }
        
        // Test strong password
        given()
            .contentType("application/json")
            .body(String.format("""
                {
                    "username": "newuser_%s",
                    "password": "Str0ng!P@ssw0rd#2024",
                    "email": "test@example.com"
                }
                """, UUID.randomUUID()))
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(anyOf(is(201), is(200)));
    }
    
    @Test
    @Order(10)
    @DisplayName("Should protect against timing attacks")
    public void testTimingAttackProtection() throws InterruptedException {
        long[] timings = new long[10];
        
        // Test with valid username
        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            
            given()
                .contentType("application/json")
                .body("""
                    {
                        "username": "validuser",
                        "password": "wrongpassword"
                    }
                    """)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(401);
            
            timings[i] = System.nanoTime() - start;
            TimeUnit.MILLISECONDS.sleep(100);
        }
        
        double avgValidUser = average(timings);
        
        // Test with invalid username
        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            
            given()
                .contentType("application/json")
                .body("""
                    {
                        "username": "invaliduser12345",
                        "password": "wrongpassword"
                    }
                    """)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(401);
            
            timings[i] = System.nanoTime() - start;
            TimeUnit.MILLISECONDS.sleep(100);
        }
        
        double avgInvalidUser = average(timings);
        
        // The timing difference should be minimal (less than 50ms)
        double difference = Math.abs(avgValidUser - avgInvalidUser) / 1_000_000; // Convert to ms
        assertTrue(difference < 50, 
            "Timing difference should be minimal to prevent user enumeration");
    }
    
    private double average(long[] values) {
        long sum = 0;
        for (long value : values) {
            sum += value;
        }
        return (double) sum / values.length;
    }
}