using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Xml;
using System.Xml.Xsl;
using System.Threading;
using System.Xml.XPath;
using System.Windows.Forms;
using System.Timers;
using System.Xml.Schema;



namespace NVBG
{
    public class NVBGManager
    {
        /// <summary>
        /// vhmsg object
        /// </summary>
        public static VHMsg.Client m_vhmsg = null;
        private bool m_writeOutputToFile = false;
        /// <summary>
        /// specifies whether to use the default data path or whether to use the one specified by the user
        /// </summary>
        private bool m_useCommonDataPath = true;       
        /// <summary>
        /// object that handles vrExpress messages
        /// </summary>
        private VrExpressHandler m_vrExpressHandler;
        /// <summary>
        /// folder that contains the .xml and .xsl files
        /// </summary>
        public static string m_dataFolderName = @"../data/nvbg-toolkit/";
        public static string m_expressionFileName = "NVBG_face_expressions.xml";
        /// <summary>
        /// path to the .xsl rule transform file
        /// </summary>
        private string m_transformXsl;     
   
        private XslCompiledTransform m_xslTransform;        
        /// <summary>
        /// contains the xml that was received as a message by NVBG
        /// </summary>
        public static XmlDocument m_inputXmlDocument;
        /// <summary>
        /// contains the list of messages that were received by NVBG
        /// </summary>
        private List<VHMessage> m_messageQueue;

        /// <summary>
        /// flag indicating whether NVBG is currently active and processing messages or ignoring messages
        /// </summary>
        public bool m_isDisabled = false;

        /// <summary>
        /// character name to class map
        /// </summary>
        public  Dictionary<string, NVBGCharacter> m_characters;

        public static Object m_parserLock;
        public static Object m_switchLock;
        public static Object m_handlerLock;
        public static Object m_lockVhMSG;        

        /// <summary>
        /// holds the final processed xml file to be sent out as a vrSpeak message
        /// </summary>
        private XmlDocument m_finalDocument;

        /// <summary>
        /// not being used anymore
        /// </summary>
        private int m_timerMillisec;
        private const int m_fpsLimit = 100;

        /// <summary>
        /// idle animation. Currently not being used.
        /// </summary>
        private const int m_idleAnimationTime = 1; //10;


        private int m_fileNameId = 0;
        private string m_writeToFilePath = @"./";

        /// <summary>
        /// file to cache parser results in
        /// </summary>
        public static string m_parseCacheFile = "nvbg_parse_trees.txt";
        public static bool m_CacheFileSpecified = false;

        /// <summary>
        ///  indicate whether in a conversationi or not, if yes, disable the idle gaze generation
        /// </summary>
        private bool m_generateGaze = true; 

        bool m_activeMQClosed = false;

        /// <summary>
        /// delegates for callbacks
        /// </summary>
        public static MessageHandlerDelegate m_callbackHandler;
        public static NVBGSetOptionCallback m_optionCallback;    
        public delegate void CloseAMQCallBack();
        public static IdleTimerValueChangeCallback m_idleTimerValueChangeHandler;
        public static IdleTimerEnableCallback m_idleTimerEnableHandler;
        public static SaccadeCheckBox m_saccadeInitializeHandler;        
        public static GUILabelUpdate m_GUILabelUpdateHandler;
        public static RefreshGUI m_refreshGUIHandler;

        /// <summary>
        /// The story point is read from the config file and is used in the saliency map
        /// </summary>
        private string storyPoint = null;
        public static bool m_isHelp = false;

        bool m_Processing = false;
        private SaliencyMap m_saliencyMap;

        private IniParser m_configFile;

        System.Timers.Timer m_messageTimer;

        private string m_language = "FR";

        // Encoding for communication between NVBG and Greta
        public System.Text.Encoding m_encoding = System.Text.Encoding.UTF8;
        // public System.Text.Encoding encoding = System.Text.Encoding.Unicode;
        // public System.Text.Encoding encoding = System.Text.Encoding.GetEncoding("ISO-8859-1");
        // public System.Text.Encoding encoding = System.Text.Encoding.GetEncoding("1252");

        // Encoding for communication between NVBG and charniak perser (since charniak perser is built in UTF16)
        // public System.Text.Encoding encoding_parser = System.Text.Encoding.Unicode; //UTF16

        /// <summary>
        /// Constructor that takes in parameters passed in from the command line
        /// </summary>
        /// <param name="args"></param>
        public NVBGManager()
        {
            if (string.IsNullOrEmpty(Thread.CurrentThread.Name))
                Thread.CurrentThread.Name = "NVBGManager";

            m_xslTransform = new XslCompiledTransform();
            //m_ruleInputXmlDocument = new XmlDocument();
            m_messageQueue = new List<VHMessage>();
            m_inputXmlDocument = new XmlDocument();
            NVBGManager.m_optionCallback = new NVBGSetOptionCallback(NVBGSetOptions);            
            m_characters = new Dictionary<string, NVBGCharacter>();

            //m_data = new NVBGCharacter();

            m_parserLock = new Object();
            m_switchLock = new Object();
            m_lockVhMSG = new Object();
            m_finalDocument = new XmlDocument();            

            // initialize member variables        
            m_saliencyMap = new SaliencyMap();
            m_vrExpressHandler = new VrExpressHandler(m_language, m_encoding);                       
            
            

            m_Processing = false;            


            m_messageTimer = new System.Timers.Timer();
            m_messageTimer.Elapsed += new ElapsedEventHandler(TimerHandleMessages);
            m_messageTimer.Interval = 100;
            m_messageTimer.AutoReset = false;
            m_messageTimer.Start();
        }             


        /// <summary>
        /// Initialize caching of parse trees
        /// </summary>
        private void InitializeParseTreeCaching()
        {
            m_vrExpressHandler.InitializeParseTreeCaching();
        }

        







        /// <summary>
        /// Loads the XSL file. The XSL file is present in the data folder and basically
        /// takes the xml generated by NVBG and converts it to the final vrSpeak BML.
        /// The XSL transform is where a generic rule is converted to an animation or gaze
        /// </summary>
        private void LoadXSL()
        {
            try
            {
                m_xslTransform.Load(m_transformXsl);
                NVBGLogger.Log("Transform file being used is : " + m_transformXsl);
            }
            catch (Exception e)
            {
                NVBGLogger.Log("Error while loading XSL files: ERROR : " + e.ToString());
            }
        }

        /// <summary>
        /// Checks if the message for idle animation needs to be generated based on whether
        /// the character has been idle for specified time and if yes, then generate the message
        /// </summary>
        public void CheckForIdle(object sender, EventArgs eArgs)
        {

            if (m_isDisabled)
                return;

            foreach (KeyValuePair<string, NVBGCharacter> entry in m_characters)
            {
                if (sender == entry.Value.IdleTimer)
                {
                    if (entry.Value.Switch.allBehaviour && entry.Value.Switch.saliencyIdleGaze)
                    {
                        VHMessage idleMessage = new VHMessage();
                        idleMessage.AgentId = entry.Value.AgentInfo.Name;
                        idleMessage.Target = "all";
                        idleMessage.Type = "idleBehavior";
                        idleMessage.MsgId = "idle-";
                        idleMessage.MsgId += System.DateTime.Now.Second + new Random().Next();
                        idleMessage.MsgId += "-";
                        idleMessage.MsgId += System.DateTime.Now.Millisecond;
                        idleMessage.MessageString = "Idle Message for character " + entry.Key+"\n";
                        InsertXMLSkeleton(idleMessage);

                        lock (m_lockVhMSG)
                        {
                            if (!m_Processing)
                            {
                                m_messageQueue.Add(idleMessage);
                                //m_callbackHandler();
                            }
                        }
                    }
                }
            }
        }



        public void SetIdleTimerInterval(string _characterName, float _interval)
        {            
            //m_idleTimer.Stop();

            if (!m_characters.ContainsKey(_characterName))
                return;

            m_characters[_characterName].IdleTimer.Interval = Convert.ToInt32(_interval * 1000);            
            //m_idleTimer.Enabled = false;
            m_characters[_characterName].IdleTimer.Enabled = false;
            m_characters[_characterName].IdleTimer.Enabled = true;
            m_characters[_characterName].IdleTimer.Start();
            //NVBGSaliencyMap.m_idleGazePeriod = Convert.ToInt32(_interval);
        }

        public void SetIdleTimerEnable(string _characterName, bool _enable)
        {
            if (!m_characters.ContainsKey(_characterName))
                return;

            m_characters[_characterName].IdleTimer.Enabled = _enable;
        }

        /// <summary>
        /// Initialize all components
        /// </summary>
        public void Initialize()
        {
            if (!m_isHelp)
            {
                InitializeParseTreeCaching();
                InitializeActiveMQ();
                //CheckRuleInputFile();
                //LoadXMLandXSL();
                CheckRuleInputFile();
                LoadXSL();
                m_timerMillisec = System.DateTime.Now.Millisecond;
                m_saliencyMap.init(m_dataFolderName, m_useCommonDataPath, storyPoint);
            }
        }


        public void TimerHandleMessages(object source, ElapsedEventArgs e)
        {
            //HandleMessage();
            ReactToInputMessage();
            //m_callbackHandler();

            m_messageTimer.Start();  // manually restart the timer, since we don't want overlapping events to fire
        }


        /// <summary>
        /// handles the messages in the message queue
        /// This method picks a message out of the message queue, sends it for processing
        /// and then on receiving the final BML, it sends it out as a vrSpeak message.
        /// If the write option is turned on, it writes the bml to a file.
        /// </summary>
        public void ReactToInputMessage()
        {
            {
                while (m_messageQueue.Count > 0)
                {
                    {
                        m_Processing = true;

                        VHMessage currentMessage;

                        lock (m_lockVhMSG)
                        {
                            currentMessage = m_messageQueue[0];
                            m_messageQueue.Remove(m_messageQueue[0]);
                        }

                        NVBGLogger.Log("***********************************************************************************");
                        NVBGLogger.Log("Processing message:" + currentMessage.MessageString + " - " + currentMessage.Type + " - " + currentMessage.MsgId + " - " + currentMessage.AgentId + " - " + m_messageQueue.Count);

                        ProcessMessage(currentMessage);

                        string outputMessage = ""; ;

                        try
                        {
                            StringWriter stringWriter = new StringWriter();
                            XmlTextWriter xmlWriter = new XmlTextWriter(stringWriter);
                            xmlWriter.Formatting = Formatting.Indented;
                            m_finalDocument.WriteTo(xmlWriter);

                            outputMessage = "vrSpeak " + currentMessage.AgentId + " " + currentMessage.Target;
                            outputMessage += " " + currentMessage.MsgId + " " + stringWriter.ToString();

                            if (!currentMessage.Type.Equals("idleBehavior")) 
                                CheckIfWriteToFile(stringWriter.ToString());
                        }
                        catch (Exception e)
                        {
                            NVBGLogger.Log("Error while converting output bml to string : ERROR : " + e.ToString());
                        }
                        

                        //string xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-16\"?>";
                        //string innerXml = outputMessage.Substring(outputMessage.IndexOf(xmlHeader) + xmlHeader.Length);
                        try
                        {
                            if ((m_finalDocument.GetElementsByTagName("bml")[0].ChildNodes.Count > 0) && (m_characters.ContainsKey(currentMessage.AgentId)))
                            {

                                outputMessage = m_vrExpressHandler.m_speakerBehavior.ReplaceSpecialChracters(outputMessage, true);

                                NVBGManager.m_vhmsg.SendMessage(outputMessage);
                                                            
                                NVBGLogger.Log(outputMessage);
                                // restarting the idle timer
                                if (m_generateGaze) // if not in conversation, reset the idle timer
                                {
                                    m_idleTimerEnableHandler(currentMessage.AgentId, false);
                                    m_idleTimerEnableHandler(currentMessage.AgentId, true);                                 
                                }
                            }

                        }
                        catch (Exception e)
                        {
                            NVBGLogger.Log(e.ToString());
                        }

                        m_Processing = false;

                    }
                }                
            }
                  
        }



        /// <summary>
        /// Checks if the output bml needs to be written out to a file and writes it if yes
        /// </summary>
        private void CheckIfWriteToFile(string _message)
        {
            if (m_writeOutputToFile)
            {
                ++m_fileNameId;
                string fileName = "utterance" + m_fileNameId + ".xml";
                fileName = m_writeToFilePath + fileName;

                try
                {
                    TextWriter writer = new StreamWriter(fileName);
                    writer.Write(_message);
                    writer.Close();
                }
                catch(Exception e)
                {
                    NVBGLogger.Log("Error while writing output bml to file : ERROR : " + e.ToString());
                }

            }
        }

        /// <summary>
        /// Shuts down ActiveMQ and informs the other components
        /// </summary>
        public void CloseActiveMQ()
        {
            try
            {
                lock (m_lockVhMSG)
                {                                        
                    m_vhmsg.SendMessage("vrProcEnd nvb");
                    m_vhmsg.CloseConnection();                                            
                }
            }
            catch(Exception e)
            {
                NVBGLogger.Log(e.ToString());
            }
        }

        /// <summary>
        /// Limit CPU usage
        /// </summary>
        private void LimitCPUUsage()
        {
            // code to limit the CPU usage as otherwise it takes up the complete CPU
            // Currently limiting to 60 frames per second. Might not be accurately 60 due to granularity issues.
            int timesincelastframe = System.DateTime.Now.Millisecond - m_timerMillisec;

            int ttW;
            ttW = (1000 / m_fpsLimit) - timesincelastframe;
            if (ttW > 0)
                Thread.Sleep(ttW);

            m_timerMillisec = System.DateTime.Now.Millisecond;
        }



        /// <summary>
        /// Populate the xml content of the message with a skeleton xml
        /// </summary>
        /// <param name="message"></param>
        public void InsertXMLSkeleton(VHMessage _message)
        {
            _message.InputXml = "";
            _message.InputXml += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n";
            _message.InputXml += "<act>\n";
            _message.InputXml += "<participant id=\"" + m_characters[_message.AgentId].AgentInfo.Name + "\" role=\"actor\" />\n";
            _message.InputXml += "<bml/>\n";	
            _message.InputXml += "</act>\n";
        }

        /// <summary>
        /// Generates idle behavior rules to be inserted into the output bml
        /// This method randomlygenerates an idle animation which is specified in the
        /// rule input file for that character, OR, it generates a gaze from the saliency map.
        /// The gazes are controlled by the saliency map and the pawns are specified in 
        /// the saliency map's xml file.
        /// </summary>
        private void CreateIdleBehavior(string _characterName)
        {
            try
            {
                XmlNodeList ruleNodes = m_characters[_characterName].BehaviorFile.GetElementsByTagName("rule");
                

                Random randomizer = new Random();
                int randomNumber = randomizer.Next() % 2;
                //int randomNumber = 1; // currently only generate idle gaze

                for (int i = 0; i < ruleNodes.Count; ++i)
                {
                    try
                    {
                        string keyWord = ruleNodes[i].Attributes["keyword"].Value;

                        if (randomNumber == 0 && keyWord.Equals("idle_animation"))
                        {
                            XmlNode ruleTag = m_inputXmlDocument.CreateElement("rule");
                            XMLHelperMethods.AttachAttributeToNode(m_inputXmlDocument, ruleTag, "participant", m_characters[_characterName].AgentInfo.Name);
                            XMLHelperMethods.AttachAttributeToNode(m_inputXmlDocument, ruleTag, "type", "idle_animation");
                            XMLHelperMethods.AttachAttributeToNode(m_inputXmlDocument, ruleTag, "participant", m_characters[_characterName].AgentInfo.Name);
                            XMLHelperMethods.AttachAttributeToNode(m_inputXmlDocument, ruleTag, "priority", ruleNodes[i].Attributes["priority"].Value);
                            XMLHelperMethods.AttachAttributeToNode(m_inputXmlDocument, ruleTag, "pose", m_characters[_characterName].AgentInfo.Posture);
                            m_inputXmlDocument.GetElementsByTagName("bml")[0].AppendChild(ruleTag);
                            break;
                        }
                        else if (randomNumber == 1)
                        {
                            if(m_characters[_characterName].Switch.saliencyGlance && m_characters[_characterName].HasSaliencyMap)                     
                                m_saliencyMap.generateGazeCommand(m_inputXmlDocument, m_characters[_characterName]);
                            break;
                        }
                    }
                    catch (Exception e)
                    {
                        NVBGLogger.Log(e.ToString());
                    }
                }
                //m_inputXmlDocument.GetElementsByTagName("bml")[0].AppendChild(ruleTag);
            }
            catch (Exception e)
            {
                NVBGLogger.Log("Error while creating Idle behavior : ERROR : " + e.ToString());
            }
        }



        /// <summary>
        /// Goes through the message queue and processes each message based on it's type
        /// Each message in the queue has a type (listen/speaker/idle etc.)
        /// Once the message has been processed,it is transformed using the .xsl transform file.
        /// it is then filtered to remove overlapping behavior based on time-marks
        /// and then the vrSpeechPartial events are appended to the message
        /// </summary>
        /// <param name="_currentMessage"></param>
        public void ProcessMessage(VHMessage _currentMessage)
        {            

            NVBGLogger.Log("ProcessMessage: " + _currentMessage.MessageString);

            if (!_currentMessage.InputXml.Equals(""))
            {
                try
                {
                    if (_currentMessage.InputXml.StartsWith("\""))
                        _currentMessage.InputXml = _currentMessage.InputXml.Substring(1);

                    if (_currentMessage.InputXml.EndsWith("\""))
                        _currentMessage.InputXml = _currentMessage.InputXml.Substring(0, _currentMessage.InputXml.Length - 1);
                    
                    m_inputXmlDocument.LoadXml(_currentMessage.InputXml);                    
                }
                catch(Exception e)
                {
                    NVBGLogger.Log("Error while trying to load input xml from received VHMessage" + e.ToString());
                }


                // If the message is of type vrExpress, then the vrExpressHandler object will process the message
                m_vrExpressHandler.ProcessMessage(m_inputXmlDocument, _currentMessage, m_characters[_currentMessage.AgentId]);

                //if (m_characters[_currentMessage.AgentId].Switch.saliencyGlance && m_characters[_currentMessage.AgentId].HasSaliencyMap)
                   // m_saliencyMap.ProcessMessage(m_inputXmlDocument, _currentMessage, m_characters[_currentMessage.AgentId]);
            }

            if (_currentMessage.Type.Equals("idleBehavior")) 
            {
                CreateIdleBehavior(_currentMessage.AgentId);
            }


            // Applying the transform to the processed message
            ApplyTransform();


            //Filter message and remove overlapping behavior
            if (m_finalDocument.GetElementsByTagName("rule").Count >= 2)
            {
                FilterGestures();
            }

            // clean up the bml and remove unwanted nodes in the xml
            CleanUpBml();

            if (m_characters[_currentMessage.AgentId].Switch.saliencyGlance)
            {
                m_saliencyMap.updateGazeRange(m_finalDocument);
                m_saliencyMap.trackGazeEvent(m_finalDocument, m_characters[_currentMessage.AgentId]);
            }


            //Attach the vrAgentPartial messages which will notify us of when the character finishes 
            //speaking each word in the sentence
            if (_currentMessage.Type.Equals("dialogue"))
            {
                AttachVRAgentPartialMessage(_currentMessage);
            }
        }


        /// <summary>
        /// Transforms the xml document and generates another xml document
        /// The transform rules perform the mapping to behavior
        /// </summary>
        private void ApplyTransform()
        {
            NVBGLogger.Log("Mapping rules to output behavior");
            try
            {
				//Copy m_inputXmlDocument. Resolves namespace error on OSX.
				XmlDocument m_inputXmlDocument2 = new XmlDocument();
				m_inputXmlDocument2.LoadXml(m_inputXmlDocument.InnerXml.ToString());
				
                m_inputXmlDocument2.Normalize();
                XslObjectMethods objectMethod = new XslObjectMethods(this);
                XsltArgumentList xsltArgList = new XsltArgumentList();
                xsltArgList.AddExtensionObject("http://ExternalFunction.xslt.isi.edu", objectMethod);
                StringWriter sw = new StringWriter();
                m_xslTransform.Transform(m_inputXmlDocument2.CreateNavigator(), xsltArgList, sw);
                m_finalDocument.LoadXml(sw.ToString());
                m_finalDocument.Normalize();
            }
            catch (Exception e)
            {
                NVBGLogger.Log("Error during XML transformation: " + e.ToString());
            }
        }


        /// <summary>
        /// Attach vrAgent partial messages to the final bml which will notify us of when the character finishes 
        //speaking each word in the sentence
        /// </summary>
        /// <param name="_currentMessage"></param>
        private void AttachVRAgentPartialMessage(VHMessage _currentMessage)
        {

            
            NVBGLogger.Log("Attaching vrAgentPartial messages");

            XmlNode speechTag = m_finalDocument.GetElementsByTagName("speech")[0];
            XmlNode bmlTag = m_finalDocument.GetElementsByTagName("bml")[0];

            XmlNodeList eventTagList = m_finalDocument.GetElementsByTagName("sbm:event", "http://ict.usc.edu");
            XmlNode lastEvent;

            NVBGLogger.Log("AttachVRAgentPartialMessage: eventTagList.Count: " + eventTagList.Count);
            NVBGLogger.Log("AttachVRAgentPartialMessage: eventTagList: " + eventTagList.ToString());

            try
            {

                if (eventTagList.Count != 0)
                {
                    lastEvent = eventTagList.Item(eventTagList.Count - 1);
                }
                else
                {
                    try
                    {
                        lastEvent = speechTag.NextSibling;
                    }
                    catch
                    {
                        lastEvent =speechTag;
                    }
                }
                NVBGLogger.Log("AttachVRAgentPartialMessage: lastEvent: " + lastEvent.Name + lastEvent.InnerText + " " + " " + lastEvent.Value);


                XmlNodeList timeMarkers = m_finalDocument.GetElementsByTagName("mark");
                List<string> prefixWordBuffer = new List<string>();
                string prefixWord = "";

                string spId = speechTag.Attributes["id"].Value;
                spId += ":";


                if (timeMarkers.Count > 0)
                {
                    for (int i = 0; i < timeMarkers.Count; i = i + 2)
                    {
                        prefixWord = "";
                        XmlNode wordNode = timeMarkers[i].NextSibling;
                        string wordText = wordNode.InnerText;
                        wordText.Trim();
                        prefixWordBuffer.Add(wordText);

                        for (int j = 0; j < prefixWordBuffer.Count; ++j)
                        {
                            string thisWord = prefixWordBuffer[j];
                            thisWord += " ";
                            thisWord = thisWord.Replace("\n", "");
                            thisWord = thisWord.Replace("\t", "");
                            thisWord = thisWord.Replace("\r", "");
                            prefixWord += thisWord;
                        }

                        string endingTimeMarker = "T" + Convert.ToString(i + 1);

                        XmlNode eventTag = m_finalDocument.CreateElement("sbm:event");
                        string messageAttribute = "vrAgentSpeech partial " + _currentMessage.MsgId;
                        messageAttribute += " ";
                        messageAttribute += endingTimeMarker;
                        messageAttribute += " ";
                        messageAttribute += prefixWord;
                        XMLHelperMethods.AttachAttributeToNode(m_finalDocument, eventTag, "message", messageAttribute);
                        XMLHelperMethods.AttachAttributeToNode(m_finalDocument, eventTag, "stroke", spId + endingTimeMarker);

                        bmlTag.InsertBefore(eventTag, lastEvent);


                    }
                }
            }
            catch (Exception e)
            {
                NVBGLogger.Log("Error while attaching vrAgentPartial Message : ERROR : " + e.ToString());
            }

        }


        /// <summary>
        /// Removes overlapping rules in the final generated bml based on their priorities
        /// Basically checks to see if the time-marks start/end overlap each other and then 
        /// checks to see if it's an animation/gaze. It prunes if there are conflicts e.g. 2 animations at same time
        /// </summary>
        private void FilterGestures()
        {
            try
            {
                NVBGLogger.Log("Filtering behaviors based on priority");

                XmlNode bml = m_finalDocument.GetElementsByTagName("bml")[0];
                XmlNodeList rules = m_finalDocument.GetElementsByTagName("rule");
                bool[] rulesToBeDeleted = new bool[rules.Count];
                for (int i = 0; i < rules.Count; ++i)
                {
                    rulesToBeDeleted[i] = false;
                }

                if (rules.Count > 2)
                {
                    for (int i = 0; i < rules.Count; ++i)
                    {
                        if (rulesToBeDeleted[i])
                            continue;

                        XmlNode currentRule = rules[i];
                        //string currentName = currentRule.Attributes["type"].Value;
                        string currentPriority = currentRule.Attributes["priority"].Value;
                        string currentStart = currentRule.Attributes["ready"].Value;
                        string currentEnd = currentRule.Attributes["relax"].Value;

                        string ruleType = "";
                        for (int k = 0; k < currentRule.ChildNodes.Count; ++k)
                        {
                            ruleType = currentRule.ChildNodes[k].Name;
                            if (!ruleType.Equals("#comment"))
                                break;
                        }                        


                        currentStart = currentStart.Replace("T", "");
                        currentEnd = currentEnd.Replace("T", "");


                        for (int j = i + 1; j < rules.Count; ++j)
                        {
                            if (rulesToBeDeleted[j])
                                continue;

                            XmlNode nextRule = rules[j];
                            //string nextName = nextRule.Attributes["type"].Value;
                            string nextPriority = nextRule.Attributes["priority"].Value;
                            string nextStart = nextRule.Attributes["ready"].Value;
                            string nextEnd = nextRule.Attributes["relax"].Value;
                            string nextRuleType = "";
                            for (int k = 0; k < nextRule.ChildNodes.Count; ++k)
                            {
                                nextRuleType = nextRule.ChildNodes[k].Name;
                                if (!nextRuleType.Equals("#comment"))
                                    break;
                            }


                            nextStart = nextStart.Replace("T", "");
                            nextEnd = nextEnd.Replace("T", "");

                            if (((Convert.ToInt32(currentStart) <= Convert.ToInt32(nextStart)) &&
                                (Convert.ToInt32(currentEnd) > Convert.ToInt32(nextStart))) ||
                                ((Convert.ToInt32(currentStart) > Convert.ToInt32(nextStart)) &&
                                (Convert.ToInt32(currentStart) <= Convert.ToInt32(nextEnd))))
                            {

                                //remove the animation only if it conflicts with another animation. Animations
                                // and gazes are fine as they do not conflict
                                if (!(nextRuleType.Equals("animation") && ruleType.Equals("animation")))
                                    return;
                                    

                                if (Convert.ToInt32(currentPriority) > Convert.ToInt32(nextPriority))
                                    rulesToBeDeleted[i] = true;
                                else if (Convert.ToInt32(currentPriority) < Convert.ToInt32(nextPriority))
                                    rulesToBeDeleted[j] = true;
                                else if (Convert.ToInt32(currentPriority) == Convert.ToInt32(nextPriority))
                                {
                                    Random randomGenerator = new Random();
                                    int remainder = randomGenerator.Next() % 2;

                                    if (remainder == 0)
                                        rulesToBeDeleted[i] = true;
                                    else
                                        rulesToBeDeleted[j] = true;
                                }
                            }
                        }
                    }
                }

                for (int i = 0, j = 0; i < rules.Count; ++i, ++j)
                {
                    XmlNode nodeToBeDeleted = rules[i];
                    if (rulesToBeDeleted[j])
                    {
                        bml.RemoveChild(nodeToBeDeleted);
                        --i;
                    }
                }
            }
            catch (Exception e)
            {
                NVBGLogger.Log("Error while filtering gestures based on priorities : ERROR : " + e.ToString());
            }
        }



        /// <summary>
        /// Removes unwanted xml nodes generated during processing
        /// </summary>
        private void CleanUpBml()
        {
            XmlNode bml = m_finalDocument.GetElementsByTagName("bml")[0];
            XmlNodeList rules = m_finalDocument.GetElementsByTagName("rule");
            List<XmlNode> rulesCopy;

            rulesCopy = new List<XmlNode>();

            for (int i = 0; i < rules.Count; ++i)
            {
                XmlNode rule = rules[i];
                XmlNode copyRule = rule.Clone();
                rulesCopy.Add(copyRule);
            }

            while (m_finalDocument.GetElementsByTagName("rule").Count > 0)
            {
                bml.RemoveChild(m_finalDocument.GetElementsByTagName("rule")[0]);
            }

            for (int i = 0; i < rulesCopy.Count; ++i)
            {
                XmlNodeList childNodes = rulesCopy[i].ChildNodes;

                for (int j = 0; j < childNodes.Count; ++j)
                {
                    XmlNode nodeToAdd = childNodes[j].Clone();

                    if (nodeToAdd.Name.Equals("animation"))
                    {
                        if (!nodeToAdd.Attributes["name"].Value.Equals("none"))
                            bml.AppendChild(nodeToAdd);
                    }
                    else
                        bml.AppendChild(nodeToAdd);
                }
            }

            rulesCopy.Clear();

            XmlNodeList feedbacks = m_finalDocument.GetElementsByTagName("feedbacks");
            List<XmlNode> feedbacksCopy;

            feedbacksCopy = new List<XmlNode>();

            for (int i = 0; i < feedbacks.Count; ++i)
            {
                XmlNode feedback = feedbacks[i];
                XmlNode copyFeedback = feedback.Clone();
                feedbacksCopy.Add(copyFeedback);
            }

            while (m_finalDocument.GetElementsByTagName("feedback").Count > 0)
            {
                bml.RemoveChild(m_finalDocument.GetElementsByTagName("feedback")[0]);
            }


            for (int i = 0; i < feedbacksCopy.Count; ++i)
            {
                XmlNodeList childNodes = feedbacksCopy[i].ChildNodes;

                for (int j = 0; j < childNodes.Count; ++j)
                {
                    XmlNode nodeToAdd = childNodes[j].Clone();

                    if (nodeToAdd.Name.Equals("animation"))
                    {
                        if (!nodeToAdd.Attributes["name"].Value.Equals("none"))
                            bml.AppendChild(nodeToAdd);
                    }
                    else
                        bml.AppendChild(nodeToAdd);
                }
            }

            feedbacksCopy.Clear();




            //Removing nods for first few words as this conflicts with gazes and causes popping
            //SB needs to handle this better
            XmlNodeList nods = m_finalDocument.GetElementsByTagName("head");
            List<XmlNode> toBeRemoved = new List<XmlNode>();

            for (int i = 0; i < nods.Count; ++i)
            {
                XmlAttribute type = nods[i].Attributes["type"];
                if (type.Value.Equals("NOD"))
                {
                    try
                    {
                        XmlAttribute relaxTime = nods[i].Attributes["relax"];
                        string time = relaxTime.Value;
                        time = time.Replace("sp1:T", "");

                        if (time.Contains("+"))
                        {
                            time = time.Substring(0, time.Length - time.IndexOf("+") - 2);
                        }

                        if (time.Contains("-"))
                        {
                            time = time.Substring(0, time.Length - time.IndexOf("-") - 2);
                        }

                        if (Convert.ToInt32(time) < 5)
                        {
                            toBeRemoved.Add(nods[i]);
                        }
                    }
                    catch (Exception ex)
                    {
                        NVBGLogger.Log("Error while trying clean bml for nods" + ex.ToString());
                    }
                }
            }

            for (int i = 0; i < toBeRemoved.Count; ++i)
            {
                bml.RemoveChild(toBeRemoved[i]);
            }





            m_finalDocument.Normalize();

        }


        /// <summary>
        /// Check if the rule input file (which specifies behavior rules) exists
        /// </summary>
        private void CheckRuleInputFile()
        {
            //string ruleInputFile;
            if(!m_useCommonDataPath)
            {
                //ruleInputFile = m_dataFolderName + @"rule_input_" + m_characters[_characterName].AgentInfo.Culture + @".xml";
                m_transformXsl = m_dataFolderName + @"NVBG_transform.xsl";

                if (!File.Exists(m_transformXsl))
                {
                    NVBGLogger.Log(m_transformXsl + " does not exist.");
                    m_useCommonDataPath = true;
                }                          
            }

            if (m_useCommonDataPath)
            {
                
                m_dataFolderName = @"../data/nvbg-common/";
                //ruleInputFile = m_dataFolderName + @"rule_input_" + m_characters[_characterName].AgentInfo.Culture + @".xml";
                m_transformXsl = m_dataFolderName + @"NVBG_transform.xsl";

                if (!File.Exists(m_transformXsl))
                {
                    NVBGLogger.Log("ERROR: default xsl file " + m_transformXsl + " does not exist.");
                }              
            }            
        }


        /// <summary>
        /// turns off idle behavior for the duration of the speech
        /// when the character is speaking, he shouldn't be doing idle fidget animations        
        /// </summary>
        public void TurnOffIdleOnSpeech(VHMessage _currentMessage)
        {

            // check if this is a speech message, if so, stop idle gaze
            XmlDocument tmpDocument = new XmlDocument();
            if (!_currentMessage.InputXml.Equals(""))
            {
                try
                {
                    if (_currentMessage.InputXml.StartsWith("\""))
                        _currentMessage.InputXml = _currentMessage.InputXml.Substring(1);

                    if (_currentMessage.InputXml.EndsWith("\""))
                        _currentMessage.InputXml = _currentMessage.InputXml.Substring(0, _currentMessage.InputXml.Length - 1);

                    tmpDocument.LoadXml(_currentMessage.InputXml);
                }
                catch (Exception ex)
                {
                    NVBGLogger.Log("Error while trying to load input xml from received VHMessage" + ex.ToString());
                }
                if (tmpDocument.GetElementsByTagName("speech").Count > 0)
                {
                    m_generateGaze = false;
                    // disable idle timer for speaker as he has begin speaking
                    m_idleTimerEnableHandler(_currentMessage.AgentId, false);
                    // disable idle timer for listener as he has begin listening
                    m_idleTimerEnableHandler(_currentMessage.Target, false);
                }
            }
        }
    

        /// <summary>
        /// Callback method that handles VHMessages received over the network
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void MessageCallback(object sender, VHMsg.Message e)
        {

            if (e == null)
                return;
            string[] splitargs = e.s.Split(" ".ToCharArray());


            // if NVBG is currently disable, then stop processing the messages and return
            if (m_isDisabled)
            {
                if (splitargs[0].Equals("nvbg_set_option", StringComparison.OrdinalIgnoreCase))
                {
                    if (splitargs.Length >= 3)
                    {
                        if (splitargs[1].Equals("disable_nvbg"))
                            m_optionCallback("", splitargs[1], splitargs[2]);
                    }
                }
                else
                {
                    return;
                }
 
            }             
            

            // reply to ping from launcher
            if (splitargs[0].Equals("vrAllCall"))
            {
                try
                {
                    m_vhmsg.SendMessage("vrComponent nvb generator");
                }
                catch (Exception ex)
                {
                    NVBGLogger.Log(ex.ToString());
                }
                
            }
            // kill self if message received
            if (splitargs[0].Equals("vrKillComponent"))
            {
                if ((splitargs.Length > 1) && ((splitargs[1].Equals("nvb")) || (splitargs[1].Equals("all")) ))
                {
                    if (!m_activeMQClosed)
                    {
                        m_activeMQClosed = true;                        
                        //m_callbackHandler();
                        Application.Exit();
                        CloseActiveMQ();
                    }
                    return;
                }
            }
            // This is the result received from the parser in response to the NVBG sending it 
            // a sentence to process. On receiving it NVB stores it in a string to be processed later
            // e.g. message ----------- parser_result Rachel (S1 (NP (NNP Great.)))
            if (splitargs[0].Equals("parser_result"))
            {
                VHMessage currentMessage = DecomposeMessage(e.s);


                if (m_characters.ContainsKey(currentMessage.AgentId))
                {
                    NVBGLogger.Log("Received reply from parser: ");
                    NVBGLogger.Log(e.s);
                    lock (NVBGManager.m_parserLock)
                    {
                        SpeakerBehaviorManager.parserResultString = currentMessage.InputXml;
                    }                    
                }
                
            }

            //This is the main message that NVBG receives/processes.
            //The vrExpress can be of listen type of of speak type depending on whether the character in 
            //NVBG is the speaker in the message or the addressee
            // e.g. vrExpress message is as below in this message, the speaker is Rachel and the addresse is Brad

            /*vrExpress Rachel Brad 1378420464767-28-1 <?xml version="1.0" encoding="UTF-8" standalone="no" ?>
                      <act>
                       <participant id="Rachel" role="actor" />
                       <fml>
                       <turn start="take" end="give" />
                       <affect type="neutral" target="addressee"></affect>
                       <culture type="neutral"></culture>
                       <personality type="neutral"></personality>
                       </fml>
                       <bml>
                       <speech id="sp1" ref="rachel_great" type="application/ssml+xml">Great!</speech>
                       </bml>
                      </act>*/

            if (splitargs[0].Equals("vrExpress"))
            {
                VHMessage currentMessage = DecomposeMessage(e.s);                
                //m_data.CurrentDialogue.Listener = currentMessage.Target;                
                //if (currentMessage.AgentId.Equals(m_data.AgentInfo.Name))
                if(m_characters.ContainsKey(currentMessage.AgentId))
                {
                    m_characters[currentMessage.AgentId].CurrentDialogue.Speaker = currentMessage.AgentId;
                    m_characters[currentMessage.AgentId].CurrentDialogue.Listener = currentMessage.Target;
                    m_characters[currentMessage.AgentId].ConversationInfo.LastMyExpressId = currentMessage.MsgId;
                    NVBGLogger.Log("Received vrExpress Message: " + e.s);

                    lock (m_lockVhMSG)
                    {
                        m_messageQueue.Add(currentMessage);
                    }

                    //TurnOffIdleOnSpeech(currentMessage);
                    //m_callbackHandler();
                }
                if (m_characters.ContainsKey(currentMessage.Target))
                {
                    // if the listener is added, then we need to add a message for the listener too
                    //currentMessage.Type = "listen";
                    VHMessage duplicateMessage = DecomposeMessage(e.s);
                    duplicateMessage.Type = "listen";

                    lock (m_lockVhMSG)
                    {
                        m_messageQueue.Add(currentMessage);
                    }

                    //TurnOffIdleOnSpeech(currentMessage);
                    //m_callbackHandler();
                }

                TurnOffIdleOnSpeech(currentMessage);

            }

            //The vrSpoke message is processed by NVBG for generating gazes and to check
            //how long it's been since the character spoke so he can idle. This message is received from Smartbody 
            // after the character has finished speaking

            if (splitargs[0].Equals("vrSpoke"))
            {
                VHMessage currentMessage = DecomposeMessage(e.s);
                currentMessage.Type = "vrSpoke";

                if (m_characters.ContainsKey(currentMessage.AgentId))
                {
                    currentMessage.Type = "vrBackchannel";
                    InsertXMLSkeleton(currentMessage);

                    lock (m_lockVhMSG)
                    {
                        m_messageQueue.Add(currentMessage);
                    }

                    //m_callbackHandler();
                }
                m_generateGaze = true;
                // enable the idle timer for speaker as he has finished speaking
                m_idleTimerEnableHandler(currentMessage.AgentId, true);
                // enable the idle timer for listener as he has finished listening
                m_idleTimerEnableHandler(currentMessage.Target, true);
            }

            // This message was used to generate character feedback when the conversation
            // is in progress. This is hardly used and hasn't been tested well.
            // It's safe to ignore it as long as it's not used
            // Please talk to Stacy to get more details on this message
            if (splitargs[0].Equals("vrBCFeedback"))
            {
                VHMessage currentMessage = DecomposeMessage(e.s);

                if (m_characters.ContainsKey(currentMessage.AgentId))
                {
                    currentMessage.Type = "vrBCFeedback";
                    InsertXMLSkeleton(currentMessage);

                    lock (m_lockVhMSG)
                    {
                        m_messageQueue.Add(currentMessage);
                    }

                    //m_callbackHandler();
                }

            }

            // This message is received when the agent is speaking and it tells us which word he just spoke
            //e.g. vrAgentSpeech partial 1378420464767-28-1 T1 Great.
            if (splitargs[0].Equals("vrAgentSpeech"))
            {
                // receive other virtual characters' speech
                foreach (KeyValuePair<string, NVBGCharacter> entry in m_characters)
                {
                    VHMessage currentMessage = DecomposeMessage(e.s);
                    // exclude the speaker to receive this message
                    if (currentMessage.MsgId != m_characters[entry.Key].ConversationInfo.LastMyExpressId)
                    {
                        currentMessage.AgentId = entry.Key;
                        currentMessage.Type = "vrAgentSpeech";
                        InsertXMLSkeleton(currentMessage);
                        currentMessage.MsgId = "idle-";
                        currentMessage.MsgId += System.DateTime.Now.Second;
                        currentMessage.Target = "all";

                        lock (m_lockVhMSG)
                        {
                            m_messageQueue.Add(currentMessage);
                        }

                        //m_callbackHandler();
                    }
                }
            }

            // This message is received from acquireSpeech and contains the sentence spoken
            // by the user into the mic.
            // vrSpeech interp user0002 1 1.0 normal HOW ARE YOU
            if (splitargs[0].Equals("vrSpeech") && splitargs[1].Equals("interp"))
            {
                // receive user speech
                foreach (KeyValuePair<string, NVBGCharacter> entry in m_characters)
                {
                    VHMessage currentMessage = DecomposeMessage(e.s);
                    currentMessage.AgentId = entry.Key;
                    currentMessage.Type = "vrSpeech";
                    InsertXMLSkeleton(currentMessage);
                    currentMessage.MsgId = "idle-";
                    currentMessage.MsgId += System.DateTime.Now.Second;
                    currentMessage.Target = "all";

                    lock (m_lockVhMSG)
                    {
                        m_messageQueue.Add(currentMessage);
                    }

                    //m_callbackHandler();
                }
            }

            // this message is received by NVBG after a gaze requested by the saliency map has completed             
            // This was implemented by Wenjia. Not reeeally sure what this does
            if (splitargs[0].Equals("SaliencyMapGaze"))
            {
                // receive message about a gaze is finished
                VHMessage currentMessage = DecomposeMessage(e.s);
                if (m_characters.ContainsKey(currentMessage.AgentId))
                {
                    m_saliencyMap.checkForFinishedGaze(currentMessage);
                }
            }

            // This message specifies to NVBG that an even has occured
            // The intention is to update the saliency map
            // e.g. when Rio enters the scene in gunslinger, that is an event
            // now the saliency map should be updated as the characters gazes/priorities will change accordingly
            if (splitargs[0].Equals("SaliencyMapEvent"))
            {
                // receive an event is happened
                VHMessage currentMessage = DecomposeMessage(e.s);
                m_saliencyMap.updateEvent(currentMessage.MessageString);   
            }

            // NOT BEING USED NOW
            // was to update the story-point in saliency map. The story point
            // basically specifies a section in the saliency .xml file which contains
            // the pawns and emotion etc. for that part of the story/scene
            if (splitargs[0].Equals("SaliencyMapSP"))
            {
                // receive a story point change
                //VHMessage currentMessage = DecomposeMessage(e.s);
                //m_saliencyMap.updateOneStoryPoint(currentMessage.MessageString);
            }

            //NOT BEING USED NOW
            // Updates the emotion in the saliency map which should affect how the characters
            // percieve the world.
            if (splitargs[0].Equals("SaliencyMapEmotion"))
            {
                //// receive an emotion change
                //VHMessage currentMessage = DecomposeMessage(e.s);
                //if (m_characters.ContainsKey(currentMessage.AgentId))
                //{
                //    int idleTimerInterval = 0;
                //    m_saliencyMap.updateEmotion(currentMessage.MessageString, ref idleTimerInterval);
                //    if ((idleTimerInterval * 1000) != m_idleTimer.Interval)
                //    {
                //        m_idleTimerValueChangeHandler(Convert.ToString(idleTimerInterval));
                //    }
                //}
            }

            // This message allows us to send a message to NVBG and update the priority
            // of a pawn in the saliency map making it more important or less
            // These pawns are what the characters gaze at
            if (splitargs[0].Equals("SaliencyMapPawnPrimacy"))
            {
                // receive setting of the primacy of a pawn
                VHMessage currentMessage = DecomposeMessage(e.s);
                if (m_characters.ContainsKey(currentMessage.AgentId))
                {
                    m_saliencyMap.updatePawnPrimacy(currentMessage.MessageString);
                }
            }

            // This message allows the user to tell nvbg to change it's options such as
            // character posture, gazes on/off, parts of speech on/off etc.
            //e.g. nvbg_set_option [char-name] all_behavior true/false - sets/unsets flag that allows all behavior generated by NVBG.
            if (splitargs[0].Equals("nvbg_set_option",StringComparison.OrdinalIgnoreCase))
            {
                if (splitargs.Length >= 4)
                {
                    if (m_characters.ContainsKey(splitargs[1]))
                        m_optionCallback(splitargs[1], splitargs[2], splitargs[3]);
                }
                else if (splitargs.Length >= 3)
                {
                    if (splitargs[1].Equals("disable_nvbg"))
                        m_optionCallback("", splitargs[1], splitargs[2]);
                }
                else if (splitargs.Length >= 2)
                {
                    if (splitargs[1].Equals("refresh_transform"))
                        m_optionCallback("", splitargs[1], "");
                }
            }

            // message that allows you to create a character
            if (splitargs[0].Equals("nvbg_create_character", StringComparison.OrdinalIgnoreCase))
            {
                if (splitargs.Length >= 2)
                {
                    CreateCharacter(splitargs[1].Trim(), CheckForIdle);                    
                }
            }            

            // Respond the input message that was just received            
            //if ((m_messageQueue.Count > 0) && (!m_activeMQClosed))
                //m_callbackHandler();
            
        }



        /// <summary>
        /// handles the set options message/commmand and sets the option accordingly
        /// </summary>
        /// <param name="_characterName"></param>
        /// <param name="_type"></param>
        /// <param name="_optionValue"></param>
        private void NVBGSetOptions(string _characterName,string _type, string _optionValue)
        {
            switch (_type)
            {
                case "all_behavior":
                    m_characters[_characterName].Switch.allBehaviour = Convert.ToBoolean(_optionValue);
                    m_GUILabelUpdateHandler(_characterName, "all_behavior", Convert.ToBoolean(_optionValue));
                    break;
                case "saliency_glance":
                    m_characters[_characterName].Switch.saliencyGlance = Convert.ToBoolean(_optionValue);
                    m_GUILabelUpdateHandler(_characterName, "saliency_glance", Convert.ToBoolean(_optionValue));
                    break;
                case "saliency_idle_gaze":
                    m_characters[_characterName].Switch.saliencyIdleGaze = Convert.ToBoolean(_optionValue);
                    m_GUILabelUpdateHandler(_characterName, "saliency_idle_gaze", Convert.ToBoolean(_optionValue));
                    break;
                case "speaker_gaze":
                    m_characters[_characterName].Switch.speakerGaze = Convert.ToBoolean(_optionValue);
                    m_GUILabelUpdateHandler(_characterName, "speaker_gaze", Convert.ToBoolean(_optionValue));
                    break;
                case "speaker_gesture":
                    m_characters[_characterName].Switch.speakerGestures = Convert.ToBoolean(_optionValue);
                    m_GUILabelUpdateHandler(_characterName, "speaker_gesture", Convert.ToBoolean(_optionValue));
                    break;
                case "listener_gaze":
                    m_characters[_characterName].Switch.listenerGaze = Convert.ToBoolean(_optionValue);
                    m_GUILabelUpdateHandler(_characterName, "listener_gaze", Convert.ToBoolean(_optionValue));
                    break;
                case "nvbg_POS_rules":
                    m_characters[_characterName].Switch.POSRules = Convert.ToBoolean(_optionValue);
                    m_GUILabelUpdateHandler(_characterName, "nvbg_POS_rules", Convert.ToBoolean(_optionValue));
                    break;
                case "rule_input_file":
                    m_characters[_characterName].LoadXML(m_dataFolderName + _optionValue);
                    break;
                case "saliency_map":
                    m_characters[_characterName].LoadSaliencyMap(m_dataFolderName + _optionValue);
                    m_characters[_characterName].UpdateOneStoryPoint(storyPoint);
                    break;
                case "posture":
                    m_characters[_characterName].AgentInfo.Posture = _optionValue;
                    break;
                case "change_idle_time":
                    m_idleTimerValueChangeHandler(_characterName, _optionValue);
                    break;
                case "disable_nvbg":
                    m_isDisabled = Convert.ToBoolean(_optionValue);
                    break;
                case "refresh_transform":
                    LoadXSL();
                    break;
                default:
                    break;
            }
        }


        /// <summary>
        /// Decompose the input message into NVBG data structure of  messages
        /// Please look at the VHMessage class to see the structure of the message
        /// </summary>
        /// <param name="_inputString"></param>
        /// <returns></returns>
        private VHMessage DecomposeMessage(string _inputString)
        {

            string[] args = _inputString.Split(" ".ToCharArray());
            VHMessage receivedMessage = new VHMessage();
            receivedMessage.MessageString = _inputString;

            if (args.Length <= 1)
                return receivedMessage;

            receivedMessage.AgentId = args[1];
            receivedMessage.AgentId = receivedMessage.AgentId.Replace("\"", "");

            if (!(args[0].Equals("parser_result") || args[0].Equals("vrBCFeedback") || args[0].Equals("vrSpoke") || args[0].Equals("vrAgentSpeech") || args[0].Equals("vrSpeech")
                || args[0].Equals("SaliencyMapGaze") || args[0].Equals("SaliencyMapEvent") || args[0].Equals("SaliencyMapSP") || args[0].Equals("SaliencyMapEmotion") || args[0].Equals("SaliencyMapPawnPrimacy")))
            {
                receivedMessage.Target = args[2];
                receivedMessage.Target = receivedMessage.Target.Replace("\"", "");

                receivedMessage.MsgId = args[3];
                receivedMessage.MsgId = receivedMessage.MsgId.Replace("\"", "");

                receivedMessage.InputXml = _inputString.Substring(_inputString.IndexOf(args[3]) + args[3].Length);
                receivedMessage.InputXml = receivedMessage.InputXml.Trim();

                // This has been done to ensure that the sbm: namespace has a value in the input xml
                // or we get an exception when we load the input xml into an XmlDocument object
                if (receivedMessage.InputXml.Contains("sbm:interrupt ") && !(receivedMessage.InputXml.Contains("xmlns:sbm=")))
                {
                    receivedMessage.InputXml = receivedMessage.InputXml.Replace("sbm:interrupt ", "sbm:interrupt xmlns:sbm=\"http://ict.usc.edu\" ");
                }
                if (receivedMessage.InputXml.Contains("sbm:event ") && !(receivedMessage.InputXml.Contains("xmlns:sbm=")))
                {
                    receivedMessage.InputXml = receivedMessage.InputXml.Replace("sbm:event ", "sbm:event xmlns:sbm=\"http://ict.usc.edu\" ");
                }
            }
            else if (args[0].Equals("vrSpoke"))
            {
                receivedMessage.Target = args[2];
                receivedMessage.Target = receivedMessage.Target.Replace("\"", "");

                receivedMessage.MsgId = args[3];
                receivedMessage.MsgId = receivedMessage.MsgId.Replace("\"", "");                
            }
            else if (args[0].Equals("vrAgentSpeech"))
            {
                receivedMessage.MsgId = args[2];
                receivedMessage.MsgId = receivedMessage.MsgId.Replace("\"", "");
                receivedMessage.MessageString = args[args.Length-1];
            }
            else if (args[0].Equals("vrSpeech"))
            {
                receivedMessage.MessageString = _inputString.Substring(_inputString.IndexOf(args[5]) + args[5].Length);
                receivedMessage.MessageString = receivedMessage.MessageString.Trim();
            }
            else if (args[0].Equals("SaliencyMapGaze"))
            {
                receivedMessage.MsgId = args[2];
            }
            else if (args[0].Equals("SaliencyMapEvent") || args[0].Equals("SaliencyMapSP") 
                || args[0].Equals("SaliencyMapEmotion") || args[0].Equals("SaliencyMapPawnPrimacy"))
            {
                receivedMessage.MessageString = _inputString.Substring(_inputString.IndexOf(args[1]) + args[1].Length);
                receivedMessage.MessageString = receivedMessage.MessageString.Trim();
            }
            else
            {
                receivedMessage.InputXml = _inputString.Substring(_inputString.IndexOf(args[1]) + args[1].Length);
                receivedMessage.InputXml = receivedMessage.InputXml.Trim();
            }            

            return receivedMessage;
        }


        /// <summary>
        /// Initialize activeMQ and register to input messages
        /// </summary>
        private void InitializeActiveMQ()
        {

            NVBGLogger.Log("Initializing Active MQ");

            try
            {
                m_vhmsg = new VHMsg.Client(m_encoding);
                m_vhmsg.OpenConnection();
                NVBGLogger.Log("Subscribing to input messages");
                m_vhmsg.SubscribeMessage("elvinSim_to_nvbGen");
                m_vhmsg.SubscribeMessage("parser_result");
                m_vhmsg.SubscribeMessage("vrExpress");
                m_vhmsg.SubscribeMessage("vrKillComponent");
                m_vhmsg.SubscribeMessage("vrGazeDone");
                m_vhmsg.SubscribeMessage("vrSpeech");
                m_vhmsg.SubscribeMessage("vrSpoke");
                m_vhmsg.SubscribeMessage("vrMiniBrain");
                m_vhmsg.SubscribeMessage("vrBCFeedback");
                m_vhmsg.SubscribeMessage("vrNvbgFeedbackRuleTest");
                m_vhmsg.SubscribeMessage("vrAllCall");
                m_vhmsg.SubscribeMessage("vrAgentSpeech");
                m_vhmsg.SubscribeMessage("vrSpeech");
                m_vhmsg.SubscribeMessage("SaliencyMapGaze");
                m_vhmsg.SubscribeMessage("SaliencyMapEvent");
                m_vhmsg.SubscribeMessage("SaliencyMapSP");
                m_vhmsg.SubscribeMessage("SaliencyMapEmotion");
                m_vhmsg.SubscribeMessage("SaliencyMapPawnPrimacy");
                m_vhmsg.SubscribeMessage("nvbg_set_option");
                m_vhmsg.SubscribeMessage("nvbg_create_character");
                m_vhmsg.SubscribeMessage("vrSpeak");
                m_vhmsg.MessageEvent += new VHMsg.Client.MessageEventHandler(MessageCallback);
                NotifyPeers();

                String server = m_vhmsg.Server;
                String port = m_vhmsg.Port;
                String scope = m_vhmsg.Scope;
                NVBGLogger.Log("Connected to " + server + " " + port + " " + scope);

            }
            catch (Exception e)
            {
                NVBGLogger.Log("Error while initializing ActiveMQ : ERROR :" + e.ToString());
            }
        }


        /// <summary>
        /// Read config file for character
        /// </summary>
        /// <param name="_characterName"></param>
        public void ReadConfigFileForCharacter(string _characterName)
        {
            string ruleInputFile = m_configFile.GetSetting("general", "rule_input_file");
            if (!string.IsNullOrEmpty(ruleInputFile))
            {
                m_characters[_characterName].LoadXML(m_dataFolderName + ruleInputFile);
            }
            string saliencyMap = m_configFile.GetSetting("general", "saliency_map");
            if (!string.IsNullOrEmpty(saliencyMap))
            {
                m_characters[_characterName].LoadSaliencyMap(m_dataFolderName + saliencyMap);
                m_characters[_characterName].UpdateOneStoryPoint(storyPoint);
            } 
            string idleTimerValue = m_configFile.GetSetting("general", "idle_timer_value");
            if (!string.IsNullOrEmpty(idleTimerValue))
            {
                m_idleTimerValueChangeHandler(_characterName, idleTimerValue);
            }
            string posture = m_configFile.GetSetting("general", "posture");
            if (!string.IsNullOrEmpty(posture))
            {
                m_characters[_characterName].AgentInfo.Posture = posture;
            }
            string allBehavior = m_configFile.GetSetting("general", "all_behavior");
            if (!string.IsNullOrEmpty(allBehavior))
            {
                if(allBehavior.Equals("off", StringComparison.OrdinalIgnoreCase))
                    m_characters[_characterName].Switch.allBehaviour = false;                
            }
            string saliencyGlance = m_configFile.GetSetting("general", "saliency_glance");
            if (!string.IsNullOrEmpty(saliencyGlance))
            {
                if (saliencyGlance.Equals("on", StringComparison.OrdinalIgnoreCase))
                    m_characters[_characterName].Switch.saliencyGlance = true;
            }
            string saliencyIdleGaze = m_configFile.GetSetting("general", "saliency_idle_gaze");
            if (!string.IsNullOrEmpty(saliencyIdleGaze))
            {
                if (saliencyIdleGaze.Equals("off", StringComparison.OrdinalIgnoreCase))
                    m_characters[_characterName].Switch.saliencyIdleGaze = false;
            }
            string speakerGaze = m_configFile.GetSetting("general", "speaker_gaze");
            if (!string.IsNullOrEmpty(speakerGaze))
            {
                if (speakerGaze.Equals("off", StringComparison.OrdinalIgnoreCase))
                    m_characters[_characterName].Switch.speakerGaze = false;
            }
            string speakerGesture = m_configFile.GetSetting("general", "speaker_gaze");
            if (!string.IsNullOrEmpty(speakerGesture))
            {
                if (speakerGesture.Equals("off", StringComparison.OrdinalIgnoreCase))
                    m_characters[_characterName].Switch.speakerGestures = false;
            }
            string listenerGaze = m_configFile.GetSetting("general", "listener_gaze");
            if (!string.IsNullOrEmpty(listenerGaze))
            {
                if (listenerGaze.Equals("off", StringComparison.OrdinalIgnoreCase))
                    m_characters[_characterName].Switch.listenerGaze = false;
            }
            string posRule = m_configFile.GetSetting("general", "nvbg_POS_rules");
            if (!string.IsNullOrEmpty(posRule))
            {
                if (posRule.Equals("off", StringComparison.OrdinalIgnoreCase))
                    m_characters[_characterName].Switch.POSRules = false;
            }

            m_refreshGUIHandler(_characterName);
        }



        /// <summary>
        /// Inform other componenets on startup
        /// </summary>
        private void NotifyPeers()
        {
            m_vhmsg.SendMessage("vrComponent " + "nvb generator");            
            foreach(KeyValuePair<string,NVBGCharacter> entry in m_characters)
            {
                string elvinArgs = entry.Value.AgentInfo.Name + " ready";
                m_vhmsg.SendMessage("nvbGen_ready " + elvinArgs);
            }
        }

        public void CreateCharacter(string _characterName, EventHandler _idleCallback)
        {
            m_characters.Add(_characterName, new NVBGCharacter(_characterName, _idleCallback));
            if (m_characters.Count == 1)
            {
                m_GUILabelUpdateHandler(_characterName,"update_character_name",false);
                m_refreshGUIHandler(_characterName);
            }
            
        }

        public void CreateCharacter(string _characterName )
        {
            m_characters.Add(_characterName, new NVBGCharacter(_characterName));
            if (m_characters.Count == 1)
            {
                m_GUILabelUpdateHandler(_characterName, "update_character_name", false);
                m_refreshGUIHandler(_characterName);
            }
            
        }


        /// <summary>
        /// Parse the input commandline parameters
        /// </summary>
        /// <param name="args"></param>
        public bool ParseArguments(List<string> args)
        {
            int numberOfArgs = args.Count;

            // parsing for data_folder_path first as it is used later on in the method, if specified
            for (int i = 0; i < numberOfArgs; i++)
            {
                string currentParameter = args[i];
                if (currentParameter.Equals("-data_folder_path", System.StringComparison.OrdinalIgnoreCase))
                {
                    if (++i < args.Count)
                    {
                        m_dataFolderName = args[i];
                        if (!m_dataFolderName.EndsWith("/"))
                        {
                            m_dataFolderName += "/";
                        }
                        m_useCommonDataPath = false;
                    }
                }
            }
            for (int i = 0; i < numberOfArgs; i++)
            {
                string currentParameter = args[i];

                if (currentParameter.Equals("-write_to_file",System.StringComparison.OrdinalIgnoreCase))
                {
                    if (++i < args.Count)
                    {
                        string argValue = args[i]; 
                        if (argValue.Equals("true", System.StringComparison.OrdinalIgnoreCase))
                        {
                            m_writeOutputToFile = true;
                        }
                    }
                }
                if (currentParameter.Equals("-create_character", System.StringComparison.OrdinalIgnoreCase))
                {
                    string characterName = "";
                    if (++i < args.Count)
                    {
                        characterName = args[i].Trim();
                        CreateCharacter(args[i].Trim(), CheckForIdle);
                    }
                    // check if config file is specified. If yes then load it and read it
                    if ((i+1) < args.Count)
                    {
                        string configFile = args[i + 1].Trim();
                        if (File.Exists(m_dataFolderName + configFile))
                        {
                            m_configFile = new IniParser(m_dataFolderName + configFile);
                            ReadConfigFileForCharacter(characterName);
                        }
                        ++i;
                    }
                }
            
                if (currentParameter.Equals("-write_to_file_path", System.StringComparison.OrdinalIgnoreCase))
                {
                    if (++i < args.Count)
                    {
                        m_writeToFilePath = args[i];
                    }
                }                
                if (currentParameter.Equals("-parsetree_cachefile_path", System.StringComparison.OrdinalIgnoreCase))
                {
                    if (++i < args.Count)
                    {                        
                        m_parseCacheFile = args[i].Trim();
                        m_CacheFileSpecified = true;                        
                    }
                }
                if (currentParameter.Equals("-expressions_file_name", System.StringComparison.OrdinalIgnoreCase))
                {
                    if (++i < args.Count)
                    {
                        m_expressionFileName = args[i];
                    }
                }
                if (currentParameter.Equals("-storypoint", System.StringComparison.OrdinalIgnoreCase))
                {
                    if (++i < args.Count)
                    {
                        storyPoint = args[i];
                    }
                }
                if (currentParameter.Equals("-language", System.StringComparison.OrdinalIgnoreCase))
                {
                    if (++i < args.Count)
                    {
                        m_language = args[i];
                    }
                }
                if (currentParameter.Equals("-help", System.StringComparison.OrdinalIgnoreCase))
                {
                    NVBGLogger.Log("NVBG CommandLine Parameters explained:");
                    NVBGLogger.Log("[-write_to_file]  values:[true/false] specifies if the output bml should be written to a file. You can specify the path for this output file using the flag [-write_to_file_path].");
                    NVBGLogger.Log("[-write_to_file_path]  The path where NVBG writes the output bml to file");
                    NVBGLogger.Log("[-data_folder_path]   The path to the xml and xsl folder.");
                    NVBGLogger.Log("[-parsetree_cachefile_path]  The path (including folder path) to the parse file for this agent.");
                    NVBGLogger.Log("[-create_character]  Specify the character name followed by the name of it's corresponding config file. Only the file name is expected. It is assumed to reside at the specified data_folder_path");
                    NVBGLogger.Log("[-language]  Specify the processing language tag (2 letters in captal) in ISO 639 format (e.g. EN for English, FR for French)");
                    return true;
                }
            }
            return false;
        }
        



    }// end class
}// end namespace
