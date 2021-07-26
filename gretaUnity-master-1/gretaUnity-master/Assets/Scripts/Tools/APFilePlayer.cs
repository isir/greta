using System;
using UnityEngine;
using animationparameters;

namespace tools
{
    public class APFilePlayer
    {
        private int numberOfAP;
        APFramesList apFramesList;
        AnimationParametersFrame lastFrame;

        public APFilePlayer(int numOfAP)
        {
            numberOfAP = numOfAP;
            lastFrame = new AnimationParametersFrame(numberOfAP, 0);
            apFramesList = new APFramesList(lastFrame);
        }

        public AnimationParametersFrame getCurrentFrame(long currentTime)
        {
            //Debug.Log ("current frame: "+ apFramesList.getCurrentFrame(currentTime));
            return apFramesList.getCurrentFrame(currentTime);
        }

        public AnimationParametersFrame newAnimParamFrame(int frameNumber)
        {
            return new AnimationParametersFrame(frameNumber);
        }

        public void playAPFile(String fileName, long time)
        {

            Debug.Log("APFilePlayer numberOfAP " + numberOfAP);
            apFramesList.addAPFramesFromFile(fileName, time / 40);
        }

        public void emptyFrameList()
        {
            apFramesList.emptyFramesList();
        }
    }
}
