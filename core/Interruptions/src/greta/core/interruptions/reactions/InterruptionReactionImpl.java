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
