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
package greta.core.signals;

import greta.core.util.Mode;
import greta.core.util.id.ID;
import java.util.List;

/**
 * This interface describes an object that can receive and use a list of
 * {@code Signal}.
 *
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because they are
 * UmlGraph tags, not javadoc tags.
 * @inavassoc - "used by" * greta.core.signals.Signal
 */
public interface SignalPerformer {
    /**
     * This function receives a list of {@code Signal}.<br/> This function is
     * typically call by {@code SignalEmitters}.
     *
     * @param signals the list of {@code Signal}
     * @param requestId the identifier of the request
     * @param mode how the list is added to previous list : blend, replace,
     * append
     */
    void performSignals(List<Signal> signals, ID requestId, Mode mode);
}
