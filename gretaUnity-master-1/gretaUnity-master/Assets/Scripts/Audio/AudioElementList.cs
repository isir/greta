using System;
using System.Collections.Generic;
using System.Runtime.CompilerServices;
using UnityEngine;

namespace audioElements
{
    public class AudioElementList
    {
        private List<AudioElement> audioElementList;

        public AudioElementList()
        {
            audioElementList = new List<AudioElement>();
            audioElementList.Add(new AudioElement());
            //ELISABETTA
            //I commented the add of a first element in the list (it causes errors). I cannot find if it is really necessary.
        }

        public AudioElementList(AudioElement firstAudioElement)
        {
            audioElementList = new List<AudioElement>();
            audioElementList.Add(firstAudioElement);
            //ELISABETTA
            //I commented the add of a first element in the list (it causes errors). I cannot find if it is really necessary.
            //It seems to me that this constructor is never called.
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public void addAudioElement(AudioElement audioElement)
        {
            Debug.Log("addAudioElement of framenumber: " + audioElement.getFrameNumber() + " " + audioElement.getName() + " " + audioElement.getId() + " " + audioElement.rawData.Length);
            int audioElementListLenght = audioElementList.Count;
            long frameNumberOfLastAudioElement = audioElementList[audioElementListLenght - 1].getFrameNumber();
            if (frameNumberOfLastAudioElement >= audioElement.getFrameNumber())
            {
                for (int i = audioElementListLenght - 1; i >= 0; i--)
                {
                    if (audioElement.getFrameNumber() > audioElementList[i].getFrameNumber())
                    {
                        audioElementList.Insert(i + 1, audioElement);
                        break;
                    }
                    else {
                        audioElementList[i] = audioElement;
                        break;
                    }

                }
            }
            else {
                audioElementList.Add(audioElement);
            }
        }

        public void addAudioElements(List<AudioElement> audioElementList, String id)
        {
            foreach (AudioElement audioElement in audioElementList)
            {
                addAudioElement(audioElement);
            }
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public void updateAudioElements(long currentFrame)
        {
            long currentFrameNumber = currentFrame;
            AudioElement firstAudioElement = peek();
            for (int j = 0; j < audioElementList.Count; j++)
            {
                AudioElement audioElement = audioElementList[j];
                if (audioElement.getFrameNumber() > currentFrameNumber)
                {
                    break;
                }
                if (audioElement != firstAudioElement)
                {
                    firstAudioElement.setName(audioElement.getName());
                    firstAudioElement.setFrameNumber(audioElement.getFrameNumber());
                    firstAudioElement.setId(audioElement.getId());

                    //EB : I need to copy the raw data buffer too
                    firstAudioElement.setRawData(audioElement.getRawData());
                    //BR : I need the sampleRate too
                    firstAudioElement.setSampleRate(audioElement.getSampleRate());
                    audioElementList.RemoveAt(j);
                    j--;
                }
            }
            // add as peek frame
            //firstAudioElement.setFrameNumber (currentFrameNumber);

            //audioElementList.Insert (0, firstAPFrame);
        }

        public AudioElement getCurrentAudioElement(long currentFrameNumber)
        {
            updateAudioElements(currentFrameNumber);
            return peek();
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public AudioElement peek()
        {
            if (audioElementList.Count == 0)
                return null;
            else
                return audioElementList[0];
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public void Clear()
        {
            this.audioElementList.Clear();
        }
    }
}
