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
package greta.core.repositories;

import greta.core.signals.Signal;
import greta.core.util.parameter.ParameterSet;

/**
 *
 * @author Andre-Marie Pez
 */
public abstract class SignalLibrary<S extends Signal> extends ParameterSet<SignalEntry<S>> {

    public SignalLibrary() {
        super();
    }

    public SignalLibrary(String defaultDefinitionName) {
        super(defaultDefinitionName);
    }

    public S getSignal(String paramName){
        SignalEntry<S> entry = get(paramName);
        return entry == null ? null : entry.getSignal();
    }
}
