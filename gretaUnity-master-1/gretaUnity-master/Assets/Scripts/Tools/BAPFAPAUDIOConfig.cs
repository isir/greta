using UnityEngine;

namespace BAPFAPAUDIOConfig
{
    public struct RotationMatrix
    {
        public float X1, X2, X3;
        public float Y1, Y2, Y3;
        public float Z1, Z2, Z3;
    }

    public struct Point3D
    {
        public static Point3D ZeroPoint = new Point3D(0f, 0f, 0f);
        private float x;
        private float y;
        private float z;
        public float X
        {
            get
            {
                return this.x;
            }
            set
            {
                this.x = value;
            }
        }
        public float Y
        {
            get
            {
                return this.y;
            }
            set
            {
                this.y = value;
            }
        }
        public float Z
        {
            get
            {
                return this.z;
            }
            set
            {
                this.z = value;
            }
        }
        public Point3D(float x, float y, float z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public enum BAPJoint
    {
        Invalid = 0,
        Sacrum = 1, // HumanoidRoot
        Pelvis = 2, // Sacroiliac
                    // Lower body
        LeftHip = 3,
        LeftKnee = 4,
        LeftAnkle = 5,
        LeftSubtalar = 6,
        LeftMidtarsal = 7,
        LeftMetatarsal = 8,
        LeftAnkleORIENT = 9,
        LeftAnkleTwist = 10,
        RightHip = 11,
        RightKnee = 12,
        RightAnkle = 13,
        RightSubtalar = 14,
        RightMidtarsal = 15,
        RightMetatarsal = 16,
        RightAnkleORIENT = 17,
        RightAnkleTwist = 18,
        // Upper body
        VL5 = 19,
        VL4 = 20,
        VL3 = 21,
        VL2 = 22,
        VL1 = 23,
        VT12 = 24,
        VT11 = 25,
        VT10 = 26,
        VT9 = 27,
        VT8 = 28,
        VT7 = 29,
        VT6 = 30,
        VT5 = 31,
        VT4 = 32,
        VT3 = 33,
        VT2 = 34,
        VT1 = 35,
        LeftClavicle = 36, // Left sternoclavicular
        LeftScapula = 37, // Left acromioclavicular
        LeftShoulder = 38, // Left upperarm
        LeftElbow = 39, // Left forearm
        LeftWrist = 40, // Left hand
        LeftIndex0 = 41,
        LeftIndex1 = 42,
        LeftIndex2 = 43,
        LeftIndex3 = 44,
        LeftMiddle0 = 45,
        LeftMiddle1 = 46,
        LeftMiddle2 = 47,
        LeftMiddle3 = 48,
        LeftPinky0 = 49,
        LeftPinky1 = 50,
        LeftPinky2 = 51,
        LeftPinky3 = 52,
        LeftRing0 = 53,
        LeftRing1 = 54,
        LeftRing2 = 55,
        LeftRing3 = 56,
        LeftThumb1 = 57,
        LeftThumb2 = 58,
        LeftThumb3 = 59,
        RightClavicle = 60, // Right sternoclavicular
        RightScapula = 61, // Right acromioclavicular
        RightShoulder = 62, // Right upperarm
        RightElbow = 63, // Right forearm
        RightWrist = 64, // Right hand
        RightIndex0 = 65,
        RightIndex1 = 66,
        RightIndex2 = 67,
        RightIndex3 = 68,
        RightMiddle0 = 69,
        RightMiddle1 = 70,
        RightMiddle2 = 71,
        RightMiddle3 = 72,
        RightPinky0 = 73,
        RightPinky1 = 74,
        RightPinky2 = 75,
        RightPinky3 = 76,
        RightRing0 = 77,
        RightRing1 = 78,
        RightRing2 = 79,
        RightRing3 = 80,
        RightThumb1 = 81,
        RightThumb2 = 82,
        RightThumb3 = 83,
        // Head
        VC7 = 84,
        VC6 = 85,
        VC5 = 86,
        VC4 = 87,
        VC3 = 88,
        VC2 = 89,
        VC1 = 90,
        Skullbase = 91,
        LeftWristTwist1 = 92,
        LeftWristTwist2 = 93,
        RightWristTwist1 = 94,
        RightWristTwist2 = 95,
        // Autodesk
        LeftUpLegRoll = 96,
        LeftLegRoll = 97,
        RightUpLegRoll = 98,
        RightLegRoll = 99,
        LeftArmRoll = 100,
        LeftForeArmRoll = 101,
        LeftFingerBase = 102,
        RightArmRoll = 103,
        RightForeArmRoll = 104,
        RightFingerBase = 105
    }

    public enum FAPJoint
    {
        Bone0 = 0,
        Bone21 = 1,
        Bone22 = 2,
        Bone23 = 3,
        Bone210 = 4,
        Bone31 = 5,
        Bone32 = 6,
        Bone33 = 7,
        Bone34 = 8,
        Bone35 = 9,
        Bone36 = 10,
        Bone41 = 11,
        Bone42 = 12,
        Bone43 = 13,
        Bone44 = 14,
        Bone45 = 15,
        Bone46 = 16,
        Bone51 = 17,
        Bone52 = 18,
        Bone53 = 19,
        Bone54 = 20,
        Bone81 = 21,
        Bone82 = 22,
        Bone83 = 23,
        Bone84 = 24,
        Bone91 = 25,
        Bone92 = 26,
        // Autodesk
        BrowInnerL = 27,
        BrowInnerR = 28,
        BrowOuterL = 29,
        BrowOuterR = 30,
        CheekL = 31,
        CheekR = 32,
        LipLowerL = 33,
        LipLowerR = 34,
        LeftEye = 35,
        LipCornerL = 36,
        LipCornerR = 37,
        LipUpperL = 38,
        LipUpperR = 39,
        LowerLidL = 40,
        LowerLidR = 41,
        Nostrils = 42,
        RightEye = 43,
        UpperLidL = 44,
        UpperLidR = 45
    }

    public struct BAPJointTransformation
    {
        private BAPJointPosition position;
        private BAPJointOrientation orientation;

        public BAPJointPosition Position
        {
            get
            {
                return this.position;
            }
            set
            {
                this.position = value;
            }
        }

        public BAPJointOrientation Orientation
        {
            get
            {
                return this.orientation;
            }
            set
            {
                this.orientation = value;
            }
        }
    }

    public struct BAPJointPosition
    {
        private Point3D position;
        private float confidence;
        public Point3D Position
        {
            get
            {
                return this.position;
            }
            set
            {
                this.position = value;
            }
        }
        public float Confidence
        {
            get
            {
                return this.confidence;
            }
            set
            {
                this.confidence = value;
            }
        }
    }

    public struct BAPJointOrientation
    {
        public Quaternion rotation;
    }

    public struct jointValueList
    {
        public Vector3[] list;
        public long id;
        public Quaternion headRotation;
        public Vector3 rootpos;
    }

    public struct AUDIOList
    {
        public string name;
        public long id;
        public byte[] rawData;
        public float[] data;
        public int sample, frequency, channels, sampleRate;
        public float length;
        public bool isBigEndian;
        public long frameNumber;
    }

    public struct BAPJointRotationImpulsion
    {
        private Vector3 rotation;
        private BAPJoint joint;

        public Vector3 Rotation
        {
            get
            {
                return this.rotation;
            }
            set
            {
                this.rotation = value;
            }
        }

        public BAPJoint Joint
        {
            get
            {
                return this.joint;
            }
            set
            {
                this.joint = value;
            }
        }
    }
}
