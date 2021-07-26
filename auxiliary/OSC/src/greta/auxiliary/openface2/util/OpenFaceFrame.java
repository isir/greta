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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * This file represents an OpenFace2 frame
 *
 * Format based on <https://github.com/TadasBaltrusaitis/OpenFace>:
 * frame,           face_id,        timestamp,      confidence,     success,
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

    private final static int MAX_AUS = 18;
    private final static List<String> expectedPreAUFeatures
            = Arrays.asList(("frame,face_id,timestamp,confidence,success,"
                    + "gaze_0_x,gaze_0_y,gaze_0_z,"
                    + "gaze_1_x,gaze_1_y,gaze_1_z,"
                    + "gaze_angle_x,gaze_angle_y,"
                    + "pose_Tx,pose_Ty,pose_Tz,pose_Rx,pose_Ry,pose_Rz").split(","));

    private static List<String> auFeatureKeys = new ArrayList<>();
    private static List<String> auFeatureMaskKeys = new ArrayList<>();
    private static List<String> selectedFeatures = null;	
    private DecimalFormat _decFormat = new DecimalFormat("#0.00");

    public static class AUFeature {

        public int index;   // integer representing the column index where it's read from
        public int num;     // number representing the AU (ex: 1, for key "AU01_r")

        public AUFeature(int index, int num) {
            this.index = index;
            this.num = num;
        }

        public AUFeature(int index, String num) {
            this.index = index;
            this.num = Integer.parseInt(num);
        }
		public String getKey(){
            return String.format("AU%02d", num);
        }										
    }

    public static String[] availableFeatures = new String[0];

    public static Map<String, Integer> preAUFeatureKeysMap = new TreeMap<>();
    public static Map<String, AUFeature> auFeaturesMap = new TreeMap<>();
    public static Map<String, AUFeature> auFeatureMasksMap = new TreeMap<>();

    public final static String BLINK_AU = "AU45_r";

    public static int getAUFeaturesCount() {
        return auFeaturesMap.size()>MAX_AUS?MAX_AUS:auFeaturesMap.size();
    }

    public static int getAUFeatureMasksCount() {
        return auFeatureMasksMap.size()>MAX_AUS?MAX_AUS:auFeatureMasksMap.size();
    }

    public static String separator = ", *";

    public int frameNumber = 0;
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
    public boolean isNull = false;
    
    public OpenFaceFrame(){
        isNull = true;
    }

    public static boolean readHeader(String line) {

        String[] tokens = new String[0];

        if (line != null) {
            tokens = line.split(separator);
        }

        if (Arrays.equals(availableFeatures, tokens)) {
            return false;
        }

        preAUFeatureKeysMap.clear();
        auFeaturesMap.clear();
        auFeatureMasksMap.clear();

        Pattern p = Pattern.compile("AU([0-9]+)_[rc]");
        for (int i = 0; i < tokens.length; ++i) {
            if (expectedPreAUFeatures.contains(tokens[i])) {
                preAUFeatureKeysMap.put(tokens[i], i);
            } else {
                Matcher m = p.matcher(tokens[i]);
                if (m.matches()) {
					String auKey = tokens[i];
                    String auNum = m.group(1);
                    if (auKey.endsWith("r")) {
                        auFeatureKeys.add(auKey);
                        auFeaturesMap.put(auKey, new AUFeature(i, auNum));
                    } else if (auKey.endsWith("c")) {
                        auFeatureMaskKeys.add(auKey);
                        auFeatureMasksMap.put(auKey, new AUFeature(i, auNum));
                    }
                }
            }
        }

        availableFeatures = tokens;

        return true;
    }
	public static String getAUFeatureKey(int index){
        return auFeatureKeys.get(index);
    }
    
    public static String getAUFeatureMaskKey(int index){
        return auFeatureMaskKeys.get(index);
    }

    public static int getAUFeatureNumber(int index) {
        String key = getAUFeatureKey(index);
        return auFeaturesMap.get(key).num;
    }

    public static int getAUFeatureMaskNumber(int index) {
        String key = auFeatureMaskKeys.get(index);
        return auFeatureMasksMap.get(key).num;
    }

    private boolean isFeatureSelected(String feature) {
        return selectedFeatures == null || selectedFeatures.contains(feature);
    }

    private String readDataCol(String key, String[] cols, Map<String, Integer> set) {
        if (isFeatureSelected(key)) {
            if (set.containsKey(key)) {
                return cols[set.get(key)];
            } else {
                LOGGER.warning(String.format("Map doesn't contains key: %s", key));
            }
        }
        return "0";
    }

    private double readAUDataCol(String key, String[] cols, Map<String, AUFeature> set) {
        double d = 0.;
        if (isFeatureSelected(key)) {
            d = Double.parseDouble(cols[set.get(key).index]);
        }
        return d;
    }

    public void readDataLine(String data) {

        String[] outputs = data.split(separator);

        frameNumber = Integer.parseInt(readDataCol("frame", outputs, preAUFeatureKeysMap));
        faceId      = Integer.parseInt(readDataCol("face_id", outputs, preAUFeatureKeysMap));
        timestamp   = Double.parseDouble(readDataCol("timestamp", outputs, preAUFeatureKeysMap));
        confidence  = Double.parseDouble(readDataCol("confidence", outputs, preAUFeatureKeysMap));
        success     = Integer.parseInt(readDataCol("success", outputs, preAUFeatureKeysMap)) == 1;

        gaze0.set(Double.parseDouble(readDataCol("gaze_0_x", outputs, preAUFeatureKeysMap)),
                Double.parseDouble(readDataCol("gaze_0_y", outputs, preAUFeatureKeysMap)),
                Double.parseDouble(readDataCol("gaze_0_z", outputs, preAUFeatureKeysMap)));
        gaze1.set(Double.parseDouble(readDataCol("gaze_1_x", outputs, preAUFeatureKeysMap)),
                Double.parseDouble(readDataCol("gaze_1_y", outputs, preAUFeatureKeysMap)),
                Double.parseDouble(readDataCol("gaze_1_z", outputs, preAUFeatureKeysMap)));

        gazeAngleX  = Double.parseDouble(readDataCol("gaze_angle_x", outputs, preAUFeatureKeysMap));
        gazeAngleY  = Double.parseDouble(readDataCol("gaze_angle_y", outputs, preAUFeatureKeysMap));

        headPoseT.set(Double.parseDouble(readDataCol("pose_Tx", outputs, preAUFeatureKeysMap)),
                Double.parseDouble(readDataCol("pose_Ty", outputs, preAUFeatureKeysMap)),
                Double.parseDouble(readDataCol("pose_Tz", outputs, preAUFeatureKeysMap)));
        headPoseR.set(Double.parseDouble(readDataCol("pose_Rx", outputs, preAUFeatureKeysMap)),
                Double.parseDouble(readDataCol("pose_Ry", outputs, preAUFeatureKeysMap)),
                Double.parseDouble(readDataCol("pose_Rz", outputs, preAUFeatureKeysMap)));

        int i = 0;
        for (String key : auFeaturesMap.keySet()) {
            if(i>= MAX_AUS){
                //LOGGER.warning(String.format("AU[%d] %s is ignored, expected %d AUs maximum",i, key,MAX_AUS));                
                break;
            }
            aus[i] = readAUDataCol(key, outputs, auFeaturesMap) / 5.0; // AU**_r are between 0-5.0
            
            if (BLINK_AU.equals(key)) {
                blink = aus[i];
            }
            ++i;
        }
        i = 0;
        for (String key : auFeatureMasksMap.keySet()) {
            if(i < MAX_AUS)
                auMasks[i++] = readAUDataCol(key, outputs, auFeatureMasksMap);
        }
        isNull = false;
    }
    
    @Override
    public String toString(){
        if(isNull)
            return "null off";
        int i =0;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%d\t%f\t%f:%b",frameNumber,timestamp,confidence,success));
        for (String key : auFeaturesMap.keySet()) {
            if(i < MAX_AUS){
                sb.append(", ");
                sb.append(key);
                sb.append(":");
                sb.append(auMasks[i]);
                sb.append(":");
                sb.append(_decFormat.format(aus[i]));
            }
            i++;
        }
        return sb.toString();
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
        frameNumber    = f.frameNumber;
        faceId         = f.faceId;
        timestamp      = f.timestamp;
        confidence     = f.confidence;
        success        = f.success;
        gaze0          = f.gaze0.clone();
        gaze1          = f.gaze1.clone();
        gazeAngleX     = f.gazeAngleX;
        gazeAngleY     = f.gazeAngleY;
        headPoseT      = f.headPoseT.clone();
        headPoseR      = f.headPoseR.clone();
        isNull      = f.isNull;
        System.arraycopy(f.aus,         0, aus,         0, f.aus.length);
        System.arraycopy(f.auMasks,     0, auMasks,     0, f.auMasks.length);
        System.arraycopy(f.intensity,   0, intensity,   0, f.intensity.length);
    }

    /**
     * @return the selected output features
     */
    public static List<String> getSelectedFeatures() {
        return selectedFeatures;
    }

    /**
     * @param features the selected output features to set
     */
    public static void setSelectedFeatures(List<String> features) {
        selectedFeatures = features;
    }

    /**
     * @param features the selected output features to set
     */
    public static void setSelectedFeatures(String[] features) {
        setSelectedFeatures(Arrays.asList(features));
    }
}
