using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml;
using System.IO;


namespace NVBG
{
    public class NVBGCharacter
    {

        // member variables
        private CharacterInfo m_characterInfo;
        private GazeInfo m_gazeInfo;
        private ConversationInfo m_conversationInfo;
        private Dialogue m_currentDialogue;
        private NVBGSwitch m_switch;
        private string m_projectName;
        private XmlDocument m_ruleInputXmlDocument;
        private XmlDocument m_saliencyMapXML;
        private System.Windows.Forms.Timer m_idleTimer;
        public bool m_saliencyMapInitialized = false;
        private List<SaliencyItem> m_saliencyMap;
        private Dictionary<string, List<KeyValuePair<string, float>>> m_gazeObjectMap;
        //private List<GazeIdToPawn> m_gazeIdToPawn;
        private System.Data.DataTable m_calculatorDataTable;

        // properties
        public CharacterInfo AgentInfo { get { return m_characterInfo; } }
        public GazeInfo GazeInfo { get { return m_gazeInfo; } }
        public ConversationInfo ConversationInfo { get { return m_conversationInfo; } }
        public Dialogue CurrentDialogue { get { return m_currentDialogue; } }
        public string ProjectName { get { return m_projectName; } set { m_projectName = value; } }
        public NVBGSwitch Switch { get { return m_switch; } }
        public XmlDocument BehaviorFile { get { return m_ruleInputXmlDocument; }  set { m_ruleInputXmlDocument = value; } }
        public XmlDocument SaliencyMapXML { get { return m_saliencyMapXML; } set { m_saliencyMapXML = value; } } 
        public System.Windows.Forms.Timer IdleTimer { get { return m_idleTimer; }  set { m_idleTimer = value; } }
        public List<SaliencyItem> SaliencyMap { get { return m_saliencyMap; } }
        public bool HasSaliencyMap { get { return m_saliencyMapInitialized; } }
        

        // constructor
        public NVBGCharacter(string _name)
        {
            m_characterInfo = new CharacterInfo();
            m_characterInfo.Name = _name;
            m_gazeInfo = new GazeInfo();
            m_conversationInfo = new ConversationInfo();
            m_currentDialogue = new Dialogue();
            m_switch = new NVBGSwitch();
            m_ruleInputXmlDocument = new XmlDocument();
            m_saliencyMapXML = new XmlDocument();
            m_idleTimer = new System.Windows.Forms.Timer();
            m_projectName = "GENERAL";
        }

        public NVBGCharacter(string _name, EventHandler _eventHandler)
        {
            m_characterInfo = new CharacterInfo();
            m_characterInfo.Name = _name;
            m_gazeInfo = new GazeInfo();
            m_conversationInfo = new ConversationInfo();
            m_currentDialogue = new Dialogue();
            m_switch = new NVBGSwitch();
            m_ruleInputXmlDocument = new XmlDocument();
            m_idleTimer = new System.Windows.Forms.Timer();
            m_idleTimer.Interval = Convert.ToInt32(NVBGSaliencyMap.m_idleGazePeriod * 1000);
            m_idleTimer.Tick += _eventHandler;
            m_idleTimer.Enabled = true;
            m_idleTimer.Start();
            m_projectName = "GENERAL";
            m_saliencyMapXML = new XmlDocument();
            m_saliencyMap = new List<SaliencyItem>();
            m_gazeObjectMap = new Dictionary<string, List<KeyValuePair<string, float>>>();
            //m_gazeIdToPawn = new List<GazeIdToPawn>();

            m_calculatorDataTable = new System.Data.DataTable(); // this should go before sorting saliency map
        }



        public  void setIdleHandler(EventHandler _eventHandler)
        {
            m_idleTimer.Interval = Convert.ToInt32(NVBGSaliencyMap.m_idleGazePeriod * 1000);
            m_idleTimer.Tick += _eventHandler;
            m_idleTimer.Enabled = true;
            m_idleTimer.Start();
        }



        /// <summary>
        /// Loads the XML file
        /// </summary>
        public void LoadXML(string _ruleInputFile)
        {
            if (!File.Exists(_ruleInputFile))
            {
                NVBGLogger.Log("FILE DOES NOT EXIST : " + _ruleInputFile);
                NVBGLogger.Log("Please check file name/path again.");
                return;
            }

            try
            {
               m_ruleInputXmlDocument.Load(_ruleInputFile);

               NVBGLogger.Log("Agent Name: " + m_characterInfo.Name);
               NVBGLogger.Log("Rule Input Xml: " + _ruleInputFile);                
            }
            catch (Exception e)
            {
                NVBGLogger.Log("Error while loading rule_input_file for character " + m_characterInfo.Name + " : ERROR : " + e.ToString());
            }
        }



        /// <summary>
        /// Loads the saliency map for the character
        /// </summary>
        public void LoadSaliencyMap(string _saliencyMap)
        {
            if (!File.Exists(_saliencyMap))
            {
                NVBGLogger.Log("FILE DOES NOT EXIST : " + _saliencyMap);
                NVBGLogger.Log("Please check file name/path again.");
                return;
            }

            try
            {
               m_saliencyMapXML.Load(_saliencyMap);

                NVBGLogger.Log("Agent Name: " + m_characterInfo.Name);
                NVBGLogger.Log("Saliency Map : " + _saliencyMap);
                m_saliencyMapInitialized = true;
            }
            catch (Exception e)
            {
                NVBGLogger.Log("Error while loading saliency map for character " + m_characterInfo.Name + " : ERROR : " + e.ToString());
            }            
        }


        // given one story point, update to new initial saliency map, keyword to object map, and emotion
        public void UpdateOneStoryPoint(string storyPoint)
        {            
            // update saliency map
            XmlNodeList saliencyMapInitNodes = m_saliencyMapXML.GetElementsByTagName("SaliencyMapInit");
            for (int i = 0; i < saliencyMapInitNodes.Count; ++i)
            {
                if ((saliencyMapInitNodes[i].ParentNode.Attributes["name"].Value.Equals(storyPoint)) || (storyPoint == null))
                {
                    XmlNodeList pawnNodes = saliencyMapInitNodes[i].ChildNodes;
                    m_saliencyMap.Clear();
                    for (int j = 0; j < pawnNodes.Count; ++j)
                    {
                        string pawnName = pawnNodes[j].Attributes["name"].Value;
                        float recentness = float.Parse(pawnNodes[j].Attributes["recency"].Value);
                        float primacy = float.Parse(pawnNodes[j].Attributes["primacy"].Value);
                        int k;
                        k = FindSaliencyItem(pawnName);
                        if (k != -1)
                        {
                            m_saliencyMap[k].recency += recentness;
                            m_saliencyMap[k].primacy += primacy;
                        }
                        else
                        {
                            m_saliencyMap.Add(new SaliencyItem(pawnName, recentness, primacy));
                        }
                    }
                    break;
                }
            }
            SortSaliencyMap();

            // update keyword to object map
            XmlNodeList keywordToObjectMapNodes = m_saliencyMapXML.GetElementsByTagName("keywordToObjectMap");
            for (int i = 0; i < keywordToObjectMapNodes.Count; ++i)
            {
                if (keywordToObjectMapNodes[i].ParentNode.Attributes["name"].Value.Equals(storyPoint))
                {
                    XmlNodeList keywordNodes = keywordToObjectMapNodes[i].ChildNodes;
                    m_gazeObjectMap.Clear(); // garbage collection issue
                    for (int j = 0; j < keywordNodes.Count; ++j)
                    {
                        string keyword = keywordNodes[j].Attributes["name"].Value;
                        XmlNodeList pawnNodes = keywordNodes[j].ChildNodes;
                        for (int k = 0; k < pawnNodes.Count; ++k)
                        {
                            string pawn = pawnNodes[k].Attributes["name"].Value;
                            string primacy = pawnNodes[k].Attributes["primacy"].Value;

                            if (!m_gazeObjectMap.ContainsKey(keyword))
                            {
                                List<KeyValuePair<string, float>> pawnList = new List<KeyValuePair<string, float>>();
                                pawnList.Add(new KeyValuePair<string, float>(pawn, float.Parse(primacy)));
                                m_gazeObjectMap.Add(keyword, pawnList);
                            }
                            else
                            {
                                m_gazeObjectMap[keyword].Add(new KeyValuePair<string, float>(pawn, float.Parse(primacy)));
                            }
                        }
                    }
                    break;
                }
            }

            // update emotion
            XmlNodeList emotionInitNodes = m_saliencyMapXML.GetElementsByTagName("emotionInit");
            for (int i = 0; i < emotionInitNodes.Count; ++i)
            {
                if (emotionInitNodes[i].ParentNode.Attributes["name"].Value.Equals(storyPoint))
                {
                    string emotionMessage = "SaliencyMapEmotion " + AgentInfo.Name + " " + emotionInitNodes[i].Attributes["name"].Value;
                    try
                    {
                        NVBGManager.m_vhmsg.SendMessage(emotionMessage);
                    }
                    catch (Exception e)
                    {
                        NVBGLogger.Log(e.ToString());
                    }
                    NVBGLogger.Log(emotionMessage);
                }
            }

            NVBGSaliencyMap.StoryPointView(storyPoint);
        }


        private int FindSaliencyItem(string pawnName)
        {
            int i;
            for (i = 0; i < m_saliencyMap.Count; ++i)
            {
                if (m_saliencyMap[i].objectName.Equals(pawnName))
                {
                    break;
                }
            }
            if (i == m_saliencyMap.Count)
            {
                return -1;
            }
            return i;
        }



        // calculate the priority of each item with the formula
        // then do sorting
        private void SortSaliencyMap()
        {
            for (int i = 0; i < m_saliencyMap.Count; ++i)
            {
                string expr = "";
                for (int j = 0; j < NVBGSaliencyMap.m_priorityFormula.Length; ++j)
                {
                    if (NVBGSaliencyMap.m_priorityFormula[j].Equals('P'))
                    {
                        expr += m_saliencyMap[i].primacy;
                    }
                    else if (NVBGSaliencyMap.m_priorityFormula[j].Equals('R'))
                    {
                        expr += m_saliencyMap[i].recency;
                    }
                    else
                    {
                        expr += NVBGSaliencyMap.m_priorityFormula[j];
                    }
                }
                m_saliencyMap[i].priority = (float)(Convert.ToDouble(m_calculatorDataTable.Compute(expr, "")));
            }
            m_saliencyMap.Sort(CompareSaliencyItem);
            NVBGSaliencyMap.SaliencyMapView(m_saliencyMap, GetIdleGazeRandomRange(), GetKeywordGazeRange());
        }


        private int CompareSaliencyItem(SaliencyItem x, SaliencyItem y)
        {
            // this is a simple native model, should be improved to reflect human mind
            if (x.priority == y.priority)
            {
                return 0;
            }
            else if (x.priority > y.priority)
            {
                return -1;
            }
            else
            {
                return 1;
            }
        }


        // get the threshold for idle gaze
        public int GetIdleGazeRandomRange()
        {
            int randomRange = 0;
            if (m_saliencyMap.Count > 0)
            {
                if (NVBGSaliencyMap.m_idleGazeThresholdNumTypeNum)
                {
                    randomRange = (m_saliencyMap.Count > NVBGSaliencyMap.m_idleGazeThresholdNum) ?
                        NVBGSaliencyMap.m_idleGazeThresholdNum : m_saliencyMap.Count;
                }
                else
                {
                    while (randomRange < m_saliencyMap.Count
                        && m_saliencyMap[randomRange].priority >= NVBGSaliencyMap.m_idleGazeThresholdValue)
                    {
                        randomRange++;
                    }
                }
            }
            return randomRange;
        }


        // get the threshold for keyword triggered gaze
        public int GetKeywordGazeRange()
        {
            int randomRange = 0;
            if (m_saliencyMap.Count > 0)
            {
                if (NVBGSaliencyMap.m_keywordGazeThresholdTypeNum)
                {
                    randomRange = (m_saliencyMap.Count > NVBGSaliencyMap.m_keywordGazeThresholdNum) ?
                        NVBGSaliencyMap.m_keywordGazeThresholdNum : m_saliencyMap.Count;
                }
                else
                {
                    while (randomRange < m_saliencyMap.Count
                        && m_saliencyMap[randomRange].priority >= NVBGSaliencyMap.m_keywordGazeThresholdValue)
                    {
                        randomRange++;
                    }
                }
            }
            return randomRange;
        }



    }

}






