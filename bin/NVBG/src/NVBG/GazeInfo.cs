using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml;

namespace NVBG
{
    public class GazeInfo
    {
        // member variables
        private string m_gazeReason;
        private string m_previousTarget;
        private string m_gazeTarget;
        private string m_gazeSpeed;
        private string m_gazeType;
        private string m_gazeTrack;

        // properties
        public string GazeReason { get { return m_gazeReason; } set { m_gazeReason = value; } }
        public string PreviousTarget { get { return m_previousTarget; } set { m_previousTarget = value; } }
        public string GazeTarget { get { return m_gazeTarget; } set { m_gazeTarget = value; } }
        public string GazeSpeed { get { return m_gazeSpeed; } set { m_gazeSpeed = value; } }
        public string GazeType { get { return m_gazeType; } set { m_gazeType = value; } }
        public string GazeTrack { get { return m_gazeTrack; } set { m_gazeTrack = value; } }

        // constructor
        public GazeInfo()
        {
            GazeReason = "none";
            PreviousTarget = "none";
            GazeTarget = "";
            GazeSpeed = "default";
            GazeType = "";
            GazeTrack = "";
        }

        // set gaze variables
        public void SetGaze(string _target, string _type, string _track)
        {
            GazeTarget = _target;
            GazeType = _type;
            GazeTrack = _track;
            GazeSpeed = "default";
            GazeReason = "none";
        }
    }
}
