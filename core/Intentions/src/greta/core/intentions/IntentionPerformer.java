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
package greta.core.intentions;

import greta.core.util.Mode;
import greta.core.util.id.ID;
import java.util.List;

/**
 * This interface descibes an object that can receive and use a list of
 * {@code Intention}.
 *
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because they are
 * UmlGraph tags, not javadoc tags.
 * @inavassoc - "used by" * greta.core.intentions.Intention
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
