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
 * Format based on <https://github.com/TadasBaltrusaitis/OpenFace>:
 * frame_id,        face_id,        timestamp,      confidence,     success,
 * gaze_0_x,        gaze_0_y,       gaze_0_z,
 * gaze_1_x,        gaze_1_y,       gaze_1_z,
 * gaze_angle_x,    gaze_angle_y,
 * pose_Tx,         pose_Ty,        pose_Tz,
 * pose_Rx,         pose_Ry,        pose_Rz,
 * AUs_r,           AUs_c
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

    private final static int MAX_AUS = 18;
    private final static List<String> expectedPreAUHeader
            = Arrays.asList(("frame_id,face_id,timestamp,confidence,success,"
                    + "gaze_0_x,gaze_0_y,gaze_0_z,"
                    + "gaze_1_x,gaze_1_y,gaze_1_z,"
                    + "gaze_angle_x,gaze_angle_y,"
                    + "pose_Tx,pose_Ty,pose_Tz,pose_Rx,pose_Ry,pose_Rz").split(","));

    private static List<String> auKeys = new ArrayList<>();
    private static List<String> auMaskKeys = new ArrayList<>();
    private static List<String> selectedHeaders = null;

    public static String[] headers;

    public static Map<String, Integer> featureKeysMap = new HashMap<>();
    public static Map<String, AU> ausMap = new HashMap<>();
    public static Map<String, AU> auMasksMap = new HashMap<>();

    public final static String BLINK_AU = "AU45_r";

    public static int getAURCount() {
        return ausMap.size();
    }

    public static int getAUCCount() {
        return auMasksMap.size();
    }

    public static String separator = ", *";

    public int frameId = 0;
    public int faceId = 0;

    public double timestamp = 0.0;
    public double confidence = 0.0;

    public boolean success = false;

    public Vec3d gaze0 = new Vec3d();
    public Vec3d gaze1 = new Vec3d();

    public double gazeAngleX;
    public double gazeAngleY;

    public Vec3d headPoseT = new Vec3d();
    public Vec3d headPoseR = new Vec3d();

    public double[] aus = new double[MAX_AUS];
    public double[] auMasks = new double[MAX_AUS];
    public double[] intensity = new double[MAX_AUS];
    public double blink = 0.0;

    public static boolean readHeader(String line) {

        String[] outputs = line.split(separator);
        if (OpenFaceFrame.headers != null && Arrays.equals(OpenFaceFrame.headers, outputs)) {
            return false;
        }

        featureKeysMap.clear();
        ausMap.clear();
        auMasksMap.clear();

        Pattern p = Pattern.compile("AU([0-9]+)_[rc]");
        for (int i = 0; i < outputs.length; ++i) {
            if (expectedPreAUHeader.contains(outputs[i])) {
                featureKeysMap.put(outputs[i], i);
            }
            else {
                Matcher m = p.matcher(outputs[i]);
                if (m.matches()) {
                    if (outputs[i].endsWith("r")) {
                        auKeys.add(outputs[i]);
                        ausMap.put(outputs[i], new AU(i, m.group(1)));
                    }
                    else if (outputs[i].endsWith("c")) {
                        auMaskKeys.add(outputs[i]);
                        auMasksMap.put(outputs[i], new AU(i, m.group(1)));
                    }
                }
            }
        }

        OpenFaceFrame.headers = outputs;

        return true;
    }

    public static int getAUNum(int index) {
        String key = auKeys.get(index);
        return ausMap.get(key).num;
    }

    public static int getAUMaskNum(int index) {
        String key = auMaskKeys.get(index);
        return auMasksMap.get(key).num;
    }

    private boolean isHeaderSelected(String header) {
        return selectedHeaders == null || selectedHeaders.contains(header);
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
        return 0.0;
    }

    public void readDataLine(String data) {

        String[] outputs = data.split(separator);

        frameId     = Integer.parseInt(readDataCol("frame_id", outputs, featureKeysMap));
        faceId      = Integer.parseInt(readDataCol("face_id", outputs, featureKeysMap));
        timestamp   = Double.parseDouble(readDataCol("timestamp", outputs, featureKeysMap));
        confidence  = Double.parseDouble(readDataCol("confidence", outputs, featureKeysMap));
        success     = Integer.parseInt(readDataCol("success", outputs, featureKeysMap)) == 1;

        gaze0.set(Double.parseDouble(readDataCol("gaze_0_x", outputs, featureKeysMap)),
                Double.parseDouble(readDataCol("gaze_0_y", outputs, featureKeysMap)),
                Double.parseDouble(readDataCol("gaze_0_z", outputs, featureKeysMap)));
        gaze1.set(Double.parseDouble(readDataCol("gaze_1_x", outputs, featureKeysMap)),
                Double.parseDouble(readDataCol("gaze_1_y", outputs, featureKeysMap)),
                Double.parseDouble(readDataCol("gaze_1_z", outputs, featureKeysMap)));

        gazeAngleX  = Double.parseDouble(readDataCol("gaze_angle_x", outputs, featureKeysMap));
        gazeAngleX  = Double.parseDouble(readDataCol("gaze_angle_y", outputs, featureKeysMap));

        headPoseT.set(Double.parseDouble(readDataCol("pose_Tx", outputs, featureKeysMap)),
                Double.parseDouble(readDataCol("pose_Ty", outputs, featureKeysMap)),
                Double.parseDouble(readDataCol("pose_Tz", outputs, featureKeysMap)));
        headPoseR.set(Double.parseDouble(readDataCol("pose_Rx", outputs, featureKeysMap)),
                Double.parseDouble(readDataCol("pose_Ry", outputs, featureKeysMap)),
                Double.parseDouble(readDataCol("pose_Rz", outputs, featureKeysMap)));

        int i = 0;
        for (String key : ausMap.keySet()) {
            aus[i] = readAUDataCol(key, outputs, ausMap) / 5.0; // AU**_r are between 0-5.0
            if (BLINK_AU.equals(key)) {
                blink = aus[i];
            }
            ++i;
        }
        i = 0;
        for (String key : auMasksMap.keySet()) {
            auMasks[i++] = readAUDataCol(key, outputs, auMasksMap);
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
        gaze0       = f.gaze0.clone();
        gaze1       = f.gaze1.clone();
        gazeAngleX  = f.gazeAngleX;
        gazeAngleY  = f.gazeAngleY;
        headPoseT   = f.headPoseT.clone();
        headPoseR   = f.headPoseR.clone();
        System.arraycopy(f.aus,         0, aus,         0, f.aus.length);
        System.arraycopy(f.auMasks,     0, auMasks,     0, f.auMasks.length);
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
