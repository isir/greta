using System.Collections.Generic;

namespace animationparameters
{
    public class JointType
    {
        public static List<JointType> values = new List<JointType>();

        public static JointType HumanoidRoot = new JointType(BAPType.HumanoidRoot_rt_body_tilt, BAPType.HumanoidRoot_rt_body_turn, BAPType.HumanoidRoot_rt_body_roll, null);
        public static JointType sacroiliac = new JointType(BAPType.sacroiliac_tilt, BAPType.sacroiliac_torsion, BAPType.sacroiliac_roll, HumanoidRoot);
        public static JointType l_hip = new JointType(BAPType.l_hip_flexion, BAPType.l_hip_twisting, BAPType.l_hip_abduct, sacroiliac);
        public static JointType l_knee = new JointType(BAPType.l_knee_flexion, BAPType.l_knee_twisting, BAPType.null_bap, l_hip);
        public static JointType l_ankle = new JointType(BAPType.l_ankle_flexion, BAPType.l_ankle_twisting, BAPType.null_bap, l_knee);
        public static JointType l_subtalar = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.l_subtalar_flexion, l_ankle);
        public static JointType l_midtarsal = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.l_midtarsal_twisting, l_subtalar);
        public static JointType l_metatarsal = new JointType(BAPType.l_metatarsal_flexion, BAPType.null_bap, BAPType.null_bap, l_midtarsal);
        public static JointType r_hip = new JointType(BAPType.r_hip_flexion, BAPType.r_hip_twisting, BAPType.r_hip_abduct, sacroiliac);
        public static JointType r_knee = new JointType(BAPType.r_knee_flexion, BAPType.r_knee_twisting, BAPType.null_bap, r_hip);
        public static JointType r_ankle = new JointType(BAPType.r_ankle_flexion, BAPType.r_ankle_twisting, BAPType.null_bap, r_knee);
        public static JointType r_subtalar = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.r_subtalar_flexion, r_ankle);
        public static JointType r_midtarsal = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.r_midtarsal_twisting, r_subtalar);
        public static JointType r_metatarsal = new JointType(BAPType.r_metatarsal_flexion, BAPType.null_bap, BAPType.null_bap, r_midtarsal);
        public static JointType vl5 = new JointType(BAPType.vl5_tilt, BAPType.vl5_torsion, BAPType.vl5_roll, HumanoidRoot);
        public static JointType vl4 = new JointType(BAPType.vl4_tilt, BAPType.vl4_torsion, BAPType.vl4_roll, vl5);
        public static JointType vl3 = new JointType(BAPType.vl3_tilt, BAPType.vl3_torsion, BAPType.vl3_roll, vl4);
        public static JointType vl2 = new JointType(BAPType.vl2_tilt, BAPType.vl2_torsion, BAPType.vl2_roll, vl3);
        public static JointType vl1 = new JointType(BAPType.vl1_tilt, BAPType.vl1_torsion, BAPType.vl1_roll, vl2);
        public static JointType vt12 = new JointType(BAPType.vt12_tilt, BAPType.vt12_torsion, BAPType.vt12_roll, vl1);
        public static JointType vt11 = new JointType(BAPType.vt11_tilt, BAPType.vt11_torsion, BAPType.vt11_roll, vt12);
        public static JointType vt10 = new JointType(BAPType.vt10_tilt, BAPType.vt10_torsion, BAPType.vt10_roll, vt11);
        public static JointType vt9 = new JointType(BAPType.vt9_tilt, BAPType.vt9_torsion, BAPType.vt9_roll, vt10);
        public static JointType vt8 = new JointType(BAPType.vt8_tilt, BAPType.vt8_torsion, BAPType.vt8_roll, vt9);
        public static JointType vt7 = new JointType(BAPType.vt7_tilt, BAPType.vt7_torsion, BAPType.vt7_roll, vt8);
        public static JointType vt6 = new JointType(BAPType.vt6_tilt, BAPType.vt6_torsion, BAPType.vt6_roll, vt7);
        public static JointType vt5 = new JointType(BAPType.vt5_tilt, BAPType.vt5_torsion, BAPType.vt5_roll, vt6);
        public static JointType vt4 = new JointType(BAPType.vt4_tilt, BAPType.vt4_torsion, BAPType.vt4_roll, vt5);
        public static JointType vt3 = new JointType(BAPType.vt3_tilt, BAPType.vt3_torsion, BAPType.vt3_roll, vt4);
        public static JointType vt2 = new JointType(BAPType.vt2_tilt, BAPType.vt2_torsion, BAPType.vt2_roll, vt3);
        public static JointType vt1 = new JointType(BAPType.vt1_tilt, BAPType.vt1_torsion, BAPType.vt1_roll, vt2);
        public static JointType vc7 = new JointType(BAPType.vc7_tilt, BAPType.vc7_torsion, BAPType.vc7_roll, vt1);
        public static JointType vc6 = new JointType(BAPType.vc6_tilt, BAPType.vc6_torsion, BAPType.vc6_roll, vc7);
        public static JointType vc5 = new JointType(BAPType.vc5_tilt, BAPType.vc5_torsion, BAPType.vc5_roll, vc6);
        public static JointType vc4 = new JointType(BAPType.vc4_tilt, BAPType.vc4_torsion, BAPType.vc4_roll, vc5);
        public static JointType vc3 = new JointType(BAPType.vc3_tilt, BAPType.vc3_torsion, BAPType.vc3_roll, vc4);
        public static JointType vc2 = new JointType(BAPType.vc2_tilt, BAPType.vc2_torsion, BAPType.vc2_roll, vc3);
        public static JointType vc1 = new JointType(BAPType.vc1_tilt, BAPType.vc1_torsion, BAPType.vc1_roll, vc2);
        public static JointType skullbase = new JointType(BAPType.skullbase_tilt, BAPType.skullbase_torsion, BAPType.skullbase_roll, vc1);
        public static JointType l_eyeball_joint = new JointType(skullbase);// useless ?
        public static JointType r_eyeball_joint = new JointType(skullbase);// useless ?
        public static JointType l_sternoclavicular = new JointType(BAPType.null_bap, BAPType.l_sternoclavicular_rotate, BAPType.l_sternoclavicular_abduct, vt1);
        public static JointType l_acromioclavicular = new JointType(BAPType.null_bap, BAPType.l_acromioclavicular_rotate, BAPType.l_acromioclavicular_abduct, l_sternoclavicular);
        public static JointType l_shoulder = new JointType(BAPType.l_shoulder_flexion, BAPType.l_shoulder_twisting, BAPType.l_shoulder_abduct, l_acromioclavicular);
        public static JointType l_elbow = new JointType(BAPType.l_elbow_flexion, BAPType.l_elbow_twisting, BAPType.null_bap, l_shoulder);
        public static JointType l_wrist = new JointType(BAPType.l_wrist_pivot, BAPType.l_wrist_twisting, BAPType.l_wrist_flexion, l_elbow);
        public static JointType l_thumb1 = new JointType(BAPType.l_thumb1_flexion, BAPType.l_thumb1_pivot, BAPType.l_thumb1_twisting, l_wrist);
        public static JointType l_thumb2 = new JointType(BAPType.l_thumb2_flexion, BAPType.null_bap, BAPType.null_bap, l_thumb1);
        public static JointType l_thumb3 = new JointType(BAPType.l_thumb3_flexion, BAPType.null_bap, BAPType.null_bap, l_thumb2);
        public static JointType l_index0 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.l_index0_flexion, l_wrist);
        public static JointType l_index1 = new JointType(BAPType.l_index1_pivot, BAPType.l_index1_twisting, BAPType.l_index1_flexion, l_index0);
        public static JointType l_index2 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.l_index2_flexion, l_index1);
        public static JointType l_index3 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.l_index3_flexion, l_index2);
        public static JointType l_middle0 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.l_middle0_flexion, l_wrist);
        public static JointType l_middle1 = new JointType(BAPType.l_middle1_pivot, BAPType.l_middle1_twisting, BAPType.l_middle1_flexion, l_middle0);
        public static JointType l_middle2 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.l_middle2_flexion, l_middle1);
        public static JointType l_middle3 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.l_middle3_flexion, l_middle2);
        public static JointType l_ring0 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.l_ring0_flexion, l_wrist);
        public static JointType l_ring1 = new JointType(BAPType.l_ring1_pivot, BAPType.l_ring1_twisting, BAPType.l_ring1_flexion, l_ring0);
        public static JointType l_ring2 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.l_ring2_flexion, l_ring1);
        public static JointType l_ring3 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.l_ring3_flexion, l_ring2);
        public static JointType l_pinky0 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.l_pinky0_flexion, l_wrist);
        public static JointType l_pinky1 = new JointType(BAPType.l_pinky1_pivot, BAPType.l_pinky1_twisting, BAPType.l_pinky1_flexion, l_pinky0);
        public static JointType l_pinky2 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.l_pinky2_flexion, l_pinky1);
        public static JointType l_pinky3 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.l_pinky3_flexion, l_pinky2);
        public static JointType r_sternoclavicular = new JointType(BAPType.null_bap, BAPType.r_sternoclavicular_rotate, BAPType.r_sternoclavicular_abduct, vt1);
        public static JointType r_acromioclavicular = new JointType(BAPType.null_bap, BAPType.r_acromioclavicular_rotate, BAPType.r_acromioclavicular_abduct, r_sternoclavicular);
        public static JointType r_shoulder = new JointType(BAPType.r_shoulder_flexion, BAPType.r_shoulder_twisting, BAPType.r_shoulder_abduct, r_acromioclavicular);
        public static JointType r_elbow = new JointType(BAPType.r_elbow_flexion, BAPType.r_elbow_twisting, BAPType.null_bap, r_shoulder);
        public static JointType r_wrist = new JointType(BAPType.r_wrist_pivot, BAPType.r_wrist_twisting, BAPType.r_wrist_flexion, r_elbow);
        public static JointType r_thumb1 = new JointType(BAPType.r_thumb1_flexion, BAPType.r_thumb1_pivot, BAPType.r_thumb1_twisting, r_wrist);
        public static JointType r_thumb2 = new JointType(BAPType.r_thumb2_flexion, BAPType.null_bap, BAPType.null_bap, r_thumb1);
        public static JointType r_thumb3 = new JointType(BAPType.r_thumb3_flexion, BAPType.null_bap, BAPType.null_bap, r_thumb2);
        public static JointType r_index0 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.r_index0_flexion, r_wrist);
        public static JointType r_index1 = new JointType(BAPType.r_index1_pivot, BAPType.r_index1_twisting, BAPType.r_index1_flexion, r_index0);
        public static JointType r_index2 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.r_index2_flexion, r_index1);
        public static JointType r_index3 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.r_index3_flexion, r_index2);
        public static JointType r_middle0 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.r_middle0_flexion, r_wrist);
        public static JointType r_middle1 = new JointType(BAPType.r_middle1_pivot, BAPType.r_middle1_twisting, BAPType.r_middle1_flexion, r_middle0);
        public static JointType r_middle2 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.r_middle2_flexion, r_middle1);
        public static JointType r_middle3 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.r_middle3_flexion, r_middle2);
        public static JointType r_ring0 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.r_ring0_flexion, r_wrist);
        public static JointType r_ring1 = new JointType(BAPType.r_ring1_pivot, BAPType.r_ring1_twisting, BAPType.r_ring1_flexion, r_ring0);
        public static JointType r_ring2 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.r_ring2_flexion, r_ring1);
        public static JointType r_ring3 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.r_ring3_flexion, r_ring2);
        public static JointType r_pinky0 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.r_pinky0_flexion, r_wrist);
        public static JointType r_pinky1 = new JointType(BAPType.r_pinky1_pivot, BAPType.r_pinky1_twisting, BAPType.r_pinky1_flexion, r_pinky0);
        public static JointType r_pinky2 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.r_pinky2_flexion, r_pinky1);
        public static JointType r_pinky3 = new JointType(BAPType.null_bap, BAPType.null_bap, BAPType.r_pinky3_flexion, r_pinky2);
        public static JointType null_joint = new JointType();

        public static int NUMJOINTS = 89;

        public BAPType rotationX;
        public BAPType rotationY;
        public BAPType rotationZ;
        public JointType parent;

        private JointType()
        {
            rotationX = BAPType.null_bap;
            rotationY = BAPType.null_bap;
            rotationZ = BAPType.null_bap;
            parent = null;
            values.Add(this);
        }

        private JointType(BAPType rx, BAPType ry, BAPType rz, JointType parent)
        {
            rotationX = rx;
            rotationY = ry;
            rotationZ = rz;
            this.parent = parent;
            values.Add(this);
        }

        private JointType(JointType parent)
        {
            rotationX = BAPType.null_bap;
            rotationY = BAPType.null_bap;
            rotationZ = BAPType.null_bap;
            this.parent = parent;
            values.Add(this);
        }
    }
}
