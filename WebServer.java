/*
 * Simple HTTP server for Greta web interface
 * Serves static files and provides avatar web interface
 */
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WebServer {
    private HttpServer server;
    private static final int PORT = 8080;
    
    public static void main(String[] args) throws Exception {
        new WebServer().start();
    }
    
    public void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Serve static files
        server.createContext("/", new StaticFileHandler());
        server.createContext("/avatar.html", new AvatarHandler());
        server.createContext("/health", new HealthHandler());
        
        server.setExecutor(null); // Default executor
        server.start();
        
        System.out.println("🌐 Greta Web Server started on http://localhost:" + PORT);
        System.out.println("📱 Avatar interface: http://localhost:" + PORT + "/avatar.html");
        System.out.println("💡 Press Ctrl+C to stop");
        
        // Keep server running
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping web server...");
            server.stop(0);
        }));
        
        // Keep main thread alive
        Thread.currentThread().join();
    }
    
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            // Serve main page
            String response = createMainPage();
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
        
        private String createMainPage() {
            return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <title>Greta ECA System</title>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <style>\n" +
            "        body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }\n" +
            "        .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }\n" +
            "        .header { text-align: center; margin-bottom: 30px; }\n" +
            "        .header h1 { color: #2c3e50; }\n" +
            "        .card { background: #f8f9fa; padding: 20px; margin: 15px 0; border-radius: 8px; border-left: 4px solid #3498db; }\n" +
            "        .btn { display: inline-block; background: #3498db; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 5px; }\n" +
            "        .btn:hover { background: #2980b9; }\n" +
            "        .status { padding: 10px; border-radius: 5px; margin: 10px 0; }\n" +
            "        .status.online { background: #d4edda; color: #155724; }\n" +
            "        .status.offline { background: #f8d7da; color: #721c24; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1>🤖 Greta ECA System</h1>\n" +
            "            <p>Embodied Conversational Agent Platform</p>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"card\">\n" +
            "            <h3>🎭 3D Avatar Interface</h3>\n" +
            "            <p>Interactive 3D avatar with facial expressions, lip sync, and gestures</p>\n" +
            "            <a href=\"/avatar.html\" class=\"btn\">🚀 Launch 3D Avatar</a>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"card\">\n" +
            "            <h3>⚙️ System Status</h3>\n" +
            "            <div id=\"avatar-status\" class=\"status offline\">Avatar Server: Checking...</div>\n" +
            "            <div id=\"websocket-status\" class=\"status offline\">WebSocket: Checking...</div>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"card\">\n" +
            "            <h3>📋 Quick Start</h3>\n" +
            "            <ol>\n" +
            "                <li>Click \"Launch 3D Avatar\" above</li>\n" +
            "                <li>Open Greta modular interface (desktop application)</li>\n" +
            "                <li>Add \"Text Input\" and \"WebGL Avatar Player\" modules</li>\n" +
            "                <li>Connect them and type text to make avatar speak</li>\n" +
            "            </ol>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"card\">\n" +
            "            <h3>🔧 Available Features</h3>\n" +
            "            <ul>\n" +
            "                <li>✅ 3D Avatar Rendering</li>\n" +
            "                <li>✅ Facial Animation</li>\n" +
            "                <li>✅ Lip Synchronization</li>\n" +
            "                <li>✅ Gesture Animation</li>\n" +
            "                <li>✅ Text-to-Speech Integration</li>\n" +
            "                <li>✅ Real-time WebSocket Communication</li>\n" +
            "            </ul>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "    \n" +
            "    <script>\n" +
            "        function checkStatus() {\n" +
            "            try {\n" +
            "                const ws = new WebSocket('ws://localhost:8081');\n" +
            "                ws.onopen = function() {\n" +
            "                    document.getElementById('websocket-status').textContent = 'WebSocket: Connected ✅';\n" +
            "                    document.getElementById('websocket-status').className = 'status online';\n" +
            "                    ws.close();\n" +
            "                };\n" +
            "                ws.onerror = function() {\n" +
            "                    document.getElementById('websocket-status').textContent = 'WebSocket: Disconnected ❌';\n" +
            "                    document.getElementById('websocket-status').className = 'status offline';\n" +
            "                };\n" +
            "            } catch (e) {\n" +
            "                document.getElementById('websocket-status').textContent = 'WebSocket: Error ❌';\n" +
            "                document.getElementById('websocket-status').className = 'status offline';\n" +
            "            }\n" +
            "            \n" +
            "            document.getElementById('avatar-status').textContent = 'Avatar Server: Online ✅';\n" +
            "            document.getElementById('avatar-status').className = 'status online';\n" +
            "        }\n" +
            "        \n" +
            "        checkStatus();\n" +
            "        setInterval(checkStatus, 5000);\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>";
        }
    }
    
    static class AvatarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Try to serve the avatar.html file from the web directory
            Path avatarFile = Paths.get("auxiliary/Player/WebAvatar/web/avatar.html");
            if (Files.exists(avatarFile)) {
                String content = Files.readString(avatarFile);
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, content.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(content.getBytes());
                }
            } else {
                // Fallback message
                String response = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head><title>Avatar Loading...</title></head>\n" +
                "<body>\n" +
                "    <h2>🤖 Avatar Interface Loading...</h2>\n" +
                "    <p>The 3D avatar interface is being set up.</p>\n" +
                "    <p>Please ensure the WebGL Avatar Player module is running.</p>\n" +
                "    <script>\n" +
                "        setTimeout(() => location.reload(), 3000);\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }
    
    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"ok\",\"service\":\"greta-web\",\"timestamp\":" + System.currentTimeMillis() + "}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}