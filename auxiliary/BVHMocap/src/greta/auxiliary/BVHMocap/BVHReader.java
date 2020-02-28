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
package greta.auxiliary.BVHMocap;

import greta.core.animation.common.Joint;
import greta.core.animation.common.Skeleton;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
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

/**
 *
 * @author Nesrine Fourati
 */
public class BVHReader implements BAPFrameEmitter {

    public String fileName;//path + name+ ".bvh"
    public Dictionary dictionary;
    private float[] coordOffset;//Today
    private boolean USE_COORDINATES_OFFSET = false;
    private boolean USE_DICTIONNARY = true;

    public BVHReader() {
        this("");
        USE_COORDINATES_OFFSET = false;
        USE_DICTIONNARY = true;
    }

    public BVHReader(String fname) {
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

    public static BufferedReader ReadFile(String fileName) {
        BufferedReader lecteurAvecBuffer = null;

        try {
            FileInputStream fileinput = new FileInputStream(fileName);
            InputStreamReader streamreader = new InputStreamReader(fileinput);
            lecteurAvecBuffer = new BufferedReader(streamreader);
        } catch (FileNotFoundException exc) {
            //exc.printStackTrace();
            System.out.println("Erreur d'ouverture du fichier " + fileName);
        }
        return lecteurAvecBuffer;
    }

    public long load(String bvhFileName) {
        fileName = bvhFileName;
        return process();
    }

    public long process() {
        int bapframe_startTime = (int) Timer.getTimeMillis() / 40;
        //(int) (Timer.getTime()*Constants.FRAME_PER_SECOND);Today


        ArrayList<BAPFrame> bap_animation = new ArrayList<BAPFrame>();
        try {

            // For Tardis project
//        PreRotationDefinition prerot_definition=new PreRotationDefinition("C:\\Users\\fourati\\Desktop\\Pre Rotations for Maya T-Pose.txt");
//        prerot_definition.SetJointsPreRotation();
            HashMap<String, Quaternion> AllPreRotation = null;//prerot_definition.GetJointsPreRotation();
            // *** end for tardis project


            BufferedReader br = ReadFile(fileName);
            Skeleton skeleton = BVHSkeleton(br);
            int nbframe = GetFrameNumber(br);
            float frameTime = GetFrameTime(br);
            int EulerAngleOrder = EulerOrder();
            System.out.println(fileName);
            bap_animation = BAPFramesCreator(AllPreRotation, br, skeleton, nbframe, EulerAngleOrder, frameTime, bapframe_startTime);


           //  bap_animation=OneBAPFrame(bapframe_startTime); // To test a posture or a small gesture (created in the function OneBAPFrame)
        } catch (Exception e) {
        }

        ID id = IDProvider.createID(fileName);//today
        for (int i = 0; i < _bapFramePerformer.size(); ++i) {
            BAPFramePerformer performer = _bapFramePerformer.get(i);
            performer.performBAPFrames(bap_animation, id);

        }

        return bap_animation.size();

    }

//    public ArrayList<BAPFrame> BVHToBAPFrames() throws FileNotFoundException, IOException {
//        BufferedReader br = ReadFile(fileName);
//        Skeleton skeleton = BVHSkeleton(br);
//        int nbframe = GetFrameNumber(br);
//
//        float frameTime = GetFrameTime(br);
//        int EulerAngleOrder = EulerOrder();
//        ArrayList<BAPFrame> bap_animation = BAPFramesCreator(br, skeleton, nbframe, EulerAngleOrder, frameTime, 0);
//        return bap_animation;
//    }
    public BVH MotionBasedBVHCreator() throws FileNotFoundException, IOException {
        BufferedReader br = ReadFile(fileName);
        Skeleton skeleton = BVHSkeleton(br);
        int nbframe = GetFrameNumber(br);
        float frameTime = GetFrameTime(br);
        int EulerAngleOrder = EulerOrder();
        ArrayList motion = bvhMotionList(br, skeleton, nbframe, EulerAngleOrder);
        return new BVH(motion, nbframe, skeleton, frameTime, EulerAngleOrder);
    }

    public BVH JFTableBasedBVHCreator() throws FileNotFoundException, IOException {   //  BVH Creater is based on Joint Frame Table
        BufferedReader br = ReadFile(fileName);
        Skeleton skeletonWithES = BVHSkeletonWithEndSite(br);
        int nbframe = GetFrameNumber(br);
        float frameTime = GetFrameTime(br);
        int EulerAngleOrder = EulerOrder();
        AllJointFramesTable jointframestable = bvhJFTable(br, skeletonWithES, nbframe, EulerAngleOrder);
        return new BVH(jointframestable, nbframe, skeletonWithES, frameTime, EulerAngleOrder);
    }

    public AllJointFramesTable bvhJFTable(BufferedReader br, Skeleton skeleton, int nbframe, int EulerAngleOrder) throws IOException {
        String line;
        AllJointFramesTable jointframestable = new AllJointFramesTable(skeleton);// initialize  the joint frames table with the names and the neutral position of all the joints
        //String joint_name;
        //Motion motion;
        float RootTranslationX, RootTranslationY, RootTranslationZ;
        float rx, ry, rz;
        Vec3d RootTranslation = null;
        Vec3d world_jointtranslation = null;
        int ix = EulerAngleOrder / 100;
        int iy = (EulerAngleOrder % 100) / 10;
        int iz = (EulerAngleOrder % 100) % 10;
        Joint Currentjoint = null;
        //System.out.println("ix="+ix+"  iy="+iy+"  iz="+iz);
        BapAnimationConverter bapconverter = new BapAnimationConverter();
        line = br.readLine();
        //System.out.println(line);
        String[] data;
        line = line.replace("\t", " ");
        line = line.replace("  ", " ");
        data = line.split(" ");

        int firstCharacter;
        int jjoint = 0;
        String typech = ChannelsStyle();
        int f = 0;
        while (line != null && !line.isEmpty())// each line corresponds to a frame
        {
            line = SpaceRegularization(line);
            data = line.split(" ");
            //check if the first element is a space
            if (data[0].isEmpty()) {
                firstCharacter = 1;
            } else {
                firstCharacter = 0;
            }
            Quaternion ChangeAxesRotation = new Quaternion();
            Vec3d RootRelated_position = new Vec3d();
            // save the information related to each joint in this frame in jointframetable
            while (firstCharacter < data.length) // each step corresponds to a joint
            {

                if (firstCharacter == 0 || firstCharacter == 1) {//Add the information of the root

                    if (firstCharacter == 1) {
                        rx = (Float.parseFloat(data[4 + ix]));
                        ry = (Float.parseFloat(data[4 + iy]));
                        rz = (Float.parseFloat(data[4 + iz]));

                        RootTranslationX = (Float.parseFloat(data[1]));
                        RootTranslationY = (Float.parseFloat(data[2]));
                        RootTranslationZ = (Float.parseFloat(data[3]));
                    } else {
                        rx = (Float.parseFloat(data[3 + ix]));
                        ry = (Float.parseFloat(data[3 + iy]));
                        rz = (Float.parseFloat(data[3 + iz]));

                        RootTranslationX = (Float.parseFloat(data[0]));
                        RootTranslationY = (Float.parseFloat(data[1]));
                        RootTranslationZ = (Float.parseFloat(data[2]));
                    }

                    Quaternion joint_rotation = JointQuaternion(rx, ry, rz, EulerAngleOrder); // JointQuaternion90(skeleton.getJoint(jjoint).getName(),rx, ry, rz, EulerAngleOrder);  for Tardis
                    RootTranslation = new Vec3d(RootTranslationX, RootTranslationY, RootTranslationZ);
                    Currentjoint = skeleton.getJoint(jjoint);
                    Currentjoint.rotate(joint_rotation);
                    Currentjoint.update();

                    //ChangeAxesRotation.setAxisAngle(new Vec3d(0, 1, 0), -joint_rotation.angle() * joint_rotation.axis().y());
                    JointFrame jointframe = new JointFrame(f, joint_rotation, RootTranslation, RootRelated_position);  //11/09/2013 joint_rotation   Currentjoint.getWorldRotation()
                    //  new JointFrame(f, joint_rotation, Vec3d.multiplication(RootTranslation, 100), RootRelated_position);   for tardis
                    jointframe.EulerAngles.set(rx, ry, rz);


                    // Add BAP
                    BAPFrame bapframe = new BAPFrame(0);
                    String bvh_joint = Currentjoint.getName();
                    String dict_name;
                    if (USE_DICTIONNARY) {
                        dict_name = (String) (dictionary.GetDict()).get(bvh_joint);//from bvh joint name to dictionnary name
                    } else {
                        dict_name = bvh_joint; //fix mocap cathy
                    }
                    if (dict_name == null) {
                        dict_name = " "; // bvh_joint 18/09
                    }

                    bapframe = bapconverter.setBAPframeRotation(bapframe, dict_name, joint_rotation);
                    jointframe.SetBAPJointName(dict_name);
                    jointframe.CurrentBapX = bapframe.getAnimationParameter((JointType.get(dict_name).rotationX).ordinal());
                    jointframe.CurrentBapY = bapframe.getAnimationParameter((JointType.get(dict_name).rotationY).ordinal());
                    jointframe.CurrentBapZ = bapframe.getAnimationParameter((JointType.get(dict_name).rotationZ).ordinal());
                    // End Add BAP

                    jointframestable.AddJointFrameAt(jjoint, jointframe);
//                   if (f==0)
//                   {System.out.println(Currentjoint.getName());
//                     for(int k=0;k<Currentjoint.getChildren().size();k++)
//                     {
//                         System.out.println("\t"+Currentjoint.getChild(k).getName());
//                     }
//                   }
                    firstCharacter = firstCharacter + 6;
                } // End of Getting Root information
                else //Retrieve the rotation of the other joints; Head, neck, arms...
                {
                    if (typech.equals("3channels")) {
                        rx = (Float.parseFloat(data[firstCharacter + ix]));
                        ry = (Float.parseFloat(data[firstCharacter + iy]));
                        rz = (Float.parseFloat(data[firstCharacter + iz]));
                        firstCharacter = firstCharacter + 3;
                    } else {
                        rx = (Float.parseFloat(data[firstCharacter + 3 + ix]));
                        ry = (Float.parseFloat(data[firstCharacter + 3 + iy]));
                        rz = (Float.parseFloat(data[firstCharacter + 3 + iz]));
                        firstCharacter = firstCharacter + 6;
                    }

                    Quaternion joint_rotation = JointQuaternion(rx, ry, rz, EulerAngleOrder); // JointQuaternion90(skeleton.getJoint(jjoint).getName(),rx, ry, rz, EulerAngleOrder);   for tardis

                    if (skeleton.getJoint(jjoint).getName().contains("EndSite"))
                    {
                        Currentjoint = skeleton.getJoint(jjoint);
                        Currentjoint.rotate(new Quaternion());
                        Currentjoint.update();

                        world_jointtranslation = Vec3d.addition(Currentjoint.getWorldPosition(), RootTranslation);
                        //RootRelated_position = Quaternion.multiplication(ChangeAxesRotation, Currentjoint.getWorldPosition());
                        JointFrame jointframe = new JointFrame(f, new Quaternion(), world_jointtranslation, new Vec3d());// RootRelated_position
                        //jointframe.EulerAngles.set(rx, ry, rz);

//                        // Add BAP
//                        BAPFrame bapframe = new BAPFrame(0);
//                        String bvh_joint = Currentjoint.getName();
//                        String dict_name;
//                        if (USE_DICTIONNARY) {
//                            dict_name = (String) (dictionary.GetDict()).get(bvh_joint);//from bvh joint name to dictionnary name
//                        } else {
//                            dict_name = bvh_joint; //fix mocap cathy
//                        }
//                        if (dict_name == null) {
//                            dict_name = " "; // bvh_joint 18/09
//                        }
//
//                        bapframe = bapconverter.setBAPframeRotation(bapframe, dict_name, jointframe.GetRotation());
//                        jointframe.SetBAPJointName(dict_name);
//                        jointframe.CurrentBapX = bapframe.getAnimationParameter((JointType.get(dict_name).rotationX).ordinal());
//                        jointframe.CurrentBapY = bapframe.getAnimationParameter((JointType.get(dict_name).rotationY).ordinal());
//                        jointframe.CurrentBapZ = bapframe.getAnimationParameter((JointType.get(dict_name).rotationZ).ordinal());
//                        // End Add BAP


                        jointframestable.AddJointFrameAt(jjoint, jointframe);

                        jjoint = jjoint + 1; // put the next value of orientation in the next joint
                        Currentjoint = skeleton.getJoint(jjoint);
                        Currentjoint.rotate(joint_rotation);
                        Currentjoint.update();

                        world_jointtranslation = Vec3d.addition(Currentjoint.getWorldPosition(), RootTranslation);
                        //RootRelated_position = Quaternion.multiplication(ChangeAxesRotation, Currentjoint.getWorldPosition());
                        jointframe = new JointFrame(f, joint_rotation, world_jointtranslation, new Vec3d()); //11/09/2013 joint_rotation
                        jointframe.EulerAngles.set(rx, ry, rz);

                        // Add BAP
                       BAPFrame bapframe = new BAPFrame(0);
                       String bvh_joint = Currentjoint.getName();
                       String dict_name;
                        if (USE_DICTIONNARY) {
                            dict_name = (String) (dictionary.GetDict()).get(bvh_joint);//from bvh joint name to dictionnary name
                        } else {
                             dict_name = bvh_joint; //fix mocap cathy
                        }
                        if (dict_name == null) {
                            dict_name = " "; // bvh_joint 18/09
                        }

                        bapframe = bapconverter.setBAPframeRotation(bapframe, dict_name, joint_rotation);
                        jointframe.SetBAPJointName(dict_name);
                        jointframe.CurrentBapX = bapframe.getAnimationParameter((JointType.get(dict_name).rotationX).ordinal());
                        jointframe.CurrentBapY = bapframe.getAnimationParameter((JointType.get(dict_name).rotationY).ordinal());
                        jointframe.CurrentBapZ = bapframe.getAnimationParameter((JointType.get(dict_name).rotationZ).ordinal());
                        // End Add BAP


                        jointframestable.AddJointFrameAt(jjoint, jointframe);
                    }
                    else
                    {
                        Currentjoint = skeleton.getJoint(jjoint);
                        Currentjoint.rotate(joint_rotation);
                        Currentjoint.update();

                        world_jointtranslation = Vec3d.addition(Currentjoint.getWorldPosition(), RootTranslation);
                        //RootRelated_position = Quaternion.multiplication(ChangeAxesRotation, Currentjoint.getWorldPosition());
                        JointFrame jointframe = new JointFrame(f, joint_rotation, world_jointtranslation, new Vec3d());//// 11/09/2013 joint_rotation
                        jointframe.EulerAngles.set(rx, ry, rz);

                        // Add BAP
                        BAPFrame bapframe = new BAPFrame(0);
                        String bvh_joint = Currentjoint.getName();
                        String dict_name;
                        if (USE_DICTIONNARY) {
                            dict_name = (String) (dictionary.GetDict()).get(bvh_joint);//from bvh joint name to dictionnary name
                        } else {
                            dict_name = bvh_joint; //fix mocap cathy
                        }
                        if (dict_name == null) {
                            dict_name = " "; // bvh_joint 18/09
                        }

                        bapframe = bapconverter.setBAPframeRotation(bapframe, dict_name, joint_rotation);
                         jointframe.SetBAPJointName(dict_name);
                        jointframe.CurrentBapX = bapframe.getAnimationParameter((JointType.get(dict_name).rotationX).ordinal());
                        jointframe.CurrentBapY = bapframe.getAnimationParameter((JointType.get(dict_name).rotationY).ordinal());
                        jointframe.CurrentBapZ = bapframe.getAnimationParameter((JointType.get(dict_name).rotationZ).ordinal());
                        // End Add BAP

                        jointframestable.AddJointFrameAt(jjoint, jointframe);
                    }
//                    if (f == 0) {
//                        System.out.println(jointframestable.GetJointFramesTable().get(jjoint).JointName);
//                        System.out.println(" localposition   " + RootRelated_position);
//                        System.out.println(" globalposition   " + world_jointtranslation);
//
//                    }

                }
                jjoint++;
            }
            //LeftToeEndSite joint:
            Currentjoint = skeleton.getJoint(jjoint);
            Currentjoint.rotate(new Quaternion());
            Currentjoint.update();

            world_jointtranslation = Vec3d.addition(Currentjoint.getWorldPosition(), RootTranslation);
            //RootRelated_position = Quaternion.multiplication(ChangeAxesRotation, Currentjoint.getWorldPosition());
            JointFrame jointframe = new JointFrame(f, new Quaternion(), world_jointtranslation, new Vec3d()); // RootRelated_position
            // do not add BAP; useless because there is no rotation for END Site joint
            jointframestable.AddJointFrameAt(jjoint, jointframe);
            //prepare for the next frame

            Joint joint = skeleton.getJoint(0);
            joint.reset();
            joint.update();
            jjoint = 0;
            line = br.readLine();
            f = f + 1;
        }


        return jointframestable;
    }

    public float GetFrameTime(BufferedReader br) throws FileNotFoundException, IOException {
        float frame_time;
        String ligne = br.readLine();

        ligne = SpaceRegularization(ligne);
        frame_time = Float.parseFloat(ligne.split(" ")[2]);
        return frame_time;

    }

    public int GetFrameNumber(BufferedReader br) throws FileNotFoundException, IOException {
        int frame_nb;
        String ligne = br.readLine();
        ligne = SpaceRegularization(ligne);
        frame_nb = Integer.parseInt(ligne.split(" ")[1]);
        return frame_nb;

    }

    /**
     * Replace all the tabulations and the multiple spaces by a single space
     *
     * @param string that may contain words seperated by space (s) and/or
     * tabulation
     * @return string that contains only space between words
     */
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

    public Skeleton BVHSkeletonWithEndSite(BufferedReader br) throws FileNotFoundException, IOException {
        Skeleton skeleton = new Skeleton(fileName.split("[.]")[0]);
        String line;
        String name;
        int id_joint;
        int word_index;
        Vector sublist = new Vector();
        int cpt_EndSublist = 0;

        Joint joint1;// Root
        line = br.readLine();//HIERARCHY
        line = br.readLine();//ROOT Hips

        line = SpaceRegularization(line);
        word_index = FirstWordIndex(line, "ROOT");
        name = line.split(" ")[word_index + 1];
        id_joint = skeleton.createJoint(name, -1);
        joint1 = skeleton.getJoint(id_joint);
        sublist.addElement(0);

        while (!line.contains("OFFSET")) {
            line = br.readLine();
        }

        line = SpaceRegularization(line);
        word_index = FirstWordIndex(line, "OFFSET");

        Vec3d vect1 = new Vec3d();
        vect1.setX(Float.parseFloat(line.split(" ")[word_index + 1]));
        vect1.setY(Float.parseFloat(line.split(" ")[word_index + 2]));
        vect1.setZ(Float.parseFloat(line.split(" ")[word_index + 3]));

        joint1.setOrigine(vect1);
        joint1.setLocalPosition(vect1);

        //System.out.println(Float.parseFloat(line.split(" ")[word_index + 1])+" "+Float.parseFloat(line.split(" ")[word_index + 2])+" "+Float.parseFloat(line.split(" ")[word_index + 3]));
        line = br.readLine();//CHANNELS

        while (!line.contains("MOTION")) {
            if (line.contains("JOINT")) {
                line = SpaceRegularization(line);
                word_index = FirstWordIndex(line, "JOINT");
                name = line.split(" ")[word_index + 1];

                while (!line.contains("OFFSET")) {
                    line = br.readLine();
                }

                line = SpaceRegularization(line);
                word_index = FirstWordIndex(line, "OFFSET");

                Vec3d vect = new Vec3d();
                vect.setX(Float.parseFloat(line.split(" ")[word_index + 1]));
                vect.setY(Float.parseFloat(line.split(" ")[word_index + 2]));
                vect.setZ(Float.parseFloat(line.split(" ")[word_index + 3]));

                int parent = id_joint;
                id_joint = skeleton.createJoint(name, parent);//previous id_joint
                Joint joint = skeleton.getJoint(id_joint);
                joint.setLocalPosition(vect);

                joint.setParentById(parent);
                Joint p = skeleton.getJoint(parent);

                p.updateLocally();
                joint.update();

                sublist.addElement(id_joint);

            } else {
                if (line.contains("End Site")) {
                    name = "EndSite".concat(skeleton.getJoint(id_joint).getName());
                    while (!line.contains("OFFSET")) {
                        line = br.readLine();
                    }

                    line = SpaceRegularization(line);
                    word_index = FirstWordIndex(line, "OFFSET");

                    Vec3d vect = new Vec3d();
                    vect.setX(Float.parseFloat(line.split(" ")[word_index + 1]));
                    vect.setY(Float.parseFloat(line.split(" ")[word_index + 2]));
                    vect.setZ(Float.parseFloat(line.split(" ")[word_index + 3]));

                    int parent = id_joint;
                    id_joint = skeleton.createJoint(name, id_joint);
                    Joint joint = skeleton.getJoint(id_joint);
                    joint.setLocalPosition(vect);

                    joint.setParentById(parent);
                    Joint p = skeleton.getJoint(parent);

                    p.updateLocally();
                    joint.update();
                } else if (line.contains("}")) // Update parent
                {
                    while (line.contains("}")) {
                        cpt_EndSublist++;
                        line = br.readLine();
                    }

                    if (sublist.size() - cpt_EndSublist >= 0) {
                        id_joint = Integer.parseInt((sublist.get(sublist.size() - cpt_EndSublist)).toString());
                        int size = sublist.size();
                        for (int i = sublist.size() - cpt_EndSublist + 1; i < size; i++) {   //System.out.println(sublist.lastElement());
                            sublist.remove(sublist.size() - 1);

                        }
                    }

                    cpt_EndSublist = 0;
                } else {
                    line = br.readLine();
                }
            }
        }
        return skeleton;
    }

    public Skeleton BVHSkeleton(BufferedReader br) throws FileNotFoundException, IOException {
        Skeleton skeleton = new Skeleton(fileName.split("[.]")[0]);
        String line;
        String name;
        int id_joint;
        int word_index;
        Vector sublist = new Vector();
        int cpt_EndSublist = 0;

        Joint joint1;
        line = br.readLine();//HIERARCHY
        line = br.readLine();//ROOT Hips

        line = SpaceRegularization(line);
        word_index = FirstWordIndex(line, "ROOT");
        name = line.split(" ")[word_index + 1];
        id_joint = skeleton.createJoint(name, -1);
        joint1 = skeleton.getJoint(id_joint);
        sublist.addElement(0);

        while (!line.contains("OFFSET")) {
            line = br.readLine();
        }

        line = SpaceRegularization(line);
        word_index = FirstWordIndex(line, "OFFSET");

        Vec3d vect1 = new Vec3d();
        vect1.setX(Float.parseFloat(line.split(" ")[word_index + 1]));
        vect1.setY(Float.parseFloat(line.split(" ")[word_index + 2]));
        vect1.setZ(Float.parseFloat(line.split(" ")[word_index + 3]));

        joint1.setOrigine(vect1);
        joint1.setLocalPosition(vect1);

        //System.out.println(Float.parseFloat(line.split(" ")[word_index + 1])+" "+Float.parseFloat(line.split(" ")[word_index + 2])+" "+Float.parseFloat(line.split(" ")[word_index + 3]));
        line = br.readLine();//CHANNELS

        while (!line.contains("MOTION")) {
            if (line.contains("JOINT")) {
                line = SpaceRegularization(line);
                word_index = FirstWordIndex(line, "JOINT");
                name = line.split(" ")[word_index + 1];

                while (!line.contains("OFFSET")) {
                    line = br.readLine();
                }

                line = SpaceRegularization(line);
                word_index = FirstWordIndex(line, "OFFSET");

                Vec3d vect = new Vec3d();
                vect.setX(Float.parseFloat(line.split(" ")[word_index + 1]));
                vect.setY(Float.parseFloat(line.split(" ")[word_index + 2]));
                vect.setZ(Float.parseFloat(line.split(" ")[word_index + 3]));

                int parent = id_joint;
                id_joint = skeleton.createJoint(name, id_joint);
                Joint joint = skeleton.getJoint(id_joint);
                joint.setLocalPosition(vect);

                joint.setParentById(parent);
                Joint p = skeleton.getJoint(parent);

                p.updateLocally();
                joint.update();

                sublist.addElement(id_joint);

            } else {
                if (line.contains("}")) {
                    while (line.contains("}")) {
                        cpt_EndSublist++;
                        line = br.readLine();
                    }

                    if (sublist.size() - cpt_EndSublist >= 0) {
                        id_joint = Integer.parseInt((sublist.get(sublist.size() - cpt_EndSublist)).toString());
                        int size = sublist.size();
                        for (int i = sublist.size() - cpt_EndSublist + 1; i < size; i++) {   //System.out.println(sublist.lastElement());
                            sublist.remove(sublist.size() - 1);

                        }
                    }

                    cpt_EndSublist = 0;
                } else {
                    line = br.readLine();
                }
            }
        }
        return skeleton;
    }

    public Quaternion JointQuaternion90(String jname, float vx, float vy, float vz, int EulerAngleOrder) {
        BVH bvh = new BVH();
        Quaternion rotationx = new Quaternion();
        Quaternion rotationy = new Quaternion();
        Quaternion rotationz = new Quaternion();

        Quaternion q = new Quaternion();


        Quaternion q2 = new Quaternion();

        rotationx.setAxisAngle(new Vec3d(1, 0, 0), bvh.degree_radian(vx));
        rotationy.setAxisAngle(new Vec3d(0, 1, 0), bvh.degree_radian(vy));
        rotationz.setAxisAngle(new Vec3d(0, 0, 1), bvh.degree_radian(vz));

        //*************** for particular initial position of skeleton : T-Pose
        Quaternion rotationz_90 = new Quaternion(new Vec3d(0, 0, 1), bvh.degree_radian(-90));
        Quaternion rotationz90 = new Quaternion(new Vec3d(0, 0, 1), bvh.degree_radian(90));

        if (jname.equals("r_shoulder")) {
            q2 = Quaternion.multiplication(q, rotationz_90);
        } else {
            if (jname.equals("l_shoulder")) {
                q2 = Quaternion.multiplication(q, rotationz90);
            }
        }


        // System.out.println("display euler order    " +EulerAngleOrder);
        switch (EulerAngleOrder) {
            case 120://zxy
            {
                q = Quaternion.multiplication(q, rotationz);//z x y
                q = Quaternion.multiplication(q, rotationx);
                q = Quaternion.multiplication(q, rotationy);
//             System.out.println("zxy");
                break;
            }
            case 102://yxz
            {
                q = Quaternion.multiplication(q, rotationy);//yxz
                q = Quaternion.multiplication(q, rotationx);
                q = Quaternion.multiplication(q, rotationz);
                System.out.println("yxz");
                break;
            }
            case 12://xyz
            {
                q = Quaternion.multiplication(q, rotationx);
                q = Quaternion.multiplication(q, rotationy);
                q = Quaternion.multiplication(q, rotationz);
                //System.out.println(rotationz.angle()+"   "+rotationz.axis());
//             System.out.println("xyz");
                break;
            }
            case 21://xzy
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
        }

        if (jname.equals("r_shoulder") || jname.equals("l_shoulder")) {
            q = Quaternion.multiplication(q, q2);
        }

        return q;
    }

    public Quaternion JointQuaternion(float vx, float vy, float vz, int EulerAngleOrder) {
        BVH bvh = new BVH();
        Quaternion rotationx = new Quaternion();
        Quaternion rotationy = new Quaternion();
        Quaternion rotationz = new Quaternion();

        Quaternion q = new Quaternion();

        rotationx.setAxisAngle(new Vec3d(1, 0, 0), bvh.degree_radian(vx));
        rotationy.setAxisAngle(new Vec3d(0, 1, 0), bvh.degree_radian(vy));
        rotationz.setAxisAngle(new Vec3d(0, 0, 1), bvh.degree_radian(vz));

        // System.out.println("display euler order    " +EulerAngleOrder);

        switch (EulerAngleOrder) {
            case 120://zxy
            {
                q = Quaternion.multiplication(q, rotationz);//z x y
                q = Quaternion.multiplication(q, rotationx);
                q = Quaternion.multiplication(q, rotationy);
//             System.out.println("zxy");
                break;
            }
            case 102://yxz
            {
                q = Quaternion.multiplication(q, rotationy);//yxz
                q = Quaternion.multiplication(q, rotationx);
                q = Quaternion.multiplication(q, rotationz);
//             System.out.println("yxz");
                break;
            }
            case 12://xyz
            {

                q = Quaternion.multiplication(q, rotationx);

                q = Quaternion.multiplication(q, rotationy);

                q = Quaternion.multiplication(q, rotationz);

                //System.out.println(rotationz.angle()+"   "+rotationz.axis());
//             System.out.println("xyz");
                break;
            }
            case 21://xzy
            {
                q = Quaternion.multiplication(q, rotationx);
                q = Quaternion.multiplication(q, rotationz);
                q = Quaternion.multiplication(q, rotationy);
                //System.out.println("xzy");
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
        }


        return q;
    }

    public ArrayList<BAPFrame> BAPFramesCreator(HashMap<String, Quaternion> AllPreRotation, BufferedReader br, Skeleton skeleton, int nbframe, int EulerAngleOrder, float frameTime, int bapframe_startTime) throws IOException {
        Float Initial_RootTranslationXBVH = 0.0f;//Today
        Float Initial_RootTranslationYBVH = 0.0f;
        Float Initial_RootTranslationZBVH = 0.0f;
        Float Initial_RootOrientationXBVH = 0.0f;//Today
        Float Initial_RootOrientationYBVH = 0.0f;
        Float Initial_RootOrientationZBVH = 0.0f;

        Float Initial_RootTranslationX = 0.0f;
        Float Initial_RootTranslationY = 0.0f;
        Float Initial_RootTranslationZ = 0.0f;
        Float Initial_RootOrientationX = 0.0f;
        Float Initial_RootOrientationY = 0.0f;
        Float Initial_RootOrientationZ = 0.0f;

        if (USE_COORDINATES_OFFSET)//Today
        {
            Initial_RootTranslationX = coordOffset[0];
            Initial_RootTranslationY = coordOffset[1];
            Initial_RootTranslationZ = coordOffset[2];

            Initial_RootOrientationX = coordOffset[3];
            Initial_RootOrientationY = coordOffset[4];
            Initial_RootOrientationZ = coordOffset[5];
        }


        String line;
        line = br.readLine();
        ArrayList<BAPFrame> bap_animation = new ArrayList<BAPFrame>();

        BapAnimationConverter bapconverter = new BapAnimationConverter();
        SpineInterpolation spine_interpo = new SpineInterpolation();

        ArrayList<Joint> joints = skeleton.getJoints();
        String bvh_joint;
        String dict_name;
        Float vx, vy, vz;
        Float RootTranslationX, RootTranslationY, RootTranslationZ;
        String[] data;
        int frame_skip = (int) Math.round(1 / (frameTime * Constants.FRAME_PER_SECOND));

        int ix = EulerAngleOrder / 100;
        int iy = (EulerAngleOrder % 100) / 10;
        int iz = (EulerAngleOrder % 100) % 10;
        //System.out.println("Euler Angles Orientation Order in BVH file: x=" + ix + "  y=" + iy + "  z=" + iz);
        int firstCharacter;
        int jjoint = 0;
        int f = 0;
        int cpt = 0;

        String typech;
        typech = ChannelsStyle();
        double DTOR = 0.017453293;
        Quaternion RootRotation = null;
        float rotationRootX = 0;
        float rotationRootY = 0;
        float rotationRootZ = 0;

        while (f < nbframe && line != null) // nbframe ////////////////////
        {
            BAPFrame bapframe = new BAPFrame(cpt + bapframe_startTime);

            // for mohamed study:
            //Control Fingers posture ...
            // end for mohamed study

            line = line.replace("\t", " ");
            line = line.replace("  ", " ");
            data = line.split(" ");
            /*Here we check if the first element is a space */
            if (data[0].isEmpty()) {
                firstCharacter = 1;
            } else {
                firstCharacter = 0;
            }
            /*  */
            while (firstCharacter < data.length) {
                Joint Currentjoint = joints.get(jjoint);
                bvh_joint = Currentjoint.getName();
                if (USE_DICTIONNARY) {
                    dict_name = (String) (dictionary.GetDict()).get(bvh_joint);//from bvh joint name to dictionnary name
                } else {
                    dict_name = bvh_joint; //fix mocap cathy
                }
                if (dict_name == null) {
                    dict_name = " "; // bvh_joint 18/09
                }

                if (firstCharacter == 0 || firstCharacter == 1) // Retrieve the rotation of humanoidRoot (Hips)
                {//Add the first 3 channels (HumanoidRoot rotation)

                    if (firstCharacter == 1)// the first character is a space
                    {
                        vx = (Float.parseFloat(data[4 + ix]));
                        vy = (Float.parseFloat(data[4 + iy]));
                        vz = (Float.parseFloat(data[4 + iz]));

                        RootTranslationX = (Float.parseFloat(data[1]));
                        RootTranslationY = (Float.parseFloat(data[2]));
                        RootTranslationZ = (Float.parseFloat(data[3]));


                    } else {
                        vx = (Float.parseFloat(data[3 + ix]));
                        vy = (Float.parseFloat(data[3 + iy]));
                        vz = (Float.parseFloat(data[3 + iz]));

                        RootTranslationX = (Float.parseFloat(data[0]));
                        RootTranslationY = (Float.parseFloat(data[1]));
                        RootTranslationZ = (Float.parseFloat(data[2]));

                    }
                    if (USE_COORDINATES_OFFSET) {
                        if (f == 0) // first frame //Today
                        {
                            Initial_RootTranslationXBVH = RootTranslationX;
                            Initial_RootTranslationYBVH = RootTranslationY;
                            Initial_RootTranslationZBVH = RootTranslationZ;

                            Initial_RootOrientationXBVH = vx;
                            Initial_RootOrientationYBVH = vy;
                            Initial_RootOrientationZBVH = vz;

                            RootTranslationX = Initial_RootTranslationX;
                            RootTranslationY = Initial_RootTranslationY;
                            RootTranslationZ = Initial_RootTranslationZ;

                            vx = Initial_RootOrientationX;
                            vy = Initial_RootOrientationY;
                            vz = Initial_RootOrientationZ;


                        } else {

                            RootTranslationX = (RootTranslationX - Initial_RootTranslationXBVH) + Initial_RootTranslationX;
                            RootTranslationY = (RootTranslationY - Initial_RootTranslationYBVH) + Initial_RootTranslationY;
                            RootTranslationZ = (RootTranslationZ - Initial_RootTranslationZBVH) + Initial_RootTranslationZ;

                            vx = (vx - Initial_RootOrientationXBVH) + Initial_RootOrientationX;
                            vy = (vy - Initial_RootOrientationYBVH) + Initial_RootOrientationY;
                            vz = (vz - Initial_RootOrientationZBVH) + Initial_RootOrientationZ;

                        }
                    }

//                    Joint Rootjoint=skeleton.getJoint(bvh_joint);
//                    Rootjoint.setWorldPosition(new Vec3d(RootTranslationX,RootTranslationY,RootTranslationZ));


                    // *********** For Tardis
                    Quaternion q;
//                    if (  AllPreRotation.containsKey(dict_name))
//
//                    {
//                       // q=AllPreRotation.get(dict_name);
//                        q=Quaternion.multiplication(JointQuaternion(vx, vy, vz, EulerAngleOrder) ,AllPreRotation.get(dict_name));
//                    }
//                    else
//                    {
                    q = JointQuaternion(vx, vy, vz, EulerAngleOrder);   // JointQuaternion90(dict_name,vx, vy, vz, EulerAngleOrder);  for tardis (In the case of T-Pose)


                    // }
//
//                       RootRotation=q;
//                       rotationRootX=vx;
//                       rotationRootY=vy;
//                       rotationRootZ=vz;


                    //*********** end for Tardis project


//                    if (f==0)
//                    {System.out.println("joint id="+jjoint+"   joint name: "+bvh_joint+ "   vx="+ vx+"  vy="+vy+"  vz="+vz);//now
//                    }
                    Vec3d RootTranslation = new Vec3d(RootTranslationX, RootTranslationY, RootTranslationZ);

                    bapframe = bapconverter.setBAPframeRotation(bapframe, dict_name, q);
                    bapframe = bapconverter.setBAPframeTranslation(bapframe, dict_name, RootTranslation);
                    firstCharacter = firstCharacter + 6;
                    jjoint++;
                } else //Retrieve the rotation of the other joints; Head, neck, arms...
                {
                    if (typech.equals("3channels")) // For every joint in the bvh file we have; CHANNELS 3 Yrotation Xrotation Zrotation
                    {

                        vx = (Float.parseFloat(data[firstCharacter + ix]));
                        vy = (Float.parseFloat(data[firstCharacter + iy]));
                        vz = (Float.parseFloat(data[firstCharacter + iz]));


                        firstCharacter = firstCharacter + 3;
                    } else // For every joint in the bvh file we have; CHANNELS 6 Xposition Yposition Zposition Zrotation Xrotation Yrotation
                    {
                        vx = (Float.parseFloat(data[firstCharacter + 3 + ix]));
                        vy = (Float.parseFloat(data[firstCharacter + 3 + iy]));
                        vz = (Float.parseFloat(data[firstCharacter + 3 + iz]));
                        firstCharacter = firstCharacter + 6;
                    }

                    // *********** For Tardis
                    Quaternion q;
//                    if (  AllPreRotation.containsKey(dict_name))
//
//                    {
//                       // q=AllPreRotation.get(dict_name);
//                        //System.out.println("Pre rotation exist with:  "+dict_name);
//                        q=Quaternion.multiplication(JointQuaternion(vx, vy, vz, EulerAngleOrder) ,AllPreRotation.get(dict_name) );
//                        if (f==0)
//                        {
//                            System.out.println(dict_name+"   "+"old rotation:  "+BVH.RTOD*AllPreRotation.get(dict_name).angle()+ "  "+AllPreRotation.get(dict_name).axis());
//                            System.out.println(dict_name+"   "+"new rotation:  "+BVH.RTOD*JointQuaternion(vx, vy, vz, EulerAngleOrder) .angle()+ "  "+JointQuaternion(vx, vy, vz, EulerAngleOrder).axis());
//                             System.out.println(dict_name+"   "+"new rotation:  "+vx+" "+vy+" "+vz);
//                            System.out.println(dict_name+"   "+"obtained rotation:  "+BVH.RTOD*q.angle()+ "  "+q.axis());
//                        }
//                    }
//                    else
//                    {
                    q = JointQuaternion(vx, vy, vz, EulerAngleOrder);// // JointQuaternion90(dict_name,vx, vy, vz, EulerAngleOrder); for tardis (In the case of T-Pose)

                    // }

                    //*********** end for Tardis Project


                    bapframe = bapconverter.setBAPframeRotation(bapframe, dict_name, q);
                    if (dict_name.equals("vt1") || dict_name.equals("vt6") || dict_name.equals("vt12")) {
                        spine_interpo.spineKeys.put(dict_name, q);
                    }

                    jjoint++;
                }
            }

            bapframe = spine_interpo.Interpolation(bapframe);
            bap_animation.add(bapframe);
            jjoint = 0;
            f = f + frame_skip;
            cpt = cpt + 1;
            for (int j = 0; j < frame_skip; j++) {
                line = br.readLine();
            }
        }
        return bap_animation;
    }

    public java.io.FileFilter getFileFilter() {
        return new java.io.FileFilter() {
            @Override
            public boolean accept(File pathName) {
                return true;//
                //pathName.getName().toLowerCase().endsWith(".bvh");//Today
            }
        };
    }

    public int EulerOrder() throws FileNotFoundException, IOException {
        BufferedReader br = ReadFile(fileName);
        String line;
        line = br.readLine();
        Hashtable tablech = new Hashtable();

        int channel_length = 8;

        while (!line.contains("CHANNELS")) {
            line = br.readLine();
        }

        line = SpaceRegularization(line);
        String[] l_split = line.split(" ");
        channel_length = l_split.length;
        for (int i = 0; i < channel_length; i++) {
            tablech.put(l_split[i], i);
        }

        int difference = channel_length - 3;
        int ix = ((Number) tablech.get("Xrotation")).intValue() - difference;
        int iy = ((Number) tablech.get("Yrotation")).intValue() - difference;
        int iz = ((Number) tablech.get("Zrotation")).intValue() - difference;

        int order_index = Integer.parseInt(Integer.toString(ix) + Integer.toString(iy) + Integer.toString(iz));
        //System.out.println(order_index);
        return order_index;

    }

    public String ChannelsStyle() throws FileNotFoundException, IOException {
        //6channels style means offset and rotation; For every joint in the bvh file we have; CHANNELS 6 Xposition Yposition Zposition Zrotation Xrotation Yrotation
        //3channels style means only rotation (except for the hips channels); For every joint in the bvh file we have; CHANNELS 3 Yrotation Xrotation Zrotation
        BufferedReader br = ReadFile(fileName);
        String line = br.readLine();
        String style;
        Vector channels = new Vector();
        int cpt = 0;
        int length;
        while (cpt < 3) {
            if (line.contains("CHANNELS")) {
                line = SpaceRegularization(line);
                String[] l_split = line.split(" ");
                length = l_split.length;
                cpt = cpt + 1;
                channels.add(length);
            }

            line = br.readLine();

        }
        if (channels.get(0) == channels.get(1)) {
            style = "6channels";
        } else {
            style = "3channels";
        }


        return style;
    }

    public ArrayList bvhMotionList(BufferedReader br, Skeleton skeleton, int nbframe, int EulerAngleOrder) throws IOException {
        String line;
        ArrayList<Motion> mylist = new ArrayList<Motion>();

        String name;
        Motion motion;


        ArrayList<Joint> joints = skeleton.getJoints();
        Iterator<Joint> it = joints.iterator();
        Joint joint;
        while (it.hasNext()) {
            joint = it.next();
            name = joint.getName();
            motion = new Motion(name);
            mylist.add(motion); // mylist.add(   new Obj(c1,c2,c3)  );
        }

        int ix = EulerAngleOrder / 100;
        int iy = (EulerAngleOrder % 100) / 10;
        int iz = (EulerAngleOrder % 100) % 10;
        //System.out.println("ix="+ix+"  iy="+iy+"  iz="+iz);

        line = br.readLine();
        //System.out.println(line);
        String[] data;
        line = line.replace("\t", " ");
        line = line.replace("  ", " ");
        data = line.split(" ");

        int firstCharacter;
        int jjoint = 0;
        String typech;
        int i;
        while (line != null && !line.isEmpty()) {

            line = SpaceRegularization(line);
            data = line.split(" ");
            //check if the first element is a space

            if (data[0].isEmpty()) {

                firstCharacter = 1;
            } else {
                firstCharacter = 0;
            }

            typech = ChannelsStyle();

            while (firstCharacter < data.length) {
                if (firstCharacter == 0 || firstCharacter == 1) {//Add the first 3 channels
                    motion = mylist.get(0);

                    if (firstCharacter == 1) {
                        motion.vect.addElement(Float.parseFloat(data[4 + ix]));
                        motion.vect.addElement(Float.parseFloat(data[4 + iy]));
                        motion.vect.addElement(Float.parseFloat(data[4 + iz]));
                    } else {
                        motion.vect.addElement(Float.parseFloat(data[3 + ix]));
                        motion.vect.addElement(Float.parseFloat(data[3 + iy]));
                        motion.vect.addElement(Float.parseFloat(data[3 + iz]));
                    }
                    mylist.set(0, motion);
                    firstCharacter = firstCharacter + 6;
                    jjoint++;
                }
                motion = mylist.get(jjoint);

                if (typech.equals("3channels")) {
                    motion.vect.addElement(Float.parseFloat(data[firstCharacter + ix]));
                    motion.vect.addElement(Float.parseFloat(data[firstCharacter + iy]));
                    motion.vect.addElement(Float.parseFloat(data[firstCharacter + iz]));
                    firstCharacter = firstCharacter + 3;
                } else {
                    motion.vect.addElement(Float.parseFloat(data[firstCharacter + 3 + ix]));
                    motion.vect.addElement(Float.parseFloat(data[firstCharacter + 3 + iy]));
                    motion.vect.addElement(Float.parseFloat(data[firstCharacter + 3 + iz]));
                    firstCharacter = firstCharacter + 6;
                }
                mylist.set(jjoint, motion);
                jjoint++;
            }
            jjoint = 0;
            line = br.readLine();
        }

        return mylist;
    }
    ArrayList<BAPFramePerformer> _bapFramePerformer = new ArrayList<BAPFramePerformer>();

    @Override
    public void addBAPFramePerformer(BAPFramePerformer performer) {
        if (performer != null) {
            _bapFramePerformer.add(performer);
        }
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer performer) {
        _bapFramePerformer.remove(performer);
    }

    public ArrayList<BAPFrame> OneBAPFrame(int bapframe_startTime) {
        ArrayList<BAPFrame> bapframes = new ArrayList<BAPFrame>();

        //r_shoulder:0 0 -30
        //l_shoulder:0 0 30
        //r_arm: 0 0 30
        // l_arm: 0 0 -30
        int cpt = 0;
        BAPFrame bapframe = new BAPFrame(cpt + bapframe_startTime);
        BapAnimationConverter bapconverter = new BapAnimationConverter();
        Quaternion q_rsh = JointQuaternion((float) -3.44302, (float) 4.797295, (float) -10.418204, 102);
        Quaternion q_lsh = JointQuaternion((float) -2.833775, (float) -1.034448, (float) 12.602939, 102);
        Quaternion q_lelbow = JointQuaternion((float) -64.692444, (float) -140.6921, (float) 121.36047, 102);
        // Quaternion q_relbow = JointQuaternion((float) -29.848253, (float) -0.250643, (float) -4.888558, 102);
        Quaternion q_relbow = JointQuaternion((float) -69.50336, (float) 130.97655, (float) -111.58504, 102);
//

        bapframe = bapconverter.setBAPframeRotation(bapframe, "r_shoulder", q_rsh);
        bapframe = bapconverter.setBAPframeRotation(bapframe, "l_shoulder", q_lsh);
        bapframe = bapconverter.setBAPframeRotation(bapframe, "r_elbow", q_relbow);
        bapframe = bapconverter.setBAPframeRotation(bapframe, "l_elbow", q_lelbow);

        for (int i = 0; i < 100; i++) {
            bapframes.add(bapframe);
        }

        return bapframes;
    }

    public ArrayList<BAPFrame> BAPFramesCreatorJFTBased(BVH bvh, int bapframe_startTime) throws IOException {
        ArrayList<BAPFrame> bap_animation = new ArrayList<BAPFrame>();
        AllJointFramesTable jointframestable = bvh.GetAllJointFramesTable();
        BapAnimationConverter bapconverter = new BapAnimationConverter();
        SpineInterpolation spine_interpo = new SpineInterpolation();

        int EulerAngleOrder = bvh.GetEulerAngleOrder();
        System.out.println("Euler Angle Order: " + EulerAngleOrder);
        int cpt = 0;
        for (int f = 0; f < 200; f++) //bvh.GetFrameNumber()
        {
            BAPFrame bapframe = new BAPFrame(cpt + bapframe_startTime);
            for (int joint_index = 0; joint_index < jointframestable.GetJointFramesTable().size(); joint_index++) {
                JointFramesList jointList = jointframestable.GetJointFramesTable().get(joint_index);
                String bvh_joint = jointList.GetJointName();

                String joint_name = (String) (dictionary.GetDict()).get(bvh_joint);//from bvh joint name to dictionnary name
                if (joint_name == null) {
                    joint_name = bvh_joint;
                }

                JointFrame jointframe = jointList.GetJointFrames().get(f);
                //float joint_angle = bvh.radian_degree(jointframe.GetRotation().angle());
                // Vec3d joint_axis = jointframe.GetRotation().axis();
                Quaternion q = jointframe.GetRotation();


                if (f == 0) {
                    // System.out.println("joint name:  "+joint_name+"   Angle="+ joint_angle+"   axis: "+joint_axis);
                    System.out.println("joint name:  " + joint_name + "   EulerAngles    " + jointframe.EulerAngles);
                }

                //  Quaternion joint_rotation = JointQuaternion(jointframe.EulerAngles.x(), jointframe.EulerAngles.y(), jointframe.EulerAngles.z(), EulerAngleOrder);
                bapframe = bapconverter.setBAPframeRotation(bapframe, joint_name, q);

                if (joint_index == 0)//HIPS
                {
                    bapframe = bapconverter.setBAPframeTranslation(bapframe, joint_name, jointframe.GetWorldPosition());
                }
                if (joint_name.equals("vt1") || joint_name.equals("vt6") || joint_name.equals("vt12")) {
                    spine_interpo.spineKeys.put(joint_name, q);
                }
            }

            bapframe = spine_interpo.Interpolation(bapframe);
            bap_animation.add(bapframe);
            cpt = cpt + 1;
        }

        return bap_animation;
    }
}
