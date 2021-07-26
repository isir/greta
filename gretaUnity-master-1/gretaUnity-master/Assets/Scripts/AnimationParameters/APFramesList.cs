using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.CompilerServices;
using UnityEngine;

namespace animationparameters
{
    public class APFramesList
    {
        private List<AnimationParametersFrame> apFramesList;
        int numAPs;

        public APFramesList(int apFrameLength)
        {
            apFramesList = new List<AnimationParametersFrame>();
            numAPs = apFrameLength;
        }

        public APFramesList(AnimationParametersFrame firstAPFrame)
        {
            apFramesList = new List<AnimationParametersFrame>();
            numAPs = firstAPFrame.APVector.Count;
            apFramesList.Add(firstAPFrame);
            firstAPFrame.setFrameNumber(0);
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public void addFrame(AnimationParametersFrame apFrame)
        {
            // Debug.Log("addFrame number: " + apFrame.getFrameNumber());
            int framesListLenght = apFramesList.Count;
            long numberOfLastFrame = apFramesList[framesListLenght - 1].getFrameNumber();
            if (numberOfLastFrame >= apFrame.getFrameNumber())
            {
                for (int i = framesListLenght - 1; i >= 0; i--)
                {
                    if (apFrame.getFrameNumber() > apFramesList[i].getFrameNumber())
                    {
                        apFramesList.Insert(i + 1, apFrame);
                        break;
                    }
                    if (apFrame.getFrameNumber() == apFramesList[i].getFrameNumber())
                    {
                        for (int j = 0; j < apFrame.size(); j++)
                        {
                            AnimationParameter ap = apFrame.getAnimationParametersList()[j];

                            if (ap.getMask())
                            {
                                apFramesList[i].setValue(j, ap.getValue());
                                apFramesList[i].setMask(j, true);
                            }
                        }
                        //apFramesList [i] = apFrame;
                        break;
                    }
                }
            }
            else {
                apFramesList.Add(apFrame);
            }
        }

        public void addAPFrames(List<AnimationParametersFrame> apFrames, String id)
        {
            foreach (AnimationParametersFrame apFrame in apFrames)
            {
                addFrame(apFrame);
            }
        }

        public void addAPFramesFromFile(String fileName, long firstFrameNumber)
        {
            StringReader apDataReader = null;
            // apData is a string containing the whole file. To be read line-by-line
            //Debug.Log ("firstAPFrame number " + firstFrameNumber);
            TextAsset apData = (TextAsset)Resources.Load(fileName, typeof(TextAsset));
            long firstFileFrameNum = 0;
            bool firstFrame = true;
            if (apData == null)
            {
                Debug.Log(fileName + " not found");
            }
            else {
                apDataReader = new StringReader(apData.text);
                if (apDataReader == null)
                {
                    Debug.Log(fileName + " not readable");
                }
                else {
                    String readLine = "";
                    // Read each line from the file
                    // Debug.Log ("APFrameList APnum " +numAPs + " at time " + DateTime.Now.Ticks/10000);

                    while ((readLine = apDataReader.ReadLine()) != null)
                    {
                        String firstLine = readLine;
                        String secondLine = apDataReader.ReadLine();

                        String[] firstLineTab = firstLine.Split(' ');
                        String[] secondLineTab = secondLine.Split(' ');
                        /* // Debug of line content
                        String firstLineTabStr = "";
                        for(int i=0; i<firstLineTab.Length;i++){
                        firstLineTabStr += firstLineTab[i];
                        }
                        firstLineTabStr += "End";
                        Debug.Log ("FirstLineTab "+ firstLineTabStr);*/
                        // Debug.Log ("first line length "+ firstLineTab.Length);
                        // Debug.Log ("second line length "+ secondLineTab.Length);
                        int firstLineIter = 0;
                        int secondLineIter = 0;
                        // frameNum
                        if (firstFrame)
                        {
                            firstFileFrameNum = long.Parse(secondLineTab[secondLineIter]);
                            firstFrame = false;
                        }
                        long frameNum = long.Parse(secondLineTab[secondLineIter]) - firstFileFrameNum;
                        secondLineIter++;

                        int apnr = 1;

                        AnimationParametersFrame frame = new AnimationParametersFrame(numAPs);
                        frame.setFrameNumber(firstFrameNumber + frameNum);

                        while (firstLineIter < numAPs - 1)
                        {
                            // Debug.Log (firstLineTab[firstLineIter]);
                            int mask = int.Parse(firstLineTab[firstLineIter]);
                            firstLineIter++;

                            if (mask == 1)
                            {
                                int apValue = int.Parse(secondLineTab[secondLineIter]);
                                secondLineIter++;
                                frame.setAnimationParameter(apnr, apValue);
                            }//end more tokens
                            else {
                                frame.setAnimationParameter(apnr, 0);
                            }
                            apnr++;
                        }
                        this.addFrame(frame);
                    }
                    // Debug.Log (fileName + " loaded at time " + DateTime.Now.Ticks/10000);
                }
            }
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public void emptyFramesList()
        {
            AnimationParametersFrame firstAPFrame = new AnimationParametersFrame(peek());
            apFramesList.Clear();
            apFramesList.Add(firstAPFrame);
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public void updateFrames(long currentFrameNumber)
        {
            long currentFrame = currentFrameNumber;
            AnimationParametersFrame firstAPFrame = peek();
            for (int j = 0; j < apFramesList.Count; j++)
            {
                AnimationParametersFrame apFrame = apFramesList[j];
                if (apFrame.getFrameNumber() > currentFrame)
                {
                    break;
                }
                if (apFrame != firstAPFrame)
                {
                    for (int i = 0; i < apFrame.size(); i++)
                    {
                        AnimationParameter ap = apFrame.getAnimationParametersList()[i];

                        if (ap.getMask())
                        {
                            firstAPFrame.setValue(i, ap.getValue());
                            firstAPFrame.setMask(i, true);
                        }
                    }

                    apFramesList.RemoveAt(j);
                    j--;
                }
            }
            // add as peek frame
            firstAPFrame.setFrameNumber(currentFrame);

            //apFramesList.Insert (0, firstAPFrame);
        }

        public void afficheFirstFrame()
        {
            AnimationParametersFrame firstAPFrame = peek();
            String strMask = "";
            String strValue = "";
            for (int i = 0; i < firstAPFrame.size(); i++)
            {
                AnimationParameter ap = firstAPFrame.getAnimationParametersList()[i];
                strMask += ap.getMask() + " ";
                if (ap.getMask())
                {
                    strValue += ap.getValue() + " ";
                }
            }
            Debug.Log("firstAPFrame Mask: \n" + strMask + "\nand value: \n" + strValue);
        }

        public AnimationParametersFrame getCurrentFrame(long currentFrameNumber)
        {
            updateFrames(currentFrameNumber);
            /* if(peek (1)!=null){
            //Debug.Log ("second frame "+ peek (1).getFrameNumber ());
            }*/
            return peek();
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public AnimationParametersFrame peek()
        {
            if (apFramesList.Count == 0)
                return null;
            else
                return apFramesList[0];
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public AnimationParametersFrame peek(int index)
        {
            //Debug.Log ("apFramesList.Count: "+apFramesList.Count+" index: " +index);
            if (apFramesList.Count <= index)
            {
                return null;
            }
            else
                return apFramesList[index];
        }
    }
}
