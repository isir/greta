/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.intentions;

import vib.core.util.Mode;
import vib.core.util.id.ID;
import java.util.List;

/**
 * This interface descibes an object that can receive and use a list of
 * {@code Intention}.
 *
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because they are
 * UmlGraph tags, not javadoc tags.
 * @inavassoc - "used by" * vib.core.intentions.Intention
 */
public interface IntentionPerformer {

    /**
     * This fuction receives a list of {@code Intention}.<br> This function is
     * typically call by {@code IntentionEmitters}.
     *
     * @param intentions the list of {@code Intention}
     * @param requestId the identifier of the request
     * @param mode how the list is added to previous list : blend, replace,
     * append
     */
    public void performIntentions(List<Intention> intentions, ID requestId, Mode mode);//TODO use of mode in intention performers
}
