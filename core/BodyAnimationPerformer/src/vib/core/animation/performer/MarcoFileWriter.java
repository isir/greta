/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package vib.core.animation.performer;

import java.io.File;
import java.util.ArrayList;
import vib.core.animation.Joint;
import vib.core.animation.Skeleton;
import vib.core.util.Constants;
import vib.core.util.log.Logs;

/**
 *
 * @author Jing Huang
 */


public class MarcoFileWriter {
    static public void writeFile(String filename, ArrayList<Skeleton> sks){
         try {
            java.io.FileWriter fos;
            if (new File(filename).exists()) {
                fos = new java.io.FileWriter(filename, true);

                //TODO rewrite header

            } else {
                fos = new java.io.FileWriter(filename);
                //String first_line = "2.1 " + filename + " " + Constants.FRAME_PER_SECOND + " " + bapframes.size() + "\n"; // is it good?
                //fos.write(first_line);
            }
           
           
            for (int i = 0; i < sks.size(); ++i) {
                fos.write(i + "\n");
                i++;
                Skeleton sk = sks.get(i);
                for(Joint j : sk.getJoints()){
                    fos.write(j.getName() + " "+ j.getPosition().x() +" "+ j.getPosition().y() +" "+ j.getPosition().z() +" " +j.getLocalRotation().x() + " "+ j.getLocalRotation().y() + " "+j.getLocalRotation().z() + " "+j.getLocalRotation().w() + "\n");
                }
            }
          
            fos.close();
        } catch (Exception ignored2) {
            Logs.warning("Error saving file: " + ignored2);
        }
    }
}
