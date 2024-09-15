package greta.auxiliary.activemq.gui;

/**
 *
 * @author saga
 */


public class ShutdownHook extends Thread {
    
    private Process process;
    
    public ShutdownHook(Process p) {
        process = p;
    }
    
    @Override
    public void run() {
        try {
            process.destroy();
            System.out.println("greta.auxiliary.activemq.gui.ShutdownHook(): ActiveMQ broker was successfully killed.");
        }
        catch (Exception e) {
            
        }
    }
}
