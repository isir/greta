/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util;

import vib.core.util.enums.CompositionType;
import vib.core.util.enums.SocialAttitude;
import vib.core.util.enums.interruptions.ReactionType;
import vib.core.util.enums.interruptions.ReactionDuration;

/**
 *
 * @author Brice Donval
 * @author Angelo Cafaro
 */
public final class Mode {

    private CompositionType compositionType;
    private ReactionType reactionType;
    private ReactionDuration reactionDuration;
    private SocialAttitude socialAttitude;

    /* ---------------------------------------------------------------------- */

    public Mode(CompositionType compositionType, ReactionType reactionType, ReactionDuration reactionDuration, SocialAttitude socialAttitude) {
        setCompositionType(compositionType);
        setReactionType(reactionType);
        setReactionDuration(reactionDuration);
        setSocialAttitude(socialAttitude);
    }
    
    public Mode(CompositionType compositionType, ReactionType reactionType, ReactionDuration reactionDuration) {
        this(compositionType, reactionType, reactionDuration, null);
    }

    public Mode(CompositionType compositionType) {
        this(compositionType, null, null, null);
    }

    /* -------------------------------------------------- */

    public Mode(String compositionType, String reactionType, String reactionDuration, String socialAttitude) {
        setCompositionType(compositionType);
        setReactionType(reactionType);
        setReactionDuration(reactionDuration);
        setSocialAttitude(socialAttitude);
    }
    
    public Mode(String compositionType, String reactionType, String reactionDuration) {
        this(compositionType, reactionType, reactionDuration, null);
    }

    public Mode(String compositionType) {
        this(compositionType, null, null);
    }

    /* -------------------------------------------------- */

    public Mode(String compositionType, CompositionType defaultCompositionType, String reactionType, String reactionDuration, String socialAttitude) {
        setCompositionType(compositionType, defaultCompositionType);
        setReactionType(reactionType);
        setReactionDuration(reactionDuration);
        setSocialAttitude(socialAttitude);
    }
    
    public Mode(String compositionType, CompositionType defaultCompositionType, String reactionType, String reactionDuration) {
        this(compositionType, defaultCompositionType, reactionType, reactionDuration, null);
    }

    public Mode(String compositionType, CompositionType defaultCompositionType) {
        this(compositionType, defaultCompositionType, null, null, null);
    }

    /* ---------------------------------------------------------------------- */

    public CompositionType getCompositionType() {
        return compositionType;
    }

    public ReactionType getReactionType() {
        return reactionType;
    }

    public ReactionDuration getReactionDuration() {
        return reactionDuration;
    }
    
    public SocialAttitude getSocialAttitude() {
        return socialAttitude;
    }

    /* ---------------------------------------------------------------------- */

    public void setCompositionType(CompositionType compositionType) {
        this.compositionType = compositionType;
    }

    public void setReactionType(ReactionType reactionType) {
        this.reactionType = (reactionType == null) ? ReactionType.NONE : reactionType;
    }

    public void setReactionDuration(ReactionDuration reactionDuration) {
        this.reactionDuration = (reactionDuration == null) ? ReactionDuration.NONE : reactionDuration;
    }
    
    public void setSocialAttitude(SocialAttitude socialAttitude) {
        this.socialAttitude = (socialAttitude == null) ? SocialAttitude.neutral : socialAttitude;
    }

    /* -------------------------------------------------- */

    public void setCompositionType(String compositionType) {
        this.compositionType = CompositionType.interpret(compositionType);
    }

    public void setCompositionType(String compositionType, CompositionType defaultCompositionType) {
        this.compositionType = CompositionType.interpret(compositionType, defaultCompositionType);
    }

    public void setReactionType(String reactionType) {
        this.reactionType = ReactionType.interpret(reactionType);
    }

    public void setReactionDuration(String reactionDuration) {
        this.reactionDuration = ReactionDuration.interpret(reactionDuration);
    }
    
    public void setSocialAttitude(String socialAttitude) {
        this.socialAttitude = SocialAttitude.interpret(socialAttitude);
    }
}
