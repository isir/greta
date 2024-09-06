using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace NVBG
{
    public class Dialogue
    {
        // member variables
        private string m_speaker;
        private string m_listener;

        // properties
        public string Speaker { get { return m_speaker; } set { m_speaker = value; } }
        public string Listener { get { return m_listener; } set { m_listener = value; } }

        // constructor
        public Dialogue()
        {
            Speaker = "none";
            Listener = "none";
        }
    }
}
