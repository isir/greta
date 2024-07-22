using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace NVBG
{
    public class BackChannelMessage
    {
        // member variables
        private string m_msgID;
        private string m_utteranceID;
        private string m_agreement;
        private string m_speaker;
        private string m_polarity;
        private int m_progress;
        private bool m_isComplete;
        private string m_partialText;

        // properties
        public string MsgID { get { return m_msgID; } set { m_msgID = value; } }
        public string UtteranceID { get { return m_utteranceID; } set { m_utteranceID = value; } }
        public string Agreement { get { return m_agreement; } set { m_agreement = value; } }
        public string Speaker { get { return m_speaker; } set { m_speaker = value; } }
        public string Polarity { get { return m_polarity; } set { m_polarity = value; } }
        public int Progress { get { return m_progress; } set { m_progress = value; } }
        public bool IsComplete { get { return m_isComplete; } set { m_isComplete = value; } }
        public string PartialText { get { return m_partialText; } set { m_partialText = value; } }

        // constructor
        public BackChannelMessage()
        {
            MsgID = "";
            UtteranceID = "";
            Agreement = "";
            Speaker = "";
            Polarity = "";
            Progress = 0;
            IsComplete = false;
            PartialText = "";
        }
    }
}
