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
package greta.core.util;

import greta.core.util.enums.CompositionType;
import greta.core.util.enums.SocialAttitude;
import greta.core.util.enums.interruptions.ReactionDuration;
import greta.core.util.enums.interruptions.ReactionType;

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

    // create a variable to store the bml id
    private String bml_id;

    /* ---------------------------------------------------------------------- */

    public Mode(CompositionType compositionType, ReactionType reactionType, ReactionDuration reactionDuration, SocialAttitude socialAttitude, String id) {
        setCompositionType(compositionType);
        setReactionType(reactionType);
        setReactionDuration(reactionDuration);
        setSocialAttitude(socialAttitude);
        setBml_id(id);
    }

    public Mode(CompositionType compositionType, ReactionType reactionType, ReactionDuration reactionDuration) {
        this(compositionType, reactionType, reactionDuration, null, "");
    }

    public Mode(CompositionType compositionType) {
        this(compositionType, null, null, null, "");
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

    /**
     * @return the bml_id
     */
    public String getBml_id() {
        return bml_id;
    }

    /**
     * @param bml_id the bml_id to set
     */
    public void setBml_id(String bml_id) {
        this.bml_id = bml_id;
    }
}
