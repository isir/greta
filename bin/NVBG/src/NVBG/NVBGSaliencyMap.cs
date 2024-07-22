using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace NVBG
{
    // storing the data for connecting saliency map and GUI controls
    class NVBGSaliencyMap
    {
        public static SaliencyMapParameterDelegate m_callback = null;
        public static StringParameterDelegate m_storyPointCallBack = null;
        public static StringParameterDelegate m_emotionCallBack = null;

        public static bool m_idleGazeThresholdNumTypeNum = true;
        public static int m_idleGazeThresholdNum = 5;
        public static float m_idleGazeThresholdValue = 20;
        public static string m_priorityFormula = "P-R";
        public static int m_idleGazePeriod = 10;
        public static float m_recencyAfterGaze = 20;
        public static string m_jointRange = "EYES NECK";
        public static bool m_keywordGazeThresholdTypeNum = true;
        public static int m_keywordGazeThresholdNum = 10;
        public static float m_keywordGazeThresholdValue = 20;
        public static int m_bias = 0;
        public static string m_storyPoint = "RangerEnters1";

        // for joint range
        public static bool m_fixedJointRange = true; 
        public static bool m_jointRangeThresholdTypeNum = true;
        public static int[] m_jointRangeThresholdNum = new int[4] {5,1,0,0};
        public static float[] m_jointRangeThresholdValue = new float[4] {-5,0,10,10};
        public static float[] m_jointRangeRate = new float[4] { 0.5f, 0.5f, 0.25f, 0.6f };
        public static float m_currentJointRangeRate = 0.5f;

        // for gaze limits
        public static int[] m_jointPitchUp = new int[4] { -7, -30, -6, -15 };
        public static int[] m_jointPitchDown = new int[4] { 10, 30, 6, 15 };
        public static int[] m_jointHeading = new int[4] { 14, 60, 15, 30 };
        public static int[] m_jointRoll = new int[4] { 0, 35, 5, 10 };

        public static int m_saccadeMagnitude = 2;
        public static float m_saccadePeriod = 0.21f;
        public static bool m_useSaccade = false;
        public static float m_saccadeFixationPeriod = 3.0f;
        public static float m_saccadeFixationLength = 1.5f;

        // for personality models
        public static float[] m_gazePeriodPer = new float[4] { 10.0f, 10.0f, 5.0f, 3.0f };
        // the first dimension is personality, the second dimension is joint name
        // currently only have the eyes and neck joint personalized values, so the second dimension has two values, corresponding to eyes and neck
        public static int[,] m_jointPitchUpPer = new int[,] { { -16, -20 }, { -10, -20 }, { -5, -14 }, { -16, -30 } };
        public static int[,] m_jointPitchDownPer = new int[,] { { 20, 30 }, { 10, 30 }, { 5, 20 }, { 20, 30 } };
        public static int[,] m_jointHeadingPer = new int[,] { { 30, 50 }, { 20, 50 }, { 10, 31 }, { 20, 60 } };
        public static int[,] m_jointRollPer = new int[,] { { 0, 35 }, { 0, 30 }, { 0, 18 }, { 0, 35 } };
        
        public static int[] m_saccadeMagnitudePer = new int[4] { 2, 2, 2, 3 };
        public static float[] m_saccadePeriodPer = new float[4] { 0.21f, 0.21f, 0.39f, 0.19f };
        public static bool m_showSaccadeMessage = true;

        public enum jointName
        { 
            eyes = 0,
            neck,
            chest,
            back
        }

        public enum jointRange
        { 
            eye = 0,
            eyeNeck,
            eyeChest,
            eyeBack,
            num
        }

        public enum personality
        { 
            woman = 0,
            man,
            old,
            child
        }

        // update saliency map table GUI
        public static void SaliencyMapView(List<SaliencyItem> _salMap, int _randRange, int _keywordRange)
        {
            if (m_callback != null)
            {
                m_callback(_salMap, _randRange, _keywordRange);
            }
        }

        // update story point GUI
        public static void StoryPointView(string _storyPoint)
        {
            if (m_storyPointCallBack != null)
            {
                m_storyPointCallBack(_storyPoint);
            }
        }

        // update emotion GUI
        public static void EmotionView(string _emotion)
        {
            if (m_emotionCallBack != null)
            {
                m_emotionCallBack(_emotion);
            }
        }
    }
}
