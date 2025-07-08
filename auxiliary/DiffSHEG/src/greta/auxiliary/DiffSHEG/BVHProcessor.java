package greta.auxiliary.DiffSHEG;

import greta.auxiliary.BVHMocap.BapAnimationConverter;
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

    public BAPFrame convertLineToBap(String frameLine) {
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

            eulerAngles.putIfAbsent(info.jointName, new Vec3d(0, 0, 0));
            Vec3d angles = eulerAngles.get(info.jointName);

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
        
            Quaternion rotation = JointQuaternion(angles.x(), angles.y(), angles.z(), this.eulerAngleOrder);

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
            case 120: q = rotationz.multiply(rotationx).multiply(rotationy); break; // ZXY
            case 102: q = rotationy.multiply(rotationx).multiply(rotationz); break; // YXZ
            case 12:  q = rotationx.multiply(rotationy).multiply(rotationz); break; // XYZ
            case 21:  q = rotationx.multiply(rotationz).multiply(rotationy); break; // XZY
            case 210: q = rotationz.multiply(rotationy).multiply(rotationx); break; // ZYX
            case 201: q = rotationy.multiply(rotationz).multiply(rotationx); break; // YZX
            default:  q = rotationx.multiply(rotationy).multiply(rotationz); break; // Default to XYZ
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
    public BAPFrame setBAPframeRotation(BAPFrame frame, String jointName, Quaternion q) {
        JointType joint = JointType.get(jointName);
        if (joint != null) {
            Vec3d angles = q.getEulerAngleXYZ(); // Using a consistent final representation
            if(joint.rotationX != null) frame.setRadianValue(joint.rotationX, angles.x());
            if(joint.rotationY != null) frame.setRadianValue(joint.rotationY, angles.y());
            if(joint.rotationZ != null) frame.setRadianValue(joint.rotationZ, angles.z());
        }
        return frame;
    }

    public BAPFrame setBAPframeTranslation(BAPFrame frame, String jointName, Vec3d t) {
        JointType joint = JointType.get(jointName);
        if (joint != null) {
            if(joint.translationX != null) frame.setRadianValue(joint.translationX, t.x());
            if(joint.translationY != null) frame.setRadianValue(joint.translationY, t.y());
            if(joint.translationZ != null) frame.setRadianValue(joint.translationZ, t.z());
        }
        return frame;
    }
}