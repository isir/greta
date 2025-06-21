import java.io.*;
import java.net.*;
import java.time.LocalDateTime;

/**
 * Simple HTTP server for headless Greta container
 * Provides basic status information when GUI cannot run
 */
public class HeadlessServer {
    private static final int PORT = 8080;
    
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Greta Headless Server started on port " + PORT);
            System.out.println("Access at http://localhost:" + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
    
    private static void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream())) {
            
            // Read the request line
            String requestLine = in.readLine();
            System.out.println("Request: " + requestLine);
            
            // Skip headers
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                // Skip headers
            }
            
            // Send response
            String response = buildResponse(requestLine);
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html");
            out.println("Content-Length: " + response.length());
            out.println();
            out.println(response);
            
        } catch (IOException e) {
            System.err.println("Request handling error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    private static String buildResponse(String requestLine) {
        String path = requestLine != null && requestLine.contains(" ") ? 
                      requestLine.split(" ")[1] : "/";
        
        if ("/health".equals(path)) {
            return "{\"status\":\"ok\",\"timestamp\":\"" + LocalDateTime.now() + "\"}";
        }
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Greta Platform - Headless Mode</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
                    .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    h1 { color: #2c3e50; }
                    .status { background: #d4edda; color: #155724; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .info { background: #d1ecf1; color: #0c5460; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    code { background: #f8f9fa; padding: 2px 5px; border-radius: 3px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🎭 Greta Platform</h1>
                    <div class="status">
                        ✅ <strong>Status:</strong> Running in headless mode<br>
                        ⏰ <strong>Time:</strong> """ + LocalDateTime.now() + """
                    </div>
                    
                    <h2>About Greta</h2>
                    <p>Greta is a platform for creating embodied conversational agents with:</p>
                    <ul>
                        <li>🗣️ Speech synthesis and recognition</li>
                        <li>🤖 Behavior planning and realization</li>
                        <li>💭 Emotion and intention modeling</li>
                        <li>🎨 Animation and gesture generation</li>
                        <li>🔗 Multimodal AI integration</li>
                    </ul>
                    
                    <div class="info">
                        <strong>Note:</strong> The main Greta application is a desktop GUI application. 
                        This web interface is provided for container environments where GUI display is not available.
                    </div>
                    
                    <h2>API Endpoints</h2>
                    <ul>
                        <li><a href="/health">/health</a> - Health check (JSON)</li>
                        <li><a href="/">/</a> - This status page</li>
                    </ul>
                    
                    <h2>Quick Links</h2>
                    <ul>
                        <li><a href="https://github.com/isir/greta">📂 GitHub Repository</a></li>
                        <li><a href="https://github.com/isir/greta/docs">📖 Documentation</a></li>
                    </ul>
                    
                    <footer style="margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; color: #666;">
                        <small>Greta Platform v1.0.0-SNAPSHOT | Container Mode</small>
                    </footer>
                </div>
            </body>
            </html>
            """;
    }
}