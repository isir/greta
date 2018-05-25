/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.interruptions.reactions;

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
