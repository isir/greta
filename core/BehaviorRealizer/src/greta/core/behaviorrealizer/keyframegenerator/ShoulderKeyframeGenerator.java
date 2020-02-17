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
package greta.core.behaviorrealizer.keyframegenerator;

import greta.core.keyframes.Keyframe;
import greta.core.keyframes.ShoulderKeyframe;
import greta.core.keyframes.TorsoKeyframe;
import greta.core.signals.ShoulderSignal;
import greta.core.signals.Signal;
import greta.core.signals.SpineDirection;
import greta.core.signals.SpinePhase;
import greta.core.util.enums.Side;
import greta.core.util.time.TimeMarker;
import java.util.Comparator;
import java.util.List;

/**
 * Syntax:
 *
 * <shoulder amount="0.35" end="9.5" id="shoulder" lexeme="shake"
 * repetition="3120" side="both" start="0.2" />
 * amount - intensity (normalized = 0) or amplitude (normalized = 0) repetition
 * - velocity (normalized = 0) or frequency (normalized = 0) normalized {0,1} -
 * default 1 torso {0,1} - default 0 - no torso movement side - left, right,
 * both
 *
 * <shoulder id="sh1" start="1.0" end="3.0" lexeme="up" side="both" amount="1"/>
 * <shoulder id="sh1" start="1.0" end="3.0" lexeme="front" side="left"
 * amount="1">
 * <shoulder id="sh1" start="1.0" end="3.0" lexeme="back" side="both"
 * amount="1">
 *
 * @author Radoslaw Niewiadomski
 */
public class ShoulderKeyframeGenerator extends KeyframeGenerator {

    public ShoulderKeyframeGenerator() {
        super(ShoulderSignal.class);
    }

    //{0.4688,   0.01893,   -7.494,   0.2344,   0.01058,   -4.851,    4.043,   0.001226,  -75.75,     0,      0,          0},
    //{0.19531,  0.057195,  -1.1947,  1.1719,   0.0079467,  1.5853,   3.3203,  0.0021477,  -1.1947,   3.9063, 0.0013512,  1.5853},
    //{0.29297,  0.036887,  -2.198,   0.097656, 0.032051,   0.34931,  4.6875,  0.001387,   -2.198,    3.9063, 0.0013305,  0.34931},
    //{0.29297,  0.017427,  2.8195,   0.097656, 0.014806,  -1.6313,   3.3203,  0.0019308,   2.8195,   3.6133, 0.0016668, -1.6313},
    //{0.097656, 0.055865,  2.1927,   0.29297,  0.018604,   0.89769,  3.2227,  0.0016488,   2.1927,   3.9063, 0.0015881,  0.89769},
    //{0.19531,  0.049224,  2.4462,   0.58594,  0.027756,   1.3709,   3.3203,  0.0020389,   2.4462,   3.7109, 0.0016555,  1.3709},
    //{0.48828,  0.058533,  2.597,    0.097656, 0.053163,   2.3925,   3.7109,  0.0015854,   2.597,    3.3203,                0.0015061,  2.3925},
    //{0.39063,  0.048633,  2.7611,   1.3672,   0.0044226,  0.81326,  3.3203,  0.0022925,   2.7611,   4.1016,                0.001311,   0.81326},
    //{0.48828,  0.040806,  0.90393,  0.097656, 0.037172,   2.0213,   3.2227,  0.0076312,   0.90393,  3.6133,                0.0072674,  2.0213},
    //{0.19531,  0.10765,   2.6766,   1.5625,   0.016446,   0.43344,  3.3203,  0.0037318,   2.6766,   4.1016,                0.0034619,  0.43344},
    //{0.78125,  0.0138,   -2.1576,   0.19531,  0.0095147,  2.2117,   3.125,   0.0026177,  -2.1576,   4.4922,                0.0024456,  2.2117},
    //{0.39063,  0.023223,  0.89297,  1.7578,   0.0069854,  1.348,    3.3203,  0.0057945,   0.89297,  3.7109,                0.0055804,  1.348},
    //fi, ampl, phase
    //1 colum - min:, 2 - column max, 3 - column mean
    double[][] limits = {
        //  {0.36 - 0.23    ,0.21 - 0.21   ,0.58,     0.36 - 0.23 + 0.81 ,          0.21 - 0.21-0.14,               1.0672454545 + 1.8996234857,            3.65-0.51,  0.02-0.02,      0.73,               3.65-0.51 +0.83,             0.02-0.02-0.03,                 1.0672454545 + 1.4699906857},
        //  {0.36 - 0.23    ,0.21 + 0.21   ,0.58,     0.36 - 0.23 + 0.81,           0.21 + 0.21-0.14,               1.0672454545 + 1.8996234857,            3.65-0.51,  0.02+0.02,      0.73,               3.65-0.51 +0.83,             0.01+0.01-0.03,                 1.0672454545 + 1.4699906857},
        //  {0.36 + 0.23    ,0.21 - 0.21   ,0.58,     0.36 + 0.23 + 0.81,           0.21 - 0.21-0.14 ,              1.0672454545 + 1.8996234857,            3.65+0.51,  0.02-0.02,      0.73,               3.65+0.51 +0.83,             0.02-0.02-0.03,                 1.0672454545 + 1.4699906857},
        //  {0.36 + 0.23    ,0.21 + 0.21   ,0.58,     0.36 + 0.23 + 0.81,           0.21 + 0.21-0.14,               1.0672454545 + 1.8996234857,            3.65+0.51,  0.02+0.02,      0.73,               3.65+0.51 +0.83,             0.02+0.02-0.03,                 1.0672454545 + 1.4699906857},
        //   {0.36 ,         0.21           ,0.58,     0.36        + 0.81           ,0.08 - 0.14,                    1.0672454545 + 1.8996234857,            3.65 ,      0.02 ,          0.73,               3.65 +    0.83,                  0.02 -0.03,                  1.0672454545 + 1.4699906857},
        //   {0.5,           0.4            ,0.58     ,0.5         + 0.81,           0.28 -  0.14,                   1.0672454545 + 1.8996234857,            3.7,        0.05,           0.73,               3.7+       0.83,                   0.05 -0.03 ,               1.0672454545 + 1.4699906857},

        //check the values again
        {0.36 - 0.23, 0.21 - 0.21, 1.0672454545, 1.16 - 0.61, 0.08 - 0.08, 1.0672454545 - 1.8996234857, 3.65 - 0.51, 0.02 - 0.02, 0.73, 4.26 - 0.55, 0.01 - 0.01, 0.73 - 1.4699906857},
        {0.36 + 0.23, 0.21 + 0.21, 1.0672454545, 1.16 + 0.61, 0.08 + 0.08, 1.0672454545 + 1.8996234857, 3.65 + 0.51, 0.02 + 0.02, 0.73, 4.26 + 0.55, 0.01 + 0.01, 0.73 + 1.4699906857},
        {0.36, 0.21, 1.0672454545, 1.16, 0.08, 1.0672454545 + 1.8996234857, 3.65, 0.02, 0.73, 4.26, 0.01, 0.73 + 1.4699906857},};

    double fft(double f, double a, double p, double t) {
        return (a * java.lang.Math.cos(2 * java.lang.Math.PI * f * t + p));
    }

    @Override
    protected void generateKeyframes(List<Signal> inputSignals, List<Keyframe> outputKeyframes) {

        for (Signal signal : inputSignals) {

            ShoulderSignal shoulder = (ShoulderSignal) signal;

            //if not scheduled it does not contain good time markers
            if (shoulder.isScheduled()) {

                //if it is simple signal
                //for istance lexemes are hardcoded - in future there should be a library of gestures
                if ((shoulder.getReference().equalsIgnoreCase("up"))
                        || (shoulder.getReference().equalsIgnoreCase("back"))
                        || (shoulder.getReference().equalsIgnoreCase("front"))
                        || (shoulder.getReference().equalsIgnoreCase("custom"))) {

                    double up = shoulder.getUp();
                    double front = shoulder.getFront();

                    //System.out.println(up + "  " + front + "  " + shoulder.getIntensity());
                    //do one side
                    if ((shoulder.getSide() == Side.BOTH)
                            || (shoulder.getSide() == Side.LEFT)) {

                        //end of signal is end - relative to the beginning of the message
                        TimeMarker temp = shoulder.getStart();
                        if (temp != null) {

                            ShoulderKeyframe keyframe_1_left = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), temp.getName(), temp.getValue(), "left");

                            //add   animation and intenisty
                            keyframe_1_left.setUp(0);
                            keyframe_1_left.setFront(0);
                            outputKeyframes.add(keyframe_1_left);
                        }

                        //end of signal is end - relative to the beginning of the message
                        TimeMarker temp1 = shoulder.getEnd();
                        if (temp != null) {

                            ShoulderKeyframe keyframe_1_left = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), temp1.getName(), temp1.getValue(), "left");

                            //add   animation and intenisty
                            keyframe_1_left.setUp(0);
                            keyframe_1_left.setFront(0);
                            outputKeyframes.add(keyframe_1_left);
                        }

                        //end of signal is end - relative to the beginning of the message
                        TimeMarker temp2 = shoulder.getTimeMarker("attack");
                        if (temp != null) {

                            ShoulderKeyframe keyframe_1_left = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), temp2.getName(), temp2.getValue(), "left");

                            //add   animation and intenisty
                            keyframe_1_left.setUp(up);
                            keyframe_1_left.setFront(front);
                            outputKeyframes.add(keyframe_1_left);
                        }

                        //end of signal is end - relative to the beginning of the message
                        TimeMarker temp3 = shoulder.getTimeMarker("decay");
                        if (temp != null) {

                            ShoulderKeyframe keyframe_1_left = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), temp3.getName(), temp3.getValue(), "left");

                            //add   animation and intenisty
                            keyframe_1_left.setUp(up * 0.9);
                            keyframe_1_left.setFront(front * 0.9);
                            outputKeyframes.add(keyframe_1_left);
                        }

                        //end of signal is end - relative to the beginning of the message
                        TimeMarker temp4 = shoulder.getTimeMarker("sustain");
                        if (temp != null) {

                            ShoulderKeyframe keyframe_1_left = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), temp4.getName(), temp4.getValue(), "left");

                            //add   animation and intenisty
                            keyframe_1_left.setUp(up * 0.9);
                            keyframe_1_left.setFront(front * 0.9);
                            outputKeyframes.add(keyframe_1_left);
                        }

                    }//end of one side

                    if ((shoulder.getSide() == Side.BOTH)
                            || (shoulder.getSide() == Side.RIGHT)) {

                        //end of signal is end - relative to the beginning of the message
                        TimeMarker temp = shoulder.getStart();
                        if (temp != null) {

                            ShoulderKeyframe keyframe_1_left = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), temp.getName(), temp.getValue(), "right");

                            //add   animation and intenisty
                            keyframe_1_left.setUp(0);
                            keyframe_1_left.setFront(0);
                            outputKeyframes.add(keyframe_1_left);
                        }

                        //end of signal is end - relative to the beginning of the message
                        TimeMarker temp1 = shoulder.getEnd();
                        if (temp != null) {

                            ShoulderKeyframe keyframe_1_left = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), temp1.getName(), temp1.getValue(), "right");

                            //add   animation and intenisty
                            keyframe_1_left.setUp(0);
                            keyframe_1_left.setFront(0);
                            outputKeyframes.add(keyframe_1_left);
                        }

                        //end of signal is end - relative to the beginning of the message
                        TimeMarker temp2 = shoulder.getTimeMarker("attack");
                        if (temp != null) {

                            ShoulderKeyframe keyframe_1_left = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), temp2.getName(), temp2.getValue(), "right");

                            //add   animation and intenisty
                            keyframe_1_left.setUp(up);
                            keyframe_1_left.setFront(front);
                            outputKeyframes.add(keyframe_1_left);
                        }

                        //end of signal is end - relative to the beginning of the message
                        TimeMarker temp3 = shoulder.getTimeMarker("decay");
                        if (temp != null) {

                            ShoulderKeyframe keyframe_1_left = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), temp3.getName(), temp3.getValue(), "right");

                            //add   animation and intenisty
                            keyframe_1_left.setUp(up * 0.9);
                            keyframe_1_left.setFront(front * 0.9);
                            outputKeyframes.add(keyframe_1_left);
                        }

                        //end of signal is end - relative to the beginning of the message
                        TimeMarker temp4 = shoulder.getTimeMarker("sustain");
                        if (temp != null) {

                            ShoulderKeyframe keyframe_1_left = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), temp4.getName(), temp4.getValue(), "right");

                            //add   animation and intenisty
                            keyframe_1_left.setUp(up * 0.9);
                            keyframe_1_left.setFront(front * 0.9);
                            outputKeyframes.add(keyframe_1_left);
                        }

                    }//end of one side
                }//end of simple case

                ///////////////////////////////////////////////////////////////////////////////////
                // MODEL of torso and shake a good one
                if (((shoulder.getReference().equalsIgnoreCase("shake"))) || ((shoulder.getReference().equalsIgnoreCase("shake4")))) {

                    //read control parameters
                    //double speed = shoulder.getRepetition();
                    //double speed = values[6];
                    double[] values = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

                    int mode = shoulder.getMode();
                    int torsoin = shoulder.getTorsoIn();
                    double speed = shoulder.getRepetition();
                    double intensity = shoulder.getIntensity();

                    System.out.println("repetition" + speed);
                    System.out.println("intensity" + intensity);
                    System.out.println("normalized" + mode);
                    System.out.println("torso" + torsoin);

                    //decide the parameters of the movement
                    if (mode == 0) {
                        //not normalized

                        values[0] = limits[2][0];
                        values[1] = limits[2][1];
                        values[2] = limits[2][2];
                        values[3] = limits[2][3];
                        values[4] = limits[2][4];
                        values[5] = limits[2][5];

                        values[6] = speed;
                        values[7] = intensity;
                        values[8] = limits[2][8];
                        values[9] = limits[2][9];
                        values[10] = limits[2][10];
                        values[11] = limits[2][11];

                    }

                    if (mode == 1) {
                        //normalized

                        if ((speed < 0) || (speed > 1)) {
                            System.out.println("Incorrect value of shoulder repetition parameter");
                        }
                        if ((intensity < 0) || (intensity > 1)) {
                            System.out.println("Incorrect value of shoulder amount parameter");
                        }

                        double phase = Math.random() * 1.56;
                        values[0] = 0.5* speed * (limits[1][0] - limits[0][0]) + limits[0][0];
                        values[1] = 0.5* intensity * (limits[1][1] - limits[0][1]) + limits[0][1];
                        values[2] = phase;
                        values[3] = 0.5* speed * (limits[1][3] - limits[0][3] + limits[0][3]);
                        values[4] = 0.5* intensity * (limits[1][4] - limits[0][4]) + limits[0][4];
                        values[5] = values[2] + (limits[1][5] - limits[0][5]);
                        values[6] = speed * (limits[1][6] - limits[0][6]) + limits[0][6];
                        values[7] = intensity * (limits[1][7] - limits[0][7]) + limits[0][7];
                        values[8] = phase;
                        values[9] = speed * (limits[1][9] - limits[0][9]) + limits[0][9];
                        values[10] = intensity * (limits[1][10] - limits[0][10]) + limits[0][10];
                        values[11] = values[8] + (limits[1][11] - limits[0][11]);

                    }

                    for (int iii = 0; iii < 12; iii++) {
                        System.out.println("value" + iii + " " + values[iii]);
                    }

                    //double amplitude = shoulder.getIntensity();
                    //System.out.println("My speed is" + speed + "My ampli is" + amplitude );
                    //if (speed> 6.0d) speed=6.0d;
                    //System.out.println("My speed is" + speed + "My ampli is" + amplitude );
                    double start = shoulder.getStart().getValue();
                    double end = shoulder.getEnd().getValue();

                    double shoulders_real_min_on_offset = 0;
                    double shoulders_real_max_on_offset = 0;

                    double shoulders_estimate_min = 0;
                    double shoulders_estimate_max = 0;

                    double torso_real_min_on_offset = 0;
                    double torso_real_max_on_offset = 0;

                    double torso_estimate_min = 0;
                    double torso_estimate_max = 0;

                    double shoulder_value = -1;

                    double offset1 = 0;

                    //find shoulders offset
                    for (double i = start; i < 2 * end; i = i + 0.04) {
                        //for (double i = 0; i < 600; i = i + 0.1) {

                        double s1 = 0.0;
                        double s2 = 0.0;
                        double s3 = 0.0;
                        double s4 = 0.0;

                        s1 = fft(values[0], values[1], values[2], i);
                        //s1 =  0.01893  * java.lang.Math.sin(2* java.lang.Math.PI  *  0.4688 * i  -7.494);

                        s2 = fft(values[3], values[4], values[5], i);
                        //s2 =   0.01058  * java.lang.Math.sin(2* java.lang.Math.PI * 0.2344 *  i  -4.851);

                        s3 = fft(values[6], values[7], values[8], i);
                        //s3 = 0.01135 *  java.lang.Math.sin(2* java.lang.Math.PI * 0.6445 * i  -10.49 );

                        //how many of the frequencies
                        s4 = fft(values[9], values[10], values[11], i);

                        //double s4 = 0.001226 *  java.lang.Math.sin(2* java.lang.Math.PI * 4.043 * i -75.75 );
                        //SHOULDERS FIRST
                        double shoulders_val = 0 + 0 + s3 + s4;

                        if (shoulders_val < shoulders_real_min_on_offset) {
                            shoulders_real_min_on_offset = shoulders_val;
                            offset1 = i;
                            //System.out.println("lowest " + shoulders_real_min_on_offset);
                        }

                        if (shoulders_val > shoulders_real_max_on_offset) {
                            shoulders_real_max_on_offset = shoulders_val;
                            //System.out.println("lowest " + shoulders_real_max_on_offset);
                        }

                        //TORSO SECOND
                        double torso_val = s1 + s2 + 0 + 0;

                        if (torso_val < torso_real_min_on_offset) {
                            torso_real_min_on_offset = torso_val;
                            offset1 = i;
                            //System.out.println("lowest " + torso_real_min_on_offset);
                        }

                        if (torso_val > torso_real_max_on_offset) {
                            torso_real_max_on_offset = torso_val;
                            //System.out.println("lowest " + torso_real_max_on_offset);
                        }
                    }

                    //System.out.println("shoulders_real_min_on_offset " + shoulders_real_min_on_offset + "shoulders_real_max_on_offset: " + shoulders_real_max_on_offset + "offset1 :" + offset1);
                    //System.out.println("torso_real_min_on_offset " + torso_real_min_on_offset + "torso_real_max_on_offset: " + torso_real_max_on_offset + "offset1 :" + offset1);
                    ////////////////again but on offset
                    shoulders_real_min_on_offset = 0;
                    shoulders_real_max_on_offset = 0;

                    torso_real_min_on_offset = 0;
                    torso_real_max_on_offset = 0;

                    for (double i = offset1; i < (end - start) + offset1; i = i + 0.04) {

                        //for (double i = 0; i < 600; i = i + 0.1) {
                        double s1 = 0.0;
                        double s2 = 0.0;
                        double s3 = 0.0;
                        double s4 = 0.0;
                        //...

                        s1 = fft(values[0], values[1], values[2], i);
                        //s1 =  0.01893  * java.lang.Math.sin(2* java.lang.Math.PI  *  0.4688 * i  -7.494);

                        s2 = fft(values[3], values[4], values[5], i);
                        //s2 =   0.01058  * java.lang.Math.sin(2* java.lang.Math.PI * 0.2344 *  i  -4.851);

                        s3 = fft(values[6], values[7], values[8], i);
                        //s3 = 0.01135 *  java.lang.Math.sin(2* java.lang.Math.PI * 0.6445 * i  -10.49 );

                        //how many of the frequencies
                        s4 = fft(values[9], values[10], values[11], i);

                        //double s4 = 0.001226 *  java.lang.Math.sin(2* java.lang.Math.PI * 4.043 * i -75.75 );
                        //SHOULDERS FIRST
                        double shoulders_val = 0 + 0 + s3 + s4;

                        if (shoulders_val < shoulders_real_min_on_offset) {
                            shoulders_real_min_on_offset = shoulders_val;
                        }

                        if (shoulders_val > shoulders_real_max_on_offset) {
                            shoulders_real_max_on_offset = shoulders_val;
                        }

                        //TORSO SECOND
                        double torso_val = s1 + s2 + 0 + 0;

                        if (torso_val < torso_real_min_on_offset) {
                            torso_real_min_on_offset = torso_val;
                        }

                        if (torso_val > torso_real_max_on_offset) {
                            torso_real_max_on_offset = torso_val;
                        }

                    }

                    //System.out.println("shoulders_real_min_on_offset " + shoulders_real_min_on_offset + "shoulders_real_max_on_offset: " + shoulders_real_max_on_offset + "offset1 :" + offset1);
                    //System.out.println("torso_real_min_on_offset " + torso_real_min_on_offset + "torso_real_max_on_offset: " + torso_real_max_on_offset + "offset1 :" + offset1);
                    //additional torso line
                    if (torsoin == 1) {
                        //addMINTorso(start, 0, outputKeyframes);
                    }

                    //-1 backward
                    //-1 forward
                    int torso_move = 0;

                    double torso_value = 0;

                    double torso_front_prob = 1;
                    double torso_back_prob = 1;

                    for (double i = offset1; i < (end - start) + offset1; i = i + 0.04) {

                        double t1 = fft(values[0], values[1], values[2], i);
                        //t1 =  0.01893  * java.lang.Math.sin(2* java.lang.Math.PI  *  0.4688 * i  -7.494);

                        double t2 = fft(values[3], values[4], values[5], i);
                        //t2 =   0.01058  * java.lang.Math.sin(2* java.lang.Math.PI * 0.2344 *  i  -4.851);

                        double t3 = fft(values[6], values[7], values[8], i);
                        //t3 = 0.01135 *  java.lang.Math.sin(2* java.lang.Math.PI * 0.6445 * i  -10.49 );

                        double t4 = fft(values[9], values[10], values[11], i);
                        //double t4 = 0.001226 *  java.lang.Math.sin(2* java.lang.Math.PI * 4.043 * i -75.75 );

                        double previous_value = torso_value;

                        torso_value = t1 + t2;

                        if (torso_value < 0) {
                            torso_value = torso_value * 0.5;
                        }

                        double max_t = 2 * java.lang.Math.max(torso_real_min_on_offset, torso_real_max_on_offset);

                        double norm_valuet = torso_value / max_t;

                        if (torsoin == 1) {
                            addTorsoLeaning(start + (i - offset1), norm_valuet, outputKeyframes);
                        }

                        //shoulders movements
                        double s1 = fft(values[0], values[1], values[2], i);

                        double s2 = fft(values[3], values[4], values[5], i);

                        double s3 = fft(values[6], values[7], values[8], i);

                        double s4 = fft(values[9], values[10], values[11], i);
                        //double s4 = 0.001226 *  java.lang.Math.sin(2* java.lang.Math.PI * 4.043 * i -75.75 );

                        shoulder_value = 0 + 0 + s3 + s4;

                        //value *= shoulder.getIntensity();
                        //value = value + ((-1) * lowest);
                        //value = (value * 3) / ((-1) * lowest + highest);
                        shoulder_value = shoulder_value / (3d * ((-1) * shoulders_real_min_on_offset + shoulders_real_max_on_offset));

                        shoulder_value *= shoulder.getIntensity();

                        ShoulderKeyframe keyframe_c_left = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), "end", start + (i - offset1), "left");
                        keyframe_c_left.setUp(shoulder_value);
                        keyframe_c_left.setFront(0);
                        outputKeyframes.add(keyframe_c_left);

                        ShoulderKeyframe keyframe_c_right = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), "end", start + (i - offset1), "right");
                        keyframe_c_right.setUp(shoulder_value);
                        keyframe_c_right.setFront(0);
                        outputKeyframes.add(keyframe_c_right);
                    }

                    //ending
                    //if ((Math.abs(torso_value) - 0.001 > 0) && (torso_move != 0)) {
                    if ((Math.abs(torso_value) - 0.001 > 0)) {

                        double i = (end - start) + offset1 + 0.04;

                        double previous_value = torso_value;

                        //System.out.println("previous_value" + previous_value + "torso_value " + torso_value);
                        while (previous_value * torso_value > 0) {

                            double t1 = fft(values[0], values[1], values[2], i);
                            //t1 =  0.01893  * java.lang.Math.sin(2* java.lang.Math.PI  *  0.4688 * i  -7.494);

                            double t2 = fft(values[3], values[4], values[5], i);
                            //t2 =   0.01058  * java.lang.Math.sin(2* java.lang.Math.PI * 0.2344 *  i  -4.851);

                            double t3 = fft(values[6], values[7], values[8], i);
                            //t3 = 0.01135 *  java.lang.Math.sin(2* java.lang.Math.PI * 0.6445 * i  -10.49 );

                            double t4 = fft(values[9], values[10], values[11], i);
                            //double t4 = 0.001226 *  java.lang.Math.sin(2* java.lang.Math.PI * 4.043 * i -75.75 );

                            torso_value = t1 + t2;

                            if (torso_value < 0) {
                                torso_value = torso_value * 0.5;
                            }

                            double max_t = 2 * java.lang.Math.max(torso_real_min_on_offset, torso_real_max_on_offset);

                            double norm_valuet = torso_value / max_t;

                            if (torsoin == 1) {
                                addTorsoLeaning(start + (i - offset1), norm_valuet, outputKeyframes);
                            }

                            /*
                             double norm_valuet = 3 * torso_value / ((-1) * torso_real_min_on_offset + torso_real_max_on_offset);

                             if (torso_move > 0.8) {
                             addForwardTorso(start + (i - offset1), norm_valuet, outputKeyframes);

                             }
                             if (torso_move < -0.8) {
                             addBackwardTorso(start + (i - offset1), (norm_valuet / 2d), outputKeyframes);
                             }
                             */
                            //if we continue with torso, better continue also with shoulders
                            //shoulders movements
                            double s1 = fft(values[0], values[1], values[2], i);
                            double s2 = fft(values[3], values[4], values[5], i);
                            double s3 = fft(values[6], values[7], values[8], i);
                            double s4 = fft(values[9], values[10], values[11], i);

                            shoulder_value = 0 + 0 + s3 + s4;

                            //value *= shoulder.getIntensity();
                            //value = value + ((-1) * lowest);
                            //value = (value * 3) / ((-1) * lowest + highest);
                            shoulder_value = shoulder_value / (3d * ((-1) * shoulders_real_min_on_offset + shoulders_real_max_on_offset));

                            shoulder_value *= shoulder.getIntensity();

                            ShoulderKeyframe keyframe_c_left = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), "end", start + (i - offset1), "left");
                            keyframe_c_left.setUp(shoulder_value);
                            keyframe_c_left.setFront(0);
                            outputKeyframes.add(keyframe_c_left);

                            ShoulderKeyframe keyframe_c_right = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), "end", start + (i - offset1), "right");
                            keyframe_c_right.setUp(shoulder_value);
                            keyframe_c_right.setFront(0);
                            outputKeyframes.add(keyframe_c_right);
                            i = i + 0.04;

                            //System.out.println("Additional turn:" + torso_value);
                        }//end of while

                    } else {

                        if (torsoin == 1) {
                            addMINTorso(end, 0, outputKeyframes);
                        }

                    }

                }//end of shake

                 ///////////////////////////////////////////////////////
                // SHAKE Procedural - removed no anymore shake procedural
                /*
                 if ((shoulder.getReference().equalsIgnoreCase("shake"))) {

                 double up = 0.0;
                 double front = 0.0;

                 double start = shoulder.getStart().getValue();
                 double end = shoulder.getEnd().getValue();

                 int repetition = (int) shoulder.getRepetition();

                 double time = (end - start) / (double) ((repetition + 1) * 2);

                 //System.out.println("duration" + time);

                 if ((shoulder.getSide().equalsIgnoreCase("both"))
                 || (shoulder.getSide().equalsIgnoreCase("left"))) {

                 //first keyframe
                 //ShoulderKeyframe keyframe_s_left = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), "start", start, "left");
                 //keyframe_s_left.setUp(shoulder.getIntensity());
                 //keyframe_s_left.setFront(0);
                 //outputKeyframes.add(keyframe_s_left);

                 for (int i = 1; i < (repetition + 1) * 2; i++) {
                 if (i % 2 == 1) {
                 ShoulderKeyframe keyframe_c_left = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), "start", start + (time * i), "left");
                 keyframe_c_left.setUp(0);
                 keyframe_c_left.setFront(0);
                 outputKeyframes.add(keyframe_c_left);
                 } else {
                 ShoulderKeyframe keyframe_c_left = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), "end", start + (time * i), "left");
                 keyframe_c_left.setUp(shoulder.getIntensity());
                 keyframe_c_left.setFront(0);
                 outputKeyframes.add(keyframe_c_left);
                 }
                 }
                 //last keyframe
                 //ShoulderKeyframe keyframe_e_left = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), "start", end, "left");
                 //keyframe_e_left.setUp(0);
                 //keyframe_e_left.setFront(0);
                 //outputKeyframes.add(keyframe_e_left);
                 }

                 if ((shoulder.getSide().equalsIgnoreCase("both"))
                 || (shoulder.getSide().equalsIgnoreCase("right"))) {

                 //first keyframe
                 //ShoulderKeyframe keyframe_s_right = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), "start", start, "right");
                 //keyframe_s_right.setUp(shoulder.getIntensity());
                 //keyframe_s_right.setFront(0);
                 //outputKeyframes.add(keyframe_s_right);

                 for (int i = 1; i < (repetition + 1) * 2; i++) {
                 if (i % 2 == 1) {
                 ShoulderKeyframe keyframe_c_right = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), "start", start + (time * i), "right");
                 keyframe_c_right.setUp(0);
                 keyframe_c_right.setFront(0);
                 outputKeyframes.add(keyframe_c_right);
                 } else {

                 ShoulderKeyframe keyframe_c_right = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), "end", start + (time * i), "right");
                 keyframe_c_right.setUp(shoulder.getIntensity());
                 keyframe_c_right.setFront(0);
                 outputKeyframes.add(keyframe_c_right);
                 }
                 }

                 //last keyframe
                 //ShoulderKeyframe keyframe_e_right = new ShoulderKeyframe(shoulder.getId(), shoulder.getCategory(), "start", end, "right");
                 //keyframe_e_right.setUp(0);
                 //keyframe_e_right.setFront(0);
                 //outputKeyframes.add(keyframe_e_right);

                 }
                 }//end of complex

                 */
            }//end if scheduled

        }//end for
    }

    //add neutral torso frame
    protected void addMINTorso(double time, double intensity, List<Keyframe> outputKeyframes) {

        SpinePhase strokePhase = new SpinePhase("start", time, time + 0.03);

        strokePhase.collapse.flag = false;
        strokePhase.collapse.value = intensity * 0.0;
        strokePhase.lateralRoll.value = intensity * 0.0;

        strokePhase.sagittalTilt.value = 0.0;
        strokePhase.sagittalTilt.valueMax = 1.0;
        strokePhase.sagittalTilt.flag = true;
        strokePhase.sagittalTilt.direction = SpineDirection.Direction.BACKWARD;

        //strokePhase.verticalTorsion.value = intensity * 0;
        TorsoKeyframe keyframe = new TorsoKeyframe("torso" + time, strokePhase, "start");

        //System.out.println("Torso neutral");
        outputKeyframes.add(keyframe);

    }

    /*
     protected void addBackwardTorso(double time, double intensity, List<Keyframe> outputKeyframes) {

     intensity = intensity * (-1d);

     SpinePhase strokePhase = new SpinePhase("stroke", time, time + 0.03);

     //strokePhase.collapse.value = intensity * 0;
     strokePhase.collapse.flag = true;
     strokePhase.collapse.value = 0.5 * intensity;
     strokePhase.collapse.valueMax = intensity;

     strokePhase.collapse.direction = "Backward";

     strokePhase.lateralRoll.value = intensity * 0.0;
     strokePhase.lateralRoll.value = intensity * 0.0;

     strokePhase.sagittalTilt.value = 0.5 * intensity;
     strokePhase.sagittalTilt.valueMax = intensity;
     strokePhase.sagittalTilt.flag = true;
     strokePhase.sagittalTilt.direction = "Backward";

     strokePhase.verticalTorsion.flag = false;

     TorsoKeyframe keyframe = new TorsoKeyframe("torso" + time, strokePhase, "stoke");

     //System.out.println("Torso backward");
     outputKeyframes.add(keyframe);
     }
     */
    //add torso movements backward forward
    protected void addTorsoLeaning(double time, double intensity, List<Keyframe> outputKeyframes) {

        SpinePhase strokePhase = new SpinePhase("", time, time + 0.03);

        //strokePhase.collapse.value = intensity * 0;
        strokePhase.collapse.flag = true;
        strokePhase.collapse.value = 0.5 * intensity;
        strokePhase.collapse.valueMax = intensity;
        strokePhase.collapse.direction = SpineDirection.Direction.FORWARD;

        strokePhase.lateralRoll.flag = false;
        strokePhase.verticalTorsion.flag = false;

        //strokePhase.lateralRoll.value = intensity * 0;
        strokePhase.sagittalTilt.flag = true;
        strokePhase.sagittalTilt.value = 0.5 * intensity;
        strokePhase.sagittalTilt.valueMax = intensity;
        strokePhase.sagittalTilt.direction = SpineDirection.Direction.FORWARD;

        TorsoKeyframe keyframe = new TorsoKeyframe("torso" + time, strokePhase, "stoke");
        outputKeyframes.add(keyframe);
    }

    @Override
    protected Comparator<Signal> getComparator() {
        return emptyComparator;
    }
}
