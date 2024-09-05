using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml;

namespace NVBG
{
    // XSL extension Object that gets invoked from the XSL sheet
    class XslObjectMethods
    {
        NVBGManager m_nvbg;

        public XslObjectMethods(NVBGManager _manager)
        {
            m_nvbg = _manager;
        }

        /// <summary>
        /// method is invoked form the .xsl file in data folder. This selects an animation from the xml rule file and inserts it into the final BML
        /// </summary>
        /// <param name="_keyWord"></param>
        /// <param name="_posture"></param>
        /// <param name="_participant"></param>
        /// <returns></returns>
        public string xslGetAnimation(string _keyWord, string _posture, string _participant)
        {

            XmlNodeList rules = m_nvbg.m_characters[_participant].BehaviorFile.GetElementsByTagName("rule");
            XmlNode thisRule;
            XmlNodeList postures;

            NVBGLogger.Log("xslGetAnimation: " + _keyWord + " " + _posture + " " + _participant);

            for (int i = 0; i < rules.Count; ++i)
            {
                thisRule = rules[i];


                try
                {
                    string keyWord = thisRule.Attributes["keyword"].Value;

                    if (keyWord.Equals(_keyWord))
                    {

                        NVBGLogger.Log("Animation acquisition start for " + keyWord);

                        if (keyWord.Equals("idle_gaze"))
                        {
                            XmlNodeList patterns = thisRule.ChildNodes;
                            if (patterns.Count > 0)
                            {
                                Random randomizer = new Random();
                                int temp = randomizer.Next(0, patterns.Count);                                

                                string gazeOffset = patterns[temp].InnerText;
                                return gazeOffset;
                                
                            }
                        }

                        postures = ((XmlElement)thisRule).GetElementsByTagName("posture");

                        for (int j = 0; j < postures.Count; ++j)
                        {

                            string posture = postures[j].Attributes["name"].Value;

                            NVBGLogger.Log("posture: " + posture + " _posture: " + _posture);

                            if (posture.Equals(_posture))
                            {
                                XmlNodeList clips = postures[j].ChildNodes;
                                if (clips.Count < 1)
                                {
                                    return "none";
                                }
                                else
                                {
                                    //Random randomizer = new Random();
                                    Random randomizer = new Random(Guid.NewGuid().GetHashCode());
                                    int temp = randomizer.Next(0,clips.Count);                                    

                                    string animationName = clips[temp].InnerText;

                                    NVBGLogger.Log("Randomly selected animation: " + animationName);

                                    return animationName;
                                }
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    NVBGLogger.Log(e.ToString());
                }
            }
                return "none";
        }
    }
}
