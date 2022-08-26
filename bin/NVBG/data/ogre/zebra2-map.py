
def Zebra2map():

    # Mapping from Zebra2 skeleton to SmartBody skeleton

    jointMapManager = scene.getJointMapManager()
    zebra2Map = jointMapManager.createJointMap("zebra2")

    # Core
    zebra2Map.setMapping("JtRoot", "base")
    zebra2Map.setMapping("JtSpineA", "spine1")
    zebra2Map.setMapping("JtSpineB", "spine2")
    zebra2Map.setMapping("JtSpineC", "spine3")
    zebra2Map.setMapping("JtNeckA", "spine4")
    zebra2Map.setMapping("JtNeckB", "spine5")
    zebra2Map.setMapping("JtSkullA", "skullbase")

    # Arm, left
    zebra2Map.setMapping("JtClavicleLf", "l_sternoclavicular")
    zebra2Map.setMapping("JtShoulderLf", "l_shoulder")
    zebra2Map.setMapping("JtUpperArmTwistALf", "l_upperarm1")
    zebra2Map.setMapping("JtUpperArmTwistBLf", "l_upperarm2")
    zebra2Map.setMapping("JtElbowLf", "l_elbow")
    zebra2Map.setMapping("JtForearmTwistALf", "l_forearm1")
    zebra2Map.setMapping("JtForearmTwistBLf", "l_forearm2")
    zebra2Map.setMapping("JtWristLf", "l_wrist")
    zebra2Map.setMapping("JtThumbALf", "l_thumb1")
    zebra2Map.setMapping("JtThumbBLf", "l_thumb2")
    zebra2Map.setMapping("JtThumbCLf", "l_thumb3")
    zebra2Map.setMapping("JtThumbDLf", "l_thumb4")
    zebra2Map.setMapping("JtIndexALf", "l_index1")
    zebra2Map.setMapping("JtIndexBLf", "l_index2")
    zebra2Map.setMapping("JtIndexCLf", "l_index3")
    zebra2Map.setMapping("JtIndexDLf", "l_index4")
    zebra2Map.setMapping("JtMiddleALf", "l_middle1")
    zebra2Map.setMapping("JtMiddleBLf", "l_middle2")
    zebra2Map.setMapping("JtMiddleCLf", "l_middle3")
    zebra2Map.setMapping("JtMiddleDLf", "l_middle4")
    zebra2Map.setMapping("JtRingALf", "l_ring1")
    zebra2Map.setMapping("JtRingBLf", "l_ring2")
    zebra2Map.setMapping("JtRingCLf", "l_ring3")
    zebra2Map.setMapping("JtRingDLf", "l_ring4")
    zebra2Map.setMapping("JtLittleALf", "l_pinky1")
    zebra2Map.setMapping("JtLittleBLf", "l_pinky2")
    zebra2Map.setMapping("JtLittleCLf", "l_pinky3")
    zebra2Map.setMapping("JtLittleDLf", "l_pinky4")

    # Arm, right
    zebra2Map.setMapping("JtClavicleRt", "r_sternoclavicular")
    zebra2Map.setMapping("JtShoulderRt", "r_shoulder")
    zebra2Map.setMapping("JtUpperArmTwistARt", "r_upperarm1")
    zebra2Map.setMapping("JtUpperArmTwistBRt", "r_upperarm2")
    zebra2Map.setMapping("JtElbowRt", "r_elbow")
    zebra2Map.setMapping("JtForearmTwistARt", "r_forearm1")
    zebra2Map.setMapping("JtForearmTwistBRt", "r_forearm2")
    zebra2Map.setMapping("JtWristRt", "r_wrist")
    zebra2Map.setMapping("JtThumbARt", "r_thumb1")
    zebra2Map.setMapping("JtThumbBRt", "r_thumb2")
    zebra2Map.setMapping("JtThumbCRt", "r_thumb3")
    zebra2Map.setMapping("JtThumbDRt", "r_thumb4")
    zebra2Map.setMapping("JtIndexARt", "r_index1")
    zebra2Map.setMapping("JtIndexBRt", "r_index2")
    zebra2Map.setMapping("JtIndexCRt", "r_index3")
    zebra2Map.setMapping("JtIndexDRt", "r_index4")
    zebra2Map.setMapping("JtMiddleARt", "r_middle1")
    zebra2Map.setMapping("JtMiddleBRt", "r_middle2")
    zebra2Map.setMapping("JtMiddleCRt", "r_middle3")
    zebra2Map.setMapping("JtMiddleDRt", "r_middle4")
    zebra2Map.setMapping("JtRingARt", "r_ring1")
    zebra2Map.setMapping("JtRingBRt", "r_ring2")
    zebra2Map.setMapping("JtRingCRt", "r_ring3")
    zebra2Map.setMapping("JtRingDRt", "r_ring4")
    zebra2Map.setMapping("JtLittleARt", "r_pinky1")
    zebra2Map.setMapping("JtLittleBRt", "r_pinky2")
    zebra2Map.setMapping("JtLittleCRt", "r_pinky3")
    zebra2Map.setMapping("JtLittleDRt", "r_pinky4")

    # Leg, left
    zebra2Map.setMapping("JtHipLf", "l_hip")
    zebra2Map.setMapping("JtKneeLf", "l_knee")
    zebra2Map.setMapping("JtAnkleLf", "l_ankle")
    zebra2Map.setMapping("JtBallLf", "l_forefoot")
    zebra2Map.setMapping("JtToeLf", "l_toe")

    # Leg, right
    zebra2Map.setMapping("JtHipRt", "r_hip")
    zebra2Map.setMapping("JtKneeRt", "r_knee")
    zebra2Map.setMapping("JtAnkleRt", "r_ankle")
    zebra2Map.setMapping("JtBallRt", "r_forefoot")
    zebra2Map.setMapping("JtToeRt", "r_toe")

    # Head, left
    zebra2Map.setMapping("JtEyeLf", "eyeball_left")
    zebra2Map.setMapping("JtEyelidUpperLf", "upper_eyelid_left")
    zebra2Map.setMapping("JtEyelidLowerLf", "lower_eyelid_left")

    # Head, right
    zebra2Map.setMapping("JtEyeRt", "eyeball_right")
    zebra2Map.setMapping("JtEyelidUpperRt", "upper_eyelid_right")
    zebra2Map.setMapping("JtEyelidLowerRt", "lower_eyelid_right")

    #zebra2Map.setMapping("eyeJoint_R", "eyeball_right")
    #zebra2Map.setMapping("eyeJoint_L", "eyeball_left")



Zebra2map()
