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
package greta.auxiliary.openface2.util;

import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This file represents an OpenFace2 frame
 *
 * @author Philippe Gauthier <philippe.gauthier@sorbonne-universite.fr>
 * @author Brice Donval
 */
public class OpenFaceFrame {

    private static final Logger LOGGER = Logger.getLogger(OpenFaceFrame.class.getName());

    private static class AU {
        public int index;   // integer representing the column index where it's read from
        public int num;     // number representing the AU (ex: 1, for key "AU01_r")

        public AU(int index, int num) {
            this.index = index;
            this.num = num;
        }

        public AU(int index, String num) {
            this.index = index;
            this.num = Integer.parseInt(num);
        }
    }

    private final static int MAX_AUS = 100;
    private final static List<String> expectedPreAUHeader
            = Arrays.asList(("frame_id,face_id,timestamp,confidence,success,"
                    + "head_x,head_y,head_z,head_rx,head_ry,head_rz,"
                    + "gaze_0_x,gaze_0_y,gaze_0_z,gaze_1_x,gaze_1_y,"
                    + "gaze_1_z,gaze_angle_x,gaze_angle_y").split(","));

    private static List<String> selectedHeaders = null;
    private static List<String> aurKeys = new ArrayList<>();
    private static List<String> aucKeys = new ArrayList<>();

    public final static String BLINK_AU = "AU45_r";

    public static Map<String, Integer> keysOrder = new HashMap<>();
    public static Map<String, AU> keysAUROrder = new HashMap<>();
    public static Map<String, AU> keysAUCOrder = new HashMap<>();
    public static String[] headers;

    public static int getAURCount() {
        return keysAUROrder.size();
    }

    public static int getAUCCount() {
        return keysAUCOrder.size();
    }

    public static int col_blink;
    public static String separator = ",";

    public int frameId = 0;
    public int faceId = 0;

    public double timestamp = 0;
    public double confidence = 0;

    public boolean success = false;

    public Vec3d headPos = new Vec3d();
    public Vec3d headRot = new Vec3d();

    public Vec3d gaze0 = new Vec3d();
    public Vec3d gaze1 = new Vec3d();

    public double gazeAngleX;
    public double gazeAngleY;

    public double[] au_r = new double[MAX_AUS];
    public double[] au_c = new double[MAX_AUS];
    public double[] intensity = new double[MAX_AUS];
    public double blink = 0;

    public static boolean readHeader(String line) {
        String[] headPoseCols = line.split(separator);
        Pattern p = Pattern.compile("AU([0-9]+)_[rc]");

        if (OpenFaceFrame.headers != null && Arrays.equals(OpenFaceFrame.headers, headPoseCols)) {
            return false;
        }
        keysOrder.clear();
        keysAUROrder.clear();
        keysAUCOrder.clear();
        for (int i = 0; i < headPoseCols.length; i++) {
            if (expectedPreAUHeader.contains(headPoseCols[i])) {
                keysOrder.put(headPoseCols[i], i);
            }

            Matcher m = p.matcher(headPoseCols[i]);
            if (m.matches()) {
                if (headPoseCols[i].endsWith("r")) {
                    AU au = new AU(i, m.group(1));
                    keysAUROrder.put(headPoseCols[i], au);
                    aurKeys.add(headPoseCols[i]);
                }
                if (headPoseCols[i].endsWith("c")) {
                    keysAUCOrder.put(headPoseCols[i], new AU(i, m.group(1)));
                    aucKeys.add(headPoseCols[i]);
                }
            }
        }

        OpenFaceFrame.headers = headPoseCols;

        return true;
    }

    public static int getAURIndex(int index) {
        String key = aurKeys.get(index);
        return keysAUROrder.get(key).num;
    }

    public static int getAUCIndex(int index) {
        String key = aucKeys.get(index);
        return keysAUCOrder.get(key).num;
    }

    private boolean isHeaderSelected(String h) {
        return selectedHeaders == null || selectedHeaders.contains(h);
    }

    private String readDataCol(String key, String[] cols, Map<String, Integer> set) {
        if (isHeaderSelected(key)) {
            if (set.containsKey(key)) {
                return cols[set.get(key)];
            } else {
                LOGGER.warning(String.format("Map doesn't contains key: %s", key));
            }
        }
        return "0";
    }

    private double readAUDataCol(String key, String[] cols, Map<String, AU> set) {
        if (isHeaderSelected(key)) {
            return Double.parseDouble(cols[set.get(key).index]);
        }
        return 0.;
    }

    public void readDataLine(String data) {
        String[] cols = data.split(separator);

        frameId     = Integer.parseInt(readDataCol("frame_id", cols, keysOrder));
        faceId      = Integer.parseInt(readDataCol("face_id", cols, keysOrder));
        timestamp   = Double.parseDouble(readDataCol("timestamp", cols, keysOrder));
        confidence  = Double.parseDouble(readDataCol("confidence", cols, keysOrder));
        success     = Integer.parseInt(readDataCol("success", cols, keysOrder)) == 1;
        headPos.set(Double.parseDouble(readDataCol("head_x", cols, keysOrder)),
                Double.parseDouble(readDataCol("head_y", cols, keysOrder)),
                Double.parseDouble(readDataCol("head_z", cols, keysOrder)));
        headRot.set(Double.parseDouble(readDataCol("head_rx", cols, keysOrder)),
                Double.parseDouble(readDataCol("head_ry", cols, keysOrder)),
                Double.parseDouble(readDataCol("head_rz", cols, keysOrder)));
        gaze0.set(Double.parseDouble(readDataCol("gaze_0_x", cols, keysOrder)),
                Double.parseDouble(readDataCol("gaze_0_y", cols, keysOrder)),
                Double.parseDouble(readDataCol("gaze_0_z", cols, keysOrder)));
        gaze1.set(Double.parseDouble(readDataCol("gaze_1_x", cols, keysOrder)),
                Double.parseDouble(readDataCol("gaze_1_y", cols, keysOrder)),
                Double.parseDouble(readDataCol("gaze_1_z", cols, keysOrder)));
        gazeAngleX  = Double.parseDouble(readDataCol("gaze_angle_x", cols, keysOrder));
        gazeAngleX  = Double.parseDouble(readDataCol("gaze_angle_y", cols, keysOrder));

        int j = 0;
        for (String key : keysAUROrder.keySet()) {
            au_r[j] = readAUDataCol(key, cols, keysAUROrder) / 5.; // AU**_r are between 0-5.
            if (BLINK_AU.equals(key)) {
                blink = au_r[j];
            }
            j++;
        }
        j = 0;
        for (String key : keysAUCOrder.keySet()) {
            au_c[j++] = readAUDataCol(key, cols, keysAUCOrder);
        }
    }

    /*public boolean isNumeric(String s) {
        return s.matches("[-+ ]?\\d*\\.?\\d+");
    }*/

    public OpenFaceFrame clone() {
        OpenFaceFrame f = new OpenFaceFrame();
        f.copy(this);
        return f;
    }

    public void copy(OpenFaceFrame f) {
        frameId     = f.frameId;
        faceId      = f.faceId;
        timestamp   = f.timestamp;
        confidence  = f.confidence;
        success     = f.success;
        headPos     = f.headPos.clone();
        headRot     = f.headRot.clone();
        gaze0       = f.gaze0.clone();
        gaze1       = f.gaze1.clone();
        gazeAngleX  = f.gazeAngleX;
        gazeAngleY  = f.gazeAngleY;
        System.arraycopy(f.au_r,        0, au_r,        0, f.au_r.length);
        System.arraycopy(f.au_c,        0, au_c,        0, f.au_c.length);
        System.arraycopy(f.intensity,   0, intensity,   0, f.intensity.length);
    }

    /**
     * @return the selectedHeaders
     */
    public static List<String> getSelectedHeaders() {
        return selectedHeaders;
    }

    /**
     * @param aSelectedHeaders the selectedHeaders to set
     */
    public static void setSelectedHeaders(List<String> aSelectedHeaders) {
        selectedHeaders = aSelectedHeaders;
    }

    /**
     * @param headers the selectedHeaders to set
     */
    public static void setSelectedHeaders(String[] headers) {
        selectedHeaders = Arrays.asList(headers);
    }
}
