using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml;
using System.IO;

namespace NVBG
{   
    // one item in saliency map
    public class SaliencyItem {
        public string objectName;
        public float recency;
        public float primacy;
        public float priority;

        public SaliencyItem(string objName, float rece, float prim) 
        {
            objectName = objName;
            recency = rece;
            primacy = prim;
        }
    }

    // store info related to a gaze id
    // gaze id is used for tracking specific gaze
    public class GazeIdToPawn {
        public string gazeId;
        public string pawnName;
        public float timePassed;
        public float rank;
        public float priority;

        public GazeIdToPawn(string gId, string pName, float tPassed, float ra, float pri)
        {
            gazeId = gId;
            pawnName = pName;
            timePassed = tPassed;
            rank = ra;
            priority = pri;
        }
    }

    // saliency map is for generating subconscious gazes,
    // when character is in idle state
    // or certain keyword is mentioned
    public class SaliencyMap
    {
        private List<SaliencyItem> m_saliencyMap;
        private Dictionary<string, List<KeyValuePair<string,float>>> m_gazeObjectMap;
        private static XmlDocument m_inputDoc;
        private VHMessage m_currentMessage;
        private NVBGCharacter m_data;
        private XmlNode m_bmlNode;
        //private static XmlDocument m_storyPointInputXmlDocument;
        private bool m_generateGaze = false;
        private int m_maxSaliencyItemNum = 10;
        private const float m_recentness_max = 20;
        private const float m_recentness_min = -20;
        private const float m_recentness_int = 0;
        private const float m_gazeNotExecuteThreshold = 15;
        private const float m_primacy_max = 15;
        private int m_currentGazeId = 0;
        // track all the sent out gazes
        private List<GazeIdToPawn> m_gazeIdToPawn;
        // regular update
        private const float m_updateTimeInterval = 0.1f; // in secs
        private System.Windows.Forms.Timer m_updateTimer;
        // priority formula calculator
        private System.Data.DataTable m_calculatorDataTable;
        private string m_storyPoint = "RangerEnters1";
        // saccade
        private System.Windows.Forms.Timer m_saccadeUpdateTimer;
        bool m_inSaccadeFixation = true;
        float m_saccadeFixationTime = 0;
        public static bool m_isInitialized = false;

        public void init(string dataFolderName, bool useCommonDataPath, string storyPoint)
        {
            m_saliencyMap = new List<SaliencyItem>();
            m_gazeObjectMap = new Dictionary<string, List<KeyValuePair<string, float>>>();
            m_gazeIdToPawn = new List<GazeIdToPawn>();

            m_calculatorDataTable = new System.Data.DataTable(); // this should go before sorting saliency map

            // load initial file
            //string fileName = "saliency_map_init_" + m_data.AgentInfo.Name + ".xml";
            //loadFile(ref m_storyPointInputXmlDocument, dataFolderName, useCommonDataPath, fileName);
            //updateOneStoryPoint(storyPoint);

            m_updateTimer = new System.Windows.Forms.Timer();
            m_updateTimer.Interval = (int)(m_updateTimeInterval * 1000);
            m_updateTimer.Tick += new EventHandler(update);
            m_updateTimer.Enabled = true;
            m_updateTimer.Start();

            m_saccadeUpdateTimer = new System.Windows.Forms.Timer();
            m_saccadeUpdateTimer.Interval = (int)(NVBGSaliencyMap.m_saccadePeriod * 1000);
            m_saccadeUpdateTimer.Tick += new EventHandler(updateSaccade);
            m_saccadeUpdateTimer.Enabled = true;
            m_saccadeUpdateTimer.Start();
            m_isInitialized = true;
        }       

        

        // 
        public void ProcessMessage(XmlDocument _inputDoc, VHMessage _currentMessage, NVBGCharacter _data)
        {
            m_inputDoc = _inputDoc;
            m_currentMessage = _currentMessage;
            m_data = _data;
            //m_storyPointInputXmlDocument = m_data.SaliencyMapXML;
            //updateOneStoryPoint(m_storyPoint, m_data);

            GetBmlNode();

            string gazePawnName = "";
            int pawnRank = -1;
            float pawnPriority = -1;
            if ((m_currentMessage.Type.Equals("vrAgentSpeech") || m_currentMessage.Type.Equals("vrSpeech")) && (m_data.Switch.allBehaviour))
            {
                XmlNode ruleTag = m_inputDoc.CreateElement("rule");

                //you are listening to a word
                if (m_currentMessage.Type.Equals("vrAgentSpeech")) // if the word is spoken by another virtual human
                {
                    m_generateGaze = processOneWord(m_currentMessage.MessageString, ref gazePawnName, ref pawnRank, ref pawnPriority);
                }
                else if (m_currentMessage.Type.Equals("vrSpeech")) // if the word is spoken by the user
                { 
                    string[] words = _currentMessage.MessageString.Split(" ".ToCharArray());
                    // only use the last pawn to generate gaze, the previous pawns are only used for update Saliency Map
                    for (int i = 0; i < words.Length; ++i)
                    {
                        string tmpPawn = "";
                        int tmpPawnRank = -1;
                        float tmpPawnPriority = -1;
                        if (processOneWord(words[i], ref tmpPawn, ref tmpPawnRank, ref tmpPawnPriority))
                        {
                            m_generateGaze = true;
                            gazePawnName = tmpPawn;
                            pawnRank = tmpPawnRank;
                            pawnPriority = tmpPawnPriority;
                        }
                    }
                }

                if (m_generateGaze)
                {
                    XMLHelperMethods.AttachAttributeToNode(m_inputDoc, ruleTag, "type", "idle_gaze");
                    XMLHelperMethods.AttachAttributeToNode(m_inputDoc, ruleTag, "participant", m_data.AgentInfo.Name);
                    XMLHelperMethods.AttachAttributeToNode(m_inputDoc, ruleTag, "priority", "1");
                    XMLHelperMethods.AttachAttributeToNode(m_inputDoc, ruleTag, "pose", m_data.AgentInfo.Posture);
                    XMLHelperMethods.AttachAttributeToNode(m_inputDoc, ruleTag, "target", gazePawnName);
                    XMLHelperMethods.AttachAttributeToNode(m_inputDoc, ruleTag, "id", generateGazeId(gazePawnName, pawnRank, pawnPriority));
                    _inputDoc.GetElementsByTagName("bml")[0].AppendChild(ruleTag);
                    NVBGLogger.Log("Saliency gaze for listener triggered");
                    m_generateGaze = false;
                }
            }
            else if (!m_currentMessage.Type.Equals("listen") && (m_data.Switch.allBehaviour))
            {               
                // if you are the speaker
                string priorityValue = "1";
                XmlNodeList markList = m_inputDoc.GetElementsByTagName("mark");
                for (int i = 0; i < markList.Count; i = i + 2)
                {
                    XmlNode currentMark = markList[i];
                    XmlNode wordNode = currentMark.NextSibling;
                    XmlNode parentNode = currentMark.ParentNode;

                    m_generateGaze = processOneWord(wordNode.InnerText, ref gazePawnName, ref pawnRank, ref pawnPriority);

                    if (m_generateGaze)
                    {
                        XmlNode docRuleNode = m_inputDoc.CreateElement("rule");
                        XMLHelperMethods.AttachAttributeToNode(m_inputDoc, docRuleNode, "type", "fmlbml_gaze");
                        XMLHelperMethods.AttachAttributeToNode(m_inputDoc, docRuleNode, "pose", m_data.AgentInfo.Posture);
                        XMLHelperMethods.AttachAttributeToNode(m_inputDoc, docRuleNode, "emotion", m_data.AgentInfo.Emotion);
                        XMLHelperMethods.AttachAttributeToNode(m_inputDoc, docRuleNode, "priority", priorityValue);
                        XMLHelperMethods.AttachAttributeToNode(m_inputDoc, docRuleNode, "prev_target", m_data.CurrentDialogue.Listener);
                        XMLHelperMethods.AttachAttributeToNode(m_inputDoc, docRuleNode, "target", gazePawnName);
                        XMLHelperMethods.AttachAttributeToNode(m_inputDoc, docRuleNode, "id", generateGazeId(gazePawnName, pawnRank, pawnPriority));
                        parentNode.InsertBefore(docRuleNode, currentMark);
                        NVBGLogger.Log("Saliency gaze for speaker triggered");
                        m_generateGaze = false;
                    }
                }
            }
        }

        // check the word mapped objects in saliency map
        // update primacy
        private bool processOneWord(string text, ref string gazePawnName, ref int pawn_rank, ref float pawn_priority) {
            //look up the word in object map
            gazePawnName = "unknown";
            if (m_gazeObjectMap.ContainsKey(text))
            {
                Random randomizer = new Random();
                int randomNumber = randomizer.Next() % m_gazeObjectMap[text].Count;
                string randomPawnName = m_gazeObjectMap[text][randomNumber].Key;

                for (int i = 0; i < m_gazeObjectMap[text].Count; ++i)
                {
                    string pawnName = m_gazeObjectMap[text][i].Key;
                    float primacy = m_gazeObjectMap[text][i].Value;

                    int j = findSaliencyItem(pawnName);
                    if (j != -1)
                    {
                        m_saliencyMap[j].primacy += primacy;
                        if (m_saliencyMap[j].primacy > m_primacy_max)
                        {
                            m_saliencyMap[j].primacy = m_primacy_max;
                        }
                        sortSaliencyMap();
                    }
                    else
                    {
                        m_saliencyMap.Add(new SaliencyItem(pawnName, m_recentness_int, primacy));
                        sortSaliencyMap();
                        // remove items if exceeds maximal item number
                        while (m_saliencyMap.Count > m_maxSaliencyItemNum)
                        {
                            m_saliencyMap.RemoveAt(m_saliencyMap.Count - 1);
                        }
                    }
                }

                int k = findSaliencyItem(randomPawnName);
                if (k == -1)
                {
                    return false;
                }
                else if (k < getKeywordGazeRange())
                {
                    gazePawnName = randomPawnName;
                    pawn_rank = k;
                    pawn_priority = m_saliencyMap[k].priority;
                    return true;
                }
                else
                {
                    return false;
                }
            }
            return false;
        }

        private void processOneWord(string text)
        { 
            string pawn = "";
            int pawnRank = -1;
            float pawnPriority = -1;
            processOneWord(text, ref pawn, ref pawnRank, ref pawnPriority);
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

        // regular update at certain frequency
        // decrease recency as time passed by
        public void update(object sender, EventArgs eArgs) {
            if (sender == m_updateTimer)
            {
                if (!m_storyPoint.Equals(NVBGSaliencyMap.m_storyPoint))
                {
                    //updateOneStoryPoint(NVBGSaliencyMap.m_storyPoint);
                    m_storyPoint = NVBGSaliencyMap.m_storyPoint;
                }
                else
                {
                    for (int i = 0; i < m_saliencyMap.Count; ++i)
                    {
                        m_saliencyMap[i].recency -= m_updateTimeInterval;
                        // currently disable removing the expired items
                        //if (m_saliencyMap[i].recentness < m_recentness_min)
                        //{
                        //    NVBGLogger.Log("Remove Saliency Item " + m_saliencyMap[i].objectName);
                        //    m_saliencyMap.RemoveAt(i);
                        //    --i;
                        //}
                        if (m_saliencyMap[i].recency < m_recentness_min)
                        {
                            m_saliencyMap[i].recency = m_recentness_min;
                        }
                    }
                    for (int i = 0; i < m_gazeIdToPawn.Count; ++i)
                    {
                        m_gazeIdToPawn[i].timePassed += m_updateTimeInterval;
                        if (m_gazeIdToPawn[i].timePassed > m_gazeNotExecuteThreshold)
                        {
                            int j = findSaliencyItem(m_gazeIdToPawn[i].pawnName);
                            if (j != -1)
                            {
                                m_saliencyMap[j].recency = m_recentness_int;
                                //sortSaliencyMap();
                            }
                            m_gazeIdToPawn.RemoveAt(i);
                            --i;
                        }
                    }
                }
                sortSaliencyMap();
            }
        }

        // generate saccade at set frequency
        public void updateSaccade(object sender, EventArgs eArgs)
        {
            if (NVBGSaliencyMap.m_useSaccade && !m_inSaccadeFixation)
            {
                // generate a random direction and a random magnitude
                Random randomizer = new Random();
                int direction = randomizer.Next() % 180;
                int sign = (randomizer.Next() % 2 == 0) ? -1 : 1;
                int magnitude = sign * NVBGSaliencyMap.m_saccadeMagnitude;
                string message = "sbm bml char ";
                message += m_data.AgentInfo.Name;
                message += " <saccade magnitude=\"";
                message += magnitude.ToString();
                message += "\" sbm:duration=\"";
                message += NVBGSaliencyMap.m_saccadePeriod.ToString();
                message += "\" direction=\"";
                message += direction.ToString();
                message += "\"/>";
                try
                {
                    NVBGManager.m_vhmsg.SendMessage(message);
                }
                catch (Exception ex)
                {
                    NVBGLogger.Log(ex.ToString());
                }
                if (NVBGSaliencyMap.m_showSaccadeMessage)
                {
                    NVBGLogger.Log(message);
                }
                m_saccadeFixationTime += (float)m_saccadeUpdateTimer.Interval / 1000;
                if (m_saccadeFixationTime > NVBGSaliencyMap.m_saccadeFixationPeriod)
                {
                    m_inSaccadeFixation = true;
                    m_saccadeFixationTime = 0;
                }
            }
            else if (m_inSaccadeFixation)
            {
                m_saccadeFixationTime += (float)m_saccadeUpdateTimer.Interval / 1000;
                if (m_saccadeFixationTime > NVBGSaliencyMap.m_saccadeFixationLength)
                {
                    m_inSaccadeFixation = false;
                }
            }
        }

        // generate idle gaze
        public void generateGazeCommand(XmlDocument _inputDoc, NVBGCharacter _data)
        {            
            m_inputDoc = _inputDoc;
            m_data = _data;
            m_saliencyMap = _data.SaliencyMap;

            // get the pawn with highest priority
            string pawn = "none";
            float priority = -1;
            int randomRange = getIdleGazeRandomRange();
            if (randomRange > 0)
            {
                // choose a random object to gaze
                XmlNode ruleTag = _inputDoc.CreateElement("rule");

                Random randomizer = new Random();
                int randomNum = randomizer.Next() % randomRange;
                float p = (float)randomNum / (float)randomRange;

                // biased random model
                float c = (float)Math.Pow(2, NVBGSaliencyMap.m_bias);
                float a = (c - 1);
                float b = -2 * a;
                // sample of x ~(0,1): biased sampling
                float sum1 = 0;
                float[] xrange = new float[randomRange];
                for (int i = 0; i < xrange.Length; ++i)
                {
                    xrange[i] = (float)Math.Pow(NVBGSaliencyMap.m_bias, i);
                    sum1 += xrange[i];
                }
                for (int i = 0; i < xrange.Length; ++i)
                {
                    xrange[i] = xrange[i] / sum1;
                }
                float[] x = new float[randomRange];
                x[0] = 0;
                for (int i = 1; i < x.Length; ++i)
                {
                    x[i] = xrange[x.Length - i] + x[i - 1];
                }
                float[] samples = new float[randomRange];
                float sum2 = 0;
                for (int i = 0; i < samples.Length; i++)
                { 
                    //float x = 1 / (float)randomRange * (float)i;
                    samples[i] = a * x[i] * x[i] + b * x[i] + c;
                    sum2 += samples[i];
                }
                // normalize
                for (int i = 0; i < samples.Length; ++i)
                {
                    samples[i] = samples[i] / sum2;
                }
                // define range
                float[] ranges = new float[randomRange];
                ranges[0] = samples[0];
                for (int i = 1; i < ranges.Length; ++i)
                {
                    ranges[i] = ranges[i - 1] + samples[i];   
                }
                int chooseId = 0;
                for (int i = 0; i < ranges.Length; i++)
                {
                    if (p <= ranges[i])
                    {
                        chooseId = i;
                        break;
                    }
                }

                pawn = m_saliencyMap[chooseId].objectName;
                priority = m_saliencyMap[chooseId].priority;

                XMLHelperMethods.AttachAttributeToNode(m_inputDoc, ruleTag, "type", "idle_gaze");
                XMLHelperMethods.AttachAttributeToNode(m_inputDoc, ruleTag, "participant", m_data.AgentInfo.Name);
                XMLHelperMethods.AttachAttributeToNode(m_inputDoc, ruleTag, "priority", "1");
                XMLHelperMethods.AttachAttributeToNode(m_inputDoc, ruleTag, "pose", m_data.AgentInfo.Posture);
                XMLHelperMethods.AttachAttributeToNode(m_inputDoc, ruleTag, "target", pawn);
                XMLHelperMethods.AttachAttributeToNode(m_inputDoc, ruleTag, "id", generateGazeId(pawn, chooseId, priority));
                NVBGLogger.Log("Saliency gaze for idle triggered");
                _inputDoc.GetElementsByTagName("bml")[0].AppendChild(ruleTag);
            }
        }

        // get the threshold for idle gaze
        public int getIdleGazeRandomRange()
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
        public int getKeywordGazeRange()
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

        // check to see if a gaze is finished
        public void checkForFinishedGaze(VHMessage _currentMessage)
        {
            for (int i = 0; i < m_gazeIdToPawn.Count; i++)
            {
                if (m_gazeIdToPawn[i].gazeId.Equals(_currentMessage.MsgId))
                {
                    m_gazeIdToPawn.RemoveAt(i);
                    --i;
                }
            }
        }

        // track a generated gaze by having sbm send back a message when finish the gaze
        public void trackGazeEvent(XmlDocument _inputDoc, NVBGCharacter _data)
        { 
            // check if certain gaze rule tag is still there. if yes, attach a sbm:event rule to it
            XmlNode bmlTag = _inputDoc.GetElementsByTagName("bml")[0];

            XmlNodeList ruleList = _inputDoc.GetElementsByTagName("gaze");
            for (int i = 0; i < ruleList.Count; i++)
            {
                for (int j = 0; j < ruleList[i].Attributes.Count; j++) 
                {
                    if (ruleList[i].Attributes.Item(j).Name.Equals("id"))
                    {
                        string id = ruleList[i].Attributes.Item(j).Value;
          
                        XmlNode eventTag = _inputDoc.CreateElement("sbm:event");
                        XMLHelperMethods.AttachAttributeToNode(_inputDoc, eventTag, "xmlns:sbm", "http://ict.usc.edu");
                        string messageAttribute = "SaliencyMapGaze " + _data.AgentInfo.Name + " " + id + " complete";
                        string strokeAttribute = id + ":" + "stroke";
                        XMLHelperMethods.AttachAttributeToNode(_inputDoc, eventTag, "message", messageAttribute);
                        XMLHelperMethods.AttachAttributeToNode(_inputDoc, eventTag, "stroke", strokeAttribute);
                        bmlTag.AppendChild(eventTag);
                    }
                }
            }
        }

        // change the joint range for the gaze
        public void updateGazeRange(XmlDocument _inputDoc)
        {
            XmlNodeList gazeList = _inputDoc.GetElementsByTagName("gaze");
            for (int i = 0; i < gazeList.Count; ++i)
            {
                int gazeId = -1;
                int jointRangeId = -1;
                for (int j = 0; j < gazeList[i].Attributes.Count; ++j)
                {
                    if (gazeList[i].Attributes.Item(j).Name.Equals("id"))
                    {
                        gazeId = j;
                    }
                    if (gazeList[i].Attributes.Item(j).Name.Equals("sbm:joint-range"))
                    {
                        jointRangeId = j;
                    }
                    if (gazeId != -1 && jointRangeId != -1)
                    {
                        if (NVBGSaliencyMap.m_fixedJointRange)
                        {
                            // TODO: check if the gaze id existed, if so, get its priority and change gaze type
                            for (int k = 0; k < m_gazeIdToPawn.Count; ++k)
                            {
                                if (m_gazeIdToPawn[k].gazeId.Equals(gazeList[i].Attributes[gazeId].Value))
                                {
                                    Random randomizer = new Random();
                                    int randomNum = randomizer.Next() % 100;
                                    int m = 0;
                                    if (NVBGSaliencyMap.m_jointRangeThresholdTypeNum)
                                    {
                                        while (m < (int)NVBGSaliencyMap.jointRange.num && m_gazeIdToPawn[k].rank < NVBGSaliencyMap.m_jointRangeThresholdNum[m]
                                            && randomNum < NVBGSaliencyMap.m_currentJointRangeRate * 100)
                                        {
                                            ++m;
                                        }
                                    }
                                    else
                                    {
                                        while (m < (int)NVBGSaliencyMap.jointRange.num && m_gazeIdToPawn[k].priority > NVBGSaliencyMap.m_jointRangeThresholdValue[m]
                                            && randomNum < NVBGSaliencyMap.m_currentJointRangeRate * 100)
                                        {
                                            ++m;
                                        }
                                    }
                                    if (m > 0)
                                    {
                                        m = m - 1;
                                    }
                                    string jointRange = "";
                                    switch (m)
                                    { 
                                        case (int)NVBGSaliencyMap.jointRange.eye:
                                            jointRange = "EYES";
                                            break;
                                        case (int)NVBGSaliencyMap.jointRange.eyeNeck:
                                            jointRange = "EYES NECK";
                                            break;
                                        case (int)NVBGSaliencyMap.jointRange.eyeChest:
                                            jointRange = "EYES CHEST";
                                            break;
                                        case (int)NVBGSaliencyMap.jointRange.eyeBack:
                                            jointRange = "EYES BACK";
                                            break;
                                        default:
                                            jointRange = "EYES NECK";
                                            break;
                                    }
                                    gazeList[i].Attributes.Item(jointRangeId).Value = jointRange;
                                    // the following is not a safe way
                                    if (i > 0)
                                    {
                                        for (int p = 0; p < gazeList[i - 1].Attributes.Count; ++p)
                                        {
                                            if (gazeList[i - 1].Attributes.Item(p).Name.Equals("sbm:joint-range"))
                                            {
                                                gazeList[i - 1].Attributes.Item(p).Value = jointRange;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            gazeList[i].Attributes.Item(jointRangeId).Value = NVBGSaliencyMap.m_jointRange;
                            // the following is not a safe way
                            if (i > 0)
                            {
                                for (int k = 0; k < gazeList[i - 1].Attributes.Count; ++k)
                                {
                                    if (gazeList[i - 1].Attributes.Item(k).Name.Equals("sbm:joint-range"))
                                    {
                                        gazeList[i - 1].Attributes.Item(k).Value = NVBGSaliencyMap.m_jointRange;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // an event is treated like a keyword, 
        // and will update the primacy of the related object in saliency map
        public void updateEvent(string eventName)
        {
            processOneWord(eventName);
        }

        // an emotion will affect the frequency of the gaze
        // set the new frequency
        public void updateEmotion(string emotion, ref int idleTimerInterval)
        {
            switch (emotion)
            {
                case "anxious": idleTimerInterval = 1; break;
                case "angry": idleTimerInterval = 3; break;
                case "happy": idleTimerInterval = 2; break;
                case "sad": idleTimerInterval = 10; break;
                case "feared": idleTimerInterval = 10; break;
                case "neutral": idleTimerInterval = 3; break;
                default: idleTimerInterval = 3; break;
            }
            NVBGSaliencyMap.EmotionView(emotion);
        }

        // update the primacy of a pawn to specific value in saliency map
        public void updatePawnPrimacy(string pawnPrimacy)
        {
            string[] args = pawnPrimacy.Split(" ".ToCharArray());
            string pawn = args[0];
            float primacy = float.Parse(args[1]);
            int i = findSaliencyItem(pawn);
            if (i != -1)
            {
                m_saliencyMap[i].primacy = primacy;
                sortSaliencyMap();
            }
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

        // calculate the priority of each item with the formula
        // then do sorting
        private void sortSaliencyMap()
        {
            for (int i = 0; i < m_saliencyMap.Count; ++i)
            {
                string expr = "";
                for (int j = 0; j < NVBGSaliencyMap.m_priorityFormula.Length; ++j) {
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
            NVBGSaliencyMap.SaliencyMapView(m_saliencyMap, getIdleGazeRandomRange(), getKeywordGazeRange());
        }

        private int findSaliencyItem(string pawnName)
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

        // generate and store a gaze id with related info
        private string generateGazeId(string pawn, int pawnRank, float pawnPriority)
        {
            if (m_currentGazeId > 1000) {
                m_currentGazeId = 0;
            }
            string id = m_data.AgentInfo.Name + "-" + m_currentGazeId.ToString();
            m_gazeIdToPawn.Add(new GazeIdToPawn(id, pawn, 0, pawnRank, pawnPriority));
            m_currentGazeId++;
            int i = findSaliencyItem(pawn);
            if (i != -1)
            {
                m_saliencyMap[i].recency = NVBGSaliencyMap.m_recencyAfterGaze;
                sortSaliencyMap();
            }
            return id;
        }
    }
}
