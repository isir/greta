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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 *
 * @author Van Hanh Nguyen
 *  * I dont know what it serve for ?
 * ça serve à rien ce class
 */
public class newBAP2oldBAP {


    public int[] from296to169 = { //matrix of convertion from 296 BAPs to 169 BAPs
                                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                                10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                                20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                                30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
                                40, 41, 42, 43, 44, 45, 46, 47, -1, -1,   // 50
                                -1, 98, 99, 100, 101, 102, 103, 104, 105, 106,
                                107, 108, 109, 110, 111, 112, 113, 114, 115, 116,
                                117, 118, 119, 120, 121, 122, 123, 124, 125, 126,
                                127, 128, 129, 130, 131, 132, 133, 134, 135, 136,
                                137, 138, 139, 140, 141, 142, 143, 144, 145, 146, //100
                                147, 148, 149, 150, 151, 152, 153, 154, 155, 156,
                                157, 158, 159, 160, 161, 162, 163, 164, 165, 166,
                                167, 168, 169, -1, -1, 48, 49, 50, 51, 52,
                                53, 54, 55, 56, 57, -1, -1, 58, 59, 60,
                                61, 62, 63, 64, 65, 66, 67, -1, -1, 68, //150
                                69, 70, 71, 72, 73, 74, 75, 76, 77, -1,
                                -1, 78, 79, 80, 81, 82, 83, 84, 85, 86,
                                87, 88, 89, 90, 91, 92, 93, 94, 95, 96,
                                97, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //200
                                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //250
                                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                -1, -1, -1, -1, -1, -1, -1
                              };

    public String oldFrame2String(BAPFrame bapFrame, int frameNumber)
    {
        String buffer = frameNumber + " ";
        String mask = "";
        for (int i = 1; i < 170; i++)
        {
            if (bapFrame.getMask(i))
            {
                mask += "1 ";
                buffer += bapFrame.getValue(i) + " ";
            }
            else
            {
                mask += "0 ";
            }
        }

        return (mask + "\n" + buffer + "\n");
    }

    public void load(String bapFileName) {


        String base = (new File(bapFileName)).getName().replaceAll("\\.bap$", "");

        InputStream bapfile = null;
        String readline;

        try {

            String nameFile = (bapFileName +  "_old.bap");
            java.io.FileWriter fos = new java.io.FileWriter(nameFile);
            String first_line =  "2.1 "+ nameFile + " 25  170 " +"\n";
            fos.write(first_line);

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

                frame.setFrameNumber(frameid);

                while (st.hasMoreTokens())
                {

                    String value2 = st.nextToken();
                    Integer temp = new Integer(value2);

                    if (temp.intValue() == 1)
                    {
                        String value3 = st2.nextToken();
                        temp = new Integer(value3);

                        // change here for old BAPs
                        if (from296to169[bapnr] >= 0)
                            frame.applyValue(from296to169[bapnr], temp.intValue());
                    }//end more tokens
                    else
                    {   // change here for old BAPs
                        if (from296to169[bapnr] >= 0)
                            frame.applyValue(from296to169[bapnr], 0);
                    }
                     bapnr++;

                }
                fos.write(oldFrame2String(frame, frame.getFrameNumber()));

           }//end first while
           fos.close();
           bapfile.close();

        }
        catch (Exception ignored) {
        }


    }

}
