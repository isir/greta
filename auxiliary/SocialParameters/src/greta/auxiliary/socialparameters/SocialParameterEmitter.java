/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.socialparameters;

import greta.core.util.id.ID;
import java.util.List;

/**
 * This interface describes an object that can send a list of {@code SocialParameterPerformer} to all
 * {@code SocialParameterPerformerPerformer} added.
 *
 * @author Florian Pecune
 */
public interface SocialParameterEmitter {
    /**
     * Adds a {@code SocialParameterPerformer}.<br/>
     * The function {@code performSignals} of all {@code SignalPerformer}
     * added will be called when this emmits a list of {@code Signal}.
     * @param performer the {@code SocialParameterPerformer} to add
     * @see greta.auxiliary.socialparameters.SocialParameterPerformer#performSocialParameter(List, ID) performeSocialParameter
     */
    void addSocialParameterPerformer(SocialParameterPerformer performer);

    /**
     * Removes a {@code SocialParameterPerformer} from the list.
     * @param performer the {@code SocialParameterPerformer} to be removed
     */
    void removeSocialParameterPerformer(SocialParameterPerformer performer);
}
