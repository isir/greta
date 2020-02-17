/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.player.ogre.capture;

import greta.core.util.log.Logs;
import java.util.Date;

/**
 *
 * @author Andre-Marie Pez
 */
public class FPSCaptureListener implements CaptureListener{

    Capturer currentCapturer = null;
    int countFrame = 0;
    long beginTime = 0;
    int totalFrames = 0;
    long totalDuration = 0;

    @Override
    public void captureStarted(Capturer source, long time) {
        if(currentCapturer==null){
            currentCapturer = source;
            countFrame = 0;
            Logs.info("Capture starting at "+new Date());
            beginTime = System.currentTimeMillis();
        }
    }

    @Override
    public void captureNewFrame(Capturer source, long time) {
        if(currentCapturer == source){
            ++countFrame;
        }
    }

    @Override
    public void captureEnded(Capturer source, long time) {
        if(currentCapturer == source){
            long endTime = System.currentTimeMillis();
            totalFrames+=countFrame;
            totalDuration+=(endTime - beginTime);
            Logs.info("Capture ended at "+new Date()+
                    "\ncapture framerate: "+(endTime == beginTime ?"so big (NaN)":(countFrame*1000.0/(endTime - beginTime)))+
                    "\nAverage capture framerate: "+(totalDuration == 0 ?"so big (NaN)":(totalFrames*1000.0/totalDuration)));
            currentCapturer = null;
        }

    }

}
