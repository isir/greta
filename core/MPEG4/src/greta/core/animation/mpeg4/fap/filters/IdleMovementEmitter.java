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
package greta.core.animation.mpeg4.fap.filters;

import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.animation.mpeg4.fap.FAPFrameEmitterImpl;
import greta.core.animation.mpeg4.fap.FAPType;
import greta.core.util.Constants;
import greta.core.util.id.IDProvider;
import greta.core.util.math.Functions;
import greta.core.util.time.Timer;
import java.util.ArrayList;

/**
 *
 * @author Andre-Marie Pez
 */
public class IdleMovementEmitter extends FAPFrameEmitterImpl {

    boolean stop = false;

    public IdleMovementEmitter() {
        startBlinking();
    }

    private void startBlinking() {
        Thread blinker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stop) {
                    Timer.sleep((long)Functions.changeInterval(Math.random(), 0, 1, 2500, 5000));
                    blink();
                }
            }
        });
        blinker.setDaemon(true);
        blinker.start();
    }

    private void blink() {
        int frameind = (int) (Timer.getTime()*Constants.FRAME_PER_SECOND) + 2;
        ArrayList<FAPFrame> blinkFrame = new ArrayList<FAPFrame>();

        for (int i = 0; i < 8; ++i) {
            FAPFrame temp = new FAPFrame(frameind + i);
            blinkFrame.add(temp);
        }
        apply(blinkFrame.get(0), 0);
        apply(blinkFrame.get(1), 341);
        apply(blinkFrame.get(2), 683);
        apply(blinkFrame.get(3), 1024);
        apply(blinkFrame.get(4), 1024);
        apply(blinkFrame.get(5), 683);
        apply(blinkFrame.get(6), 341);
        apply(blinkFrame.get(7), 0);

        sendFAPFrames(IDProvider.createID("blink"), blinkFrame);
    }

    private void apply(FAPFrame frame, int value){
        frame.applyValue(FAPType.close_t_l_eyelid, value);
        frame.applyValue(FAPType.close_t_r_eyelid, value);
    }

    @Override
    protected void finalize() throws Throwable {
        stop = false;
        super.finalize();
    }
}
