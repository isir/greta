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
package greta.core.intentions;

import greta.core.signals.Signal;
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
    public void performIntentions(List<Intention> intentions, ID requestId, Mode mode, List<Signal> inputSignals);//TODO use of mode in intention performers
}
