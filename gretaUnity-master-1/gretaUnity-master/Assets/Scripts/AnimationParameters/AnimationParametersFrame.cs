using System;
using System.Collections.Generic;

namespace animationparameters
{
    public class AnimationParametersFrame
    {
        public List<AnimationParameter> APVector;
        long frameNumber = 0;

        public AnimationParametersFrame(int numAPs)
        {
            APVector = new List<AnimationParameter>();
            frameNumber = 0;
            for (int i = 0; i < numAPs; ++i)
                APVector.Add(new AnimationParameter(false, 0));
        }

        public AnimationParametersFrame(AnimationParametersFrame apFrame)
        {
            APVector = new List<AnimationParameter>(apFrame.size());
            frameNumber = apFrame.getFrameNumber();
            for (int i = 0; i < apFrame.size(); ++i)
                APVector.Add(copyAnimationParameter(apFrame.APVector[i]));
            // setAnimationParameter(i, apFrame.getAnimationParameter(i));

        }

        public AnimationParametersFrame(int numAPs, long frameNum)
        {
            APVector = new List<AnimationParameter>(numAPs);
            this.frameNumber = frameNum;
            init(numAPs);
        }

        public long getFrameNumber()
        {
            return frameNumber;
        }

        public void setFrameNumber(long num)
        {
            frameNumber = num;
        }

        public void setAnimationParameter(int number, int value_)
        {
            setAnimationParameter(number, new AnimationParameter(true, value_));
        }

        public void setAnimationParameter(int number, int value_, bool mask)
        {
            setAnimationParameter(number, new AnimationParameter(mask, value_));
        }

        public void setAnimationParameter(int number, AnimationParameter ap)
        {
            APVector[number].set(ap.getMask(), ap.getValue());
        }

        public int size()
        {
            return APVector.Count;
        }

        public List<AnimationParameter> getAnimationParametersList()
        {
            return APVector;
        }

        public AnimationParameter getAnimationParameter(int index)
        {
            return APVector[index];
        }

        public bool getMask(int index)
        {
            return APVector[index].getMask();
        }
        public int getValue(int index)
        {
            return APVector[index].getValue();
        }

        public void setMask(int index, bool mask)
        {
            APVector[index].setMask(mask);
        }

        public void setValue(int index, int value_)
        {
            APVector[index].setValue(value_);
        }

        public String AnimationParametersFrame2String()
        {
            return AnimationParametersFrame2String(frameNumber);
        }

        protected AnimationParameter newAnimationParameter()
        {
            return new AnimationParameter();
        }

        protected AnimationParameter copyAnimationParameter(AnimationParameter ap)
        {
            return new AnimationParameter(ap);
        }

        public AnimationParameter newAnimationParameter(bool mask, int value_)
        {
            return new AnimationParameter(mask, value_);
        }

        public String AnimationParametersFrame2String(long frameNum)
        {
            String buffer = "";
            String mask = "";
            buffer = frameNum + " ";

            for (int i = 0; i < APVector.Count; ++i)
            {
                AnimationParameter ap = APVector[i];
                if (ap.getMask())
                {
                    mask += "1 ";
                    buffer += ap.getValue() + " ";
                }
                else {
                    mask += "0 ";
                }
            }
            String apbuffer = mask + "\n" + buffer + "\n";
            return apbuffer;
        }

        public bool isEqualTo(AnimationParametersFrame apFrame)
        {
            if (this == apFrame)
                return true;
            List<AnimationParameter> apList = this.getAnimationParametersList();
            List<AnimationParameter> apInputList = apFrame.getAnimationParametersList();
            if (apList.Count != apInputList.Count)
            {
                return false;
            }
            for (int i = 0; i < apList.Count; i++)
            {
                if (apInputList[i].getMask() != apList[i].getMask())
                {
                    return false;
                }
                if (apInputList[i].getValue() != apList[i].getValue())
                {
                    return false;
                }
            }

            return true;
        }

        public static String AnimParamFramesList2String(IList<AnimationParametersFrame> frames)
        {
            String apList = "";
            for (int i = 0; i < frames.Count; i++)
                apList += frames[i].AnimationParametersFrame2String(frames[i].getFrameNumber());
            return apList;
        }

        public void init(int size)
        {
            for (int i = 0; i < size; i++)
            {
                AnimationParameter ap = new AnimationParameter();
                APVector.Add(ap);
                ap.setMask(false);
            }
        }

        public void applyValue(int index, int value_)
        {
            APVector[index].setMask(true);
            APVector[index].setValue(value_);
        }

        //FAP part
        public bool getMask(FAPType type)
        {
            return APVector[(int)type].getMask();
        }
        public int getValue(FAPType type)
        {
            return APVector[(int)type].getValue();
        }

        public void setMask(FAPType type, bool mask)
        {
            APVector[(int)type].setMask(mask);
        }

        public void setValue(FAPType type, int value_)
        {
            APVector[(int)type].setValue(value_);
        }
        public void applyValue(FAPType type, int value_)
        {
            APVector[(int)type].setMask(true);
            APVector[(int)type].setValue(value_);
        }

        //BAP part
        private static double radianFactor = 100000;
        public bool getMask(BAPType type)
        {
            return APVector[(int)type].getMask();
        }
        public int getValue(BAPType type)
        {
            return APVector[(int)type].getValue();
        }

        public void setMask(BAPType type, bool mask)
        {
            APVector[(int)type].setMask(mask);
        }

        public void setValue(BAPType type, int value_)
        {
            APVector[(int)type].setValue(value_);
        }
        public void applyValue(BAPType type, int value_)
        {
            APVector[(int)type].setMask(true);
            APVector[(int)type].setValue(value_);
        }

        public double getRadianValue(BAPType type)
        {
            return APVector[(int)type].getValue() / radianFactor;
        }
        public double getRadianValue(int index)
        {
            return APVector[index].getValue() / radianFactor;
        }

        public void setRadianValue(BAPType type, double radian)
        {
            APVector[(int)type].setValue((int)(radian * radianFactor));
            APVector[(int)type].setMask(true);
        }
        public void setRadianValue(int index, double radian)
        {
            APVector[index].setValue((int)(radian * radianFactor));
            APVector[index].setMask(true);
        }
    }
}
