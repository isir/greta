using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace NVBG
{
    public class ConversationInfo
    {
        // member variables
        public List<string> m_participants;
        private string m_speaker;
        private string m_addressee;
        public List<string> m_sideParticipants;
        public List<string> m_bystanders;
        public List<string> m_eavesdroppers;
        public List<string> m_overhearer;
        private string m_lastMyExpressId;

        // properties
        public string Speaker { get { return m_speaker; } set { m_speaker = value; } }
        public string Addressee { get { return m_addressee; } set { m_addressee = value; } }
        public string LastMyExpressId { get { return m_lastMyExpressId; } set { m_lastMyExpressId = value; } }

        // constructor
        public ConversationInfo()
        {
            m_participants = new List<string>();
            m_speaker = "";
            m_addressee = "";
            m_sideParticipants = new List<string>();
            m_bystanders = new List<string>();
            m_eavesdroppers = new List<string>();
            m_overhearer = new List<string>();
            m_lastMyExpressId = "";
        }
    }
}
