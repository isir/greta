package greta.auxiliary.DiffSHEG;

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.animation.mpeg4.bap.JointType;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Leroux Paul
 */

// This class is used to process a BVH file or line into a BAP file or frame.
// The logic is equivalent as the one found in the BVHMocap project, but adapted for the direct reading of frame(s)
public class BVHProcessor {

    private final List<BVHChannelInfo> channelOrder = new ArrayList<>();
    private final Dictionary dictionary = new Dictionary();
    private int eulerAngleOrder;

    public BVHProcessor() {
        dictionary.Initialize();
    }

    public void parseBVHHeader(BufferedReader reader) throws IOException {
        channelOrder.clear();
        String line;
        String currentJoint = null;
        ArrayList<String> jointStack = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            line = line.trim().replaceAll("\\s+", " ");
            String[] parts = line.split(" ");

            if (parts[0].equalsIgnoreCase("ROOT") || parts[0].equalsIgnoreCase("JOINT")) {
                currentJoint = parts[1];
                jointStack.add(currentJoint);
            } else if (parts[0].equalsIgnoreCase("End")) {
                 jointStack.add("EndSite");
            } else if (line.startsWith("}")) {
                if(!jointStack.isEmpty()){
                    jointStack.remove(jointStack.size() - 1);
                }
                 if(!jointStack.isEmpty()){
                    currentJoint = jointStack.get(jointStack.size() - 1);
                }
            } else if (parts[0].equalsIgnoreCase("CHANNELS")) {
                int numChannels = Integer.parseInt(parts[1]);
                for (int i = 0; i < numChannels; i++) {
                    String jointForChannel = currentJoint;
                    if (jointStack.get(jointStack.size()-1).equals("EndSite")) {
                        jointForChannel = jointStack.get(jointStack.size()-2);
                    }
                    channelOrder.add(new BVHChannelInfo(jointForChannel, parts[i + 2]));
                }
            } else if (parts[0].equalsIgnoreCase("MOTION")) {
                this.eulerAngleOrder = determineEulerOrder();
                System.out.println("BVH structure parsed. Found " + channelOrder.size() + " channels. Euler order: " + this.eulerAngleOrder);
                return;
            }
        }
    }

    public BAPFrame convertLineToBAP(String frameLine) {
        String[] values = frameLine.trim().split("\\s+");
        if (values.length != channelOrder.size()) {
            System.err.println("Error: Motion data size (" + values.length + ") does not match expected channel count (" + channelOrder.size() + ")");
            return null;
        }

        Map<String, Vec3d> eulerAngles = new HashMap<>();
        Vec3d rootTranslation = new Vec3d();

        for (int i = 0; i < channelOrder.size(); i++){
            BVHChannelInfo info = channelOrder.get(i);
            float value = Float.parseFloat(values[i]);

            eulerAngles.putIfAbsent(info.JointName, new Vec3d(0, 0, 0));
            Vec3d angles = eulerAngles.get(info.JointName);

            String channelTypeLower = info.channelType.toLowerCase();
            switch (channelTypeLower) {
                case "xposition": rootTranslation.setX(value); break;
                case "yposition": rootTranslation.setY(value); break;
                case "zposition": rootTranslation.setZ(value); break;
                case "xrotation": angles.setX(value); break;
                case "yrotation": angles.setY(value); break;
                case "zrotation": angles.setZ(value); break;
            }
        }
        BAPFrame bapFrame = new BAPFrame();
        BapAnimationConverter bapConverter = new BapAnimationConverter();

        for (Map.Entry<String, Vec3d> entry : eulerAngles.entrySet()) {
            String bvhJointName = entry.getKey();
            Vec3d angles = entry.getValue();
        
            Quaternion rotation = JointQuaternion((float) angles.x(), (float) angles.y(), (float) angles.z(), this.eulerAngleOrder);

            String bapJointName = dictionary.GetJointName(bvhJointName);
            if (bapJointName != null && !bapJointName.isEmpty()) {
                bapConverter.setBAPframeRotation(bapFrame, bapJointName, rotation);

                if (bapJointName.equals("HumanoidRoot")) {
                    bapConverter.setBAPframeTranslation(bapFrame, bapJointName, rootTranslation);
                }
            }
        }
        return bapFrame;
    }

    private Quaternion JointQuaternion(float vx, float vy, float vz, int eulerOrder) {
        Quaternion q = new Quaternion();
        Quaternion rotationx = new Quaternion(new Vec3d(1, 0, 0), (float) Math.toRadians(vx));
        Quaternion rotationy = new Quaternion(new Vec3d(0, 1, 0), (float) Math.toRadians(vy));
        Quaternion rotationz = new Quaternion(new Vec3d(0, 0, 1), (float) Math.toRadians(vz));

        switch (eulerOrder) {
            case 120://zxy
            {
                q = Quaternion.multiplication(q, rotationz);//z x y
                q = Quaternion.multiplication(q, rotationx);
                q = Quaternion.multiplication(q, rotationy);
                //System.out.println("zxy");
                break;
            }
            case 102://yxz
            {
                q = Quaternion.multiplication(q, rotationy);//yxz
                q = Quaternion.multiplication(q, rotationx);
                q = Quaternion.multiplication(q, rotationz);
                // System.out.println("yxz");
                break;
            }
            case 012://xyz
            {
                q = Quaternion.multiplication(q, rotationx);
                q = Quaternion.multiplication(q, rotationy);
                q = Quaternion.multiplication(q, rotationz);
//             System.out.println("xyz");
                break;
            }
            case 021://xzy
            {
                q = Quaternion.multiplication(q, rotationx);
                q = Quaternion.multiplication(q, rotationz);
                q = Quaternion.multiplication(q, rotationy);
//             System.out.println("xzy");
                break;
            }
            case 210://zyx
            {
                q = Quaternion.multiplication(q, rotationz);
                q = Quaternion.multiplication(q, rotationy);
                q = Quaternion.multiplication(q, rotationx);
//             System.out.println("zyx");
                break;
            }
            case 201://yzx
            {
                q = Quaternion.multiplication(q, rotationy);
                q = Quaternion.multiplication(q, rotationz);
                q = Quaternion.multiplication(q, rotationx);
//             System.out.println("yzx");
                break;
                 
            }
            default:// Default to XYZ
            {
                q = Quaternion.multiplication(q, rotationx);
                q = Quaternion.multiplication(q, rotationy);
                q = Quaternion.multiplication(q, rotationz);
            }
        }
        return q;
    }
    private int determineEulerOrder() {
        int ix = -1, iy = -1, iz = -1;
        int diff = -1;

        for (int i =0; i < channelOrder.size(); i++) {
            String type = channelOrder.get(i).channelType.toLowerCase();
            if (type.endsWith("position")) continue;
            
            if (diff == -1) diff = i;
            
            if (type.equals("xrotation")) ix = i - diff;
            if (type.equals("yrotation")) iy = i - diff;
            if (type.equals("zrotation")) iz = i - diff;
            
            if (ix != -1 && iy != -1 && iz != -1) break;
        }

        if (ix == -1 || iy == -1 || iz == -1) return 12;
        
        return Integer.parseInt(Integer.toString(ix) + Integer.toString(iy) + Integer.toString(iz));
    }
}

class BapAnimationConverter {
    public BAPFrame setBAPframeRotation(BAPFrame bapframe, String name, Quaternion q) {


    JointType joint = JointType.get(name);

    BAPType tx = joint.rotationX;
    BAPType ty = joint.rotationY;
    BAPType tz = joint.rotationZ;
    // System.out.println(name);
    Vec3d angle = q.getEulerAngleXYZ();

    bapframe.setRadianValue(tx, angle.x());
    bapframe.setRadianValue(ty, angle.y());
    bapframe.setRadianValue(tz, angle.z());

    return bapframe;
    }

    public BAPFrame setBAPframeTranslation(BAPFrame frame, String jointName, Vec3d t) {
        JointType joint = JointType.get(jointName);
        if (joint != JointType.HumanoidRoot) {
        // Only the root is allowed to translate
        return frame;
        }

        BAPType tx = BAPType.HumanoidRoot_tr_lateral;
        BAPType ty = BAPType.HumanoidRoot_tr_vertical;
        BAPType tz = BAPType.HumanoidRoot_tr_frontal;

        double valuex = t.x() * 10;
        double valuey = t.y() * 10;
        double valuez = t.z() * 10;

        frame.applyValue(tx, ((Number)valuex).intValue());
        frame.applyValue(ty, ((Number)valuey).intValue());
        frame.applyValue(tz, ((Number)valuez).intValue());

    return frame;
}
}