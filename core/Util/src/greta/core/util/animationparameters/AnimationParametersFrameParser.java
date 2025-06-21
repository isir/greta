/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.core.util.animationparameters;

import greta.core.util.Constants;
import greta.core.util.log.Logs;
import greta.core.util.time.Timer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Andre-Marie Pez
 */
public abstract class AnimationParametersFrameParser<APF extends AnimationParametersFrame>{

    protected abstract APF instanciateFrame();

    public List<APF> readFromFile(String fileName, boolean zeroIsNow) {
        ArrayList<APF> frames = new ArrayList<APF>();
        if (new File(fileName).exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                String line = br.readLine(); //TODO: detect frame rate or bap version from this first line
                int startTime = (int) (Timer.getTime()*Constants.FRAME_PER_SECOND);
                while ((line = br.readLine()) != null) {
                    String firstline = line;
                    String secondline = br.readLine();

                    APF frame = instanciateFrame();
                    frame.readFromString(firstline, secondline);

                    if(zeroIsNow) {
                        frame.setFrameNumber(frame.getFrameNumber() + startTime);
                    }

                    frames.add(frame);
                }
                br.close();
            } catch (Exception ignored) {
                Logs.warning("Error reading file: " + ignored);
            }
        }
        return frames;
    }

    public List<APF> readFromString(String framesString, boolean hasHeader, boolean zeroIsNow) {
        ArrayList<APF> frames = new ArrayList<APF>();
        StringTokenizer lines = new StringTokenizer(framesString, "\n\r");
        if(hasHeader){
            String header = lines.nextToken(); //TODO: detect frame rate or bap version from this first line
        }

        int startTime = (int) (Timer.getTime()*Constants.FRAME_PER_SECOND);

        while (lines.hasMoreTokens()) {
            String firstline = lines.nextToken();
            String secondline = lines.nextToken();

            APF frame = instanciateFrame();
            frame.readFromString(firstline, secondline);
            if(zeroIsNow) {
                frame.setFrameNumber(frame.getFrameNumber() + startTime);
            }

            frames.add(frame);
        }
        return frames;
    }

}
