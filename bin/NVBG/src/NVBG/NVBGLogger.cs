using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Windows.Forms;
using System.IO;
using System.Diagnostics;


namespace NVBG
{    
    // Logger used for printing messages to GUI or to the console
    class NVBGLogger
    {
        public static StringParameterDelegate m_callback = null;

        public static void Log(string _logString)
        {
			if (NVBGForm.writelog){
			using (StreamWriter w = new StreamWriter(new FileStream("log.txt", FileMode.Append)))
	      		{
	        	w.WriteLine("  :{0}", _logString);
				}
			}

            Trace.WriteLine(_logString);

            //if (NVBGManager.m_vhmsg != null) NVBGManager.m_vhmsg.SendMessage("NVBG: " + Thread.CurrentThread.Name + " - " + _logString);

            _logString = "\n" + _logString + "\n";

            if (m_callback != null)
                m_callback(_logString);
            else
                System.Console.Write("\n" + _logString + "\n");
        }
    }
}
