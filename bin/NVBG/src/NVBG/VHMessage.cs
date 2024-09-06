using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace NVBG
{
    public class VHMessage
    {
        // member variables
        public string m_type;
        public string m_agentId;
        public string m_target;
        public string m_msgId;
        public string m_inputXml;
        public string m_operation;
        public string m_MessageStr;

        // properties
        public string Type { get { return m_type; } set { m_type = value; } }
        public string AgentId { get { return m_agentId; } set { m_agentId = value; } }
        public string Target { get { return m_target; } set { m_target = value; } }
        public string MsgId { get { return m_msgId; } set { m_msgId = value; } }
        public string InputXml { get { return m_inputXml; } set { m_inputXml = value; } }
        public string Operation { get { return m_operation; } set { m_operation = value; } }
        public string MessageString { get { return m_MessageStr; } set { m_MessageStr = value; } }

        // constructor
        public VHMessage()
        {
            Type = "";
            AgentId = "";
            Target = "";
            MsgId = "";
            InputXml = "";
            Operation = "";
            MessageString = "";
        }
    }
}
