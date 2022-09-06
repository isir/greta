/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.sequencemining.structures;

/**
 *
 * @author Mathieu
 */
public enum Modality {

        //0,time
        //1,occlusions
        //2,task
        //3,turn
        //4,Paraverbal
        //5,CandidateParaverbal
        //6,IPA
        //7,Topic
        //8,Eyes
        //9,HeadDrct
        //10,HeadDrctComplete
        //11,HeadMvmt
        //12,HeadMvmtIntRep
        //13,Eyebrows
        //14,EyebrowsComplete
        //15,Mouth
        //16,MouthComplete
        //17,Posture
        //18,PostureComplete
        //19,Gestures
        //20,GesturePartIntSpa
        //21,HandsPosition
        //22,ConcatGesturesTurn
        //23,DOCUMENT
        //24,HeadDrctIntensity
        //25,HeadMvmtIntRep
        //26,EyebrowsInt
        //27,MouthInt
        //28,PostureInt
        //29,GestureCommIntSpa
        //30,GestAdaptorPart
    Turn(3),
    Eyes(8),
    SpineDirection(10),
    HeadMovement(12),
    Eyebrows(14),
    Smile(16),
    Posture(18),
    Gesture(20),
    HandsPosition(21);

    private int columnNumber;

    private Modality(int c)
    {
        columnNumber=c;
    }

    public int getCol()
    {
        return columnNumber;
    }

    public static Modality getModalityFromColNumber(int c)
    {
        switch(c)
        {
            case 3: return Modality.Turn;
            case 8: return Modality.Eyes;
            case 10: return Modality.SpineDirection;
            case 12: return Modality.HeadMovement;
            case 14: return Modality.Eyebrows;
            case 16: return Modality.Smile;
            case 18: return Modality.Posture;
            case 20: return Modality.Gesture;
            case 21: return Modality.HandsPosition;
        }

        return null;
    }
}
