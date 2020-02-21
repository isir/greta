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
package greta.core.animation.mpeg4.bap.filters;

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitterImpl;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class HeadNoise extends BAPFrameEmitterImpl implements BAPFramePerformer{

    private int x;
    private int y;
    private int z;
    Random randomGenerator = new Random();

    @Override
    public void performBAPFrames(List<BAPFrame> bap_animation, ID requestId) {

        ArrayList<BAPFrame> newlist = new ArrayList<BAPFrame>();

        for (BAPFrame frame : bap_animation) {
            BAPFrame bapframetemp = new BAPFrame(frame);

            x += (randomGenerator.nextInt(50) - 25);
            y += (randomGenerator.nextInt(50) - 25);
            z += (randomGenerator.nextInt(50) - 25);

            bapframetemp.applyValue(98, (bapframetemp.getValue(98) + x));
            bapframetemp.applyValue(99, (bapframetemp.getValue(99) + y));
            bapframetemp.applyValue(100, (bapframetemp.getValue(100) + z));

            newlist.add(bapframetemp);
        }

        int frameind = randomGenerator.nextInt(40);
        while (frameind < newlist.size() - 21) {
            noise(newlist, frameind);
            frameind += 11 + (randomGenerator.nextInt(50)); //  4.8 sec ~= 120 frames
        }

        sendBAPFrames(requestId, newlist);
    }

    void noise(ArrayList<BAPFrame> newlist, int frameind) {

        double x = randomGenerator.nextInt(600) - 300;
        double y = randomGenerator.nextInt(600) - 300;
        double z = randomGenerator.nextInt(600) - 300;

        int duration = randomGenerator.nextInt(10) + 10;

        for (int i = 1; i < duration; i++) {
            newlist.get(frameind + i).applyValue(BAPType.vc4_roll, (newlist.get(frameind).getValue(98) + (int) (x * (i / duration))));
            newlist.get(frameind + i).applyValue(BAPType.vc4_tilt, (newlist.get(frameind).getValue(99) + (int) (y * (i / duration))));
            newlist.get(frameind + i).applyValue(BAPType.vc4_torsion, (newlist.get(frameind).getValue(100) + (int) (z * (i / duration))));
        }

        int ff = 0;
        for (int i = duration; i < 2; i--) { //<- it's suspicious. if duration>=2 there is no loop and if duration<2 then the loop is infinite !!! in fact duration is in [10, 20] (see declaration of duration) so this loop is useless
            newlist.get(frameind + ff + duration).applyValue(BAPType.vc4_roll, (newlist.get(frameind).getValue(98) + (int) (x * (i / duration))));
            newlist.get(frameind + ff + duration).applyValue(BAPType.vc4_tilt, (newlist.get(frameind).getValue(99) + (int) (y * (i / duration))));
            newlist.get(frameind + ff + duration).applyValue(BAPType.vc4_torsion, (newlist.get(frameind).getValue(100) + (int) (z * (i / duration))));
            ff++;
        }
    }
}
