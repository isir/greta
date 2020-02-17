/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.auxiliary.environmentmanager.util;

import greta.auxiliary.environmentmanager.core.IEnvironmentServer;
import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.util.log.Logs;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Brice Donval
 */
public class Toolbox {

    /* ---------------------------------------------------------------------- */
    /*                                                                        */
    /*                                 COLORS                                 */
    /*                                                                        */
    /* ---------------------------------------------------------------------- */

    public static final Color colorGreen = new Color(0, 150, 0);
    public static final Color colorOrange = new Color(250, 100, 0);
    public static final Color colorRed = Color.RED;

    /* ---------------------------------------------------------------------- */
    /*                                                                        */
    /*                                  LOGS                                  */
    /*                                                                        */
    /* ---------------------------------------------------------------------- */

    public static void log(IEnvironmentServer environmentServer, MPEG4Animatable mpegAnimatable, String message) {
        if (Config.EnvironmentManager_VerboseMode) {
            String line = "_______________";
            String environmentServerId = (environmentServer != null) ? environmentServer.getIdentifier() : "";
            String separator = (mpegAnimatable != null) ? " : " : "";
            String mpegAnimatableId = (mpegAnimatable != null) ? mpegAnimatable.getIdentifier() : "";
            log(line + "  " + environmentServerId + separator + mpegAnimatableId + "  " + line + "\n" + message + "\n" + line + "  END");
        }
    }

    public static void log(IEnvironmentServer environmentServer, String message) {
        log(environmentServer, null, message);
    }

    public static void logIntoFile(String s, String fileName) {
        if (Config.EnvironmentManager_VerboseMode) {
            writeFile(s, fileName);
        }
    }

    private static void log(String message) {
        if (Config.EnvironmentManager_VerboseMode) {
            Logs.info("\n" + message + "\n");
        }
    }

    /* ---------------------------------------------------------------------- */

    public static void writeFile(String s, String fileName) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(fileName));
            out.write(s);
        } catch (IOException e) {
            log(e.toString());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                log(e.toString());
            }
        }
    }

}
