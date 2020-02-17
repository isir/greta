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
package greta.core.interruptions.reactions;

public class InterruptionReactionImpl implements InterruptionReaction {

    private BehaviorType behaviorType;
    private InterruptionReactionParameters parameters;

    public InterruptionReactionImpl() {
        this(BehaviorType.NONE, new InterruptionReactionParameters());
    }

    public InterruptionReactionImpl(BehaviorType behaviorType, InterruptionReactionParameters params) {
        this.behaviorType = behaviorType;
        this.parameters = params;
    }

    public InterruptionReactionImpl(BehaviorType behaviorType) {
        this(behaviorType, new InterruptionReactionParameters());
    }

    @Override
    public BehaviorType getBehaviorType() {
        return this.behaviorType;
    }

    @Override
    public void setBehaviorType(BehaviorType behaviorType) {
        this.behaviorType = behaviorType;
    }

    @Override
    public InterruptionReactionParameters getParameters() {
        return this.parameters;
    }

    @Override
    public void setParameters(InterruptionReactionParameters params) {
        this.parameters = params;
    }

}
