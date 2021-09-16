 package greta.application.ariavaluspa.tools;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.ser1.stomp.Server;
import greta.core.util.log.Logs;

/**
 *
 * @author Angelo Cafaro
 */


public class ARIAStompBroker {
    
    // CONNECTION PARAMETERS
    private final int STOMP_PORT;
    
    // DEFAULT VALUES
    private static final int DEFAULT_STOMP_PORT = 61616;
        
    private Server server;
    
    public ARIAStompBroker() {
        this(DEFAULT_STOMP_PORT);
    }
    
    public ARIAStompBroker(int stompPort)
    {
        STOMP_PORT = stompPort;
        StartServer();
    }
    
    public void finalize()
     {
          StopServer();
     }
    
    public void StartServer(){
        try {
            server = new Server(STOMP_PORT);
            Logs.info(ARIAStompBroker.class.getSimpleName() + ": ARIA STOMP Broker Started.");
        } 
        catch (IOException ex) 
        {
            Logs.error(ARIAVideoRequestsManager.class.getSimpleName() + " [" + ex + "]");
        }
        
    }
    
    public void StopServer()
    {
        if(server != null)
        {
            server.stop();
            Logs.info(ARIAStompBroker.class.getSimpleName() + ": ARIA STOMP Broker Stopped.");
        }
    }
}