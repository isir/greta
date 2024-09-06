using System;
using System.Collections.Generic;
using System.Collections;
using System.Linq;
using System.Text;

namespace NVBG
{
    public class CharacterInfo
    {
        // member variables
        private string m_name;
        private string m_emotion;
        private string m_posture;
        private string m_personality;
        private string m_negotiationStance;
        private string m_conversationRole;
        private string m_participationGoal;
        private string m_comprehensionGoal;
        private string m_participationStatus;
        private string m_comprehensionStatus;
        private string m_culture;
        private string m_status;
        private string m_role;
        private bool m_hasSpoken;
        public Hashtable interPerRelation;

        // properties
        public string Name { get { return m_name; } set { m_name = value; } }
        public string Emotion { get { return m_emotion; } set { m_emotion = value; } }
        public string Posture { get { return m_posture; } set { m_posture = value; } }
        public string Personality { get { return m_personality; } set { m_personality = value; } }
        public string NegotiationStance { get { return m_negotiationStance; } set { m_negotiationStance = value; } }
        public string ConversationRole { get { return m_conversationRole; } set { m_conversationRole = value; } }
        public string ParticipationGoal { get { return m_participationGoal; } set { m_participationGoal = value; } }
        public string ComprehensionGoal { get { return m_comprehensionGoal; } set { m_comprehensionGoal = value; } }
        public string ParticipationStatus { get { return m_participationStatus; } set { m_participationStatus = value; } }
        public string ComprehensionStatus { get { return m_comprehensionStatus; } set { m_comprehensionStatus = value; } }
        public string Culture { get { return m_culture; } set { m_culture = value; } }
        public string Status { get { return m_status; } set { m_status = value; } }
        public string Role { get { return m_role; } set { m_role = value; } }
        public bool HasSpoken { get { return m_hasSpoken; } set { m_hasSpoken = value; } }

        // constructor
        public CharacterInfo()
        {
            Name = "brad";
            Emotion = "neutral";
            Posture = "HandsAtSide";
            Personality = "";
            NegotiationStance = "none";
            ConversationRole = "";
            ParticipationGoal = "0";
            ComprehensionGoal = "0";
            ParticipationStatus = "0";
            ComprehensionStatus = "0";
            interPerRelation = new Hashtable();
            HasSpoken = false;
            Status = "present";
            Role = "overhearer";
            Culture = "general";
        }
    }
}
