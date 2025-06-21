/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
