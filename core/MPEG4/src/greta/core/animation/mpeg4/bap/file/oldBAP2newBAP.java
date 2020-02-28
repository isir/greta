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
package greta.core.animation.mpeg4.bap.file;

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.util.Constants;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
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
 * @author Van Hanh Nguyen
 */
public class oldBAP2newBAP implements greta.core.animation.mpeg4.bap.BAPFrameEmitter{

    public int[] from169to296 = {//matrix of convertion from 169 BAPs to 296 BAPs
                          0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                          10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                          20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                          30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
                          40, 41, 42, 43, 44, 45, 46, 47, 125, 126, //50
                          127, 128, 129, 130, 131, 132, 133, 134, 137, 138,
                          139, 140, 141, 142, 143, 144, 145, 146, 149, 150,
                          151, 152, 153, 154, 155, 156, 157, 158, 161, 162,
                          163, 164, 165, 166, 167, 168, 169, 170, 171, 172,
                          173, 174, 175, 176, 177, 178, 179, 180, 51, 52, //100
                          53, 54, 55, 56, 57, 58, 59, 60, 61, 62,
                          63, 64, 65, 66, 67, 68, 69, 70, 71, 72,
                          73, 74, 75, 76, 77, 78, 79, 80, 81, 82,
                          83, 84, 85, 86, 87, 88, 89, 90, 91, 92,
                          93, 94, 95, 96, 97, 98, 99, 100, 101, 102, //150
                          103, 104, 105, 106, 107, 108, 109, 110, 111, 112,
                          113, 114, 115, 116, 117, 118, 119, 120, 121, 122
                         };

    public void load(String bapFileName) {


        ArrayList<BAPFrame> bap_animation = new ArrayList<BAPFrame>();

        String base = (new File(bapFileName)).getName().replaceAll("\\.bap$", "_newBAP");

        InputStream bapfile = null;
        String readline;


        int bapframe_startTime = (int)(Timer.getTime()*Constants.FRAME_PER_SECOND);

        try {

            bapfile = new FileInputStream(bapFileName);
            InputStreamReader bapfilesr = new InputStreamReader(bapfile);
            BufferedReader br = new BufferedReader(bapfilesr);

            //skip first line
            br.readLine();

            //TODO: detect frame rate


            while ((readline = br.readLine()) != null) {

                String firstline = readline;
                String secondline = br.readLine();

                // frameid++;

                StringTokenizer st = new StringTokenizer(firstline);
                StringTokenizer st2 = new StringTokenizer(secondline);

                //skip frame
                String empty = st2.nextToken();
                int frameid = new Integer(empty).intValue();

                int bapnr = 1;

                BAPFrame frame = new BAPFrame();

                frame.setFrameNumber(bapframe_startTime + frameid);

                while (st.hasMoreTokens()) {

                    String value2 = st.nextToken();
                    Integer temp = new Integer(value2);

                    if (temp.intValue() == 1)
                    {
                        String value3 = st2.nextToken();
                        temp = new Integer(value3);

                        // change here for new BAPs
                       if (from169to296[bapnr] != -1)
                        frame.applyValue(from169to296[bapnr], temp.intValue());
                    }//end more tokens
                    else
                    {   // change here for new BAPs
                        if (from169to296[bapnr] != -1)
                            frame.applyValue(from169to296[bapnr], 0);
                    }
                     bapnr++;
                }
               bap_animation.add(frame);
           }//end first while

           bapfile.close();

        }
        catch (Exception ignored) {
        }

        //send to all BAPPerformer added
        ID id = IDProvider.createID(base);
        for (int i = 0; i < _bapFramePerformer.size(); ++i) {
            BAPFramePerformer performer = _bapFramePerformer.get(i);
            performer.performBAPFrames(bap_animation, id);
        }

    }

    ArrayList<BAPFramePerformer> _bapFramePerformer = new ArrayList<BAPFramePerformer>();

    @Override
    public void addBAPFramePerformer(BAPFramePerformer performer) {
        if(performer!=null)
            _bapFramePerformer.add(performer);
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer performer) {
        _bapFramePerformer.remove(performer);
    }
}
