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
package greta.core.animation.mpeg4.fap.file;

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.animation.mpeg4.fap.FAPFrameEmitter;
import greta.core.animation.mpeg4.fap.FAPFramePerformer;
import greta.core.util.Constants;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import greta.core.util.time.Timer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class FAPHeadFileReader implements FAPFrameEmitter, BAPFrameEmitter {

    private ArrayList<FAPFramePerformer> performers = new ArrayList<FAPFramePerformer>();
    private ArrayList<BAPFramePerformer> _bapFramePerformer = new ArrayList<BAPFramePerformer>();

    @Override
    public void addBAPFramePerformer(BAPFramePerformer performer) {
        if (performer != null) {
            _bapFramePerformer.add(performer);
        }
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer performer) {
        _bapFramePerformer.remove(performer);
    }

    public void load(String fapFileName) {

        ArrayList<FAPFrame> fap_animation = new ArrayList<FAPFrame>();
        ArrayList<BAPFrame> bapframes = new ArrayList<BAPFrame>();

        String base = (new File(fapFileName)).getName().replaceAll("\\.fap$", "");

        InputStream fapfile = null;
        String readline;

        int fapframe_startTime = (int) (Timer.getTime()*Constants.FRAME_PER_SECOND);

        try {
            fapfile = new FileInputStream(fapFileName);
            InputStreamReader fapfilesr = new InputStreamReader(fapfile);
            BufferedReader br = new BufferedReader(fapfilesr);

            //skip first line
            br.readLine();

            //TODO: detect frame rate
            String firstline = "";
            String secondline = "";
            int frameid = 0;

            while ((readline = br.readLine()) != null) {
                firstline = readline;
                secondline = br.readLine();

                // frameid++;
                StringTokenizer st = new StringTokenizer(firstline);
                StringTokenizer st2 = new StringTokenizer(secondline);

                //skip frame
                String empty = st2.nextToken();
                frameid = new Integer(empty).intValue();

                int fapnr = 1;
                FAPFrame frame = new FAPFrame();
                BAPFrame bapframe = new BAPFrame();

                frame.setFrameNumber(fapframe_startTime + frameid);
                bapframe.setFrameNumber(fapframe_startTime + frameid);

                while (st.hasMoreTokens()) {

                    String value2 = st.nextToken();
                    Integer temp = new Integer(value2);


                    if (temp.intValue() == 1) {

                        String value3 = st2.nextToken();
                        temp = new Integer(value3);

                        if (fapnr == 48 || fapnr == 49 || fapnr == 50) {

                            //to dodadj do bap

                            if (fapnr == 49) {
                                //bapframe.setBAP(98, temp.intValue());
                                bapframe.applyValue(49, temp.intValue());
                            }
                            if (fapnr == 48) {
                                //bapframe.setBAP(99, temp.intValue());
                                bapframe.applyValue(50, temp.intValue());
                            }
                            if (fapnr == 50) {
                                //bapframe.setBAP(100, temp.intValue());
                                bapframe.applyValue(48, temp.intValue());
                            }


                        } else {
                            frame.applyValue(fapnr, temp.intValue());
                            // Sathish bug <- it should be rectified in his code, not here !!!!
                            if (fapnr == 55 || fapnr == 56) {
                                frame.applyValue(fapnr, Math.max(temp.intValue(), -160));
                            }
                            if (fapnr == 9 || fapnr == 8) {
                                frame.applyValue(fapnr, Math.max(temp.intValue(), -260));
                            }
                        }
                    } else {
                        frame.applyValue(fapnr, 0);
                    }
                    fapnr++;
                }//end more tokens
                fap_animation.add(frame);
                bapframes.add(bapframe);
            }//end while
            fapfile.close();
        } catch (Exception ignored) {
            Logs.warning("Error reading file: " + ignored);
        }
        /*
         * *
         * double delta_intensity = 0.2; // simple intensity modualtion on faps
         *
         * int max_fap52 = 0;
         *
         * for (int i = 0; i < fap_animation.size(); i++) {
         *
         * //5 i 52 i 3
         *
         * //znajdz wartosc masymalna i //dodaj albo odejmij intensity fapow
         * odpowiadajacych AU 25 i 26, trzymaj maks level
         *
         * FAPFrame frame1 = fap_animation.get(i);
         *
         * // FLExpression expression25 =
         * AULibrary.global_aulibrary.findExpression("AU" + 25); // List<FAP>
         * au_faps25 = expression25.getFAPs(); // for (FAP fap : au_faps) { //
         * double tempw = fap.getFAPValue() * temp_intensity;
         *
         *
         * //fap 3 int fap_value = 59; int valu =
         * (frame1.getFapList()).get(3).getFAPValue(); //valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); valu = (valu + (int) (fap_value * delta_intensity));
         * frame1.setFAP(3, valu);
         *
         * //fap 4 //25: <fap num="4" value="-100"/> //26: <fap num="4"
         * value="70"/>
         *
         * fap_value = - 100 + 70; valu =
         * (frame1.getFapList()).get(4).getFAPValue(); //valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); valu = (valu - (int) (fap_value * delta_intensity));
         * frame1.setFAP(4, valu);
         *
         * //fap 5 //<fap num="5" value="-330"/> fap_value = - 330; valu =
         * (frame1.getFapList()).get(5).getFAPValue(); //valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); valu = (valu - (int) (fap_value * delta_intensity));
         * frame1.setFAP(5, valu);
         *
         * //fap 8 //<fap num="8" value="-55"/> fap_value = -55; valu =
         * (frame1.getFapList()).get(8).getFAPValue(); //valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); valu = (valu - (int) (fap_value * delta_intensity));
         * frame1.setFAP(8, valu);
         *
         * //fap 9 //<fap num="9" value="-55"/> fap_value = -55; valu =
         * (frame1.getFapList()).get(9).getFAPValue(); //valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); valu = (valu - (int) (fap_value * delta_intensity);
         * frame1.setFAP(9, valu);
         *
         * //fap 10 //<fap num="10" value="-330"/> //26: <fap num="10"
         * value="-20"/> fap_value = -330; valu =
         * (frame1.getFapList()).get(10).getFAPValue(); //valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); valu = (valu - (int) (fap_value * delta_intensity);
         * frame1.setFAP(10, valu);
         *
         * //fap 11 //<fap num="11" value="-330"/> //26: <fap num="11"
         * value="-20"/> fap_value = -330; valu =
         * (frame1.getFapList()).get(11).getFAPValue(); //valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); valu = (valu - (int) (fap_value * delta_intensity);
         * frame1.setFAP(11, valu);
         *
         * //fap 18 //25: <fap num="18" value="59"/> //26: <fap num="18"
         * value="220"/> fap_value = 220; valu =
         * (frame1.getFapList()).get(18).getFAPValue(); //valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); valu = (valu + (int) (fap_value * delta_intensity);
         * frame1.setFAP(18, valu);
         *
         * //fap 41 //25: <fap num="41" value="59"/> fap_value = 59; valu =
         * (frame1.getFapList()).get(41).getFAPValue(); //valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); valu = (valu + (int) (fap_value * delta_intensity);
         * frame1.setFAP(41, valu);
         *
         * //fap 42 //25: <fap num="42" value="59"/> fap_value = 59; valu =
         * (frame1.getFapList()).get(42).getFAPValue(); //valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); valu = (valu + (int) (fap_value * delta_intensity);
         * frame1.setFAP(42, valu);
         *
         * //fap 51 //25: <fap num="51" value="-24"/> //26: <fap num="51"
         * value="120"/>
         *
         * fap_value = 120 - 24; valu =
         * (frame1.getFapList()).get(51).getFAPValue(); //valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); valu = (valu + (int) (fap_value * delta_intensity);
         * frame1.setFAP(51, valu);
         *
         * //fap 52 //25: <fap num="52" value="-330"/> //26: <fap num="52"
         * value="60"/> fap_value = -330 + 60; valu =
         * (frame1.getFapList()).get(52).getFAPValue();
         *
         * if (valu < max_fap52) { max_fap52 = valu; }
         *
         * //valu = java.lang.Math.max((valu - (int) (fap_value *
         * delta_intensity)), fap_value); valu = (valu - (int) (fap_value *
         * delta_intensity);
         *
         * frame1.setFAP(52, valu);
         *
         * //fAP 555 //25: <fap num="55" value="-12"/> //26: <fap num="55"
         * value="-70"/> fap_value = - 70; valu =
         * (frame1.getFapList()).get(55).getFAPValue(); //valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); valu = (valu - (int) (fap_value * delta_intensity);
         * frame1.setFAP(55, valu);
         *
         * //25: <fap num="56" value="-12"/> //26: <fap num="56" value="-70"/>
         * fap_value = - 70; valu = (frame1.getFapList()).get(56).getFAPValue();
         * //valu = java.lang.Math.max((valu - (int) (fap_value *
         * delta_intensity)), fap_value); valu = (valu - (int) (fap_value *
         * delta_intensity); frame1.setFAP(56, valu);
         *
         * //25: <fap num="57" value="-260"/> //26: <fap num="57" value="440"/>
         * fap_value = -260 + 440; valu =
         * (frame1.getFapList()).get(57).getFAPValue(); //valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); valu = (valu + (int) (fap_value * delta_intensity);
         * frame1.setFAP(57, valu);
         *
         * //25: <fap num="58" value="-260"/> //26: <fap num="58" value="440"/>
         * fap_value = -260 + 440; valu =
         * (frame1.getFapList()).get(58).getFAPValue(); //valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); valu = (valu + (int) (fap_value * delta_intensity));
         * frame1.setFAP(58, valu);
         *
         *
         * //26: <fap num="59" value="-160"/> fap_value = -160; valu =
         * (frame1.getFapList()).get(59).getFAPValue(); //valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); valu = ( valu - (int) (fap_value * delta_intensity));
         * frame1.setFAP(59, valu);
         *
         *
         * //26: <fap num="60" value="-160"/> fap_value = -160; valu =
         * (frame1.getFapList()).get(60).getFAPValue(); //valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); valu = (valu - (int) (fap_value * delta_intensity));
         * frame1.setFAP(60, valu);
         *
         * }
         *
         * //dla framow dla ktorych fapy 5 lub 52 sa wiekswe niz o( max
         * wartosci // dodaj wartosc do fapow odpowiadajacych 4, 12 i 7 *
         *
         * for (int i = 0; i < fap_animation.size(); i++) {
         *
         * FAPFrame frame1 = fap_animation.get(i);
         *
         * int valu = (frame1.getFapList()).get(60).getFAPValue();
         *
         * if (valu < 0.5 * max_fap52) {
         *
         * //<fap num="4" value="-310"/> //int fap_value = -310; //valu =
         * (frame1.getFapList()).get(4).getFAPValue(); //valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); //frame1.setFAP(4, valu);
         *
         * //<fap num="8" value="-230"/> //int fap_value = -230; // valu =
         * (frame1.getFapList()).get(8).getFAPValue(); // valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); // frame1.setFAP(8, valu);
         *
         * //<fap num="9" value="-230"/> //fap_value = -230; // valu =
         * (frame1.getFapList()).get(9).getFAPValue(); // valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); // frame1.setFAP(9, valu);
         *
         * //<fap num="16" value="-10"/> int fap_value = -10; valu =
         * (frame1.getFapList()).get(16).getFAPValue(); valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(16, valu);
         *
         * //<fap num="17" value="-180"/> fap_value = -180; valu =
         * (frame1.getFapList()).get(17).getFAPValue(); valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(17, valu);
         *
         * //<fap num="39" value="19"/> fap_value = 19; valu =
         * (frame1.getFapList()).get(39).getFAPValue(); valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(39, valu);
         *
         * //<fap num="40" value="19"/> fap_value = 19; valu =
         * (frame1.getFapList()).get(40).getFAPValue(); valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(40, valu);
         *
         * //<fap num="41" value="124"/> //fap_value = 124; // valu =
         * (frame1.getFapList()).get(41).getFAPValue(); // valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); // frame1.setFAP(41, valu);
         *
         * //<fap num="42" value="124"/> //fap_value = 124; // valu =
         * (frame1.getFapList()).get(42).getFAPValue(); // valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); // frame1.setFAP(42, valu);
         *
         * //<fap num="51" value="340"/> //fap_value = 340; // valu =
         * (frame1.getFapList()).get(51).getFAPValue(); // valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); // frame1.setFAP(51, valu);
         *
         * //<fap num="52" value="300"/> //fap_value = 300; // valu =
         * (frame1.getFapList()).get(41).getFAPValue(); // valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); // frame1.setFAP(41, valu);
         *
         * //<fap num="53" value="100"/> fap_value = 100; valu =
         * (frame1.getFapList()).get(53).getFAPValue(); valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(53, valu);
         *
         * //<fap num="54" value="100"/> fap_value = 100; valu =
         * (frame1.getFapList()).get(54).getFAPValue(); valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(54, valu);
         *
         * //<fap num="55" value="10"/>
         *
         * //<fap num="56" value="10"/>
         *
         * //<fap num="57" value="210"/>
         *
         * //<fap num="58" value="210"/>
         *
         * //<fap num="59" value="470"/>
         *
         * //<fap num="60" value="470"/>
         *
         *
         * //<fap num="21" value="31"/> fap_value = 31; valu =
         * (frame1.getFapList()).get(21).getFAPValue(); valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(21, valu);
         *
         * //<fap num="22" value="31"/> fap_value = 31; valu =
         * (frame1.getFapList()).get(22).getFAPValue(); valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(22, valu);
         *
         * //<fap num="19" value="130"/> fap_value = 130; valu =
         * (frame1.getFapList()).get(19).getFAPValue(); valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(19, valu);
         *
         * //<fap num="20" value="130"/> fap_value = 130; valu =
         * (frame1.getFapList()).get(20).getFAPValue(); valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(20, valu);
         *
         * //<fap num="21" value="650"/> fap_value = 650; valu =
         * (frame1.getFapList()).get(21).getFAPValue(); valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(21, valu);
         *
         * //<fap num="22" value="650"/> fap_value = 650; valu =
         * (frame1.getFapList()).get(22).getFAPValue(); valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(22, valu);
         *
         * //<fap num="31" value="-195"/> fap_value = -195; valu =
         * (frame1.getFapList()).get(31).getFAPValue(); valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(31, valu);
         *
         * //<fap num="32" value="-195"/> fap_value = -195; valu =
         * (frame1.getFapList()).get(32).getFAPValue(); valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(32, valu);
         *
         * //<fap num="33" value="-195"/> fap_value = -195; valu =
         * (frame1.getFapList()).get(33).getFAPValue(); valu =
         * java.lang.Math.max((valu - (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(33, valu);
         *
         * //<fap num="34" value="-195"/> fap_value = -195; valu =
         * (frame1.getFapList()).get(34).getFAPValue(); valu =
         * java.lang.Math.min((valu - (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(34, valu);
         *
         * //<fap num="37" value="150"/> fap_value = 150; valu =
         * (frame1.getFapList()).get(37).getFAPValue(); valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(37, valu);
         *
         * //<fap num="38" value="150"/> fap_value = 100; valu =
         * (frame1.getFapList()).get(38).getFAPValue(); valu =
         * java.lang.Math.min((valu + (int) (fap_value * delta_intensity)),
         * fap_value); frame1.setFAP(38, valu);
         *
         * }
         *
         *
         * }
         *
         */
        //send to all FAPFramePerformer added
        ID id = IDProvider.createID(base);
        for (BAPFramePerformer performer : _bapFramePerformer) {
            performer.performBAPFrames(bapframes, id);
        }

        //send to all FAPFramePerformer added
        for (FAPFramePerformer performer : performers) {
            performer.performFAPFrames(fap_animation, id);
        }
    }

    @Override
    public void addFAPFramePerformer(FAPFramePerformer performer) {
        if (performer != null) {
            performers.add(performer);
        }
    }

    @Override
    public void removeFAPFramePerformer(FAPFramePerformer performer) {
        performers.remove(performer);
    }
}
