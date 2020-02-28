/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.core.util.laugh;

import greta.core.util.Constants;
import greta.core.util.IniManager;
import greta.core.util.audio.Audio;
import greta.core.util.time.TimeMarker;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 *
 * @author Yu Ding
 */
public class PhonemeGeneratorLaughSynthetizer implements LaughSynthetizer{

    private Laugh laugh;
    private List<LaughPhoneme> phonems;
    private Audio audio;

    @Override
    public void setLaugh(Laugh laugh) {
        this.laugh = laugh;
        phonems = null;
        audio = null;
    }

    public int[] readIntStream(String signalPath)
    {
       ArrayList <Integer> dataList = new ArrayList <Integer>();
       Scanner scannerSample;
                        try {
                            scannerSample= new Scanner(new File(signalPath));
                            scannerSample.useLocale(Locale.ENGLISH);
                            int i = 0;
                            while(scannerSample.hasNextInt()){
                                  dataList.add(scannerSample.nextInt());
                                  i++;
                            }
                        }
                        catch(FileNotFoundException e){
                            e.printStackTrace();
                        }
       int[] data = new int[dataList.size()];
            for (int i=0; i < dataList.size(); i++)
            {
                data[i] = dataList.get(i);
            }
       return data;
    }

    public double[] readDoubleStream(String signalPath)
    {
        ArrayList <Double> dataList = new ArrayList <Double>();
        Scanner scannerSamplePitch;
                        try {
                            scannerSamplePitch= new Scanner(new File(signalPath));
                            scannerSamplePitch.useLocale(Locale.ENGLISH);
                            int i = 0;
                            while(scannerSamplePitch.hasNextDouble()){
                                  dataList.add(scannerSamplePitch.nextDouble());
                                  i++;
                            }
                            System.out.println("    ");
                        }
                        catch(FileNotFoundException e){
                            e.printStackTrace();
                        }
       double[] data = new double[dataList.size()];
            for (int i=0; i < dataList.size(); i++)
            {
                data[i] = dataList.get(i);
            }
       return data;
    }

    public void delete0DurationPhoneme(ArrayList <Integer> phoSeqList, ArrayList <Integer> durSeqList, int[] phoSeqIndex0, int[] durSeq0)
    {
                for (int i=0; i<durSeq0.length; i++){
                     if (durSeq0[i]!=0){
                         phoSeqList.add(phoSeqIndex0[i]);
                         durSeqList.add(durSeq0[i]);
                     }
                }
    }
    public void setPhonemeDurationSequence(int[] phoSeqIndex, int[] durSeq, ArrayList <Integer> phoSeqList, ArrayList <Integer> durSeqList)
    {
                for (int i=0; i<phoSeqList.size(); i++){
                    phoSeqIndex[i] = phoSeqList.get(i);
                    if ((phoSeqIndex[i]==11)||(phoSeqIndex[i]==13)||(phoSeqIndex[i]==14)){
                        phoSeqIndex[i]=4;
                    }
                }
                for (int i=0; i<durSeqList.size(); i++){
                    durSeq[i] = durSeqList.get(i);
                }
    }

    public int getLengthAllPhonemesLHMMFrequence(int[] durSeqLHMMFrequence)
    {
                int lengthAllPhonemesLHMMFrequence = 0;
                for (int nbDur = 0; nbDur<durSeqLHMMFrequence.length; nbDur++){
                    lengthAllPhonemesLHMMFrequence = lengthAllPhonemesLHMMFrequence + durSeqLHMMFrequence[nbDur];
                }
                return lengthAllPhonemesLHMMFrequence;
    }

    public double[] setPhoIntensityByFrame(double[] phoIntensityByFrame, double[] phonemeIntensity, int[] durSeqLHMMFrequence)
    {
    //=======================================//
                int time = 0;
                for (int nbDur = 0; nbDur<durSeqLHMMFrequence.length; nbDur++){
                    for (int nbPhoenemLastingTime = 0; nbPhoenemLastingTime<durSeqLHMMFrequence[nbDur]; nbPhoenemLastingTime++){
                         phoIntensityByFrame[time] = phonemeIntensity[nbDur];
                         time++;
                    }
                }
    //=======================================//
                return phoIntensityByFrame;
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void compute(boolean doTemporize, boolean doAudio, boolean doPhonemes) {

        //int phoSeqIndex[];
        int phoSeqIndex[] = null;
        int durSeq[] = null;
        int durSeqLHMMFrequence[] = null;

        double frameRate = Constants.FRAME_PER_SECOND;


        double duration = getDefaultDuration();
        TimeMarker start = laugh.getStart();
        TimeMarker end = laugh.getEnd();
        if (start.isConcretized()) {
            if (end.isConcretized()) {
                duration = end.getValue() - start.getValue();
            } else {
                end.setValue(start.getValue() + duration);
            }
        } else {
            if (end.isConcretized()) {
                start.setValue(end.getValue() - duration);
            } else {
                start.setValue(0);
                end.setValue(duration);
            }
        }

        double intensity = laugh.getIntensity();


        //String sequenceName = "1_170846_183622";

        if(doAudio) {
            //duration = 24;
            //intensity = 0.8;
            if ((duration >= 22) && (duration <= 25) && (intensity > 0.7)){
                String audioFileName = "CarolineSt45.wav";
                System.out.println("selected audio =:  "+audioFileName);
                try {
                    audio = Audio.getAudio(audioFileName);
                    System.out.println(" getAudio: "+audioFileName);
                } catch (Exception ex) {
                    Audio.getEmptyAudio();
                }
            }
            else if ((duration >= 25) && (duration <= 30) && (intensity > 0.7)){
                String audioFileName = "CarolineSt3.wav";
                System.out.println("selected audio =:  "+audioFileName);
                try {
                    audio = Audio.getAudio(audioFileName);
                    System.out.println(" getAudio: "+audioFileName);
                } catch (Exception ex) {
                    Audio.getEmptyAudio();
                }
            }
            else if ((duration >= 30) && (duration <= 32) && (intensity > 0.7)){
                String audioFileName = "CarolineSt7.wav";
                System.out.println("selected audio =:  "+audioFileName);
                try {
                    audio = Audio.getAudio(audioFileName);
                    System.out.println(" getAudio: "+audioFileName);
                } catch (Exception ex) {
                    Audio.getEmptyAudio();
                }
            }
            else if ((duration >= 5) && (duration <= 8.5) && intensity > 0.7){
                String audioFileName = "CarolineSt41.wav";
                System.out.println("selected audio =:  "+audioFileName);
                try {
                    audio = Audio.getAudio(audioFileName);
                    System.out.println(" getAudio: "+audioFileName);
                } catch (Exception ex) {
                    Audio.getEmptyAudio();
                }
            }
            else if ((duration >= 15) && (duration <= 22) && intensity > 0.7){
                String audioFileName = "CarolineSt24.wav";
                System.out.println("selected audio =:  "+audioFileName);
                // this wav lasts 21 seconds in fact.
                try {
                    audio = Audio.getAudio(audioFileName);
                    System.out.println(" getAudio: "+audioFileName);
                } catch (Exception ex) {
                    Audio.getEmptyAudio();
                }
            }
            else if ((duration <= 5) && intensity > 0.7){
                String audioFileName = "CarolineSt22.wav";
                System.out.println("selected audio =:  "+audioFileName);
                // this wav lasts 4 seconds in fact.
                try {
                    audio = Audio.getAudio(audioFileName);
                    System.out.println(" getAudio: "+audioFileName);
                } catch (Exception ex) {
                    Audio.getEmptyAudio();
                }
            }
            else if ((duration >= 11.5) && (duration <= 15) && intensity > 0.7){
                String audioFileName = "CarolineSt4.wav";
                System.out.println("selected audio =:  "+audioFileName);
                // this wav lasts 11 seconds in fact.
                try {
                    audio = Audio.getAudio(audioFileName);
                    System.out.println(" getAudio: "+audioFileName);
                } catch (Exception ex) {
                    Audio.getEmptyAudio();
                }
            }
            else if ((duration >= 8.5) && (duration <= 11.5) && intensity > 0.7){
                String audioFileName = "CarolineSt6.wav";
                System.out.println("selected audio =:  "+audioFileName);
                // this wav lasts 11 seconds in fact.
                try {
                    audio = Audio.getAudio(audioFileName);
                    System.out.println(" getAudio: "+audioFileName);
                } catch (Exception ex) {
                    Audio.getEmptyAudio();
                }
            }
            else if ((duration < 13) && (duration > 11) && intensity > 0.7) {
                String audioFileName = "1_170846_183622.wav";
                try {
                    audio = Audio.getAudio(audioFileName);
                    System.out.println(" getAudio: "+audioFileName);
                } catch (Exception ex) {
                    Audio.getEmptyAudio();
                }
            }
            else if ((duration < 5) && (duration > 3) && intensity > 0.7) {
                String audioFileName = "4_422890_427542.wav";
                try {
                    audio = Audio.getAudio(audioFileName);
                    System.out.println(" getAudio: "+audioFileName);
                } catch (Exception ex) {
                    Audio.getEmptyAudio();
                }
            }
            else if ((duration < 2) && (duration > 0) && intensity > 0.7) {
                String audioFileName = "1_79609_81496.wav";
                try {
                    audio = Audio.getAudio(audioFileName);
                    System.out.println(" getAudio: "+audioFileName);
                } catch (Exception ex) {
                    Audio.getEmptyAudio();
                }
            }
            else if ((duration < 14) && (duration > 12) && intensity > 0.7) {
                String audioFileName = "4_650876_662210.wav";
                try {
                    audio = Audio.getAudio(audioFileName);
                    System.out.println(" getAudio: "+audioFileName);
                } catch (Exception ex) {
                    Audio.getEmptyAudio();
                }
            }
            else if ((duration < 9) && (duration > 7) && intensity > 0.7) {
                String audioFileName = "4_668629_676815.wav";
                try {
                    audio = Audio.getAudio(audioFileName);
                    System.out.println(" getAudio: "+audioFileName);
                } catch (Exception ex) {
                    Audio.getEmptyAudio();
                }
            }

        }
        if(doPhonemes) {
            double[]speechEnergy = null;
            double[]speechPitch = null;
            double[]phonemeIntensity = null;
            double[]phoIntensityByFrame = null;  // phoneme intensity is extended and copied as phoneme intensity by frame
            String signalPath = null;
            //================= new code =====================================//
            int[] phoSeqIndex0 = null;
            int[] durSeq0 = null;
            String pathInputInformation = IniManager.getProgramPath()+"./Common/Data/laughMotionTorsoHeadLHMM/audioInformation/";
            if ((duration >= 22) && (duration <= 25) && (intensity > 0.7)){
                System.out.println(" getAudioSignal: "+"new codes");
                signalPath = pathInputInformation+"\\"+"CarolineSt45"+"\\"+"phoSeqIndex0.txt";
                phoSeqIndex0 = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt45"+"\\"+"durSeq0.txt";
                durSeq0 = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt45"+"\\"+"durSeqLHMMFrequence.txt";
                durSeqLHMMFrequence = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt45"+"\\"+"phonemeIntensity.txt";
                phonemeIntensity = readDoubleStream(signalPath);

                //========== delete durSeq0 equals to 0 ===========================//
                        ArrayList <Integer> phoSeqList = new ArrayList <Integer>();
                        ArrayList <Integer> durSeqList = new ArrayList <Integer>();
                        delete0DurationPhoneme(phoSeqList, durSeqList, phoSeqIndex0, durSeq0);
                        phoSeqIndex = new int[phoSeqList.size()];
                        durSeq = new int[durSeqList.size()];
                        setPhonemeDurationSequence(phoSeqIndex, durSeq, phoSeqList, durSeqList);
                //================== load pitch ======================//
                signalPath = pathInputInformation+"\\"+"CarolineSt45"+"\\"+"pitchFeature.txt";
                speechPitch = readDoubleStream(signalPath);
                //================== load energy ======================//
                signalPath = pathInputInformation+"\\"+"CarolineSt45"+"\\"+"energyFeature.txt";
                speechEnergy = readDoubleStream(signalPath);

                //============ load phoneme frame intensity=============//
                int lengthAllPhonemesLHMMFrequence = getLengthAllPhonemesLHMMFrequence(durSeqLHMMFrequence);
                phoIntensityByFrame = new double[lengthAllPhonemesLHMMFrequence];
                setPhoIntensityByFrame(phoIntensityByFrame, phonemeIntensity, durSeqLHMMFrequence);
            }
            else if ((duration >= 11.5) && (duration <= 15) && intensity > 0.7){
                // "CarolineSt4.wav";
                //System.out.println("CarolineSt4.wav is taken");
                signalPath = pathInputInformation+"\\"+"CarolineSt4"+"\\"+"phoSeqIndex0.txt";
                phoSeqIndex0 = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt4"+"\\"+"durSeq0.txt";
                durSeq0 = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt4"+"\\"+"durSeqLHMMFrequence.txt";
                durSeqLHMMFrequence = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt4"+"\\"+"phonemeIntensity.txt";
                phonemeIntensity = readDoubleStream(signalPath);

                //========== delete durSeq0 equals to 0 ===========================//
                        ArrayList <Integer> phoSeqList = new ArrayList <Integer>();
                        ArrayList <Integer> durSeqList = new ArrayList <Integer>();
                        delete0DurationPhoneme(phoSeqList, durSeqList, phoSeqIndex0, durSeq0);
                        phoSeqIndex = new int[phoSeqList.size()];
                        durSeq = new int[durSeqList.size()];
                        setPhonemeDurationSequence(phoSeqIndex, durSeq, phoSeqList, durSeqList);
                //================== load pitch ======================//
                signalPath = pathInputInformation+"\\"+"CarolineSt4"+"\\"+"pitchFeature.txt";
                speechPitch = readDoubleStream(signalPath);
                //================== load energy ======================//
                signalPath = pathInputInformation+"\\"+"CarolineSt4"+"\\"+"energyFeature.txt";
                speechEnergy = readDoubleStream(signalPath);

                //============ load phoneme frame intensity=============//
                int lengthAllPhonemesLHMMFrequence = getLengthAllPhonemesLHMMFrequence(durSeqLHMMFrequence);
                phoIntensityByFrame = new double[lengthAllPhonemesLHMMFrequence];
                setPhoIntensityByFrame(phoIntensityByFrame, phonemeIntensity, durSeqLHMMFrequence);
            }
            else if ((duration >= 25) && (duration <= 30) && (intensity > 0.7)){
                // "CarolineSt3.wav";
                System.out.println("CarolineSt3.wav is taken");
                signalPath = pathInputInformation+"\\"+"CarolineSt3"+"\\"+"phoSeqIndex0.txt";
                phoSeqIndex0 = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt3"+"\\"+"durSeq0.txt";
                durSeq0 = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt3"+"\\"+"durSeqLHMMFrequence.txt";
                durSeqLHMMFrequence = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt3"+"\\"+"phonemeIntensity.txt";
                phonemeIntensity = readDoubleStream(signalPath);

                //========== delete durSeq0 equals to 0 ===========================//
                        ArrayList <Integer> phoSeqList = new ArrayList <Integer>();
                        ArrayList <Integer> durSeqList = new ArrayList <Integer>();
                        delete0DurationPhoneme(phoSeqList, durSeqList, phoSeqIndex0, durSeq0);
                        phoSeqIndex = new int[phoSeqList.size()];
                        durSeq = new int[durSeqList.size()];
                        setPhonemeDurationSequence(phoSeqIndex, durSeq, phoSeqList, durSeqList);
                //================== load pitch ======================//
                signalPath = pathInputInformation+"\\"+"CarolineSt3"+"\\"+"pitchFeature.txt";
                speechPitch = readDoubleStream(signalPath);
                //================== load energy ======================//
                signalPath = pathInputInformation+"\\"+"CarolineSt3"+"\\"+"energyFeature.txt";
                speechEnergy = readDoubleStream(signalPath);

                //============ load phoneme frame intensity=============//
                int lengthAllPhonemesLHMMFrequence = getLengthAllPhonemesLHMMFrequence(durSeqLHMMFrequence);
                phoIntensityByFrame = new double[lengthAllPhonemesLHMMFrequence];
                setPhoIntensityByFrame(phoIntensityByFrame, phonemeIntensity, durSeqLHMMFrequence);
            }
            else if ((duration >= 30) && (duration <= 32) && (intensity > 0.7)){
                // "CarolineSt7.wav";
                //System.out.println("CarolineSt7.wav is taken");
                signalPath = pathInputInformation+"\\"+"CarolineSt7"+"\\"+"phoSeqIndex0.txt";
                phoSeqIndex0 = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt7"+"\\"+"durSeq0.txt";
                durSeq0 = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt7"+"\\"+"durSeqLHMMFrequence.txt";
                durSeqLHMMFrequence = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt7"+"\\"+"phonemeIntensity.txt";
                phonemeIntensity = readDoubleStream(signalPath);

                //========== delete durSeq0 equals to 0 ===========================//
                        ArrayList <Integer> phoSeqList = new ArrayList <Integer>();
                        ArrayList <Integer> durSeqList = new ArrayList <Integer>();
                        delete0DurationPhoneme(phoSeqList, durSeqList, phoSeqIndex0, durSeq0);
                        phoSeqIndex = new int[phoSeqList.size()];
                        durSeq = new int[durSeqList.size()];
                        setPhonemeDurationSequence(phoSeqIndex, durSeq, phoSeqList, durSeqList);
                //================== load pitch ======================//
                signalPath = pathInputInformation+"\\"+"CarolineSt7"+"\\"+"pitchFeature.txt";
                speechPitch = readDoubleStream(signalPath);
                //================== load energy ======================//
                signalPath = pathInputInformation+"\\"+"CarolineSt7"+"\\"+"energyFeature.txt";
                speechEnergy = readDoubleStream(signalPath);

                //============ load phoneme frame intensity=============//
                int lengthAllPhonemesLHMMFrequence = getLengthAllPhonemesLHMMFrequence(durSeqLHMMFrequence);
                phoIntensityByFrame = new double[lengthAllPhonemesLHMMFrequence];
                setPhoIntensityByFrame(phoIntensityByFrame, phonemeIntensity, durSeqLHMMFrequence);
            }
            else if ((duration >= 5) && (duration <= 8.5) && intensity > 0.7){
                // "CarolineSt41.wav";
                System.out.println("CarolineSt41.wav is taken");
                signalPath = pathInputInformation+"\\"+"CarolineSt41"+"\\"+"phoSeqIndex0.txt";
                phoSeqIndex0 = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt41"+"\\"+"durSeq0.txt";
                durSeq0 = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt41"+"\\"+"durSeqLHMMFrequence.txt";
                durSeqLHMMFrequence = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt41"+"\\"+"phonemeIntensity.txt";
                phonemeIntensity = readDoubleStream(signalPath);
                //phoSeqIndex0 = new int[]{1,11,6,6,11,1,12,8,1,7,1,8,7,12,6,12,6,12,6,12,6,12,1,12,6,12,6,12,1,8,1,11,1,11,1,10,1,6,6,8,1,6,1};
                //durSeq0 = new int[]{11,6,14,4,6,9,14,9,17,4,97,12,7,8,10,6,10,10,7,11,4,15,2,16,5,14,3,14,3,8,33,3,11,4,27,5,17,14,7,33,55,28,13};
                //durSeqLHMMFrequence = new int[]{15,10,18,6,8,13,18,13,23,6,135,16,9,10,16,8,14,12,11,15,6,19,4,22,7,18,5,20,3,12,45,5,15,6,35,9,23,18,11,45,75,40,16};
                //phonemeIntensity = new double[]{1.3740,1.8670,3.6580,4.4070,3.6010,1.9000,5.0460,1.9490,1.7240,1.4650,1.7570,3.5660,4.0340,5.9180,3.1250,5.8920,3.5970,5.9220,3.3310,8.7140,1.8560,7.4000,2.0780,9.7620,2.4600,9.5450,1.9650,4.7120,2.3440,2.2280,1.8730,1.6340,1.5610,1.8940,1.6130,1.5660,1.8660,3.4150,3.0430,2.5750,1.7470,1.0210,0.7090};
                //========== delete durSeq0 equals to 0 ===========================//
                        ArrayList <Integer> phoSeqList = new ArrayList <Integer>();
                        ArrayList <Integer> durSeqList = new ArrayList <Integer>();
                        delete0DurationPhoneme(phoSeqList, durSeqList, phoSeqIndex0, durSeq0);
                        phoSeqIndex = new int[phoSeqList.size()];
                        durSeq = new int[durSeqList.size()];
                        setPhonemeDurationSequence(phoSeqIndex, durSeq, phoSeqList, durSeqList);
                //================== load pitch ======================//
                signalPath = pathInputInformation+"\\"+"CarolineSt41"+"\\"+"pitchFeature.txt";
                speechPitch = readDoubleStream(signalPath);
                //================== load energy ======================//
                signalPath = pathInputInformation+"\\"+"CarolineSt41"+"\\"+"energyFeature.txt";
                speechEnergy = readDoubleStream(signalPath);

                //============ load phoneme frame intensity=============//
                int lengthAllPhonemesLHMMFrequence = getLengthAllPhonemesLHMMFrequence(durSeqLHMMFrequence);
                phoIntensityByFrame = new double[lengthAllPhonemesLHMMFrequence];
                setPhoIntensityByFrame(phoIntensityByFrame, phonemeIntensity, durSeqLHMMFrequence);
            }
            else if ((duration >= 15) && (duration <= 22) && intensity > 0.7){
                // "CarolineSt24.wav";
                System.out.println("CarolineSt24.wav is taken");
                signalPath = pathInputInformation+"\\"+"CarolineSt24"+"\\"+"phoSeqIndex0.txt";
                phoSeqIndex0 = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt24"+"\\"+"durSeq0.txt";
                durSeq0 = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt24"+"\\"+"durSeqLHMMFrequence.txt";
                durSeqLHMMFrequence = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt24"+"\\"+"phonemeIntensity.txt";
                phonemeIntensity = readDoubleStream(signalPath);
                //phoSeqIndex0 = new int[]{1,6,1,7,12,6,12,6,12,6,12,6,12,6,12,6,12,1,11,1,10,1,12,6,11,1,11,1,12,1,12,1,11,1,11,1,11,1,11,1,8,1,12,1,7,11,1,12,1,12,1,11,1,11,1,11,1,11,1,11,1,6,12,1,11,1,7,1,6,1,11,6,12,6,12,1,12,6,1,4,1,11,1,11,1,8,6,12,6,12,6,12,6,12,1,12,1,6,6,8,1,11,1,4,1,12,6,1,12,1,11,1,6,12,11,1,8,1,10,1,8,1,8,1,8,1,7,6,1,11,1,6,8,1,11,1,12,1,11,11,1};
                //durSeq0 = new int[]{36,18,2,5,3,2,4,1,10,2,4,2,4,2,4,2,4,3,0,2,14,17,2,5,0,1,2,2,3,1,5,19,1,5,1,4,2,4,2,7,4,1,1,4,3,3,2,6,2,5,4,2,3,1,5,2,5,2,3,2,3,3,2,1,1,8,1,56,11,6,1,2,4,2,4,1,4,2,3,2,6,2,4,2,1,3,2,3,1,4,2,3,1,4,1,15,1,2,2,1,2,2,10,2,16,6,5,1,4,1,2,1,2,4,2,3,6,4,4,4,5,4,4,6,4,6,1,2,14,2,4,3,3,1,2,2,4,0,3,5,6};
                //durSeqLHMMFrequence = new int[]{153,75,7,21,13,9,14,6,40,10,17,6,19,7,17,7,18,11,3,7,57,71,9,19,2,5,6,10,12,4,19,80,6,18,7,16,6,20,4,30,17,4,5,16,11,16,7,25,7,21,19,5,14,5,21,6,23,6,14,8,13,13,6,4,7,32,5,232,46,23,5,9,14,11,14,5,18,8,14,6,26,6,19,7,5,13,7,12,6,14,9,13,7,15,4,61,6,9,5,8,4,11,40,7,67,25,21,6,18,2,8,6,6,19,6,13,24,18,18,14,24,13,20,21,18,24,4,10,57,10,15,11,14,3,12,7,15,3,9,22,21};
                //phonemeIntensity = new double[]{1.3360,0.9780,1.7550,3.1250,4.9240,3.8450,4.9640,2.7080,6.8640,2.5530,6.8470,2.4760,6.7570,2.3820,5.1160,2.4560,4.3680,2.7010,1.9890,1.7410,1.5280,1.2770,2.7220,4.9510,4.4460,1.8600,3.8240,1.8250,4.9930,2.0680,4.8410,2.1420,1.9390,1.4540,1.9530,1.0760,1.9190,1.9110,1.8200,1.6620,4.3760,1.8740,2.2890,2.1140,4.4640,2.3790,2.1060,4.1470,1.7770,4.4540,2.2690,2.4170,1.2580,1.5520,1.6430,2.7050,1.3550,2.2070,1.2430,2.0960,1.7840,3.8560,4.1000,2.9460,2.3250,1.4810,1.8560,1.6300,1.2730,1.4800,1.8540,3.1810,5.3320,3.9380,6.7340,1.9460,5.3780,2.0160,1.7690,0.8860,1.4320,1.9080,1.8180,1.7530,1.8040,5.3670,2.4630,7.0140,1.9430,10.1520,5.1140,12.0350,2.8360,11.3690,2.4240,6.7010,2.2310,3.1980,4.2940,2.6250,1.7520,1.9000,1.6160,1.4770,1.1540,4.0050,3.0550,1.9190,4.0320,2.9940,2.2810,1.8150,3.2590,4.9530,3.7140,1.7800,5.2640,2.1500,4.3940,4.2860,5.3910,3.0760,4.4420,1.7100,5.0060,2.4410,5.0140,2.9520,1.8260,3.3950,1.8860,2.5540,5.9020,2.1210,2.8640,1.8620,4.7300,2.9110,1.9180,4.3500,1.7060};
                //========== delete durSeq0 equals to 0 ===========================//
                        ArrayList <Integer> phoSeqList = new ArrayList <Integer>();
                        ArrayList <Integer> durSeqList = new ArrayList <Integer>();
                        delete0DurationPhoneme(phoSeqList, durSeqList, phoSeqIndex0, durSeq0);
                        phoSeqIndex = new int[phoSeqList.size()];
                        durSeq = new int[durSeqList.size()];
                        setPhonemeDurationSequence(phoSeqIndex, durSeq, phoSeqList, durSeqList);
                //================== load pitch ======================//
                signalPath = pathInputInformation+"\\"+"CarolineSt24"+"\\"+"pitchFeature.txt";
                speechPitch = readDoubleStream(signalPath);
                //================== load energy ======================//
                signalPath = pathInputInformation+"\\"+"CarolineSt24"+"\\"+"energyFeature.txt";
                speechEnergy = readDoubleStream(signalPath);

                //============ load phoneme frame intensity=============//
                int lengthAllPhonemesLHMMFrequence = getLengthAllPhonemesLHMMFrequence(durSeqLHMMFrequence);
                phoIntensityByFrame = new double[lengthAllPhonemesLHMMFrequence];
                setPhoIntensityByFrame(phoIntensityByFrame, phonemeIntensity, durSeqLHMMFrequence);
            }
            else if ((duration <= 5) && intensity > 0.7){
                // "CarolineSt22.wav";
                System.out.println("CarolineSt22.wav is taken");
                signalPath = pathInputInformation+"\\"+"CarolineSt22"+"\\"+"phoSeqIndex0.txt";
                phoSeqIndex0 = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt22"+"\\"+"durSeq0.txt";
                durSeq0 = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt22"+"\\"+"durSeqLHMMFrequence.txt";
                durSeqLHMMFrequence = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt22"+"\\"+"phonemeIntensity.txt";
                phonemeIntensity = readDoubleStream(signalPath);
                //phoSeqIndex0 = new int[]{1,2,4,2,4,1,2,10,1,2,7,1,2,1,2,1,10,1,11,1,10,2,6,1};
                //durSeq0 = new int[]{8,17,13,28,15,11,14,10,11,10,8,11,8,54,8,29,26,41,2,91,10,14,45,122};
                //durSeqLHMMFrequence = new int[]{7,13,12,23,12,9,12,9,9,7,7,9,7,45,7,24,22,33,3,75,9,11,37,101};
                //phonemeIntensity = new double[]{1.7050,1.9050,3.6600,1.3460,3.8390,1.6790,1.2480,1.4990,1.5660,1.2850,1.8590,1.5250,1.2840,1.6450,1.2300,1.2580,1.5400,1.5450,1.3710,1.6240,1.8790,1.6860,1.9670,1.7700};
                //========== delete durSeq0 equals to 0 ===========================//
                        ArrayList <Integer> phoSeqList = new ArrayList <Integer>();
                        ArrayList <Integer> durSeqList = new ArrayList <Integer>();
                        delete0DurationPhoneme(phoSeqList, durSeqList, phoSeqIndex0, durSeq0);
                        phoSeqIndex = new int[phoSeqList.size()];
                        durSeq = new int[durSeqList.size()];
                        setPhonemeDurationSequence(phoSeqIndex, durSeq, phoSeqList, durSeqList);
                //================== load pitch ======================//
                signalPath = pathInputInformation+"\\"+"CarolineSt22"+"\\"+"pitchFeature.txt";
                speechPitch = readDoubleStream(signalPath);
                //================== load energy ======================//
                signalPath = pathInputInformation+"\\"+"CarolineSt22"+"\\"+"energyFeature.txt";
                speechEnergy = readDoubleStream(signalPath);

                //============ load phoneme frame intensity=============//
                int lengthAllPhonemesLHMMFrequence = getLengthAllPhonemesLHMMFrequence(durSeqLHMMFrequence);
                phoIntensityByFrame = new double[lengthAllPhonemesLHMMFrequence];
                setPhoIntensityByFrame(phoIntensityByFrame, phonemeIntensity, durSeqLHMMFrequence);
            }
            else if ((duration >= 8.5) && (duration <= 15) && intensity > 0.7){
                 // "CarolineSt6.wav";
                System.out.println("CarolineSt6.wav is taken");
                signalPath = pathInputInformation+"\\"+"CarolineSt6"+"\\"+"phoSeqIndex0.txt";
                phoSeqIndex0 = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt6"+"\\"+"durSeq0.txt";
                durSeq0 = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt6"+"\\"+"durSeqLHMMFrequence.txt";
                durSeqLHMMFrequence = readIntStream(signalPath);
                signalPath = pathInputInformation+"\\"+"CarolineSt6"+"\\"+"phonemeIntensity.txt";
                phonemeIntensity = readDoubleStream(signalPath);
                //phoSeqIndex0 = new int[]{1,12,6,7,6,12,6,12,6,12,6,12,6,12,1,4,1,7,8,6,11,1,6,1,10,9,6,1,12,1,11,1,11,1,11,1,11,1,11,1,11,1,11,1,6,1,12,6,2,6,1,11,1,8,1,6,1};
                //durSeq0 = new int[]{0,7,13,6,5,7,5,8,3,8,4,8,4,7,6,2,30,3,3,16,3,3,24,9,3,13,28,12,5,3,4,15,4,32,2,8,2,22,3,28,2,4,4,11,14,20,4,14,11,6,21,4,33,10,38,15,27};
                //durSeqLHMMFrequence = new int[]{1,14,30,13,9,15,11,17,6,17,9,18,8,15,13,5,63,7,6,34,7,6,51,19,7,28,60,25,10,7,9,31,10,68,4,18,4,46,6,60,5,9,8,24,29,44,8,30,24,13,45,7,72,21,82,32,55};
                //phonemeIntensity = new double[]{3.9090,5.8650,5.1280,8.9210,5.2340,8.3390,5.8700,7.6470,3.8890,5.7360,3.5580,5.5680,2.9580,5.0900,2.7190,1.5820,1.7610,3.7560,3.1030,2.5690,3.4490,3.6530,3.4900,1.8920,3.3230,1.6100,1.1790,0.9970,2.8620,1.7180,3.1040,2.4420,2.3370,1.8360,1.7390,1.3570,1.4530,1.7530,2.0550,1.8510,1.4150,1.6270,2.0850,2.0810,3.0570,2.0790,3.7950,3.9720,3.4620,3.7400,2.4340,1.8160,1.1650,2.8800,1.8420,1.1370,1.1970};
                //========== delete durSeq0 equals to 0 ===========================//
                        ArrayList <Integer> phoSeqList = new ArrayList <Integer>();
                        ArrayList <Integer> durSeqList = new ArrayList <Integer>();
                        delete0DurationPhoneme(phoSeqList, durSeqList, phoSeqIndex0, durSeq0);
                        phoSeqIndex = new int[phoSeqList.size()];
                        durSeq = new int[durSeqList.size()];
                        setPhonemeDurationSequence(phoSeqIndex, durSeq, phoSeqList, durSeqList);
                //================== load pitch ======================//
                signalPath = pathInputInformation+"\\"+"CarolineSt6"+"\\"+"pitchFeature.txt";
                speechPitch = readDoubleStream(signalPath);
                //================== load energy ======================//
                signalPath = pathInputInformation+"\\"+"CarolineSt6"+"\\"+"energyFeature.txt";
                speechEnergy = readDoubleStream(signalPath);

                //============ load phoneme frame intensity=============//
                int lengthAllPhonemesLHMMFrequence = getLengthAllPhonemesLHMMFrequence(durSeqLHMMFrequence);
                phoIntensityByFrame = new double[lengthAllPhonemesLHMMFrequence];
                setPhoIntensityByFrame(phoIntensityByFrame, phonemeIntensity, durSeqLHMMFrequence);
            }

            double totalTimeTemp = 0.0;
            for (int i = 0; i<durSeq0.length; i++)
            {
                totalTimeTemp = totalTimeTemp + durSeq0[i]/frameRate;
            }
            System.out.println("totalTimeTemp = "+ totalTimeTemp);

            if((duration > 20) && (duration < 25) && intensity > 0.7){

            }
            else if ((duration < 13) && (duration > 11) && intensity > 0.7) {
                //phoSeqIndex = new int[]{1, 7, 6, 7, 6, 7, 1, 7, 1, 6, 6, 7, 6, 7, 1, 7, 1, 1, 6, 6, 1, 6, 8, 6, 7, 6, 7, 1, 7, 1, 8, 1, 1, 6, 1, 8, 1, 6, 7, 1, 7, 1, 6, 1, 6, 1, 5, 7, 1, 3, 1, 5, 1};
                //{'sil','ic','fricative','ic','fricative','ic','sil','ic','sil','fricative','fricative','ic','fricative','ic','sil','ic','sil','sil','fricative','fricative','sil','fricative','e','fricative','ic','fricative','ic','sil','ic','sil','e','sil','sil','fricative','sil','e','sil','fricative','ic','sil','ic','sil','fricative','sil','fricative','sil','plosive','ic','sil','click','sil','plosive','sil'};
                //durSeq = new int[]{7, 1, 1, 5, 2, 4, 13, 1, 31, 4, 3, 2, 2, 2, 1, 1, 33, 4, 3, 4, 3, 3, 2, 4, 2, 4, 2, 4, 2, 5, 2, 2, 9, 4, 27, 6, 4, 4, 1, 4, 3, 6, 3, 28, 11, 7, 1, 3, 3, 1, 2, 1, 34};
                //speechEnergy = new double[]{0.000808839395176600, 0.00135722546838200, 0.00199444685131300, 0.00316438218578700, 0.00213878042995900, 0.00291863898746700, 0.00256358971819300, 0.00141666259151000, 0.00164466723799700, 0.00142602331470700, 0.00145313446410000, 0.00140876276418600, 0.00142854615114600, 0.00148971460294000, 0.00158848566934500, 0.00155621219892100, 0.00202645920217000, 0.00230704178102300, 0.00167432578746200, 0.00162199034821200, 0.00143535109236800, 0.00125113502144800, 0.00153229746501900, 0.00170469423756000, 0.00152161635924100, 0.00201765703968700, 0.00204841676168100, 0.00208758842200000, 0.00311809731647400, 0.00278978864662300, 0.00421693921089200, 0.00630536582320900, 0.00727441208437100, 0.00956129003316200, 0.0115046873688700, 0.0163191873580200, 0.0235323756933200, 0.0209680814296000, 0.0165941473096600, 0.0138650638982700, 0.0155264083296100, 0.0209676474332800, 0.0279179364442800, 0.0337828546762500, 0.0372431501746200, 0.0383784621954000, 0.0391020253300700, 0.0370052419602900, 0.0334956087172000, 0.0291319154202900, 0.0245260819792700, 0.0221366677433300, 0.0192228890955400, 0.0159377567470100, 0.0120669947937100, 0.00723508931696400, 0.00454388419166200, 0.00256737810559600, 0.00190444698091600, 0.00201389496214700, 0.00169753597583600, 0.00183140498120300, 0.00247059669345600, 0.00407442590221800, 0.00400851620361200, 0.00337054138071800, 0.00320294359698900, 0.0103238793090000, 0.0238937791436900, 0.0371762998402100, 0.0493671968579300, 0.0549836345017000, 0.0519642457366000, 0.0424961447715800, 0.0278842989355300, 0.0182448308914900, 0.0105112157762100, 0.00576669909060000, 0.00352045078761900, 0.00326485699042700, 0.00345883169211400, 0.00261713052168500, 0.00309502403251800, 0.00294994632713500, 0.00174755544867400, 0.00199280702509000, 0.00218719244003300, 0.00163623993285000, 0.00188417581375700, 0.00226380070671400, 0.00206250534392900, 0.00193042051978400, 0.00260121678002200, 0.00229599629528800, 0.00237047369591900, 0.00308181508444200, 0.00203453842550500, 0.00211968226358300, 0.00274406536482300, 0.00210610916838000, 0.00160298601258500, 0.00238548777997500, 0.00195321533829000, 0.00178830837830900, 0.00214602495543700, 0.00164464418776300, 0.00181392824742900, 0.00176039163488900, 0.00138590508140600, 0.00155577412806500, 0.00212139403447500, 0.00172545004170400, 0.00229189940728200, 0.00261645251885100, 0.00178238085936800, 0.00265614781528700, 0.00288236280903200, 0.00172855029813900, 0.00184680696111200, 0.00166811188682900, 0.00160354084800900, 0.00129716482479100, 0.00148310337681300, 0.00152459600940300, 0.00144545105285900, 0.00141031970270000, 0.00133764836937200, 0.00140192068647600, 0.00163761281874000, 0.00177918875124300, 0.00171249103732400, 0.00230644876137400, 0.00199147081002600, 0.00212637055665300, 0.00275099650025400, 0.00242791720666000, 0.00221131136640900, 0.00213404675014300, 0.00183949037455000, 0.00170890742447200, 0.00238808570429700, 0.00220613717101500, 0.00193960731849100, 0.00237364438362400, 0.00152466713916500, 0.00157136516645600, 0.00209280615672500, 0.00165097776334700, 0.00133590050973000, 0.00121740601025500, 0.00198633293621200, 0.00265996740199600, 0.00195556064136300, 0.00368526880629400, 0.00372798996977500, 0.00242813234217500, 0.00384162156842600, 0.00336473155766700, 0.00219443836249400, 0.00328017002902900, 0.00293463165871800, 0.00170697167050100, 0.00296367076225600, 0.00351269892416900, 0.00241831387393200, 0.00225994712673100, 0.00263590971007900, 0.00162417034152900, 0.00212075514718900, 0.00196579028852300, 0.00172156782355200, 0.00266986759379500, 0.00231036962941300, 0.00181107292883100, 0.00211274623870800, 0.00164774467703000, 0.00175668927840900, 0.00179482041858100, 0.00138774223160000, 0.00138128991238800, 0.00185904372483500, 0.00162477360572700, 0.00150708353612600, 0.00190549960825600, 0.00169015035498900, 0.00181483675260100, 0.00158944458235100, 0.00131980190053600, 0.00162767164874800, 0.00201763492077600, 0.00155118247494100, 0.00137317308690400, 0.00170533056370900, 0.00154628418386000, 0.00121762871276600, 0.00128178496379400, 0.00173706898931400, 0.00166476727463300, 0.00158540287520700, 0.00159334146883300, 0.00143389008007900, 0.00147273018956200, 0.00136965815909200, 0.00175568426493600, 0.00166038260795200, 0.00135334278456900, 0.00152677309233700, 0.00163432781118900, 0.00140316551551200, 0.00147804629523300, 0.00158537004608700, 0.00144058652222200, 0.00144701439421600, 0.00171290000435000, 0.00148215098306500, 0.00157532584853500, 0.00170843768864900, 0.00135971710551500, 0.00165964255575100, 0.00164231634698800, 0.00124941288959200, 0.00170678610447800, 0.00190576200839100, 0.00146895099896900, 0.00156124040950100, 0.00173265393823400, 0.00150287628639500, 0.00192515435628600, 0.00203405832871800, 0.00141073367558400, 0.00187877553980800, 0.00184497213922400, 0.00143453700002300, 0.00138215429615200, 0.00167157314717800, 0.00188284332398300, 0.00154196424409700, 0.00161794887390000, 0.00204486190341400, 0.00188582099508500, 0.00155340717174100, 0.00168080884032000, 0.00145695742685300, 0.00130689458455900, 0.00159977015573500, 0.00139807991217800, 0.00135029898956400, 0.00145911844447300, 0.00135223392862800, 0.00122586265206300, 0.00122198916506000, 0.00148294237442300, 0.00194512028247100, 0.00214368547312900, 0.00153129245154600, 0.00147660495713400, 0.00191286823246600, 0.00176310911774600, 0.00175853585824400, 0.00226726732216800, 0.00542275048792400, 0.0108534926548600, 0.0102422218769800, 0.00746596511453400, 0.0100843077525500, 0.0378280095756100, 0.0360112451016900, 0.0198761466890600, 0.0239890459924900, 0.0225721448659900, 0.00926443096250300, 0.00910810381174100, 0.00831374712288400, 0.00762870162725400, 0.00480690132826600, 0.00655160099268000, 0.00852068141102800, 0.0100593762472300, 0.0160658117383700, 0.0184286031872000, 0.0186130888760100, 0.0196810550987700, 0.0201707147061800, 0.0126779992133400, 0.0147841693833500, 0.0139029594138300, 0.0134421158582000, 0.0119318272918500, 0.0119588645175100, 0.0109557658433900, 0.0138057554140700, 0.0211681257933400, 0.0180312003940300, 0.00637556239962600, 0.00771637726575100, 0.00781591609120400, 0.00411243876442300, 0.00547863030806200, 0.00644243415445100, 0.00409052660688800, 0.00891030021011800, 0.00992190651595600, 0.00516587216407100, 0.00713250366970900, 0.00928464811295300, 0.00567915802821500, 0.00459094811230900, 0.00655575795099100, 0.00474873930215800, 0.00264483410865100, 0.00391056016087500, 0.00325302896089900, 0.00503838947042800, 0.00455974042415600, 0.00219076802022800, 0.00362388393841700, 0.00477607455104600, 0.00501339184120300, 0.00645243097096700, 0.00629223929718100, 0.00334618007764200, 0.00393593404442100, 0.00415714411065000, 0.00204807636328000, 0.00205921265296600, 0.00184923887718500, 0.00148855429142700, 0.00160182442050400, 0.00155478471424400, 0.00127013644669200, 0.00138252729084300, 0.00164720322936800, 0.00146797124762100, 0.00166769931092900, 0.00159711576998200, 0.00123338447883700, 0.00120606517884900, 0.00138807122129900, 0.00189384899567800, 0.00154444004874700, 0.00153917632997000, 0.00165182759519700, 0.00149649078957700, 0.00132269063033200, 0.00156899867579300, 0.00185676058754300, 0.00169624132104200, 0.00245425710454600, 0.00267861271277100, 0.00160890002735000, 0.00198803306557200, 0.00186585052870200, 0.00153898412827400, 0.00142160034738500, 0.00142513657920100, 0.00135156360920500, 0.00143400602974000, 0.00166631117463100, 0.00147834140807400, 0.00162291969172700, 0.00242582312785100, 0.00197580037638500, 0.00150434498209500, 0.00212027435191000, 0.00195687217637900, 0.00158760452177400, 0.00165431317873300, 0.00167813978623600, 0.00178476993460200, 0.00153988751117100, 0.00168517394922700, 0.00210497295483900, 0.00153289944864800, 0.00115861766971600, 0.00123589148279300, 0.00119316310156100, 0.00137881818227500, 0.00139501364901700, 0.00139593624044200, 0.00165177823510000, 0.00201487774029400, 0.00167773780413000, 0.00251554092392300, 0.00296775135211600, 0.00217571691609900, 0.00331843853928100, 0.00334959430620100, 0.00152483058627700, 0.00179066509008400, 0.00234893313609100, 0.00205119675956700, 0.00133274681866200, 0.00149489450268400, 0.00135698029771400, 0.00128199404571200, 0.00131313141901000, 0.00124100467655800, 0.00129766098689300, 0.00149667507503200, 0.00137351173907500, 0.00141307583544400, 0.00125094980467100, 0.00115592044312500, 0.00128090532962200, 0.00140247168019400, 0.00147401611320700, 0.00131421408150300, 0.00145037181209800, 0.00151479302439800, 0.00157984846737200, 0.00152545189485000, 0.00144996040035000, 0.00160723680164700, 0.00155190413352100, 0.00186300731729700, 0.00277585326694000, 0.00208948948420600, 0.00199161190539600, 0.00271645816974300, 0.00193778972607100, 0.00147391320206200, 0.00141447829082600, 0.00119842402637000, 0.00116879714187200, 0.00146326934918800, 0.00164416152983900, 0.00151038041804000, 0.00133130396716300, 0.00145955407060700, 0.00140490406192800, 0.00128249404951900, 0.00129812385421200, 0.00137775926850700, 0.00181652430910600, 0.00164955377113100, 0.00151508999988400, 0.00142227625474300, 0.00127060140948700, 0.00142674217931900, 0.00151838723104400, 0.00139612820930800, 0.00124165078159400, 0.00131037610117300, 0.00140039576217500, 0.00151108321733800, 0.00171662855427700, 0.00151843030471400, 0.00145040382631100, 0.00134084001183500, 0.00211258605122600, 0.00196484546177100, 0.00219892314635200, 0.00264876475557700, 0.00192992517259000, 0.00194373028352900, 0.00210374337621000, 0.00195441977120900, 0.00169170321896700, 0.00174140115268500, 0.00157986779231600, 0.00165948458015900, 0.00154043245129300, 0.00143532722722700, 0.00164796505123400, 0.00149671465624100, 0.00154822180047600, 0.00161035160999700, 0.00144402123987700, 0.00195361720398100, 0.00189997872803400, 0.00185781507752800, 0.00238859839737400, 0.00237171351909600, 0.00277147023007300, 0.00340509600937400, 0.00367827713489500, 0.00448027066886400, 0.00493836542591500, 0.00415849266573800, 0.00348342978395500, 0.00329777994193100, 0.00397484796121700, 0.00346796237863600, 0.00251961825415500, 0.00363810942508300, 0.00281960959546300, 0.00388145237229800, 0.00652380241081100, 0.00595891289413000, 0.00473714852705600, 0.00687748566269900, 0.00705266185104800, 0.00486848549917300, 0.00402921484783300, 0.00364140397869100, 0.00411225389689200, 0.00443616183474700, 0.00231758877635000, 0.00336348987184500, 0.00435510696843300, 0.00271161668933900, 0.00278564775362600, 0.00360581628046900, 0.00230300612747700, 0.00399497151374800, 0.00393188279122100, 0.00239443522878000, 0.00168517581187200, 0.00250604515895200, 0.00292611587792600, 0.00373323215171700, 0.00423132115975000, 0.00309703988023100, 0.00363344163633900, 0.00582074373960500, 0.00552498875185800, 0.00263679958879900, 0.00194852065760600, 0.00197117985226200, 0.00239023147150900, 0.00909513235092200, 0.0132680144161000, 0.0166075024753800, 0.0154152205213900, 0.0125326430425000, 0.00763292983174300, 0.00346524338237900, 0.00265041971579200, 0.00320203928276900, 0.00241212174296400, 0.00452223094180200, 0.00495579000562400, 0.0101648084819300, 0.0102724293246900, 0.0398257896304100, 0.0628822594881100, 0.0610409043729300, 0.0692597329616500, 0.0740690007805800, 0.0359833315014800, 0.0465049669146500, 0.0440654158592200, 0.0230118073523000, 0.0341692455112900, 0.0272624474018800, 0.0256552230566700, 0.0312529876828200, 0.0331175960600400, 0.0325568392872800, 0.0257521606981800, 0.0155771886929900, 0.00741634052246800, 0.00491606304422000, 0.00416253879666300, 0.00607786653563400, 0.00810709595680200, 0.0176967699080700, 0.0614505410194400, 0.0672600194811800, 0.0814024507999400, 0.0788985565304800, 0.0504174381494500, 0.0481667295098300, 0.0528004951775100, 0.0479727797210200, 0.0307398177683400, 0.0209520850330600, 0.0206279791891600, 0.0219748802483100, 0.00740043586120000, 0.0112069044262200, 0.0176734440028700, 0.0236524417996400, 0.0258020013570800, 0.0244979672133900, 0.0197636447846900, 0.0157824046909800, 0.0122572313994200, 0.00692377751693100, 0.00494796736165900, 0.00987968221306800, 0.00956842955201900, 0.00748012773692600, 0.0178616978228100, 0.0178910400718500, 0.0112618273124100, 0.0149454064667200, 0.0175410620868200, 0.0191091094166000, 0.0133911846205600, 0.0116483466699700, 0.0116981212049700, 0.0137637704610800, 0.00836711935699000, 0.0110589172691100, 0.0157580878585600, 0.0170504283160000, 0.0174245499074500, 0.0140596525743600, 0.0105153704062100, 0.00899323169142000, 0.00493794772774000, 0.00344378268346200, 0.00378309399820900, 0.00262425537221100, 0.00660397484898600, 0.00982357561588300, 0.00785033497959400, 0.0148602426052100, 0.0220761150121700, 0.0307877212762800, 0.0300443507731000, 0.0202933549881000, 0.0201488230377400, 0.0172061584889900, 0.0104564018547500, 0.0176245849579600, 0.0150702297687500, 0.00564605230465500, 0.00614662608131800, 0.00562087679281800, 0.00819539837539200, 0.00948905572295200, 0.0106231253594200, 0.00787487532943500, 0.00469419173896300, 0.00461259204894300, 0.00301201222464400, 0.00312491948716300, 0.00590795511379800, 0.00559727568179400, 0.00359284738078700, 0.00496531929820800, 0.00357032683678000, 0.00454716943204400, 0.00599984964355800, 0.00510237924754600, 0.00324353971518600, 0.0114798871800300, 0.0158086065203000, 0.00874289218336300, 0.00405748374760200, 0.00455520348623400, 0.00413771532475900, 0.00344764022156600, 0.00269742240198000, 0.00197187322191900, 0.00161827122792600, 0.00148665753658900, 0.00187653524335500, 0.00174482085276400, 0.00138660508673600, 0.00142692693043500, 0.00169535016175400, 0.00153233658056700, 0.00162605801597200, 0.00161999172996700, 0.00226060114800900, 0.00285711674951000, 0.00209502247162200, 0.00240992126055100, 0.00380765576846900, 0.00345857790671300, 0.00225997646339200, 0.00193257362116100, 0.00244273687712800, 0.00183615565765600, 0.00143502536229800, 0.00166334514506200, 0.00164225080516200, 0.00239282427355600, 0.00250430940650400, 0.00204511056654200, 0.00375342532061000, 0.00343138328753400, 0.00167243264149900, 0.00280090630985800, 0.00599960377439900, 0.00824279617518200, 0.00603822618722900, 0.00568104581907400, 0.0103533519431900, 0.00887611228972700, 0.00393728027120200, 0.00163562095258400, 0.00223823683336400, 0.00398522801697300, 0.00480920774862200, 0.00340731837786700, 0.00157300278078800, 0.00144957145676000, 0.00132595910690700, 0.00153758306987600, 0.00311038363724900, 0.00294536072760800, 0.00322196446359200, 0.00661807321012000, 0.00671666627749800, 0.00321590551175200, 0.00510303536430000, 0.00571633642539400, 0.00286167999729500, 0.00215543154627100, 0.00184313522186100, 0.00276624294929200, 0.00327639374882000, 0.00230394210666400, 0.00333301699720300, 0.00466928631067300, 0.00344645651057400, 0.00184881710447400, 0.00317398109473300, 0.00434713624417800, 0.00344678922556300, 0.00311366887763100, 0.00667386315763000, 0.00722898961976200, 0.00373326125554700, 0.00532915582880400, 0.00647701090201700, 0.00364699144847700, 0.00324679911136600, 0.00369930453598500, 0.00283611146733200, 0.00364123960025600, 0.00272554927505600, 0.00275120208971200, 0.00434419373050300, 0.00321186077781000, 0.00196899101138100, 0.00264879059977800, 0.00277830287814100, 0.00266888691112400, 0.00308458181098100, 0.00236583780497300, 0.00483735464513300, 0.00668952567502900, 0.00428276928141700, 0.00470684468746200, 0.00579729536548300, 0.00314762932248400, 0.00405240990221500, 0.00545940315350900, 0.00349534535780500, 0.00380244944244600, 0.00517402263358200, 0.00321476231329100, 0.00275672669522500, 0.00351836346089800, 0.00219464884139600, 0.00269451201893400, 0.00311756459996100, 0.00177639292087400, 0.00147587258834400, 0.00140503456350400, 0.00128661177586800, 0.00163969106506600, 0.00254845805466200, 0.00223816954530800, 0.00176640599966000, 0.00295143853873000, 0.00400520907715000, 0.00273638637736400, 0.00291756982915100, 0.00310550397262000, 0.00196881988085800, 0.00298939389176700, 0.00277270353399200, 0.00275437557138500, 0.00458096573129300, 0.00398175418376900, 0.00273690745234500, 0.00452055502682900, 0.00396671192720500, 0.00297865853644900, 0.00472861435264300, 0.00360538158565800, 0.00259330123662900, 0.00386666390113500, 0.00300107221119100, 0.00222498178482100, 0.00392128387466100, 0.00346493744291400, 0.00241945870220700, 0.00384132144972700, 0.00359483575448400, 0.00190700241364500, 0.00223220326006400, 0.00297413463704300, 0.00223929365165500, 0.00286908587440800, 0.00268409377895300, 0.00151018064934800, 0.00147380714770400, 0.00195606541819900, 0.00204219040460900, 0.00148220499977500, 0.00131181080360000, 0.00147093005944000, 0.00199296511709700, 0.00244903843849900, 0.00349000818096100, 0.00515948189422500, 0.00696073938161100, 0.00818539969623100, 0.0105486586689900, 0.0117517700418800, 0.0122080128639900, 0.0129757728427600, 0.0132373273372700, 0.0125713348388700, 0.0126061374321600, 0.0112235378474000, 0.00994174275547300, 0.00808196794241700, 0.00604543089866600, 0.00481341779232000, 0.00391928199678700, 0.00304403714835600, 0.00220020208507800, 0.00163401826284800, 0.00149942585267100, 0.00151703052688400, 0.00165467348415400, 0.00215869746170900, 0.00193128199316600, 0.00156198383774600, 0.00142218824476000, 0.00206853449344600, 0.00317193986848000, 0.00289618759416000, 0.00145468255505000, 0.00136609666515100, 0.00172482151538100, 0.00178651523310700, 0.00314775388687800, 0.00356356194242800, 0.00277192913927100, 0.00325246178545100, 0.0107966894283900, 0.0136562827974600, 0.0200039912015200, 0.144725188612900, 0.141420558095000, 0.138738751411400, 0.168642029166200, 0.0971574857831000, 0.0455302931368400, 0.0443435721099400, 0.0235141329467300, 0.0208044368773700, 0.0244372561574000, 0.0292279440909600, 0.0221184976398900, 0.0118156019598200, 0.00912308506667600, 0.00860055163502700, 0.00437301862984900, 0.00250978698022700, 0.00211438583210100, 0.00231610774062600, 0.00319284247234500, 0.00466977991163700, 0.00704005826264600, 0.0109385391697300, 0.0103647625073800, 0.00853439979255200, 0.0155871994793400, 0.0135731287300600, 0.00493221683427700, 0.00589050631970200, 0.00573921436443900, 0.00375878787599500, 0.00216788076795600, 0.00557504687458300, 0.00828592572361200, 0.00985663942992700, 0.0102223344147200, 0.00972862169146500, 0.00813176203519100, 0.00504299066960800, 0.00254955422133200, 0.00165221863426300, 0.00218580965884000, 0.00372933014296000, 0.00406733015552200, 0.00303248176351200, 0.00268920394592000, 0.00375964376144100, 0.00237817270681300, 0.00262195779942000, 0.00254980451427400, 0.00317438575439200, 0.00686596147716000, 0.00724914064630900, 0.00378324859775600, 0.00527647743001600, 0.0102429054677500, 0.00837346073240000, 0.00291788624599600, 0.00213005137629800, 0.00168774358462500, 0.00185732182581000, 0.00225558620877600, 0.00197494123131000, 0.00275898748077500, 0.00283985259011400, 0.00198748731054400, 0.00220882100984500, 0.00241147540509700, 0.00403973320499100, 0.00430909823626300, 0.00282468530349400, 0.00332843209616800, 0.00486393226310600, 0.00503136450424800, 0.00341198127716800, 0.00183177646249500, 0.00175374583341200, 0.00250381440855600, 0.00173388095572600, 0.00137940468266600, 0.00158129574265300, 0.00172436190769100, 0.00193644268438200, 0.00217536161653700, 0.00157776917330900, 0.00233776634559000, 0.00281930319033600, 0.00206131651066200, 0.00234422343783100, 0.00334746879525500, 0.00252438453026100, 0.00244845845736600, 0.00331874121911800, 0.00225549680180800, 0.00246805115602900, 0.00352614372968700, 0.00210544490255400, 0.00224384828470600, 0.00284786126576400, 0.00216470984742000, 0.00182897888589700, 0.00241619138978400, 0.00189263059292000, 0.00190630333963800, 0.00174398033414000, 0.00129625119734600, 0.00143472349736800, 0.00158599740825600, 0.00147342588752500, 0.00155122706200900, 0.00168880913406600, 0.00153893767856100, 0.00199977681040800, 0.00191354600247000, 0.00142430420965000, 0.00158250273671000, 0.00146609521471000, 0.00133361015468800, 0.00174289825372400, 0.00180789211299300, 0.00141346780583300, 0.00141262484248700, 0.00157325819600400, 0.00156086892820900, 0.00225398619659200, 0.00212670885957800, 0.00314440927468200, 0.00410292344167800, 0.00279243616387200, 0.00455866614356600, 0.00615172041580100, 0.00360704003833200, 0.00560022192075800, 0.00643663853406900, 0.00350952846929400, 0.00514289271086500, 0.00744356866926000, 0.00484433164820100, 0.00392866507172600, 0.00613340642303200, 0.00452101882547100, 0.00309675280004700, 0.00560224661603600, 0.00478614401072300, 0.00456876121461400, 0.00612988928332900, 0.00391471711918700, 0.00252314889803500, 0.00428596511483200, 0.00367748271673900, 0.00377279170788800, 0.00688484171405400, 0.00555705185979600, 0.00378711358644100, 0.00599813414737600, 0.00514673115685600, 0.00245355279184900, 0.00291383033618300, 0.00322470557875900, 0.00248991022817800, 0.00486147310584800, 0.00461516203358800, 0.00317181437276300, 0.00708483858034000, 0.00837396364659100, 0.00444510905072100, 0.00560007384046900, 0.00722389155998800, 0.00449796207249200, 0.00338172540068600, 0.00526180909946600, 0.00412180228158800, 0.00335095566697400, 0.00316379056312100, 0.00424399320036200, 0.00633570086211000, 0.00412838393822300, 0.00778995361179100, 0.0100615937262800, 0.00574927870184200, 0.00514939101412900, 0.00779760954901600, 0.00512994080781900, 0.00529540330171600, 0.00706427637487600, 0.00397869618609500, 0.00547006446868200, 0.00679461937397700, 0.00358540518209300, 0.00448265951126800, 0.00532699562609200, 0.00467270798981200, 0.00647539878264100, 0.00684332195669400, 0.00635128002613800, 0.00623682560399200, 0.00767714902758600, 0.00735366297885800, 0.00758268451318100, 0.00918745435774300, 0.00795248616486800, 0.00754798064008400, 0.00789132434874800, 0.00761522166431000, 0.00758077483624200, 0.00755122490227200, 0.00761270197108400, 0.00744509743526600, 0.00636832555756000, 0.00596410641446700, 0.00594629673287300, 0.00608234433457300, 0.00578028894960900, 0.00586010096594700, 0.00577554712072000, 0.00612257746979600, 0.00593167031183800, 0.00585798639804100, 0.00497363088652500, 0.00467379763722400, 0.00414327578619100, 0.00425043283030400, 0.00421506166458100, 0.00402468303218500, 0.00389988278038800, 0.00376319675706300, 0.00397432921454300, 0.00360679277218900, 0.00312859308905900, 0.00230116932652900, 0.00230892933905100, 0.00254906620830300, 0.00242607854306700, 0.00177805894054500, 0.00161740323528600, 0.00151074922177900, 0.00147824943997000, 0.00150168873369700, 0.00140308262780300, 0.00134511082433200, 0.00121868052519900, 0.00125432270579000, 0.00132283335551600, 0.00136503542307800, 0.00159833580255500, 0.00157932355068600, 0.00138205371331400, 0.00135552906431300, 0.00134427577722800, 0.00149571162182800, 0.00146049144677800, 0.00140454620122900, 0.00146187061909600, 0.00152600707951900, 0.00189320824574700, 0.00186608673539000, 0.00144569040276100, 0.00151095225010100, 0.00142796372529100, 0.00191802554763900, 0.00287993112579000, 0.00263344263657900, 0.00714475242421000, 0.00971587467938700, 0.00685919309034900, 0.0107906730845600, 0.0161525849252900, 0.0141818402335000, 0.0159038975834800, 0.0203239824622900, 0.0222396459430500, 0.0198917388916000, 0.0151973664760600, 0.0104615772143000, 0.00583077920600800, 0.00627984898164900, 0.00461811292916500, 0.00425107404589700, 0.00621946435421700, 0.00460323831066500, 0.00272608059458400, 0.00396073097363100, 0.00279417890124000, 0.00353491399437200, 0.00393294962123000, 0.00203495821915600, 0.00275859120301900, 0.00330020301044000, 0.00231535872444500, 0.00243088323622900, 0.00310431700199800, 0.00224450859241200, 0.00262571661733100, 0.00374015630222900, 0.00251778308302200, 0.00188621226698200, 0.00226007052697200, 0.00168696756009000, 0.00203248811885700, 0.00221524736844000, 0.00215363246388700, 0.00389381637796800, 0.00316782807931300, 0.00513802887871900, 0.00751821929588900, 0.00463112816214600, 0.00736962584778700, 0.00981836020946500, 0.00555177778005600, 0.00595875689759900, 0.00779209611937400, 0.00438053812831600, 0.00518066668883000, 0.00604954455047800, 0.00336397881619600, 0.00427150027826400, 0.00614827545359700, 0.00405173283070300, 0.00359622249379800, 0.00520003959536600, 0.00325612956658000, 0.00358982011675800, 0.00477264588698700, 0.00284657394513500, 0.00387542648240900, 0.00463478267192800, 0.00299869384616600, 0.00288244639523300, 0.00376291992142800, 0.00236325873993300, 0.00260089407675000, 0.00253480672836300, 0.00144800834823400, 0.00274543487466900, 0.00295488210395000, 0.00190236628986900, 0.00167343660723400, 0.00174808374140400, 0.00131734216120100, 0.00151305750478100, 0.00171316112391700, 0.00137212418485400, 0.00127761322073600, 0.00159901857841800, 0.00154428509995300, 0.00158886401914100, 0.00131834996864200, 0.00124544173013400, 0.00137225689832100, 0.00150169187691100, 0.00186619418673200, 0.00217427941970500, 0.00184064568020400, 0.00225480366498200, 0.00183193979319200, 0.00146216060966300, 0.00162860774435100, 0.00145967886783200, 0.00131521816365400, 0.00132319144904600, 0.00141000642906900, 0.00128993368707600, 0.00114243291318400, 0.00123169284779600, 0.00142547080759000, 0.00158904492855100, 0.00142414669971900, 0.00156710727606000, 0.00132382928859400, 0.00141434895340400, 0.00172228470910300, 0.00139346555806700, 0.00149009597953400, 0.00137792271561900, 0.00137280474882600, 0.00149550382047900, 0.00152752432040900, 0.00149717135354900, 0.00143079925328500, 0.00124074041377800, 0.00130958482623100, 0.00130357674788700, 0.00145960529334800, 0.00141027651261500, 0.00125582423061100, 0.00132923887576900, 0.00130409607663800, 0.00120843306649500, 0.00154930539429200, 0.00175718800164800, 0.00162138766609100, 0.00199564383365200, 0.00172108632978100, 0.00133236811962000, 0.00160878594033400, 0.00159553729463400, 0.00141105405055000, 0.00192001136019800, 0.00195001903921400, 0.00153808714821900, 0.00218456774018700, 0.00283153797499800, 0.00202841474674600, 0.00212837220169600, 0.00240863859653500, 0.00221476308070100, 0.00197048974223400, 0.00263318838551600, 0.00168049079366000, 0.00189230905380100, 0.00254632253199800, 0.00194582936819600, 0.00181927334051600, 0.00255691655911500, 0.00198190193623300, 0.00213453010655900, 0.00248922640457700, 0.00180252152495100, 0.00214858236722600, 0.00203143851831600, 0.00180844077840400, 0.00240447930991600, 0.00182636419776800, 0.00164194661192600, 0.00214948132634200, 0.00207116780802600, 0.00146836065687200, 0.00162307370919700, 0.00189164362382100, 0.00159474555403000, 0.00156708748545500, 0.00174029683694200, 0.00147435953840600, 0.00172705308068500, 0.00168940215371500, 0.00137338601052800, 0.00151289091445500};
                //speechPitch = new double[]{0, 0, 0, 0, 0, 0, 299, 299, 299, 459, 499, 500, 483, 483, 500, 121, 499, 499, 498, 498, 497, 120, 119, 119, 300, 300, 301, 501, 281, 444, 444, 576, 571, 571, 570, 570, 401, 287, 165, 318, 318, 331, 355, 361, 382, 382, 405, 418, 418, 431, 452, 460, 489, 515, 520, 521, 521, 521, 521, 522, 506, 518, 382, 508, 124, 571, 571, 359, 387, 394, 433, 434, 436, 436, 436, 437, 442, 442, 441, 438, 437, 437, 437, 147, 147, 441, 441, 441, 441, 442, 442, 149, 520, 520, 145, 433, 435, 435, 146, 446, 447, 448, 448, 386, 524, 523, 522, 522, 424, 121, 122, 336, 449, 449, 574, 519, 520, 520, 532, 532, 532, 192, 192, 498, 498, 500, 529, 462, 462, 504, 308, 118, 118, 556, 556, 556, 556, 555, 557, 542, 494, 494, 478, 479, 288, 465, 492, 492, 492, 551, 551, 447, 346, 346, 351, 352, 353, 557, 557, 551, 552, 395, 394, 432, 242, 243, 244, 595, 595, 490, 490, 378, 374, 326, 391, 391, 502, 266, 266, 529, 479, 442, 442, 574, 190, 189, 189, 189, 408, 408, 167, 167, 500, 500, 329, 329, 501, 501, 501, 501, 199, 459, 459, 137, 576, 582, 429, 544, 544, 507, 301, 490, 490, 490, 489, 332, 336, 527, 527, 527, 554, 480, 481, 247, 544, 355, 352, 352, 523, 392, 393, 222, 222, 222, 130, 501, 501, 500, 406, 366, 503, 502, 502, 154, 154, 363, 587, 587, 282, 501, 501, 245, 245, 398, 203, 375, 375, 374, 373, 372, 579, 581, 580, 579, 579, 578, 577, 516, 573, 404, 404, 567, 460, 495, 495, 552, 552, 553, 500, 500, 215, 215, 215, 322, 462, 463, 463, 52, 52, 544, 544, 276, 276, 278, 279, 279, 142, 142, 146, 250, 249, 244, 465, 465, 466, 308, 589, 589, 306, 300, 300, 300, 551, 506, 504, 511, 501, 501, 456, 226, 225, 225, 385, 385, 219, 219, 599, 494, 151, 151, 330, 331, 170, 184, 513, 513, 513, 375, 327, 530, 517, 517, 501, 500, 500, 500, 500, 500, 471, 163, 557, 557, 558, 558, 145, 145, 545, 260, 501, 503, 503, 410, 363, 362, 587, 504, 110, 164, 481, 404, 404, 501, 285, 277, 276, 562, 332, 319, 372, 375, 375, 133, 133, 226, 226, 418, 279, 500, 500, 500, 499, 499, 521, 520, 520, 530, 562, 576, 576, 162, 300, 300, 468, 299, 299, 131, 525, 543, 74, 209, 208, 299, 300, 301, 519, 535, 474, 474, 156, 343, 343, 344, 462, 462, 463, 120, 252, 120, 276, 276, 198, 198, 198, 180, 317, 317, 315, 471, 471, 445, 445, 446, 446, 446, 446, 208, 208, 527, 528, 432, 433, 433, 113, 121, 121, 170, 170, 346, 461, 461, 515, 514, 521, 521, 521, 315, 356, 356, 73, 311, 312, 314, 504, 489, 485, 486, 486, 486, 402, 526, 526, 414, 373, 373, 464, 152, 152, 549, 543, 542, 336, 466, 466, 466, 176, 585, 585, 585, 536, 536, 536, 536, 411, 411, 411, 544, 150, 150, 89, 511, 512, 480, 481, 481, 489, 161, 507, 134, 509, 510, 461, 451, 451, 451, 445, 406, 395, 366, 360, 365, 365, 343, 335, 335, 384, 385, 513, 85, 85, 375, 375, 469, 469, 360, 462, 216, 216, 431, 431, 433, 433, 438, 438, 438, 437, 220, 442, 89, 89, 90, 92, 93, 363, 363, 453, 492, 455, 257, 438, 482, 452, 452, 448, 447, 447, 447, 447, 443, 443, 218, 218, 218, 218, 449, 215, 489, 487, 487, 365, 447, 447, 447, 442, 399, 399, 474, 481, 481, 241, 475, 475, 474, 472, 479, 483, 244, 231, 232, 232, 475, 475, 475, 475, 478, 500, 242, 241, 241, 474, 473, 485, 485, 484, 461, 460, 460, 460, 442, 438, 475, 475, 315, 315, 239, 238, 239, 500, 500, 500, 242, 242, 122, 476, 124, 456, 448, 444, 131, 499, 498, 474, 474, 473, 473, 523, 495, 498, 498, 351, 351, 278, 278, 254, 294, 294, 204, 204, 282, 587, 587, 587, 453, 453, 453, 453, 257, 252, 252, 436, 329, 482, 482, 483, 342, 243, 122, 121, 120, 119, 118, 592, 591, 592, 364, 500, 550, 299, 299, 257, 257, 257, 257, 257, 256, 141, 432, 431, 242, 498, 209, 109, 512, 508, 230, 235, 235, 218, 480, 321, 321, 321, 252, 252, 253, 442, 304, 168, 498, 498, 498, 499, 500, 445, 374, 374, 376, 377, 377, 300, 300, 272, 456, 456, 500, 500, 500, 500, 501, 500, 377, 560, 537, 538, 122, 122, 253, 423, 423, 422, 421, 422, 422, 422, 324, 260, 261, 178, 352, 538, 538, 237, 237, 128, 229, 306, 139, 324, 500, 252, 253, 128, 128, 128, 444, 443, 212, 519, 519, 520, 520, 520, 468, 395, 395, 396, 396, 417, 537, 538, 539, 532, 530, 530, 530, 480, 500, 487, 575, 575, 575, 488, 494, 477, 244, 480, 480, 435, 121, 121, 187, 345, 470, 464, 109, 460, 460, 459, 233, 233, 481, 481, 482, 525, 523, 523, 289, 139, 574, 573, 499, 358, 462, 554, 554, 556, 556, 559, 474, 475, 475, 511, 510, 397, 396, 395, 380, 371, 371, 360, 360, 360, 360, 344, 343, 342, 341, 341, 473, 502, 482, 481, 480, 480, 376, 376, 499, 512, 513, 527, 580, 592, 444, 444, 373, 373, 373, 493, 488, 163, 316, 317, 480, 566, 567, 567, 276, 195, 193, 452, 452, 452, 535, 536, 457, 230, 185, 184, 500, 500, 500, 424, 100, 100, 503, 503, 504, 262, 578, 479, 479, 384, 384, 167, 167, 202, 524, 363, 364, 498, 498, 499, 499, 499, 500, 500, 524, 523, 413, 413, 414, 327, 328, 324, 109, 549, 186, 562, 562, 219, 206, 407, 408, 510, 501, 500, 500, 500, 500, 500, 290, 422, 422, 500, 500, 521, 520, 457, 307, 307, 119, 353, 353, 353, 599, 599, 600, 505, 505, 473, 473, 496, 283, 572, 572, 482, 482, 309, 308, 503, 503, 503, 316, 482, 481, 467, 465, 465, 473, 587, 212, 212, 176, 370, 358, 186, 342, 363, 585, 586, 510, 306, 306, 523, 523, 309, 309, 308, 461, 462, 458, 458, 458, 565, 565, 565, 418, 500, 500, 500, 499, 517, 382, 383, 488, 488, 488, 488, 584, 584, 584, 579, 579, 579, 216, 216, 331, 215, 214, 348, 347, 347, 207, 207, 388, 388, 506, 232, 231, 307, 307, 519, 228, 243, 243, 469, 470, 470, 480, 500, 524, 524, 532, 532, 434, 222, 108, 357, 357, 357, 546, 422, 317, 317, 327, 327, 489, 489, 97, 276, 276, 243, 194, 189, 591, 113, 329, 331, 331, 543, 443, 443, 377, 376, 376, 499, 483, 484, 363, 508, 508, 364, 365, 365, 365, 402, 433, 434, 434, 471, 486, 531, 564, 564, 432, 433, 389, 499, 375, 375, 479, 480, 480, 204, 204, 208, 208, 208, 208, 208, 500, 500, 500, 529, 528, 500, 500, 501, 501, 500, 500, 499, 500, 500, 500, 500, 292, 291, 290, 141, 462, 391, 391, 391, 393, 128, 259, 259, 258, 257, 508, 462, 462, 329, 460, 277, 279, 280, 281, 281, 282, 557, 517, 517, 517, 375, 374, 400, 398, 158, 158, 470, 534, 173, 498, 538, 539, 259, 196, 286, 583, 583, 264, 413, 406, 501, 501, 203, 322, 409, 475, 475, 475, 547, 441, 223, 561, 534, 439, 439, 449, 276, 275, 600, 272, 218, 376, 377, 272, 272, 262, 263, 178, 427, 427, 427, 427, 341, 218, 504, 555, 325, 325, 325, 325, 436, 436, 478, 478, 219, 219, 464, 167, 167, 167, 531, 525, 524, 465, 464, 419, 191, 191, 502, 315, 314, 314, 404, 405, 350, 69, 68, 68, 267, 388, 367, 171, 0, 0, 0};
            }
            else if ((duration < 5) && (duration > 3) && intensity > 0.7) {
                //phoSeqIndex = new int[]{1,4,8,6,6,12,6,12,6,12,6,12,6,12,6,12,6,12,1,8,6,8,1};
                //durSeq = new int[]{1,3,1,10,2,2,2,2,3,2,3,2,3,2,3,3,3,2,45,1,12,2,9};
                //speechEnergy = new double[]{0.000765936449170100,0.00130424578674100,0.00146596319973500,0.00187135010492100,0.00144250167068100,0.00151663389988200,0.00582482712343300,0.0129984933883000,0.0206458847969800,0.0297709312290000,0.0392864048481000,0.0486116148531400,0.0573625825345500,0.0629538819193800,0.0644142851233500,0.0613571815192700,0.0533541552722500,0.0426051020622300,0.0358593724668000,0.0358164831996000,0.0379059910774200,0.0330811217427300,0.0251715406775500,0.0141292680054900,0.00939318165183100,0.0131806507706600,0.0161050055176000,0.0131650269031500,0.0134602943435300,0.0136937303468600,0.0114322751760500,0.0101257991045700,0.00960441306233400,0.0102193187922200,0.0109844906255600,0.0121992984786600,0.0126070110127300,0.0122566418722300,0.0125823663547600,0.0119532281532900,0.0110802864655900,0.0105057265609500,0.0106616280973000,0.00995403900742500,0.0105990273878000,0.0110068181529600,0.0116498582065100,0.0114081446081400,0.0113780070096300,0.0113871451467300,0.0114847207441900,0.0115305334329600,0.0120637221261900,0.0129294469952600,0.0121443364769200,0.0108365416526800,0.00929768662899700,0.00953775458037900,0.00927339773625100,0.0100669702515000,0.00994017068296700,0.00968244392424800,0.00791168492287400,0.00597151834517700,0.00450307084247500,0.00396202038973600,0.00427984958514600,0.00406138226389900,0.00400234991684600,0.00351595040410800,0.00401647575199600,0.0117108765989500,0.0482167303562200,0.0847703367471700,0.108269743621300,0.108609750866900,0.0854684412479400,0.0530981048941600,0.0227580294013000,0.00627908995375000,0.00363881327211900,0.00391243211925000,0.00372651196084900,0.00337601127103000,0.00394425168633500,0.0468834042549100,0.0714496150612800,0.0913380756974200,0.0976197570562400,0.0931539684534100,0.0846607163548500,0.0696773305535300,0.0494910329580300,0.0281653292477100,0.0113191856071400,0.00621341029182100,0.00457961019128600,0.00444343453273200,0.00404909672215600,0.00386359891854200,0.00409047678113000,0.00378478271886700,0.00351206143386700,0.00325784250162500,0.00316968793049500,0.0281555075198400,0.0652733743190800,0.0871683731675100,0.107151649892300,0.109244562685500,0.108108118176500,0.0943498089909600,0.0715062394738200,0.0434570685029000,0.0149541134014700,0.00893926993012400,0.00593045726418500,0.00585912680253400};
                //speechPitch = new double[]{0,0,0,0,0,0,286,286,289,289,290,297,308,309,310,310,310,310,310,290,290,290,266,482,482,482,309,309,152,302,302,302,301,297,297,297,303,303,282,285,286,287,284,283,530,423,596,253,253,252,253,149,597,597,597,598,599,600,315,295,296,520,520,267,267,268,309,152,152,153,153,305,305,301,300,299,297,296,295,289,288,286,283,294,394,381,380,380,381,381,382,383,386,387,388,388,387,381,382,384,193,193,194,194,459,459,459,462,464,465,465,466,466,467,467,467,435,472};
            }else if((duration < 2) && (duration > 0)){
                //testSequence = '1_79609_81496';
                //testSequence = '1_79609_81496';
                //phoSeqIndex = new int[]{1,3,1,3,1,2,2,1,2,1,3,1};
                //durSeq = new int[]{4,1,1,1,7,8,3,3,6,3,1,11};
                //speechEnergy = new double[]{0.000377423712052400,0.000937348173465600,0.00172577647026600,0.00180764112155900,0.00186288636177800,0.00258254073560200,0.00224175467155900,0.00135651114396800,0.00132669147569700,0.00187167478725300,0.00235865265131000,0.00182019534986500,0.00179311470128600,0.00210868706926700,0.00173862592782800,0.00214978214353300,0.00215553143061700,0.00167360517662000,0.00245246128179100,0.00224065105430800,0.00168189220130400,0.00193149992264800,0.00154611177276800,0.00151666498277300,0.00171338021755200,0.00154460046906000,0.00153336464427400,0.00352947553619700,0.00417896918952500,0.00394746102392700,0.00225150748156000,0.00301122269593200,0.00258831889368600,0.00183534913230700,0.00245048734359400,0.00194240163546100,0.00196413439698500,0.00192260299809300,0.00119635183364200,0.00154432468116300,0.00168614520225700,0.00155883713159700,0.00172914494760300,0.00176254950929400,0.00152716960292300,0.00142592284828400,0.00299460161477300,0.00405946047976600,0.00332589284516900,0.00475648837164000,0.00370127684436700,0.00283437361940700,0.00290679046884200,0.00183011521585300,0.00215973006561400,0.00284828362055100,0.00317704444751100,0.00274401973001700,0.00317440624348800,0.00313052721321600,0.00397483911365300,0.00445447396487000,0.00470191007480000,0.00422984827309800,0.00438477564603100,0.00460553867742400,0.00445553520694400,0.00656787632033200,0.00692219985649000,0.00704460032284300,0.00698478193953600,0.00837228819727900,0.0104953385889500,0.0149766448885200,0.0183680187910800,0.0224588867276900,0.0237860642373600,0.0257103405892800,0.0288998503238000,0.0293454490602000,0.0269112680107400,0.0241177454590800,0.0188823565840700,0.0171895436942600,0.0154253682121600,0.0148236481472800,0.00688220420852300,0.00313166528940200,0.00328983110375700,0.00314565468579500,0.00565133756026600,0.00642309337854400,0.00665012421086400,0.00905662495642900,0.00807293597608800,0.00876296591013700,0.00768715050071500,0.00777863664552600,0.00640992354601600,0.00610338663682300,0.00535290176048900,0.00690059224143600,0.00376084609888500,0.00280769728124100,0.00310123222880100,0.00236133881844600,0.00181768450420300,0.00169889337848900,0.00221782573498800,0.00326527515426300,0.00246597896330100,0.00328779849223800,0.00472082663327500,0.00329384254291700,0.00550174247473500,0.00722617655992500,0.00428825663402700,0.00703497044742100,0.00888189021498000,0.00535407243296500,0.00382192456163500,0.00581056252121900,0.00380758894607400,0.00309203076176300,0.00403439672663800,0.00296970154158800,0.00389013276435400,0.00468791741877800,0.00240174029022500,0.00252843298949300,0.00416279956698400,0.00308211194351300,0.00278285937383800,0.00402027508243900,0.00329410610720500,0.00207185558974700,0.00328870979137700,0.00296902051195500,0.00159815070219300,0.00225126300938400,0.00209869863465400,0.00208121701143700,0.00243220455013200,0.00171343388501600,0.00269866362214100,0.00281928363256200,0.00175411894451800,0.00198612152598800,0.00183637219015500,0.00199007149785800,0.00205411645583800,0.00202543591149200,0.00139403925277300,0.00165455555543300,0.00188276497647200,0.00187651289161300,0.00210071750916500,0.00148341059684800,0.00187101017218100,0.00213426421396400,0.00161815679166500,0.00171341584064100,0.00210058549419000,0.00182621402200300,0.00151272758375900,0.00161978602409400,0.00191794906277200,0.00181804795283800,0.00166770524811000,0.00172204093541900,0.00230705109424900,0.00244475598447000,0.00152434909250600,0.00281148753128900,0.00325472187250900,0.00203270046040400,0.00329873431474000,0.00469997199252200,0.00311099132522900,0.00296654645353600,0.00433415872976200,0.00351088633760800,0.00245395465754000,0.00419087940827000,0.00364678958430900,0.00183302024379400,0.00241598067805200,0.00258543435484200,0.00186329009011400,0.00166770478244900,0.00161917810328300,0.00219629798084500};
                //speechPitch = new double[]{0,0,0,0,0,0,144,290,457,457,226,226,226,220,219,575,497,497,497,403,405,405,509,500,500,126,92,446,540,537,537,537,149,150,157,158,158,588,512,513,513,135,147,147,147,380,273,273,555,556,585,585,197,312,312,312,311,182,182,326,326,587,456,447,304,305,53,544,509,373,374,591,591,591,334,547,547,575,600,600,423,424,288,301,301,283,284,261,260,409,501,538,463,453,228,228,227,255,256,257,50,371,370,325,445,444,217,217,444,545,545,511,511,511,499,500,427,427,427,573,573,191,191,192,448,596,458,461,461,94,101,426,330,330,355,355,120,299,300,301,520,399,399,400,433,433,433,328,328,322,322,322,265,161,161,505,505,404,335,354,515,499,499,499,376,377,439,439,277,277,278,279,481,481,583,407,408,139,553,552,285,386,180,170,295,296,523,524,524,0,0,0};
            }else if((duration < 14) && (duration > 12)){
                                //testSequence = '4_650876_662210';
                //phoSeqIndex = new int[]{1,1,4,5,12,6,12,6,12,6,12,6,12,6,12,1,12,13,12,1,6,8,1,6,6,6,6,8,1,6,7,12,6,6,6,8,6,8,1,6,1};
                //durSeq = new int[]{1,1,1,5,13,2,2,4,1,4,1,5,2,3,2,129,1,1,2,3,9,2,2,6,2,3,4,2,10,2,2,3,1,2,2,2,3,3,4,11,28};
                //speechEnergy = new double[]{0.000274026562692600,0.00116323702968700,0.00128209509421100,0.00146717834286400,0.00152391812298400,0.00147568329703100,0.00266184983775000,0.00284256343729800,0.00332980533130500,0.00397450337186500,0.00701026059687100,0.0101622007787200,0.0136878918856400,0.0144043834880000,0.0136798135936300,0.0106888134032500,0.00851055700331900,0.0100380815565600,0.0137346861884000,0.0157739296555500,0.0170203614980000,0.0159588344395200,0.0163453295826900,0.0155520355328900,0.0222656801343000,0.0308878645300900,0.0407718867063500,0.0418002158403400,0.0429440699517700,0.0460376329720000,0.0490036979317700,0.0528393127024200,0.0616198889911200,0.0694831609726000,0.0654050186276400,0.0420109592378100,0.0277560893446200,0.0229022782295900,0.0186482928693300,0.0122995274141400,0.0123981824144700,0.0120903756469500,0.00882063433527900,0.00795809458941200,0.00733390450477600,0.00700517930090400,0.00677237752825000,0.00581111898645800,0.00682979822158800,0.00721577322110500,0.00614142883569000,0.00635825097560900,0.00595497479662300,0.00503899529576300,0.00555942440405500,0.00507091125473400,0.00442153774201900,0.00408018101006700,0.00408132234588300,0.00428181327879400,0.00461564632132600,0.00477578397840300,0.00437162304297100,0.00367674860172000,0.00343855121173000,0.00350335263647100,0.00386283826082900,0.00388270895928100,0.00357503001578200,0.00345475808717300,0.00350805371999700,0.00313504412770300,0.00307473144494000,0.00276130414567900,0.00254329084418700,0.00239442964084400,0.00230061495676600,0.00220274063758600,0.00226148893125400,0.00218312162905900,0.00205667270347500,0.00201593502424700,0.00194435892626600,0.00208388781175000,0.00189700594637500,0.00166071916464700,0.00152251287363500,0.00147512299008700,0.00144380866549900,0.00146787194535100,0.00145938282366800,0.00129449216183300,0.00158183160238000,0.00154465180821700,0.00165290234144800,0.00209322967566500,0.00304666720330700,0.00390474451705800,0.00325794122181800,0.00315762823447600,0.00245018652640300,0.00185408408287900,0.00330704916268600,0.00334982923232000,0.00251736608333900,0.00254810438491400,0.00203391606919500,0.00171373202465500,0.00173694302793600,0.00170026277191900,0.00194299884606200,0.00190265255514500,0.00188711611554000,0.00182000664062800,0.00163389078807100,0.00162613496650000,0.00162991601973800,0.00346136884763800,0.00382100092247100,0.00536520127207000,0.00380762154236400,0.00392323732376100,0.00301228277385200,0.00153984234202700,0.00145908631384400,0.00158269202802300,0.00179205648601100,0.00236294325441100,0.00186816300265500,0.00176508596632600,0.00184522406198100,0.00168481550645100,0.00192727881949400,0.00188787444494700,0.00231794663704900,0.0145619250834000,0.0385532267391700,0.0632852241396900,0.0813716053962700,0.0843866989016500,0.0718017816543600,0.0493722371757000,0.0246097613126000,0.00667250249534800,0.00487246690318000,0.00389027642086100,0.00313708628527800,0.00306439609266800,0.00288325757719600,0.00301137682981800,0.00290661165490700,0.00264303386211400,0.00252942903898700,0.00250865728594400,0.00236485106870500,0.00222319294698500,0.00240124762058300,0.00274764094501700,0.00627391505986500,0.0376918762922300,0.0676830038428300,0.0909690037369700,0.103246159851600,0.0919347777962700,0.0693905428051900,0.0369356833398300,0.0127909239381600,0.00625712145119900,0.00477814115583900,0.00356551096774600,0.00287896348163500,0.00264694029465300,0.00269132177345500,0.00250791222788400,0.00249924953095600,0.00251535372808600,0.00244584819302000,0.00248376606032300,0.00252817478030900,0.00248235533945300,0.0173167753964700,0.0333217382431000,0.0492755807936200,0.0622813180089000,0.0679611936211600,0.0661596134305000,0.0547875910997400,0.0391795784235000,0.0190845858305700,0.00540202623233200,0.00309560843743400,0.00272736628539900,0.00277418247424100,0.00253085233271100,0.00289712264202500,0.00288231298327400,0.00267843180336100,0.00239075045101300,0.00259513454511800,0.00212442176416500,0.00198432011529800,0.00194934161845600,0.00174791854806200,0.00170148420147600,0.00155972747597800,0.00153534323908400,0.00170113705098600,0.00160102045629200,0.00160781794693300,0.00176787551026800,0.00188222073484200,0.00156932568643200,0.00206450256519000,0.00241530337370900,0.00180536916013800,0.00210956973023700,0.00207664538174900,0.00226373132318300,0.00273838499561000,0.00197127624414900,0.00162632844876500,0.00174563878681500,0.00172151881270100,0.00153792568016800,0.00134398124646400,0.00123454409185800,0.00124266673810800,0.00128979701548800,0.00137836625799500,0.00138170365244200,0.00135039503220500,0.00149376003537300,0.00150871661026000,0.00153841171413700,0.00141384208109200,0.00139723857864700,0.00135525537189100,0.00134124618489300,0.00125741399824600,0.00144112482667000,0.00154542142991000,0.00159504648763700,0.00173164985608300,0.00172878243029100,0.00153334462083900,0.00150435767136500,0.00150269689038400,0.00154391489923000,0.00157328590285000,0.00161915982607800,0.00148368556983800,0.00150230620056400,0.00131690711714300,0.00136487430427200,0.00134803552646200,0.00134388066362600,0.00132957415189600,0.00131661724299200,0.00127997412346300,0.00130466150585600,0.00145391456317200,0.00150796119123700,0.00146513245999800,0.00135777622927000,0.00127738912124200,0.00149931374471600,0.00169127411209000,0.00150524836499200,0.00158882478717700,0.00153805885929600,0.00161032553296500,0.00151117367204300,0.00139991461765000,0.00153605139348700,0.00141015858389400,0.00129375769756700,0.00130295322742300,0.00134979106951500,0.00143027841113500,0.00145061593502800,0.00176047801505800,0.00171386147849300,0.00123894785065200,0.00137367367278800,0.00131215504370600,0.00127242947928600,0.00131741713266800,0.00132774258963800,0.00130740646272900,0.00130503776017600,0.00136109779123200,0.00139442319050400,0.00163585692644100,0.00141805200837600,0.00142916652839600,0.00136633322108500,0.00133286823984200,0.00128427101299200,0.00136715720873300,0.00125226227100900,0.00153874105308200,0.00139607361052200,0.00126478343736400,0.00160910468548500,0.00162147567607500,0.00153775734361300,0.00147408805787600,0.00218067504465600,0.00195381976664100,0.00170828681439200,0.00184581859502900,0.00151737791020400,0.00145816290751100,0.00157485774252600,0.00143843225669100,0.00140763423405600,0.00154058006592100,0.00149272405542400,0.00144809472840300,0.00150760216638400,0.00124469841830400,0.00122522434685400,0.00130253925453900,0.00138442451134300,0.00140649091918000,0.00132684386335300,0.00145141058601400,0.00148443400394200,0.00179007719270900,0.00165943463798600,0.00119475019164400,0.00138352764770400,0.00140966754406700,0.00136307871434800,0.00127125636208800,0.00127083552070000,0.00121726724319200,0.00132396107073900,0.00127741461619700,0.00128382828552300,0.00135085813235500,0.00141300796531100,0.00137424201238900,0.00131177040748300,0.00145766371861100,0.00146245246287400,0.00205006799660600,0.00182692951057100,0.00148142105899800,0.00164379319176100,0.00157028704416000,0.00144565210212000,0.00213027140125600,0.00216909684240800,0.00154279056005200,0.00160984310787200,0.00175510870758400,0.00166998826898600,0.00182889681309500,0.00176385731902000,0.00143873703200400,0.00176088244188600,0.00193815794773400,0.00188246998004600,0.00163599592633500,0.00166976382024600,0.00166663085110500,0.00153633195441200,0.00171932915691300,0.00166465900838400,0.00151728896889800,0.00150963710621000,0.00142708490602700,0.00183025619480800,0.00183610722888300,0.00177566288039100,0.00279887788929000,0.00254519283771500,0.00153032038360800,0.00213878205977400,0.00223976233974100,0.00198110961355300,0.00152526097372200,0.00162700819782900,0.00150549563113600,0.00165256811305900,0.00178023939952300,0.00139948003925400,0.00136406172532600,0.00142469780985300,0.00125620630569800,0.00128883321303900,0.00125069753266900,0.00129892188124400,0.00139773765113200,0.00137716415338200,0.00123465235810700,0.00134642841294400,0.00168361840769600,0.00162613159045600,0.00143978220876300,0.00127771508414300,0.00132325675804200,0.00131312443409100,0.00129229645244800,0.00122836115770000,0.00131914066150800,0.00136844709049900,0.00136359105818000,0.00158474838826800,0.00178003171458800,0.00176013528835000,0.00162812147755200,0.00145316869020500,0.00146671221591500,0.00184678600635400,0.00169112463481700,0.00160441698972100,0.00177167926449300,0.00152758287731600,0.00132682791445400,0.00137381639797200,0.00142940192017700,0.00143333279993400,0.00142779550515100,0.00165427720639900,0.00171210733242300,0.00145016750320800,0.00137357658240900,0.00159735907800500,0.00150321994442500,0.00125801714602900,0.00126711570192100,0.00129314267542200,0.00167807540856300,0.00159529049415100,0.00161193346139000,0.00152705400250900,0.00143131229560800,0.00118604267481700,0.00174552877433600,0.00184867798816400,0.00135990045964700,0.00136903312522900,0.00162367464508900,0.00131628313101800,0.00150471262168100,0.00153666618280100,0.00150987284723700,0.00163276703096900,0.00124242296442400,0.00128601619508100,0.00157866068184400,0.00146736972965300,0.00155331718269700,0.00142247776966500,0.00140282523352700,0.00141769717447500,0.00142038939520700,0.00132702873088400,0.00138095265720000,0.00144522020127600,0.00141079432796700,0.00148596591316200,0.00200092699378700,0.00181825773324800,0.00140319473575800,0.00155107025057100,0.00162607210222600,0.00129908160306500,0.00153481878805900,0.00165267859119900,0.00134215585421800,0.00136105576530100,0.00131371512543400,0.00135381030850100,0.00141257967334200,0.00129101006314200,0.00155263976194000,0.00134387507569000,0.00129492080304800,0.00164914014749200,0.00168236775789400,0.00159326370339800,0.00201445608399800,0.00225547864101800,0.00146776763722300,0.00224778172560000,0.00207622023299300,0.00194876268506100,0.00224156794138300,0.00290796696208400,0.00276650954037900,0.00164411251898900,0.00151631701737600,0.00133973499760000,0.00139331805985400,0.00132359820418100,0.00131257856264700,0.00134789803996700,0.00144621566869300,0.00139578245580200,0.00111318880226500,0.00117546727415200,0.00117323535960200,0.00124981091357800,0.00125632667914000,0.00125820573885000,0.00130727945361300,0.00144578726030900,0.00128853123169400,0.00128425855655200,0.00121858168859000,0.00131997896824000,0.00134243757929700,0.00125884893350300,0.00127030035946500,0.00119002116844100,0.00135621603112700,0.00132242473773700,0.00136992509942500,0.00146937975660000,0.00142147648148200,0.00137668137904300,0.00131118483841400,0.00115481030661600,0.00126036233268700,0.00127301563043100,0.00131692341528800,0.00138588901609200,0.00137411232572000,0.00142493809107700,0.00138188560959000,0.00130092503968600,0.00142026622779700,0.00137069425545600,0.00163514644373200,0.00218580127693700,0.00216603744775100,0.00134321616496900,0.00214727548882400,0.00234760460443800,0.00164775340817900,0.00131091068033100,0.00184432230889800,0.00186324235983200,0.00195392034947900,0.00229953555390200,0.00161442602984600,0.00155996729154100,0.00180052290670600,0.00133815687149800,0.00129513500724000,0.00157104432582900,0.00139883579686300,0.00155349378474100,0.00174452131614100,0.00150739226955900,0.00149524281732700,0.00149085070006500,0.00126376736443500,0.00148791191168100,0.00190449843648800,0.00171978434082100,0.00152755179442500,0.00285805133171400,0.00237737363204400,0.00227654911577700,0.00189342582598300,0.00134587136562900,0.00156730879098200,0.00190937821753300,0.00193857494741700,0.00142647081520400,0.00154708849731800,0.00208305404521500,0.00192885636352000,0.00186238111928100,0.00216194964014000,0.00160301511641600,0.00136819772888000,0.00138951162807600,0.00107882416341500,0.00117813120596100,0.00129586341790900,0.00150058069266400,0.00145976303611000,0.00143387576099500,0.00151479325722900,0.00189456029329400,0.00135730009060400,0.00149146770127100,0.00128873693756800,0.00132771942298900,0.00140289345290500,0.00144312763586600,0.00138182425871500,0.00121991010382800,0.00123634061310400,0.00157127797137900,0.00158856832422300,0.00138722674455500,0.00196937192231400,0.00177456752862800,0.00121580949053200,0.00115475780330600,0.00146593165118200,0.00158937857486300,0.00142380013130600,0.00156803592108200,0.00131671305280200,0.00120458472520100,0.00151810480747400,0.00147662358358500,0.00132098724134300,0.00143756030593100,0.00160600885283200,0.00143166363704900,0.00168147729709700,0.00174339092336600,0.00154746498446900,0.00157942145597200,0.00140975974500200,0.00138180365320300,0.00137808173894900,0.00135973526630600,0.00128439348191000,0.00136733229737700,0.00127124355640300,0.00132964132353700,0.00123117631301300,0.00128837558440900,0.00130197894759500,0.00126195291522900,0.00129339878913000,0.00137097085826100,0.00127403123769900,0.00132381683215500,0.00131315609905900,0.00152778672054400,0.00162436999380600,0.00161996495444300,0.00134883751161400,0.00175723549909900,0.00178984366357300,0.00153772241901600,0.00179937342181800,0.00146108609624200,0.00128352316096400,0.00176260410808000,0.00200077821500600,0.00135518552269800,0.00194636778906000,0.00216706166975200,0.00148534320760500,0.00187802116852300,0.00171067717019500,0.00145985675044400,0.00157886254601200,0.00141905411146600,0.00155868823640000,0.00185225706081800,0.00158204010222100,0.00140578940045100,0.00142628338653600,0.00189108098857100,0.00163430476095500,0.00132637831848100,0.00190239120274800,0.00205233949236600,0.00177188054658500,0.00206808769144100,0.00179400434717500,0.00182859913911700,0.00239726598374500,0.00186222186312100,0.00151263060979500,0.00180134421680100,0.00139513006433800,0.00138290273025600,0.00161348748952200,0.00162877910770500,0.00137637439183900,0.00146510719787300,0.00136131572071500,0.00129419134464100,0.00136725546326500,0.00140165863558700,0.00148270186036800,0.00169541162904400,0.00138803804293300,0.00133832078427100,0.00139117520302500,0.00144796434324200,0.00130118534434600,0.00139499164652100,0.00148103595711300,0.00128625542856800,0.00132718589156900,0.00144267349969600,0.00142870016861700,0.00149315828457500,0.00434543238952800,0.0157148409634800,0.0167073849588600,0.0165197234600800,0.0107733439654100,0.00273623829707500,0.00278323073871400,0.00326169515028600,0.00428854301571800,0.0146493753418300,0.0163035839796100,0.0166384465992500,0.0113913547247600,0.00635413965210300,0.00525979977101100,0.00419266056269400,0.00358219933696100,0.00302534154616300,0.00242395699024200,0.00230488902889200,0.00219005811959500,0.00170073297340400,0.00181856029667000,0.00191883859224600,0.00221334584057300,0.00201517529785600,0.00206336192786700,0.00258486904203900,0.00286292028613400,0.00328894681297200,0.00356574566103500,0.00372082903049900,0.00365190533921100,0.00477009499445600,0.00542060704901800,0.00506692845374300,0.00441524013876900,0.00519004091620400,0.00579954497516200,0.00600139144808100,0.00590856419876200,0.00585280731320400,0.00625614263117300,0.00623863050714100,0.00626431498676500,0.00670837424695500,0.00679474463686300,0.00643559964373700,0.00605143280699800,0.00586804235354100,0.00571992900222500,0.00559346843510900,0.00569834560155900,0.00589290913194400,0.00616515194997200,0.00576868979260300,0.00549456989392600,0.00555947190150600,0.00516957230866000,0.00508691882714600,0.00550192734226600,0.00628734473139000,0.00701786391437100,0.00789503939449800,0.0114656332880300,0.0146223064512000,0.0176143534481500,0.0176421944052000,0.0147148417308900,0.0111187640577600,0.00662733148783400,0.00252272607758600,0.00231871008873000,0.00269752042368100,0.00204745773226000,0.00339025654830000,0.00349931139498900,0.00205528223887100,0.00250244373455600,0.00375750125385800,0.00535101722925900,0.00829925946891300,0.0123714422807100,0.0146293258294500,0.0145983984693900,0.0124043906107500,0.00896590948104900,0.00565529940649900,0.00415386678651000,0.00441957451403100,0.00469917664304400,0.00479339435696600,0.00414768792688800,0.00366715504787900,0.00347910565324100,0.00374820898287000,0.00530573632568100,0.00546778086572900,0.00453057931736100,0.00618124241009400,0.00522491801530100,0.00327196577563900,0.00406450452283000,0.00405055098235600,0.00240400829352400,0.00259201531298500,0.00382135459221900,0.00290944008156700,0.00197434960864500,0.00298841064795900,0.00350630818866200,0.00273253535851800,0.00322353956289600,0.00489206146448900,0.00734462495893200,0.00772114703431700,0.00652676960453400,0.00528289331123200,0.00351224606856700,0.00279640266671800,0.00298060546629100,0.00289856386371000,0.00281362491659800,0.00227301544509800,0.00202692602761100,0.00188117369543800,0.00194017903413600,0.00248489901423500,0.00277511030435600,0.00226575485430700,0.00220064655877600,0.00320360134355700,0.00271560996770900,0.00181216769851700,0.00204825703986000,0.00153196253813800,0.00160576158668800,0.00181596085894900,0.00186090962961300,0.00150404416490300,0.00194894464220900,0.00398054439574500,0.00513736950233600,0.00596242258325200,0.00464944029226900,0.00405170861631600,0.00286311795935000,0.00168484810274100,0.00218656030483500,0.00251782848499700,0.00171565322671100,0.00151401734910900,0.00187455199193200,0.00149222603067800,0.00155646912753600,0.00161900778766700,0.00141863059252500,0.00192662456538500,0.00195324770174900,0.00135356152895800,0.00158664793707400,0.00178995367605200,0.00148096913471800,0.00155926507432000,0.00169972667936200,0.00165429792832600,0.00140245468355700,0.00170663138851500,0.00162390642799400,0.00131819921080000,0.00159376347437500,0.00159991986583900,0.00146500603295900,0.00145272247027600,0.00149883225094500,0.00153009744826700,0.00182758062146600,0.00203093048185100,0.00160977791529100,0.00143941957503600,0.00171903462614900,0.00164604582823800,0.00139985885471100,0.00199301471002400,0.00216113496571800,0.00177027995232500,0.00342561351135400,0.00652205664664500,0.00956994853913800,0.0143064009025700,0.0178101062774700,0.0214453749358700,0.0259513258934000,0.0287852324545400,0.0322643406689200,0.0348408371210100,0.0389946103096000,0.0417719185352300,0.0476161539554600,0.0515650622546700,0.0527081489563000,0.0578899011015900,0.0659535080194500,0.0743885636329700,0.0825388208031700,0.0903493463993100,0.102863967418700,0.109149068594000,0.106186479330100,0.0934290215373000,0.0826950818300200,0.0698334202170400,0.0588823072612300,0.0483226068317900,0.0307497698813700,0.0243607051670600,0.0162022672593600,0.00605332804843800,0.00547522120177700,0.00559776276350000,0.00931861530989400,0.0127159971743800,0.0136000374332100,0.0154967652633800,0.0132942302152500,0.0115474518388500,0.00768174091354000,0.00668279780075000,0.00552339805290100,0.00540188793093000,0.00522676622495100,0.00499918730929500,0.00508879637345700,0.00585424667224300,0.0273980088532000,0.0811685845255900,0.129034399986300,0.158060193061800,0.169409602880500,0.156945645809200,0.133177429437600,0.102501943707500,0.0649088397622100,0.0275905802846000,0.00959616433829100,0.00792300421744600,0.00746305054053700,0.00720870587974800,0.00721625098958600,0.00631615892052700,0.00617842376232100,0.00601696455851200,0.00557906040921800,0.00567547790706200,0.0174374878406500,0.0716867446899400,0.126242339611100,0.177185788750600,0.207712486386300,0.201303899288200,0.174198701977700,0.134453624486900,0.0871225893497500,0.0563914030790300,0.0265109427273300,0.0111845023930100,0.00703767966479100,0.00494820484891500,0.00487064011395000,0.00488744629547000,0.00483611412346400,0.00437555927783300,0.00469009019434500,0.00533196562901100,0.00546547584235700,0.00526391435414600,0.00452969176694800,0.00472084293142000,0.00434107286855600,0.00436108559370000,0.00535539491102100,0.00829282309860000,0.0125684412196300,0.0156943686306500,0.0183826442807900,0.0188326984643900,0.0202457644045400,0.0205876249820000,0.0209209993481600,0.0205879714340000,0.0189878344535800,0.0206882357597400,0.0200000647455500,0.0196611993014800,0.0179566014558100,0.0172970723360800,0.0161522477865200,0.0162783619016400,0.0162302032113100,0.0169915445148900,0.0166420582681900,0.0161631871014800,0.0143800498917700,0.0127662364393500,0.0114356558769900,0.0105128493160000,0.00971092376858000,0.00968047790229300,0.0102064115926600,0.0105765415355600,0.0105467876419400,0.0102284802123900,0.00911771226674300,0.00858502089977300,0.00849443580955300,0.00804559234529700,0.00756083568558100,0.00700740283355100,0.00617385609075400,0.00517477001994800,0.00492409802973300,0.00463740900158900,0.00401615537703000,0.00287854415364600,0.00225668353959900,0.00180913647636800,0.00150072656106200,0.00154466764070100,0.00137255177833100,0.00141046545468300,0.00162969331722700,0.00176876282785100,0.00202774605713800,0.00240434729494200,0.00203374517150200,0.00183231988921800,0.00220828433521100,0.00177073094528200,0.00160389160737400,0.00212730956263800,0.00180703308433300,0.00146634981501800,0.00188631238415800,0.00207780953496700,0.00137647846713700,0.00141666899435200,0.00135962467175000,0.00133480434306000,0.00135097419843100,0.00133528746664500,0.00136413774453100,0.00132512929849300,0.00143759779166400,0.00130673300009200,0.00133859983179700,0.00122559291776300,0.00135765084996800,0.00133335893042400,0.00128348928410600,0.00123919185716700,0.00122467847541000,0.00119155342690600,0.00134863343555500,0.00153938133735200,0.00153167254757100,0.00139547022990900,0.00150542601477400,0.00139577605295900,0.00145833916030800,0.00141835550312000,0.00145234900992400,0.00141049677040400,0.00138060795143200,0.00131366262212400,0.00130363996140700,0.00137439172249300,0.00130031385924700,0.00124537211377200,0.00129861955065300,0.00145244493614900,0.00151128915604200,0.00134293711744200,0.00125083501916400,0.00119631551206100,0.00121201330330200,0.00123242370318600,0.00123215187341000,0.00147283356636800,0.00156142259948000,0.00139429501723500,0.00149689801037300,0.00148079718928800,0.00143432430923000,0.00141966121736900,0.00173083855770500,0.00152229808736600,0.00148701970465500,0.00156376522500100,0.00163011183030900,0.00165483879391100,0.00208796816878000,0.00159301492385600,0.00140866602305300,0.00126428354997200,0.00127384369261600,0.00124865619000000,0.00119057716801800,0.00120784970931700,0.00123168621212200,0.00119959597941500,0.00131283141672600,0.00125327263958800,0.00119299034122400,0.00123096327297400,0.00130615581292700,0.00145762728061500,0.00145933497697100,0.00179809890687500,0.00178422743920200,0.00131639535538900,0.00145757989957900,0.00151505554094900,0.00150429736822800,0.00158236292190800,0.00144918286241600,0.00150282494723800,0.00136398558970500,0.00127860519569400,0.00135209911968600,0.00127144134603400,0.00122791633475600,0.00120645342394700,0.00121549575123900};
                //speechPitch = new double[]{0,0,0,0,0,0,257,516,413,416,421,420,201,197,197,198,198,396,171,219,343,435,435,341,383,384,322,321,262,442,441,441,265,264,264,261,261,261,261,431,276,276,276,409,404,380,379,377,376,371,370,356,238,177,177,176,286,286,286,139,139,139,139,351,351,529,529,529,527,527,527,527,572,526,105,529,516,262,505,539,539,539,351,351,544,110,110,441,442,183,183,360,361,549,550,371,214,214,283,283,284,285,285,596,300,586,585,557,538,551,551,111,112,112,400,283,284,368,368,383,480,480,290,533,533,286,366,365,365,365,214,214,508,511,479,476,476,480,494,494,495,496,496,497,493,492,492,493,493,494,492,492,491,491,491,242,513,497,494,494,502,504,504,505,505,504,503,503,503,513,494,494,493,501,500,492,493,500,501,465,465,467,468,479,479,480,480,480,480,479,421,419,395,471,475,479,480,482,482,240,240,240,478,483,478,480,480,480,480,474,482,476,479,479,484,464,463,233,361,361,509,508,481,481,321,526,316,501,500,474,232,232,546,547,489,489,453,453,320,320,321,407,406,298,314,486,486,486,151,377,164,500,499,86,483,483,200,200,201,187,434,385,363,362,566,566,491,490,490,490,497,503,500,500,500,514,357,460,465,474,159,158,536,387,388,354,354,120,501,501,253,519,519,112,112,513,513,275,595,501,164,164,164,174,174,560,141,143,172,171,487,164,164,226,529,499,181,326,499,499,437,437,348,348,500,500,463,463,150,150,189,189,199,199,495,476,476,196,453,584,584,315,364,377,526,375,223,439,451,450,209,210,112,360,573,113,114,113,113,207,125,125,489,489,387,302,501,501,500,500,500,298,233,527,502,502,501,501,501,469,469,468,233,534,534,282,282,283,550,550,550,297,162,382,219,380,381,382,382,372,171,171,172,230,230,534,533,532,583,340,340,191,191,191,194,194,549,578,578,578,237,239,396,396,262,262,334,333,333,332,332,332,568,160,305,278,51,51,51,51,524,524,524,494,494,483,487,487,562,563,291,290,290,514,514,514,225,226,227,227,435,557,559,428,428,319,318,500,499,499,500,294,293,148,148,543,543,543,473,472,472,472,219,219,220,220,220,500,501,500,421,420,420,89,479,265,266,291,291,389,389,390,390,390,391,399,399,82,446,446,590,590,469,256,524,230,538,538,450,150,150,150,150,137,514,426,102,432,432,527,585,585,500,501,500,500,500,448,448,571,144,144,463,463,399,214,374,371,320,343,343,343,479,479,193,532,532,95,104,104,289,290,572,196,490,482,482,448,448,502,342,342,342,342,472,160,166,165,528,529,529,283,475,475,401,563,229,229,318,318,135,578,578,578,562,562,266,271,492,525,374,403,404,404,225,507,218,218,587,587,526,190,189,188,437,338,503,502,336,134,134,199,199,498,300,300,498,497,510,509,509,545,545,192,192,192,295,137,135,272,272,499,500,500,500,551,377,379,379,369,373,374,375,300,300,382,382,486,485,485,516,516,516,235,532,461,326,326,327,292,501,501,500,499,498,497,497,496,137,136,134,269,415,392,392,393,393,501,501,500,325,446,460,200,188,188,188,187,379,380,365,395,395,396,396,397,397,397,397,397,313,313,321,321,304,293,293,132,132,497,327,414,414,414,433,566,566,191,543,539,538,399,399,399,359,473,473,474,473,473,591,591,591,223,223,423,202,202,260,260,450,352,352,351,354,212,194,458,399,399,540,300,501,501,496,244,316,315,315,316,317,318,318,319,319,326,327,316,316,315,311,311,312,312,275,275,275,275,455,455,531,241,242,456,241,492,492,487,487,572,572,335,335,336,294,294,497,299,402,402,402,402,402,218,404,356,297,297,587,586,586,587,587,584,537,537,538,520,519,519,518,595,595,600,120,120,120,183,344,344,472,473,517,307,288,289,290,290,290,290,290,284,376,405,405,406,406,422,346,301,562,300,473,473,264,581,581,301,301,300,300,300,590,283,283,501,502,502,502,502,204,564,226,227,299,299,299,455,303,300,299,299,307,307,315,360,359,384,384,204,408,408,204,204,203,203,406,406,388,388,379,376,376,345,332,331,330,330,331,331,331,331,330,300,300,300,571,571,571,378,318,566,567,568,333,290,262,283,283,283,280,280,300,300,298,295,295,277,277,265,280,396,395,281,281,277,269,260,260,259,258,257,257,256,256,251,250,249,250,251,256,258,246,259,259,259,259,258,257,257,257,257,258,252,297,297,297,303,304,304,590,591,148,147,147,284,286,172,172,172,294,294,333,281,282,274,54,594,261,261,261,260,275,275,250,122,122,122,122,240,241,241,240,239,247,247,499,498,500,499,500,169,286,577,197,197,328,455,501,501,117,418,418,572,571,104,105,585,585,586,283,468,301,301,300,547,547,547,547,89,353,84,172,172,172,352,352,184,79,398,399,399,499,499,499,498,499,499,313,311,310,309,309,337,337,600,600,595,595,435,424,140,143,312,312,365,365,239,239,240,240,435,513,253,253,254,502,389,197,197,359,360,360,361,326,542,336,501,500,294,123,124,125,527,527,125,403,154,155,493,579,578,0,0,0};
            }else if((duration < 9) && (duration > 7)){
                //testSequence = '4_668629_676815';
                //phoSeqIndex = new int[]{1,12,6,6,8,1,6,1,6,1,6,1};
                //durSeq = new int[]{1,3,18,1,1,50,9,31,10,66,9,7};
                //speechEnergy = new double[]{0.000649140391033100,0.00108297658152900,0.00132022402249300,0.00144520751200600,0.00131677545141400,0.00145467673428400,0.0103665944188800,0.0463209077715900,0.138057962060000,0.224902510643000,0.265286475420000,0.283681511879000,0.264594942331300,0.226314887404400,0.196140721440300,0.153406888246500,0.124637067318000,0.0953323692083400,0.0524630695581400,0.0260992366820600,0.0269420687109200,0.0276749115437300,0.0300278253853300,0.0306522585451600,0.0331840366125100,0.0335950255394000,0.0381337963044600,0.0417165718972700,0.0444113835692400,0.0467804670333900,0.0462117455899700,0.0481169857084800,0.0453417189419300,0.0446908883750400,0.0413157865405100,0.0390348434448200,0.0363046489656000,0.0372973233461400,0.0364109091460700,0.0402055755257600,0.0435972623527100,0.0440784357488200,0.0459890440106400,0.0417579188942900,0.0393894650042100,0.0374862700700800,0.0364644713699800,0.0381498597562300,0.0344074107706500,0.0340273492038200,0.0294679645448900,0.0292726606130600,0.0285506304353500,0.0296323224902200,0.0290320124477100,0.0274191908538300,0.0263470429927100,0.0256628878414600,0.0265286397188900,0.0293917600065500,0.0319180227816100,0.0324848592281300,0.0294630005955700,0.0288208462297900,0.0261914171278500,0.0268439520150400,0.0260008256882400,0.0277955345809500,0.0277400836348500,0.0257154256105400,0.0224221087992200,0.0205513872206200,0.0213898513466100,0.0213523805141400,0.0221549440175300,0.0207724217325400,0.0206675995141300,0.0187984313815800,0.0187024809420100,0.0174935702234500,0.0171174034476300,0.0209955498576200,0.0236804112792000,0.0240017343312500,0.0208512470126200,0.0151787428185300,0.0144189391285200,0.0121370628476100,0.00953145325183900,0.00616548256948600,0.00411216076463500,0.00331812817603300,0.00350603996776000,0.00362459756434000,0.00420709513127800,0.00696472683921500,0.0100234020501400,0.0102409273386000,0.00914455577731100,0.00442212121561200,0.00304805114865300,0.00335064390674200,0.00245740613900100,0.00267598312348100,0.00231483019888400,0.00182510132435700,0.00189001346007000,0.00260419584810700,0.00231145345605900,0.00258378963917500,0.00257027242332700,0.00175407051574400,0.00188379071187200,0.00246180733665800,0.00213703420013200,0.00181656179484000,0.00238125165924400,0.00254395441152200,0.00195832969620800,0.00235073338262700,0.00199668598361300,0.00263507943600400,0.00303854676894800,0.00195359205827100,0.00201178528368500,0.00210070936009300,0.00171067123301300,0.00167466956190800,0.00200190232135400,0.00173964630812400,0.00175117515027500,0.00175016722641900,0.00162323971744600,0.00181423721369400,0.00181137630716000,0.00172810943331600,0.00133998598903400,0.00139284972101400,0.00144675874617000,0.00196996168233500,0.00209735403768700,0.00210371590219400,0.00244011450558900,0.00280530145391800,0.00225788331590600,0.00165757490322000,0.00180076260585300,0.00162708072457500,0.00169095688033800,0.00151540245860800,0.00160231860354500,0.00185604114085400,0.00178261182736600,0.00183244142681400,0.00162797153461700,0.00175158004276500,0.00172159355133800,0.00144731847103700,0.00174684962257700,0.00163921876810500,0.00163271592464300,0.00159881799481800,0.00140077818650800,0.00140737579204100,0.00154527020640700,0.00144868460483800,0.00135694106575100,0.00138218142092200,0.00147308665327700,0.00152977113612000,0.00158825726248300,0.00153249886352600,0.00139572366606400,0.00144741439726200,0.00136847526300700,0.00133780355099600,0.00137196818832300,0.00144065532367700,0.00147523207124300,0.00170117651578000,0.00203758804127600,0.00211964291520400,0.00203557196073200,0.00152566027827600,0.00161118619144000,0.00166024512145700,0.00161654385738100,0.00164648040663500,0.00165310245938600,0.00155785528477300,0.00148047332186300,0.00147930847015200,0.00144648982677600,0.00140417309012300,0.00140434643253700,0.00145933078602000,0.00140519626438600,0.00178197713103100,0.00202857377007600,0.00241538137197500,0.00222248653881300,0.00177484587766200,0.00144927587825800,0.00131470756605300,0.00144926167558900,0.00153608620166800,0.00158371904399200,0.00159693649038700,0.00149462954141200,0.00169195688795300,0.00203268509358200,0.00231395964510700,0.00233540288172700,0.00197043060325100,0.00153634930029500,0.00147987238597100,0.00129207735881200,0.00141378957778200,0.00142053735908100,0.00175479613244500,0.00202181283384600,0.00203674519434600,0.00192322104703600,0.00143916241359000,0.00140928500331900,0.00148665404412900,0.00164207012858200,0.00207244744524400,0.00162739830557300,0.00140087562613200,0.00141590461134900,0.00145950820297000,0.00139504857361300,0.00133718282450000,0.00128421874251200,0.00123547238763400,0.00125064991880200,0.00136124808341300,0.00148689525667600,0.00157378101721400,0.00147753965575200,0.00157907884568000,0.00153600901830900,0.00148568756412700,0.00158753839787100,0.00162393122445800,0.00141835736576500,0.00147482613101600,0.00136932916939300,0.00149751629214700,0.00140821794048000,0.00165401445701700,0.00168182072229700,0.00203585112467400,0.00179324788041400,0.00141826504841400,0.00155111891217500,0.00155224720947400,0.00138935260474700,0.00149028736632300,0.00143767148256300,0.00136268511414500,0.00183830026071500,0.00184606714174200,0.00152977404650300,0.00164456898346500,0.00160792993847300,0.00148978224024200,0.00156191166024700,0.00136842369101900,0.00184265850111800,0.00211008870974200,0.00188204331789200,0.00155390845611700,0.00173367338720700,0.00143385468982200,0.00152899953536700,0.00149659987073400,0.00138079980388300,0.00159127113875000,0.00160400324966800,0.00136398861650400,0.00140462629497100,0.00127345626242500,0.00128080416470800,0.00133868458215100,0.00131097447592800,0.00126525666564700,0.00128859491087500,0.00138673349283600,0.00139853253494900,0.00145092385355400,0.00145056843757600,0.00123087840620400,0.00126824527978900,0.00135964900255200,0.00147339748218700,0.00166168704163300,0.00192944763694000,0.00226425682194500,0.00240275473333900,0.00233932258561300,0.00252905045636000,0.00247220764868000,0.00286343321204200,0.00295092351734600,0.00323248119093500,0.00344082410447300,0.00329479994252300,0.00353205017745500,0.00390961207449400,0.00395633140578900,0.00407093763351400,0.00371807627379900,0.00358862290158900,0.00337214162573200,0.00299160764552700,0.00284286634996500,0.00277333450503600,0.00279659708030500,0.00284870900213700,0.00303890253417200,0.00320101273246100,0.00334835704416000,0.00352448131889100,0.00355577724985800,0.00334896286949500,0.00309138582088100,0.00288228853605700,0.00247275875881300,0.00202758982777600,0.00210772966966000,0.00178406236227600,0.00203782599419400,0.00189976405818000,0.00157652015332100,0.00151484063826500,0.00131114129908400,0.00137037050444600,0.00134441442787600,0.00142043153755400,0.00136097834911200,0.00136222492437800,0.00138602626975600,0.00151591573376200,0.00136309687513900,0.00136339641176200,0.00140483840368700,0.00136023887898800,0.00144412741065000,0.00130082201212600,0.00131248927209500,0.00133521377574700,0.00125926244072600,0.00134944089222700,0.00143294001463800,0.00148028007242800,0.00147804419975700,0.00160107121337200,0.00144610728602900,0.00152219086885500,0.00154331652447600,0.00150555686559500,0.00126795261167000,0.00128854985814500,0.00121073995251200,0.00125093793030800,0.00144734350033100,0.00145243469160100,0.00144162983633600,0.00128352176398000,0.00127039151266200,0.00133160490077000,0.00139252911321800,0.00140726717654600,0.00139535043854300,0.00136114680208300,0.00137284002266800,0.00135030841920500,0.00137307099066700,0.00139148463495100,0.00151646079029900,0.00153078651055700,0.00175493431743200,0.00169010553509000,0.00159428943879900,0.00237687397748200,0.00203635706566300,0.00187459646258500,0.00209130300208900,0.00244846171699500,0.00184118025936200,0.00165118137374500,0.00143270893022400,0.00146867544390300,0.00147210096474700,0.00135825353208900,0.00147385057061900,0.00141943059861700,0.00149098259862500,0.00124617945402900,0.00128747278358800,0.00139861099887600,0.00126841675955800,0.00128979003056900,0.00110905105248100,0.00126704678405100,0.00135978846810800,0.00138377188704900,0.00136476906482100,0.00132698065135600,0.00138322869315700,0.00124559772666500,0.00139268650673300,0.00135023903567300,0.00146748742554300,0.00138244521804200,0.00129025452770300,0.00121533381752700,0.00120071950368600,0.00121868122369100,0.00130032084416600,0.00135735608637300,0.00132444617338500,0.00133565196301800,0.00133547361474500,0.00133214856032300,0.00133444287348500,0.00116287486162000,0.00120870058890400,0.00132829067297300,0.00150596292223800,0.00132338923867800,0.00137516506947600,0.00158266955986600,0.00168202782515400,0.00146107550244800,0.00148289557546400,0.00146919779945200,0.00166967418044800,0.00174233317375200,0.00160670687910200,0.00160658929962700,0.00152804469689700,0.00134145165793600,0.00140721385832900,0.00148730690125400,0.00148673309013200,0.00130251317750700,0.00120788335334500,0.00132256350480000,0.00136373331770300,0.00138264056295200,0.00181985343806400,0.00169468868989500,0.00161775026936100,0.00141870335210100,0.00133501447271600,0.00140727788675600,0.00153957062866500,0.00149963307194400,0.00197564321570100,0.00190173718147000,0.00182929844595500,0.00178447412326900,0.00179941311944300,0.00173114798963100,0.00176947657018900,0.00169260229449700,0.00184812094084900,0.00179203716106700,0.00190448598004900,0.00185343832708900,0.00149414152838300,0.00155756867025000,0.00158924516290400,0.00180251162964900,0.00174073583912100,0.00168209511321000,0.00162632018327700,0.00156451889779400,0.00175652420148300,0.00176799856126300,0.00174116541165900,0.00140686612576200,0.00136670167557900,0.00132506748195700,0.00128075189422800,0.00129930779803500,0.00122823566198300,0.00129461789038000,0.00201185722835400,0.00210282020270800,0.00150725757703200,0.00131127482745800,0.00116517813876300,0.00111294223461300,0.00115927937440600,0.00120844843331700,0.00125913461670300,0.00120325037278200,0.00125731236767000,0.00134248950053000,0.00136161386035400,0.00139019661583000,0.00110311084426900,0.00119592039845900,0.00127987447194800,0.00145281571894900,0.00188532448373700,0.00174741889350100,0.00159807561431100,0.00141357060056200,0.00143626495264500,0.00153502658940900,0.00133203552104500,0.00139184109866600,0.00146624003537000,0.00139891845174100,0.00145316484849900,0.00138789974153000,0.00130756571888900,0.00135742477141300,0.00127428839914500,0.00146312767174100,0.00156416546087700,0.00155292567797000,0.00131522922311000,0.00145612028427400,0.00155578588601200,0.00140577286947500,0.00149364268872900,0.00162425707094400,0.00154806149657800,0.00176310213282700,0.00158813816960900,0.00141670706216200,0.00132846366614100,0.00147293403279000,0.00141186686232700,0.00134076527319800,0.00133579806424700,0.00127883406821600,0.00134750828146900,0.00154792726971200,0.00173098593950300,0.00166530313435900,0.00161806959658900,0.00132142507936800,0.00145656138192900,0.00164698809385300,0.00159584311768400,0.00167685095220800,0.00158092984929700,0.00150298618245900,0.00144709192682100,0.00144026742782400,0.00132355839014100,0.00142093154136100,0.00156421703286500,0.00134788372088200,0.00154804601334000,0.00164281087927500,0.00124652846716300,0.00132939522154600,0.00141862116288400,0.00127562403213200,0.00117672409396600,0.00124903430696600,0.00130970741156500,0.00140177155844900,0.00140338286291800,0.00141478283330800,0.00136431097053000,0.00128235924057700,0.00128464750014200,0.00129883328918400,0.00137726904358700,0.00148068030830500,0.00131236028391900,0.00127727398648900,0.00120212219189900,0.00126746878959200,0.00143871933687500,0.00134191010147300,0.00127837748732400,0.00134511210490000,0.00134023407008500,0.00132330006454100,0.00138213054742700,0.00139284040778900,0.00131710176356100,0.00129622826352700,0.00128756323829300,0.00145516986958700,0.00130796653684200,0.00123995740432300,0.00120661198161500,0.00125375261995900,0.00124399201013100,0.00125448557082600,0.00131713622249700,0.00151500303763900,0.00179100222885600,0.00146516493987300,0.00149668636731800,0.00153311900794500,0.00129681325051900,0.00136721052695100,0.00174191512633100,0.00149092625360900,0.00179505313281000,0.00202254415489700,0.00175023335032200,0.00143493746873000,0.00162328931037300,0.00187468389049200,0.00165376521181300,0.00163265818264300,0.00142868026159700,0.00130243517924100,0.00142121862154500,0.00137824472039900,0.00186463468708100,0.00189873354975100,0.00157121906522700,0.00164785643573900,0.00210602791048600,0.00178098538890500,0.00183017889503400,0.00220180372707500,0.00190629065036800,0.00162178056780200,0.00162615091539900,0.00147896434646100,0.00133762310724700,0.00154919736087300,0.00169683026615500,0.00141534139402200,0.00143959466368000,0.00156789249740500,0.00166757358238100,0.00144050864037100,0.00161022983957100,0.00138608459383200,0.00142669118940800,0.00132054428104300,0.00155772292055200,0.00141403672751000,0.00151745416224000,0.00140255503356500,0.00154085701797200,0.00164105521980700,0.00156112574040900,0.00164319213945400,0.00148608093150000,0.00148221233394000,0.00140047213062600,0.00123318994883400,0.00147538108285500,0.00139868096448500,0.00152561161667100,0.00135328527540000,0.00129415839910500,0.00121632171794800,0.00120792072266300,0.00124935351777800,0.00145138276275200,0.00154173409100600,0.00156501133460600,0.00165613112039900,0.00178947264794300,0.00150386663153800,0.00143476773519100,0.00138233799953000,0.00130841904319800,0.00134216202423000,0.00114266783930400,0.00119585241191100,0.00140409846790100,0.00138328783214100,0.00130396150052500,0.00137018505483900,0.00134100299328600,0.00131517020054200,0.00126236060168600,0.00125721772201400,0.00125090370420400,0.00130074389744500,0.00127598363906100,0.00123024464119200,0.00134628673549700,0.00139401585329300,0.00138002634048500,0.00129534502048000,0.00139437953475900,0.00157474761363100,0.00141023774631300,0.00167163321748400,0.00216968171298500,0.00228236243128800,0.00179802242200800,0.00264528044499500,0.00215223617851700,0.00158067035954400,0.00149719638284300,0.00147761416155800,0.00171073945239200,0.00196894840337300,0.00168499955907500,0.00153467932250400,0.00173087825533000,0.00184145127423100,0.00158350367564700,0.00232583400793400,0.00244541070424000,0.00164968031458600,0.00174132885877000,0.00148028950206900,0.00135301984846600,0.00149645435158200,0.00146024336572700,0.00131196598522400,0.00139516044873700,0.00164352857973400,0.00170055718626800,0.00177562457975000,0.00224324013106500,0.00171790597960400,0.00163367472123400,0.00182284996844800,0.00159647583495800,0.00148350023664500,0.00162041070871100,0.00142370723187900,0.00151341781020200,0.00148548779543500,0.00142400059849000,0.00142358941957400,0.00151037401519700,0.00126303324941500,0.00129757798276800,0.00127784919459400,0.00122337276116000,0.00126010808162400,0.00135865737684100,0.00140167272184000,0.00133527803700400,0.00156151002738600,0.00152430729940500,0.00150067463982900,0.00145709048956600,0.00137381604872600,0.00134260125923900,0.00133409653790300,0.00144169491250100,0.00143332569859900,0.00143418344669000,0.00178814993705600,0.00209405552595900,0.00217277742922300,0.00281276623718400,0.00349147664383100,0.00452976441010800,0.00508205639198400,0.00543353287503100,0.00682127755135300,0.0108284866437300,0.00923543609678700,0.00865859724581200,0.00811895169317700,0.00749280489981200,0.00695653725415500,0.00508806714788100,0.00502935191616400,0.00531160226091700,0.00741708511486600,0.00757603486999900,0.00518643995747000,0.00535523658618300,0.00412496039643900,0.00377584481611800,0.00452850572764900,0.00364077696576700,0.00370712298899900,0.00442907214164700,0.00469210045412200,0.00399048347026100,0.00469216704368600,0.00303222401998900,0.00328636658377900,0.00300911744125200,0.00317160575650600,0.00187213742174200,0.00164007779676500,0.00167181331198700,0.00167776714079100,0.00205505499616300,0.00251587433740500,0.00190651416778600,0.00152783433441100,0.00190990872215500,0.00224715145304800,0.00210397806949900,0.00163415598217400,0.00158681173343200,0.00192488194443300,0.00206315563991700,0.00170587934553600,0.00212794193066700,0.00220565660856700,0.00185241422150300,0.00204135826788800,0.00166409777011700,0.00150101282633800,0.00137544784229200,0.00138246826827500,0.00137378054205300};
                //speechPitch = new double[]{0,0,0,0,0,0,140,292,293,294,295,296,299,301,304,309,310,310,294,294,266,300,300,298,295,317,315,315,315,498,521,521,560,561,561,566,381,380,380,380,380,380,380,303,516,516,516,516,205,510,510,510,510,509,508,129,328,330,322,322,322,321,311,312,313,313,314,314,314,314,314,314,214,214,215,162,319,584,584,585,585,270,292,292,293,323,322,303,303,304,281,281,150,550,550,513,451,464,463,462,462,476,476,128,380,380,380,443,443,89,89,89,174,174,147,147,147,309,308,304,304,304,516,300,301,301,94,94,536,305,305,300,300,347,85,248,331,242,241,143,143,305,288,331,377,377,246,245,312,312,121,366,504,303,302,301,318,242,242,393,372,372,586,421,422,386,201,201,83,83,83,83,533,533,314,244,238,238,238,501,500,499,498,488,489,386,319,174,245,194,590,591,590,295,295,50,50,391,248,248,248,247,492,295,295,579,579,141,215,215,425,425,425,324,300,300,300,143,574,573,578,248,248,252,252,597,299,129,129,128,112,215,548,306,306,306,129,129,500,500,500,500,495,495,495,468,286,300,392,401,401,401,402,402,301,302,301,300,300,298,297,563,563,339,473,413,413,414,375,95,300,300,301,300,300,484,121,414,248,247,247,599,235,235,235,234,306,306,306,53,53,53,377,377,152,152,146,514,514,514,515,596,596,596,291,414,267,434,434,335,216,341,340,340,341,221,221,139,139,145,144,143,594,344,337,338,274,274,273,170,365,113,553,553,573,591,590,529,413,513,512,242,406,406,127,376,244,182,182,572,115,213,213,363,146,145,145,145,465,514,133,250,251,482,482,206,206,275,456,501,500,543,544,150,135,133,133,129,560,560,132,132,131,106,301,300,300,300,454,454,435,435,435,501,501,500,500,500,500,500,500,565,565,369,153,153,456,456,421,423,423,419,418,204,533,533,84,398,398,408,408,522,576,298,298,498,498,498,140,139,242,242,243,243,243,356,118,488,487,111,111,585,585,585,583,583,179,180,233,234,406,309,530,322,321,537,437,535,576,575,575,370,370,157,157,157,453,433,433,415,415,169,353,331,333,434,434,336,410,205,205,206,517,517,290,290,560,559,451,448,185,269,268,501,267,267,268,197,398,494,495,454,454,453,453,354,354,352,524,477,477,395,396,559,206,207,500,499,499,499,500,355,96,501,520,520,520,222,380,379,377,536,157,157,301,107,106,339,239,239,349,349,350,143,143,320,157,157,156,440,295,517,137,137,547,547,564,272,491,260,107,107,459,134,134,140,409,409,138,138,66,66,565,565,101,62,495,156,235,234,233,233,233,506,506,506,506,509,509,200,86,85,375,376,376,376,376,376,377,377,377,377,377,353,235,236,237,237,502,501,214,213,212,212,485,501,501,300,326,588,209,209,500,476,512,512,54,167,168,222,223,565,565,566,426,426,537,407,320,321,199,200,201,528,528,528,528,523,523,143,143,143,141,436,436,437,443,343,569,569,570,455,455,392,392,392,311,406,552,552,441,442,442,305,305,321,319,319,319,244,245,246,246,120,120,129,129,219,518,518,518,567,569,569,276,276,544,494,511,305,578,286,286,421,403,403,471,471,317,317,106,109,551,551,369,406,407,188,188,553,553,553,542,248,245,395,395,564,564,564,564,584,451,193,193,398,560,464,98,499,499,499,93,93,520,562,92,164,164,136,136,136,407,407,303,141,144,300,542,303,302,339,339,346,345,181,579,143,143,143,465,465,340,340,339,339,339,339,568,143,277,277,297,328,586,564,586,586,586,558,558,558,188,224,329,329,530,530,277,449,450,450,449,288,288,288,488,488,488,523,523,523,452,451,451,0,0,0};
            }


            phonems = new ArrayList<LaughPhoneme>(phoSeqIndex.length);
            int totalDur = 0;
            for (int i=0; i<phoSeqIndex.length;i++){

                int phonemeDur = durSeq[i];
                LaughPhoneme.LaughPhonemeType phonemeType = phonemeIndex2PhonemeType(phoSeqIndex[i]);
                LaughPhoneme newLaughPhoneme = new LaughPhoneme(phonemeType, phonemeDur/frameRate);

                //adding energy infos to this phoneme

                double[] speechEnergyOfOnePhoneme = new double [phonemeDur];
                int integ = i+1;
                //System.out.println(" index = " + "speechEnergyLength" + speechEnergy.length + "  "+integ +" startDur = "+totalDur+"        "+"phonemeDur = "+ phonemeDur);
                System.arraycopy(speechEnergy, totalDur, speechEnergyOfOnePhoneme, 0, phonemeDur);
                newLaughPhoneme.setEnergy(speechEnergyOfOnePhoneme);

                //adding pitch infos to this phoneme
                double[] speechPitchOfOnePhoneme = new double [phonemeDur];
                System.arraycopy(speechPitch, totalDur, speechPitchOfOnePhoneme, 0, phonemeDur);
                newLaughPhoneme.setPitch(speechPitchOfOnePhoneme);

                //adding phoneme intensity
                //newLaughPhoneme.setInensity(phonemeIntensity[i]);

                //adding phoIntensityByFrame to this phoneme.
                newLaughPhoneme.setPhoIntensityByFrame(phoIntensityByFrame);

                phonems.add(newLaughPhoneme);
                totalDur += phonemeDur;

            }
            //System.out.println("total duration in phoneme generator = " + totalDur);
        }
    }


    public LaughPhoneme.LaughPhonemeType phonemeIndex2PhonemeType(int phonemeIndex){

        LaughPhoneme.LaughPhonemeType phonemeType = null;
        switch (phonemeIndex){
            case 1:
                phonemeType = LaughPhoneme.LaughPhonemeType.sil;
                break;
            case 2:
                phonemeType = LaughPhoneme.LaughPhonemeType.ne;
                break;
            case 3:
                phonemeType = LaughPhoneme.LaughPhonemeType.click;
                break;
            case 4:
                phonemeType = LaughPhoneme.LaughPhonemeType.nasal;
                break;
            case 5:
                phonemeType = LaughPhoneme.LaughPhonemeType.plosive;
                break;
            case 6:
                phonemeType = LaughPhoneme.LaughPhonemeType.fricative;
                break;
            case 7:
                phonemeType = LaughPhoneme.LaughPhonemeType.ic;
                break;
            case 8:
                phonemeType = LaughPhoneme.LaughPhonemeType.e;
                break;
            case 9:
                phonemeType = LaughPhoneme.LaughPhonemeType.o;
                break;
            case 10:
                phonemeType = LaughPhoneme.LaughPhonemeType.grunt;
                break;
            case 11:
                phonemeType = LaughPhoneme.LaughPhonemeType.cackle;
                break;
            case 12:
                phonemeType = LaughPhoneme.LaughPhonemeType.a;
                break;
            case 13:
                phonemeType = LaughPhoneme.LaughPhonemeType.glotstop;
                break;
            case 14:
                phonemeType = LaughPhoneme.LaughPhonemeType.vowel;
                break;

        }

        return phonemeType;


    }


    @Override
    public List<LaughPhoneme> getPhonemes() {
        return phonems;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    private double getDefaultDuration(){
        return 2+Math.random()*18; //TODO : get one duration inside files
    }

}
