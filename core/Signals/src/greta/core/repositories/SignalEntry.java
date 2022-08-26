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
