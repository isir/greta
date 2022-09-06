/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.sequencemining;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Mathieu
 */
public class Util {
    
    public static void checkDeleteOutputFile(String filename)
    {
            File f = new File(filename);
            if(f.exists()) f.delete();
    }

    static void createDirectory(String outputCluster) throws IOException {
        File f = new File(outputCluster);
        f.mkdir();
    }
}
