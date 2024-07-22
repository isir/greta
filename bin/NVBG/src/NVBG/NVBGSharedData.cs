using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace NVBG
{
    public class NVBGSharedData
    {

        // member variables
        public AgentInfo m_agentInfo;
        public GazeInfo m_gazeInfo;
        public ConversationInfo m_conversationInfo;
        public Dialogue m_currentDialogue;
        public string m_projectName;

        // properties
        public AgentInfo AgentInfo { get { return m_agentInfo; } }
        public GazeInfo GazeInfo { get { return m_gazeInfo; } }
        public ConversationInfo ConversationInfo { get { return m_conversationInfo; } }
        public Dialogue CurrentDialogue { get { return m_currentDialogue; } }
        public string ProjectName { get { return m_projectName; } set { m_projectName = value; } }

        // constructor
        public NVBGSharedData()
        {
            m_agentInfo = new AgentInfo();
            m_gazeInfo = new GazeInfo();
            m_conversationInfo = new ConversationInfo();
            m_currentDialogue = new Dialogue();
            m_projectName = "GENERAL";
        }
    }
}
