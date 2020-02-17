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
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * This class contains all the information related to a bvh file
 * @author Nesrine Fourati
 */
public class BVH {
    /** Degrees-to-Radians multiplication constant */
    static final double DTOR = 0.017453293;
    /** Radians-to-Degrees multiplication constant */
    static final double RTOD = 57.29578;
    private ArrayList<Motion> motionlist = null;
    private Skeleton skeleton = new Skeleton("");
    private AllJointFramesTable jointframestable = null;
    private int nb_frame = 0;
    private float frame_time = 0.01f;
    private int EulerAngleOrder = 120;// ix=1 , iy=2, iz=0 => the order is zxy

    public BVH() {
    }

    public BVH(Skeleton s) {
        skeleton = s;
    }

    public BVH(ArrayList<Motion> mlist, int nbf)
    {
        motionlist = mlist;
        nb_frame = nbf;
        jointframestable = new AllJointFramesTable(motionlist);
    }

    public BVH(Skeleton s, int nbf) {
        skeleton = s;
        nb_frame = nbf;
    }

    public BVH(ArrayList<Motion> mlist, int nbf, Skeleton s, float frame_t) {
        motionlist = mlist;
        nb_frame = nbf;
        skeleton = s;
        frame_time = frame_t;
        jointframestable = new AllJointFramesTable(s);
    }

    public BVH(ArrayList<Motion> mlist, int nbf, Skeleton s, float frame_t, int order) {
        motionlist = mlist;
        nb_frame = nbf;
        skeleton = s;
        frame_time = frame_t;
        EulerAngleOrder = order;
        jointframestable = new AllJointFramesTable(s);
    }

    public BVH(AllJointFramesTable JFtable, int nbf, Skeleton s, float frame_t, int order) {
        nb_frame = nbf;
        skeleton = s;
        frame_time = frame_t;
        EulerAngleOrder = order;
        jointframestable = JFtable;
    }

    public float degree_radian(float angle) {

        float angle2 = (float) (DTOR * angle);
        return angle2;
    }

    public float radian_degree(float angle) {

        float angle2 = (float) (RTOD * angle);
        return angle2;
    }

    public void MotionListDisplay(ArrayList<Motion> mylist) {
        Iterator<Motion> it = mylist.iterator();
        Motion s;
        while (it.hasNext()) {
            s = it.next();
            System.out.println(s.getcle() + " " + s.vect);
        }
    }

    public void JointsDisplay() {
        Iterator<Motion> it = motionlist.iterator();
        Motion s;
        while (it.hasNext()) {
            s = it.next();
            System.out.println(s.getcle());
        }
    }

    public void JointsAnglesDisplay(ArrayList<AngleAxis> mylist) {/*axis and angle representation */
        Iterator<AngleAxis> it = mylist.iterator();
        AngleAxis s;
        while (it.hasNext()) {
            s = it.next();
            System.out.println(s.getcle() + " " + s.angle);
        }
    }

    public float GetFrameTime() {
        return frame_time;
    }

    public int GetFrameNumber() {
        return nb_frame;
    }

    public int GetEulerAngleOrder() {
        return EulerAngleOrder;
    }

    public ArrayList<Motion> GetMotionList() {
        return motionlist;
    }

    public Skeleton GetSkeleton() {
        return skeleton;
    }

    public AllJointFramesTable GetAllJointFramesTable() {
        return jointframestable;
    }

    public Quaternion bvhQuaternion90(Vector data, int framecpt, String name) {
        /* this concerns the quaternion of r_shoulder and l_shoulder for a particular initial position of arms ; T-Pose*/
        float vx = ((Number) data.elementAt(framecpt)).floatValue();
        float vy = ((Number) data.elementAt(framecpt + 1)).floatValue();
        float vz = ((Number) data.elementAt(framecpt + 2)).floatValue();

        Quaternion rotationx = new Quaternion();
        Quaternion rotationy = new Quaternion();
        Quaternion rotationz = new Quaternion();

        Quaternion q = new Quaternion();
        Quaternion q2 = new Quaternion();

        rotationx.setAxisAngle(new Vec3d(1, 0, 0), degree_radian(vx));
        rotationy.setAxisAngle(new Vec3d(0, 1, 0), degree_radian(vy));
        rotationz.setAxisAngle(new Vec3d(0, 0, 1), degree_radian(vz));
        //*************** for particular initial position of skeleton : T-Pose
        Quaternion rotationz_90 = new Quaternion(new Vec3d(0, 0, 1), degree_radian(-90));
        Quaternion rotationz90 = new Quaternion(new Vec3d(0, 0, 1), degree_radian(90));

        if (name.equals("r_shoulder")) {
            q2 = Quaternion.multiplication(q, rotationz_90);
        } else {
            if (name.equals("l_shoulder")) {
                q2 = Quaternion.multiplication(q, rotationz90);
            }
        }

        q = Quaternion.multiplication(q, rotationz);
        q = Quaternion.multiplication(q, rotationx);
        q = Quaternion.multiplication(q, rotationy);

        q = Quaternion.multiplication(q, q2);
        return q;
    }

    public Quaternion bvhQuaternion(Vector data, int framecpt) {
        float vx = ((Number) data.elementAt(framecpt)).floatValue();
        float vy = ((Number) data.elementAt(framecpt + 1)).floatValue();
        float vz = ((Number) data.elementAt(framecpt + 2)).floatValue();
        // System.out.println("rotation "+vx+" "+vy+" "+vz);
        Quaternion rotationx = new Quaternion();
        Quaternion rotationy = new Quaternion();
        Quaternion rotationz = new Quaternion();

        Quaternion q = new Quaternion();

        rotationx.setAxisAngle(new Vec3d(1, 0, 0), degree_radian(vx));
        rotationy.setAxisAngle(new Vec3d(0, 1, 0), degree_radian(vy));
        rotationz.setAxisAngle(new Vec3d(0, 0, 1), degree_radian(vz));
        switch (EulerAngleOrder) {
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
        }


        return q;
    }

    public ArrayList<AngleAxis> bvhAngleAxis() {
        ArrayList<AngleAxis> angle_axis_list = new ArrayList<AngleAxis>();
        int nbjoint = motionlist.size();
        Dictionary dictionary = new Dictionary();
        dictionary.Initialize();

        int framecpt = 0;//compteur de "channels" 3 par 3
        String bvh_joint;
        String dict_name;
        Motion motion;
        SpineInterpolation spine_interpo = new SpineInterpolation();
//        HashMap spinekeys = new HashMap();
//
//        spinekeys.put("vt1", new Quaternion());
//        spinekeys.put("vt6", new Quaternion());
//        spinekeys.put("vt12", new Quaternion());

        for (int f = 0; f < nb_frame; f++) {
            // BAPFrame bapframe = new BAPFrame(f);
            for (int i = 0; i < nbjoint; i++) // i=0 joint hips
            {
                // intialize the joint_angles array
                if (f == 0) {
                    //angle_axis_list.add(new AngleAxis((String) (dictionary.GetDict()).get(motionlist.get(i).getcle())));
                    angle_axis_list.add(new AngleAxis(motionlist.get(i).getcle()));
                }

                motion = motionlist.get(i);
                Vector data = motion.getvect();
                bvh_joint = motionlist.get(i).getcle();
                dict_name = (String) (dictionary.GetDict()).get(bvh_joint);//from joint bvh name to dictionnary name
                if (dict_name == null) {
                    dict_name = " ";
                }

                Quaternion q = bvhQuaternion(data, framecpt);
                //bapframe=setBAPframe(bapframe,dict_name,q);
                AngleAxis angleaxis = angle_axis_list.get(i);
                angleaxis.angle.addElement(q.angle());
                angleaxis.axis.addElement(q.axis());
//                if (dict_name.equalsIgnoreCase("r_elbow"))
//                {
////                    System.out.println("x= "+q.axis().x());
//        float vx= ((Number) data.elementAt(framecpt)).floatValue();
//        float vy= ((Number) data.elementAt(framecpt + 1)).floatValue();
//        float vz= ((Number) data.elementAt(framecpt + 2)).floatValue();
//        System.out.println(f+"  Eulervx="+vx+"  axisx="+q.axis().x());
//                }
                angle_axis_list.set(i, angleaxis);
//                if ( dict_name.equals("vt1") || dict_name.equals("vt6") || dict_name.equals("vt12") )
//                {
//                    spine_interpo.spineKeys.put(dict_name, q);
//                }
            }
            // bapframe = spineInterpolation(bapframe, spinekeys);

            framecpt = framecpt + 3;
        }

        return angle_axis_list;
    }

    public void FillJointFramesTable() // Fill JointFramesTable from bvh Motion List and bvh skeleton already created
    {

        String joint_name;
        Motion motion;
        int nbjoint = motionlist.size();
        int framecpt = 0;// 3 by 3 counter for 3D angles in motion structure
        try {
            for (int f = 0; f < nb_frame; f++) //nb_frame
            {
                //Quaternion ChangeAxesRotation = new Quaternion();
                Vec3d localposition = new Vec3d();
                for (int j = 0; j < nbjoint; j++)//nbjoint
                {
                    motion = motionlist.get(j);
                    Vector data = motion.getvect();
                    joint_name = motion.getcle();
                    Quaternion rotation = bvhQuaternion(data, framecpt);
                    if (j == 0) // Root
                    {
//                 ChangeAxesRotation.setAxisAngle(new Vec3d(0, 1, 0), -rotation.angle()*rotation.axis().y());
//                 System.out.println(-rotation.angle()*rotation.axis().y());
                   rotation=new Quaternion();
                    }

                    Joint joint = skeleton.getJoint(joint_name);
                    joint.rotate(rotation);
                    joint.update();
                    Vec3d worldposition = joint.getWorldPosition();
//                if (j>0)
//                {localposition=Quaternion.multiplication(ChangeAxesRotation, worldposition);
//                }

                    JointFrame jointframe = new JointFrame(f, rotation, worldposition, localposition);

//                System.out.println(joint_name );
//                System.out.println(" localpositionSkeleton   "+joint.getLocalPosition());
//                System.out.println(" localposition   "+localposition);
//                System.out.println(" globalposition   "+worldposition);
//
                    jointframestable.AddJointFrameAt(j, jointframe);

                }
                framecpt = framecpt + 3;

                Joint joint = skeleton.getJoint(0);
                joint.reset();
                joint.update();

            }
        } catch (Exception e) {
            System.out.println("Motion list was not created");
        }


    }
}
