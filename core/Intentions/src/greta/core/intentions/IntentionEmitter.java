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

/**
 * This interface describes an object that can send a list of {@code Intention} to all {@code IntentionPerformer} added.
 *
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @composed - - * greta.core.intentions.IntentionPerformer
 * @navassoc - "emmits" * greta.core.intentions.Intention
 */
public interface IntentionEmitter {
    /**
     * Adds an {@code IntentionPerformer}.<br>
     * The function {@code performIntentions} of all {@code IntentionPerformer}
     * added will be called when this emmits a list of {@code Intention}.
     * @param performer the {@code IntentionPerformer} to add
     * @see greta.core.intentions.IntentionPerformer#performIntentions(java.util.List, greta.core.util.id.ID, greta.core.util.Mode) performIntentions
     */
    void addIntentionPerformer(IntentionPerformer performer);

    /**
     * Removes the first occurrence of an {@code IntentionPerformer}.
     * @param performer the {@code IntentionPerformer} to remove
     */
    void removeIntentionPerformer(IntentionPerformer performer);
}
