using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml;


namespace NVBG
{
    public class VrExpressHandler
    {        
        private XmlDocument m_inputDoc;
        private VHMessage m_currentMessage;
        private NVBGCharacter m_data;
        private string m_spId;
        private SpeakerBehaviorManager m_speakerBehavior;
        private ListenerBehaviorManager m_listenerBehavior;
        private XmlNode m_bmlNode;
        private GazeBehaviorManager m_gazeBehavior;

        /// <summary>
        /// constructor
        /// </summary>
        public VrExpressHandler()
        {
            m_speakerBehavior = new SpeakerBehaviorManager();
            m_listenerBehavior = new ListenerBehaviorManager();
            m_gazeBehavior = new GazeBehaviorManager();
        }

        public void InitializeParseTreeCaching()
        {
            m_speakerBehavior.InitializeCaching();
        }

        /// <summary>
        /// Process vrExpress message based on type
        /// Determines the type of the message by checking the xml nodes in the input message
        /// Based on which type it is, it calls the correct method to process it
        /// </summary>
        /// <param name="_inputDoc"></param>
        /// <param name="_currentMessage"></param>
        /// <param name="_data"></param>
        public void ProcessMessage(XmlDocument _inputDoc, VHMessage _currentMessage, NVBGCharacter _data)
        {
            m_inputDoc = _inputDoc;
            m_currentMessage = _currentMessage;
            m_data = _data;

            
            ProcessFMLData();
            GetBmlNode();

            // Process messages by type
            if (m_inputDoc.GetElementsByTagName("speech").Count > 0)
            {
                ProcessSpeechData();
            }
            else if (m_inputDoc.GetElementsByTagName("listenerFeedback").Count > 0)
            {
                ProcessListenerFeedback();
            }
            else if (m_currentMessage.Type.Equals("vrBackchannel")) 
            {
                ProcessVRBackchannel();
            }
            else if (m_currentMessage.Type.Equals("vrBCFeedback")) 
            {
                ProcessVRBCFeedback();
            }
            else if (m_currentMessage.Type.Equals("vrNvbgFeedbackRuleTest")) 
            {
                ProcessVRNVBGFeedbackRuleTest();
            }
            else if (m_inputDoc.GetElementsByTagName("gaze").Count > 0) 
            {
                ProcessGazeMessage();
            }
            else if (m_inputDoc.GetElementsByTagName("negotiationStance").Count > 0) 
            {
                ProcessNegotiationMessage();
            }
            else if (m_inputDoc.GetElementsByTagName("face").Count > 0) 
            {
                m_currentMessage.Type = "facs";
            }            
            else if (m_inputDoc.GetElementsByTagName("body").Count > 0)
            {
                ProcessBodyMessage();
            }

        }
        

        /// <summary>
        /// Process posture changes based on body tag in input bml
        /// </summary>
        private void ProcessBodyMessage()
        {
            if (m_data.Switch.allBehaviour)
            {
                m_currentMessage.Type = "posture";
                if (m_currentMessage.AgentId.Equals(m_data.AgentInfo.Name))
                {
                    XmlNode bodyNode = m_inputDoc.GetElementsByTagName("body")[0];
                    m_data.AgentInfo.Posture = bodyNode.Attributes["posture"].Value;
                }
            }
        }        


        /// <summary>
        /// Process negotiation message and update posture
        /// </summary>
        private void ProcessNegotiationMessage()
        {
            if (m_data.Switch.allBehaviour)
            {
                m_currentMessage.Type = "negotiation";
                ChangePosture();
            }
        }

        /// <summary>
        /// Handle input gaze messages and create appropriate rules
        /// </summary>
        private void ProcessGazeMessage()
        {
            if (m_data.Switch.allBehaviour)
            {
                m_gazeBehavior.ProcessGazeMessage(m_inputDoc, m_data, m_currentMessage);
            }
        }

        /// <summary>
        /// Process vrNVBGFeedback messages
        /// </summary>
        private void ProcessVRNVBGFeedbackRuleTest()
        {
            if (m_data.Switch.allBehaviour)
            {
                m_listenerBehavior.ProcessVRNVBGFeedbackRuleTest(m_inputDoc, m_currentMessage);
            }
        }

        /// <summary>
        /// Process back channel messages
        /// </summary>
        private void ProcessVRBackchannel()
        {
            if (m_data.Switch.allBehaviour)
            {
                m_listenerBehavior.ProcessVRBackChannel(m_inputDoc, m_currentMessage, m_data);
            }
        }

        /// <summary>
        /// Process VRBCFeedback messages
        /// </summary>
        private void ProcessVRBCFeedback()
        {
            if (m_data.Switch.allBehaviour)
            {
                m_listenerBehavior.ProcessVRBCFeedback(m_inputDoc, m_currentMessage);
            }
        }

        /// <summary>
        /// Process listener feedback messages
        /// </summary>
        private void ProcessListenerFeedback()
        {
            if (m_data.Switch.allBehaviour)
            {
                m_currentMessage.Type = "listen";
                m_listenerBehavior.ProcessListenerFeedback(m_inputDoc, m_currentMessage);
            }
        }

        /// <summary>
        /// Update the agent posture
        /// </summary>
        private void ChangePosture()
        {
            if (m_data.Switch.allBehaviour)
            {
            }
        }


        /// <summary>
        /// Get bml node if it exists else create one
        /// </summary>
        private void GetBmlNode()
        {
            try
            {
                if (m_inputDoc.GetElementsByTagName("bml").Count > 0)
                {
                    m_bmlNode = m_inputDoc.GetElementsByTagName("bml")[0];
                }
                else
                {
                    m_bmlNode = m_inputDoc.CreateElement("bml");
                    m_inputDoc.GetElementsByTagName("act")[0].AppendChild(m_bmlNode);
                }
            }
            catch (Exception e)
            {
                NVBGLogger.Log("ERROR while trying to extract bml node from input vrExpress message" + e.ToString());
            }
        }

        /// <summary>
        /// Process the vrSpeak speech messages i.e. listen and dialog
        /// </summary>
        private void ProcessSpeechData()
        {
            NVBGLogger.Log("Processing Speech Data");
            XmlNode eventTag = m_inputDoc.CreateElement("sbm:event");
            try
            {                

                // Add the vrSpoke even to the bml This is necessary to receive feedback when the character finishes speaking
                if ((m_bmlNode.ChildNodes.Count > 0) && (m_bmlNode.ChildNodes[0].Name.Equals("speech")))
                {
                    XmlNode speechTag = m_bmlNode.ChildNodes[0];
                    string messageAttribute = "vrSpoke " + m_currentMessage.AgentId + " " + m_currentMessage.Target + " " + m_currentMessage.MsgId + " " + speechTag.InnerText;
                    XMLHelperMethods.AttachAttributeToNode(m_inputDoc, eventTag, "message", messageAttribute);
                    XMLHelperMethods.AttachAttributeToNode(m_inputDoc, eventTag, "xmlns:sbm", "http://ict.usc.edu");

                    if (!speechTag.Attributes["id"].Value.Equals(""))
                    {
                        m_spId = speechTag.Attributes["id"].Value;
                    }
                }
            }
            catch (Exception e)
            {
                NVBGLogger.Log("Error while processing speech data :ERROR: " + e.ToString());
            }

                // If message type is not listen, then it is speaker type so the SpeakerBehavior object will process the message
                if (!m_currentMessage.Type.Equals("listen"))
                {
                    NVBGLogger.Log("Processing dialog messge");
                    m_currentMessage.Type = "dialogue";
                    m_speakerBehavior.ProcessDialogMessage(m_inputDoc, m_data, m_currentMessage, m_bmlNode);
                    m_data.AgentInfo.HasSpoken = true;
                    XMLHelperMethods.AttachAttributeToNode(m_inputDoc, eventTag, "stroke", m_spId + ":relax");

                    m_bmlNode.AppendChild(eventTag);
                }
                // If message type is not speak, then it is listen type so the ListenerBehavior object will process the message
                else if ((m_currentMessage.Type.Equals("listen")) && (m_data.Switch.allBehaviour) && (m_data.Switch.listenerGaze))
                {
                    NVBGLogger.Log("Generating listener behavior");
                    m_listenerBehavior.ProcessListenMessage(m_inputDoc, m_currentMessage, m_data);
                    m_currentMessage.AgentId = m_data.AgentInfo.Name;
                    m_currentMessage.Target = m_data.CurrentDialogue.Speaker;
                    XMLHelperMethods.AttachAttributeToNode(m_inputDoc, eventTag, "stroke", m_spId + ":relax");
                }           

        }


        /// <summary>
        /// Process the FML data and update internal structures
        /// OBSOLETE
        /// </summary>
        private void ProcessFMLData()
        {
            NVBGLogger.Log("Processing fml data");

            XmlNodeList affectList = m_inputDoc.GetElementsByTagName("affect");
            if (affectList.Count > 0)
            {
                ProcessFMLAffect(affectList);  
            }
            XmlNodeList statusList = m_inputDoc.GetElementsByTagName("status");
            if (statusList.Count > 0)
            {
                ProcessFMLStatus(statusList);
            }
            XmlNodeList requestList = m_inputDoc.GetElementsByTagName("request");
            if (requestList.Count > 0)
            {
                ProcessFMLRequests(requestList);  
            }
            XmlNodeList saliencyList = m_inputDoc.GetElementsByTagName("saliency");
            if (saliencyList.Count > 0)
            {
                ProcessFMLSaliency(saliencyList);
            }
            
        }

        /// <summary>
        /// Proces affect tags in fml
        /// OBSOLETE
        /// </summary>
        /// <param name="affectList"></param>
        private void ProcessFMLAffect(XmlNodeList affectList)
        {
            if (m_currentMessage.AgentId.Equals(m_data.AgentInfo.Name))
            {
                try
                {
                    m_data.AgentInfo.Emotion = affectList[0].Attributes["type"].Value;
                }
                catch (Exception e)
                {
                    NVBGLogger.Log("No type attribute in fml affect tag ERROR: " + e.ToString());
                }
            }
        }

        /// <summary>
        /// Process status tags in fml
        /// OBSOLETE
        /// </summary>
        /// <param name="statusList"></param>
        private void ProcessFMLStatus(XmlNodeList statusList)
        {
            if (m_currentMessage.AgentId.Equals(m_data.AgentInfo.Name))
            {
                try
                {
                    m_data.AgentInfo.Status = statusList[0].Attributes["type"].Value;
                }
                catch (Exception e)
                {
                    NVBGLogger.Log("No type attribute in fml status tag ERROR: " + e.ToString());
                }


                if (m_data.AgentInfo.Status.Equals("present"))
                {
                    m_data.Switch.allBehaviour = true;
                }
                if (m_data.AgentInfo.Status.Equals("absent") || m_data.AgentInfo.Status.Equals("incapacitated"))
                {
                    m_data.Switch.allBehaviour = false;
                }
            }
        }

        /// <summary>
        /// Process request tags in fml
        /// OBSOLETE
        /// </summary>
        /// <param name="requestList"></param>
        private void ProcessFMLRequests(XmlNodeList requestList)
        {
            if (m_currentMessage.AgentId.Equals(m_data.AgentInfo.Name))
            {
                for (int i = 0; i < requestList.Count; ++i)
                {
                    try
                    {
                        if (requestList[i].Attributes["type"].Value.Equals("idlebehavior"))
                        {
                            if (requestList[i].Attributes["value"].Value.Equals("on"))
                            {
                                m_data.Switch.saliencyIdleGaze = true;
                            }
                            if (requestList[i].Attributes["value"].Value.Equals("off"))
                            {
                                m_data.Switch.saliencyIdleGaze = false;
                            }
                        }
                        if (requestList[i].Attributes["type"].Value.Equals("behavior"))
                        {
                            if (requestList[i].Attributes["value"].Value.Equals("on"))
                            {
                                m_data.Switch.allBehaviour = true;
                            }
                            if (requestList[i].Attributes["value"].Value.Equals("off"))
                            {
                                m_data.Switch.allBehaviour = false;
                            }
                        }
                    }
                    catch(Exception e)
                    {
                        NVBGLogger.Log("Error while processing fml request tag ERROR: " + e.ToString());
                    }
                }
            }  
        }

        /// <summary>
        /// Update the story point in the saliency map
        /// </summary>
        /// <param name="saliencyList"></param>
        private void ProcessFMLSaliency(XmlNodeList saliencyList)
        {
            if (m_currentMessage.AgentId.Equals(m_data.AgentInfo.Name))
            {
                try
                {
                    for (int i = 0; i < saliencyList.Count; ++i)
                    {
                        if (saliencyList[i].Attributes["type"].Value.Equals("story-point"))
                        {
                            NVBGSaliencyMap.m_storyPoint = saliencyList[i].Attributes["value"].Value;
                        }
                    }
                }
                catch (Exception e)
                {
                    NVBGLogger.Log("Error while processing fml request tag ERROR: " + e.ToString());
                }
            }
        }
    }
}

