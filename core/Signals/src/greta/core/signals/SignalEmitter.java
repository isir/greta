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

/**
 * This interface describes an object that can send a list of {@code Signal} to all {@code SignalPerformer} added.
 *
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @composed - - * greta.core.signals.SignalPerformer
 * @navassoc - "emmits" * greta.core.signals.Signal
 */
public interface SignalEmitter {
    /**
     * Adds a {@code SignalPerformer}.<br/>
     * The function {@code performSignals} of all {@code SignalPerformer}
     * added will be called when this emmits a list of {@code Signal}.
     * @param performer the {@code SignalPerformer} to add
     * @see greta.core.signals.SignalPerformer#performSignals(java.util.List, greta.core.util.id.ID, greta.core.util.Mode) performeSignals
     */
    void addSignalPerformer(SignalPerformer performer);

    /**
     * Removes the first occurence of a {@code SignalPerformer}.
     * @param performer the {@code SignalPerformer} to remove
     */
    void removeSignalPerformer(SignalPerformer performer);
}
