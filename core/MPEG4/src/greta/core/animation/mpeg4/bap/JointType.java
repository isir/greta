/*
 * This file is part of Greta.
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
package greta.core.animation.mpeg4.bap;

/**
 *
 * @author Jing Huang
 * @author Andre-Marie Pez
 */
public enum JointType {

    HumanoidRoot(BAPType.HumanoidRoot_rt_body_tilt, BAPType.HumanoidRoot_rt_body_turn, BAPType.HumanoidRoot_rt_body_roll, null),
      sacroiliac(BAPType.sacroiliac_tilt, BAPType.sacroiliac_torsion, BAPType.sacroiliac_roll, HumanoidRoot),
        l_hip(BAPType.l_hip_flexion, BAPType.l_hip_twisting, BAPType.l_hip_abduct, sacroiliac),
          l_knee(BAPType.l_knee_flexion, BAPType.l_knee_twisting, null, l_hip),
            l_ankle(BAPType.l_ankle_flexion, BAPType.l_ankle_twisting, null, l_knee),
              l_subtalar(null, null,BAPType.l_subtalar_flexion, l_ankle),
                l_midtarsal(null, null, BAPType.l_midtarsal_twisting, l_subtalar),
                  l_metatarsal(BAPType.l_metatarsal_flexion, null, null, l_midtarsal),
        r_hip(BAPType.r_hip_flexion, BAPType.r_hip_twisting, BAPType.r_hip_abduct, sacroiliac),
          r_knee(BAPType.r_knee_flexion, BAPType.r_knee_twisting, null, r_hip),
            r_ankle(BAPType.r_ankle_flexion, BAPType.r_ankle_twisting, null, r_knee),
              r_subtalar(null, null,BAPType.r_subtalar_flexion, r_ankle),
                r_midtarsal(null, null, BAPType.r_midtarsal_twisting, r_subtalar),
                  r_metatarsal(BAPType.r_metatarsal_flexion, null, null, r_midtarsal),
      vl5(BAPType.vl5_tilt, BAPType.vl5_torsion, BAPType.vl5_roll, HumanoidRoot),
        vl4(BAPType.vl4_tilt, BAPType.vl4_torsion, BAPType.vl4_roll, vl5),
          vl3(BAPType.vl3_tilt, BAPType.vl3_torsion, BAPType.vl3_roll, vl4),
            vl2(BAPType.vl2_tilt, BAPType.vl2_torsion, BAPType.vl2_roll, vl3),
              vl1(BAPType.vl1_tilt, BAPType.vl1_torsion, BAPType.vl1_roll, vl2),
                vt12(BAPType.vt12_tilt, BAPType.vt12_torsion, BAPType.vt12_roll,vl1),
                  vt11(BAPType.vt11_tilt, BAPType.vt11_torsion, BAPType.vt11_roll, vt12),
                    vt10(BAPType.vt10_tilt, BAPType.vt10_torsion, BAPType.vt10_roll, vt11),
                      vt9(BAPType.vt9_tilt, BAPType.vt9_torsion, BAPType.vt9_roll, vt10),
                        vt8(BAPType.vt8_tilt, BAPType.vt8_torsion, BAPType.vt8_roll, vt9),
                          vt7(BAPType.vt7_tilt, BAPType.vt7_torsion, BAPType.vt7_roll, vt8),
                            vt6(BAPType.vt6_tilt, BAPType.vt6_torsion, BAPType.vt6_roll, vt7),
                              vt5(BAPType.vt5_tilt, BAPType.vt5_torsion, BAPType.vt5_roll, vt6),
                                vt4(BAPType.vt4_tilt, BAPType.vt4_torsion, BAPType.vt4_roll, vt5),
                                  vt3(BAPType.vt3_tilt, BAPType.vt3_torsion, BAPType.vt3_roll, vt4),
                                    vt2(BAPType.vt2_tilt, BAPType.vt2_torsion, BAPType.vt2_roll, vt3),
                                      vt1(BAPType.vt1_tilt, BAPType.vt1_torsion, BAPType.vt1_roll, vt2),
                                        vc7(BAPType.vc7_tilt, BAPType.vc7_torsion, BAPType.vc7_roll, vt1),
                                          vc6(BAPType.vc6_tilt, BAPType.vc6_torsion, BAPType.vc6_roll, vc7),
                                            vc5(BAPType.vc5_tilt, BAPType.vc5_torsion, BAPType.vc5_roll, vc6),
                                              vc4(BAPType.vc4_tilt, BAPType.vc4_torsion, BAPType.vc4_roll, vc5),
                                                vc3(BAPType.vc3_tilt, BAPType.vc3_torsion, BAPType.vc3_roll, vc4),
                                                  vc2(BAPType.vc2_tilt, BAPType.vc2_torsion, BAPType.vc2_roll, vc3),
                                                    vc1(BAPType.vc1_tilt, BAPType.vc1_torsion, BAPType.vc1_roll, vc2),
                                                      skullbase(BAPType.skullbase_tilt, BAPType.skullbase_torsion, BAPType.skullbase_roll, vc1),
                                                        l_eyeball_joint(skullbase),// useless ?
                                                        r_eyeball_joint(skullbase),// useless ?
                                        l_sternoclavicular(null, BAPType.l_sternoclavicular_rotate, BAPType.l_sternoclavicular_abduct, vt1),
                                          l_acromioclavicular(null, BAPType.l_acromioclavicular_rotate, BAPType.l_acromioclavicular_abduct, l_sternoclavicular),
                                            l_shoulder(BAPType.l_shoulder_flexion, BAPType.l_shoulder_twisting, BAPType.l_shoulder_abduct, l_acromioclavicular),
                                              l_elbow(BAPType.l_elbow_flexion, BAPType.l_elbow_twisting, null, l_shoulder),
                                                l_wrist(BAPType.l_wrist_pivot, BAPType.l_wrist_twisting, BAPType.l_wrist_flexion, l_elbow),
                                                  l_thumb1(BAPType.l_thumb1_flexion, BAPType.l_thumb1_pivot, BAPType.l_thumb1_twisting, l_wrist),
                                                    l_thumb2(BAPType.l_thumb2_flexion, null, null, l_thumb1),
                                                      l_thumb3(BAPType.l_thumb3_flexion, null, null, l_thumb2),
                                                  l_index0(null, null, BAPType.l_index0_flexion, l_wrist),
                                                    l_index1(BAPType.l_index1_pivot, BAPType.l_index1_twisting, BAPType.l_index1_flexion, l_index0),
                                                      l_index2(null, null, BAPType.l_index2_flexion, l_index1),
                                                        l_index3(null, null, BAPType.l_index3_flexion, l_index2),
                                                  l_middle0(null, null, BAPType.l_middle0_flexion, l_wrist),
                                                    l_middle1(BAPType.l_middle1_pivot, BAPType.l_middle1_twisting, BAPType.l_middle1_flexion, l_middle0),
                                                      l_middle2(null, null, BAPType.l_middle2_flexion, l_middle1),
                                                        l_middle3(null, null, BAPType.l_middle3_flexion, l_middle2),
                                                  l_ring0(null, null, BAPType.l_ring0_flexion, l_wrist),
                                                    l_ring1(BAPType.l_ring1_pivot, BAPType.l_ring1_twisting, BAPType.l_ring1_flexion, l_ring0),
                                                      l_ring2(null, null, BAPType.l_ring2_flexion, l_ring1),
                                                        l_ring3(null, null, BAPType.l_ring3_flexion, l_ring2),
                                                  l_pinky0(null, null, BAPType.l_pinky0_flexion, l_wrist),
                                                    l_pinky1(BAPType.l_pinky1_pivot, BAPType.l_pinky1_twisting, BAPType.l_pinky1_flexion, l_pinky0),
                                                      l_pinky2(null, null, BAPType.l_pinky2_flexion, l_pinky1),
                                                        l_pinky3(null, null, BAPType.l_pinky3_flexion,l_pinky2),
                                        r_sternoclavicular(null, BAPType.r_sternoclavicular_rotate, BAPType.r_sternoclavicular_abduct, vt1),
                                          r_acromioclavicular(null, BAPType.r_acromioclavicular_rotate, BAPType.r_acromioclavicular_abduct, r_sternoclavicular),
                                            r_shoulder(BAPType.r_shoulder_flexion, BAPType.r_shoulder_twisting, BAPType.r_shoulder_abduct, r_acromioclavicular),
                                              r_elbow(BAPType.r_elbow_flexion, BAPType.r_elbow_twisting, null, r_shoulder),
                                                r_wrist(BAPType.r_wrist_pivot, BAPType.r_wrist_twisting, BAPType.r_wrist_flexion, r_elbow),
                                                  r_thumb1(BAPType.r_thumb1_flexion, BAPType.r_thumb1_pivot, BAPType.r_thumb1_twisting, r_wrist),
                                                    r_thumb2(BAPType.r_thumb2_flexion, null, null, r_thumb1),
                                                      r_thumb3(BAPType.r_thumb3_flexion, null, null, r_thumb2),
                                                  r_index0(null, null, BAPType.r_index0_flexion, r_wrist),
                                                    r_index1(BAPType.r_index1_pivot, BAPType.r_index1_twisting, BAPType.r_index1_flexion, r_index0),
                                                      r_index2(null, null, BAPType.r_index2_flexion, r_index1),
                                                        r_index3(null, null, BAPType.r_index3_flexion, r_index2),
                                                  r_middle0(null, null, BAPType.r_middle0_flexion, r_wrist),
                                                    r_middle1(BAPType.r_middle1_pivot, BAPType.r_middle1_twisting, BAPType.r_middle1_flexion, r_middle0),
                                                      r_middle2(null, null, BAPType.r_middle2_flexion, r_middle1),
                                                        r_middle3(null, null, BAPType.r_middle3_flexion, r_middle2),
                                                  r_ring0(null, null, BAPType.r_ring0_flexion, r_wrist),
                                                    r_ring1(BAPType.r_ring1_pivot, BAPType.r_ring1_twisting, BAPType.r_ring1_flexion, r_ring0),
                                                      r_ring2(null, null, BAPType.r_ring2_flexion, r_ring1),
                                                        r_ring3(null, null, BAPType.r_ring3_flexion, r_ring2),
                                                  r_pinky0(null, null, BAPType.r_pinky0_flexion, r_wrist),
                                                    r_pinky1(BAPType.r_pinky1_pivot, BAPType.r_pinky1_twisting, BAPType.r_pinky1_flexion, r_pinky0),
                                                      r_pinky2(null, null, BAPType.r_pinky2_flexion, r_pinky1),
                                                        r_pinky3(null, null, BAPType.r_pinky3_flexion, r_pinky2),
    null_joint;

    public static final int NUMJOINTS = 89;

    public final BAPType rotationX;
    public final BAPType rotationY;
    public final BAPType rotationZ;
    public final JointType parent;

    private JointType() {
        rotationX = BAPType.null_bap;
        rotationY = BAPType.null_bap;
        rotationZ = BAPType.null_bap;
        parent = null;
    }

    private JointType(BAPType rx, BAPType ry, BAPType rz, JointType parent) {
        rotationX = rx==null? BAPType.null_bap : rx;
        rotationY = ry==null? BAPType.null_bap : ry;
        rotationZ = rz==null? BAPType.null_bap : rz;
        this.parent = parent;
    }

    private JointType(JointType parent) {
        rotationX = BAPType.null_bap;
        rotationY = BAPType.null_bap;
        rotationZ = BAPType.null_bap;
        this.parent = parent;
    }

    public static JointType get(String name){
        try {
            return valueOf(name);
        } catch (Throwable t) {
            return null_joint;
        }
    }

    public static JointType get(int ordinal){
        return ordinal<0 || ordinal>NUMJOINTS ? null_joint : values()[ordinal];
    }
}
