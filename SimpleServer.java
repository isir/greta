import java.io.*;
import java.net.*;

public class SimpleServer {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8080);
        System.out.println("Server started on port 8080");
        
        while (true) {
            Socket client = server.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream());
            
            String line = in.readLine();
            System.out.println("Request: " + line);
            
            // Skip headers
            while ((line = in.readLine()) != null && !line.isEmpty()) {}
            
            // Send response
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/plain");
            out.println();
            out.println("Greta Platform - Running in Docker");
            out.println("Status: OK");
            out.println("Time: " + new java.util.Date());
            out.flush();
            
            client.close();
        }
    }
}