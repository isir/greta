/*
 * This file is part of Greta.
 * 
 * Greta is free software: you can redistribute it and / or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Greta.If not, see <http://www.gnu.org/licenses/>.
 */

package vib.core.util.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import vib.core.util.id.ID;

/**
 *
 * @author Andre-Marie Pez
 */
public class AnimationFrameEmitterImpl implements AnimationFrameEmitter{

    private ArrayList<AnimationFramePerformer> performers = new ArrayList<AnimationFramePerformer>();

    @Override
    public void addAnimationFramePerformer(AnimationFramePerformer performer) {
        if (performer != null) {
            performers.add(performer);
        }
    }

    @Override
    public void removeAnimationFramePerformer(AnimationFramePerformer performer) {
        if (performer != null) {
            performers.remove(performer);
        }
    }

    public void sendAnimationFrames(ID requestId, AnimationFrame... frames){
        sendAnimationFrames(requestId, Arrays.asList(frames));
    }


    public void sendAnimationFrames(ID requestId, List<AnimationFrame> frames){
        for(AnimationFramePerformer performer : performers){
            performer.performAnimationFrames(frames, requestId);
        }
    }

    public void sendAnimationFrame(ID requestId, AnimationFrame frame){
        sendAnimationFrames(requestId, frame);
    }

}
