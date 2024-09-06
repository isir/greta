using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Forms;


namespace NVBG
{
    static class Program
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main(string[] args)
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            List<string> commandLineArguments = new List<string>(args);
            Console.WriteLine("INFO " + commandLineArguments);
            commandLineArguments.Add("Brad Brad.ini");
            Application.Run(new NVBGForm(commandLineArguments));


        }
    }
}

