using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml;

namespace NVBG
{    
    public class GazeBehaviorManager
    {

        public GazeBehaviorManager()
        {
        }

        public void ProcessGazeMessage(XmlDocument _inputDoc, NVBGCharacter _data, VHMessage _currentMessage)
        {
            try
            {
                XmlNode gazeTag = _inputDoc.GetElementsByTagName("gaze")[0];
                string reason = gazeTag.InnerText;
                //string type = gazeTag.Attributes["type"].Value;
                string target = gazeTag.Attributes["target"].Value;

                _data.GazeInfo.GazeTarget = target;
                //XmlNodeList ruleNodes = _data.BehaviorFile.GetElementsByTagName("gazereason");
                XmlNode newGazeTag = _inputDoc.CreateElement("gazereason");
                XMLHelperMethods.AttachAttributeToNode(_inputDoc, newGazeTag, "participant", _data.AgentInfo.Name);
                XMLHelperMethods.AttachAttributeToNode(_inputDoc, newGazeTag, "type", reason);
                XMLHelperMethods.AttachAttributeToNode(_inputDoc, newGazeTag, "priority", "4");
                XMLHelperMethods.AttachAttributeToNode(_inputDoc, newGazeTag, "prev_target", _data.GazeInfo.PreviousTarget);
                XMLHelperMethods.AttachAttributeToNode(_inputDoc, newGazeTag, "target", _data.GazeInfo.GazeTarget);
                _inputDoc.GetElementsByTagName("bml")[0].AppendChild(newGazeTag);
                _data.GazeInfo.PreviousTarget = _data.GazeInfo.GazeTarget;
            }
            catch (Exception e)
            {
                NVBGLogger.Log("ERROR while processing gaze message" + e.ToString());
            }
        }
        
    }
}
