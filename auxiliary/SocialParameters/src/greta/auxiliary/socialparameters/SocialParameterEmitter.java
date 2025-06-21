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
