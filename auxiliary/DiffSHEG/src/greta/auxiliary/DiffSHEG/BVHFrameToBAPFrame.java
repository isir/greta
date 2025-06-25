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

package greta.auxiliary.DiffSHEG;


import greta.core.animation.common.Joint;
import greta.core.animation.common.Skeleton;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.animation.mpeg4.bap.JointType;
import greta.core.util.Constants;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import greta.core.util.time.Timer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;


public class BVHFrameToBAPFrame implements BAPFrameEmitter {

    public String fileName;
    public Dictionary dictionary;
    private float[] coordOffset;//Today
    private boolean USE_COORDINATES_OFFSET = false;
    private boolean USE_DICTIONNARY = true;

    private boolean isPosfix = true;
    private boolean isAbsolutelyfix_pos = false;
    private boolean isOrientationfix = false;

    private Skeleton skeleton;

    public BVHFrameToBAPFrame() {
        this("");
        USE_COORDINATES_OFFSET = false;
        USE_DICTIONNARY = true;
    }

    public BVHFrameToBAPFrame(String fname) {
        fileName = fname;
        dictionary = new Dictionary();
        dictionary.Initialize();

        USE_COORDINATES_OFFSET = false;
        USE_DICTIONNARY = true;
    }

    public void setCoordinatesOffset(float[] offsets) {
        coordOffset = offsets;
    }

    public void setUseCoordinatesOffset(boolean useCO) {
        USE_COORDINATES_OFFSET = useCO;
    }

    public boolean getUseCoordinatesOffset()
    {
        return USE_COORDINATES_OFFSET;
    }

    public void setUseDictionnary(boolean useDict) {
        USE_DICTIONNARY = useDict;
    }

    public boolean getUseDictionnary()
    {
        return USE_DICTIONNARY;
    }

    public void setSkeleton(Skeleton skeleton) {
        this.skeleton = skeleton;
    }

    public String SpaceRegularization(String ligne) {
        while (ligne.contains("  ") || ligne.contains("\t")) {
            ligne = ligne.replaceAll("  ", " ");
            ligne = ligne.replaceAll("\t", " ");
        }
        return ligne;
    }

    private int FirstWordIndex(String line, String word) {   /* find the index of the first word in a string  */
        int word_index = 0;
        //line = regexp_space(line);

        while (!(line.split(" ")[word_index]).equals(word)) {
            word_index = word_index + 1;
        }

        return word_index;
    }

    public Skeleton BVHSkeleton(BufferedReader br) throws FileNotFoundException, IOException {
        skeleton = new Skeleton("SkeletonFromBVH");
        String line;
        String name;
        
        int id_joint;
        int word_index;
        ArrayList<Integer> sublist = new ArrayList<>();
        int cpt_EndSublist = 0;
    
        Joint joint1;
    
        while ((line = br.readLine()) != null && !line.contains("ROOT")) {}
        line = SpaceRegularization(line);
        word_index = FirstWordIndex(line, "ROOT");
        name = line.split(" ")[word_index + 1];
        id_joint = skeleton.createJoint(name, -1);
        joint1 = skeleton.getJoint(id_joint);
        sublist.add(id_joint);
    
        while ((line = br.readLine()) != null && !line.contains("OFFSET")) {}
        
        line = SpaceRegularization(line);
        word_index = FirstWordIndex(line, "OFFSET");
    
        Vec3d offset = new Vec3d();
        offset.setX(Float.parseFloat(line.split(" ")[word_index + 1]));
        offset.setY(Float.parseFloat(line.split(" ")[word_index + 2]));
        offset.setZ(Float.parseFloat(line.split(" ")[word_index + 3]));
    
        joint1.setOrigine(offset);
        joint1.setLocalPosition(offset);
    
        line = br.readLine(); // CHANNELS
    
        while ((line = br.readLine()) != null && !line.contains("MOTION")) {
            if (line.contains("JOINT")) {
                line = SpaceRegularization(line);
                word_index = FirstWordIndex(line, "JOINT");
                name = line.split(" ")[word_index + 1];
    
                while ((line = br.readLine()) != null && !line.contains("OFFSET")) {}
                line = SpaceRegularization(line);
                word_index = FirstWordIndex(line, "OFFSET");
    
                Vec3d offsetJoint = new Vec3d();
                offsetJoint.setX(Float.parseFloat(line.split(" ")[word_index + 1]));
                offsetJoint.setY(Float.parseFloat(line.split(" ")[word_index + 2]));
                offsetJoint.setZ(Float.parseFloat(line.split(" ")[word_index + 3]));
    
                int parent = id_joint;
                id_joint = skeleton.createJoint(name, parent);
                Joint joint = skeleton.getJoint(id_joint);
                joint.setLocalPosition(offsetJoint);
    
                joint.setParentById(parent);
                Joint parentJoint = skeleton.getJoint(parent);
                parentJoint.updateLocally();
                joint.update();
    
                sublist.add(id_joint);
    
            } else if (line.contains("End Site")) {
                while ((line = br.readLine()) != null && !line.contains("}")) {}
            } else if (line.contains("}")) {
                while (line.contains("}")) {
                    cpt_EndSublist++;
                    line = br.readLine();
                    if (line == null) break;
                }
    
                int newIndex = sublist.size() - cpt_EndSublist - 1;
                if (newIndex >= 0 && newIndex < sublist.size()) {
                    id_joint = sublist.get(newIndex);
                    for (int i = sublist.size() - 1; i > newIndex; i--) {
                        sublist.remove(i);
                    }
                }
    
                cpt_EndSublist = 0;
            }
        }
    
        return skeleton;
    }
    
    public BAPFrame convertBVHFrameToBAPFrame(String frameLine) {
        // Ceck if initialization is correct
        if (skeleton == null || dictionary == null) {
            throw new IllegalStateException("Skeleton or Dictionary not initialized.");
        }


        String[] tokens = frameLine.trim().split("\\s+");
        float[] motionValues = new float[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            motionValues[i] = Float.parseFloat(tokens[i]);
        }

        // Offset correction if added
        if (USE_COORDINATES_OFFSET && coordOffset != null && coordOffset.length >= 3) {
            motionValues[0] += coordOffset[0];
            motionValues[1] += coordOffset[1];
            motionValues[2] += coordOffset[2];
        }

        
        int cursor = 0;
        for (int i = 0; i < skeleton.getNumberOfJoints(); i++) {
            Joint joint = skeleton.getJoint(i);
            int numChannels = joint.getNbChannels();
            if (numChannels > 0) {
                float[] jointChannels = new float[numChannels];
                System.arraycopy(motionValues, cursor, jointChannels, 0, numChannels);
                joint.setCurrentChannelValues(jointChannels); // à implémenter dans Joint
                cursor += numChannels;
            }
        }

        for (int i = 0; i < skeleton.getNumberOfJoints(); i++) {
            skeleton.getJoint(i).update();
        }

        BAPFrame bapFrame = new BAPFrame();

        for (BAPType bapType : BAPType.values()) {
            if (bapType == BAPType.null_bap) continue;

            String label = bapType.name();
            String jointName = dictionary.getJointName(bapType); // à implémenter
            Joint joint = skeleton.getJoint(jointName);
            if (joint != null) {
                double angle = joint.getRotationForBAP(bapType); // à implémenter par toi
                bapFrame.setRadianValue(bapType, angle);
            }
        }
        return bapFrame;
    }
}
