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
import greta.core.util.parameter.Parameter;

/**
 *
 * @author Andre-Marie Pez
 */
public class SignalEntry <S extends Signal> implements Parameter<SignalEntry<S>>{

    private String name;
    private S signal;

    public SignalEntry(String name, S signal){
        this.name = name;
        this.signal = signal;
    }
    @Override
    public String getParamName() {
        return name;
    }

    @Override
    public void setParamName(String string) {
        name = string;
    }

    public S getSignal(){
        return signal;
    }

    public void setSignal(S signal){
        this.signal = signal;
    }

    @Override
    public boolean equals(SignalEntry<S> other) {
        return name.equalsIgnoreCase(other.name) && signal==other.signal;
    }
}
