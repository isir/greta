/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.file;

import vib.core.util.log.Logs;
import java.io.File;
import java.io.IOException;

/**
 * Thread for deleting a file.
 * Try again as long as the file isn't deleted and max tries allowed hasn't be exceeded.
 *
 * @author Brian Ravenet
 */
public class DeleteFileThread extends Thread {

    /**
     * The file to delete
     */
    private File file;
    /**
     * The delay before trying to delete
     */
    private long delay;
    /**
     * Maximum tries allowed
     */
    private int maxTries;
    /**
     * The number of tries
     */
    private int count;
    /**
     * Default value for delay
     */
    private static int DEFAULT_BEGIN_DELAY = 10000;
    /**
     * Default maximum tries allowed
     */
    private static int DEFAULT_MAXIMUM_TRIES_ALLOWED = 3600; // 3600 * 1000 secondes = 1h

    /**
     * Default Constructor
     *
     * @param file The file to be deleted
     * @param delay The delay before trying to delete
     * @param maxTries Maximum tries allowed (0 means default)
     */
    protected DeleteFileThread(File file, long delay, int maxTries) {
        super();
        this.file = file;
        this.delay = delay;
        this.maxTries = (maxTries == 0 ? DEFAULT_MAXIMUM_TRIES_ALLOWED : maxTries);
        this.count = 0;
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        try {
            sleep(delay);
            while (count < maxTries && !file.delete() && file.exists()) {
                ++count;
                if (count == maxTries) {
                    Logs.warning(DeleteFileThread.class.getName()+": Max tries exceeded (" + maxTries + ") and the file '" + file.getCanonicalPath() + "' hasn't be deleted");
                }
                sleep(1000); // sleep for 1sec (= try again in 1sec)
            }
        } catch (InterruptedException e) {
            //sleep interrupted
        } catch (IOException e) {
            //canonical path fail
        }
    }

    /**
     * @param file The file to be deleted
     */
    public static void createThread(File file) {
        createThread(file, DEFAULT_BEGIN_DELAY, 0);
    }

    /**
     * @param file The file to be deleted
     * @param delay The delay before trying to delete
     */
    public static void createThread(File file, long delay) {
        createThread(file, delay, 0);
    }

    /**
     * @param file The file to be deleted
     * @param delay The delay before trying to delete
     * @param maxTries Maximum tries allowed (0 means default)
     */
    public static void createThread(File file, long delay, int maxTries) {
        DeleteFileThread dft = new DeleteFileThread(file, delay, maxTries);
        dft.setDaemon(true);
        dft.start();
    }
}
