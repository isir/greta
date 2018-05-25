package vib.core.animation.performer;

/**
 *
 * @author Jing Huang
 */
public class RelativeDynamicsPerformer extends Thread {

    private static final Object mutex = new Object();
    private boolean requestStop = false;

    @Override
    public void run() {
        while (!requestStop) {

            try {
                sleep(5);
            } catch (Exception ex) {
            }  //
            synchronized (mutex) {
                
                
            }
        }
    }

}
