using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace NVBG
{
    public class NegotiationInfo
    {
        // member variables
        private string m_polarity;
        private string m_target;

        // properties
        public string Polarity { get { return m_polarity; } set { m_polarity = value; } }
        public string Target { get { return m_target; } set { m_target = value; } }

        // constructor
        public NegotiationInfo()
        {
            Polarity = "";
            Target = "";
        }
    }
}
