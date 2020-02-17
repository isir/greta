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
package greta.core.intentplanner.listener;

import greta.core.signals.ParametricSignal;
import greta.core.signals.Signal;
import greta.core.signals.SpeechSignal;

/**
 * This class descibes a visual or acoustic user's signal
 *
 * @author Elisabetta Bevacqua
 */
public class ProtoSignal {

    private String modality;
    private String reference;

    public ProtoSignal(String modality, String reference) {
        this.modality = modality;
        this.reference = reference;
    }

    public String getModality() {
        return this.modality;
    }

    public void setModality(String newModality){
        modality = newModality;
    }

    public String getReference() {
        return this.reference;
    }

    public void setReference(String newReference) {
        reference = newReference;
    }

    public boolean equals(ProtoSignal ps) {
        return ps!=null && (this==ps || modality.equalsIgnoreCase(ps.modality) && reference.equalsIgnoreCase(ps.reference));
    }

    public boolean equals(Signal s) {
        return s!=null && modality.equalsIgnoreCase(s.getModality()) &&
                ((s instanceof SpeechSignal && reference.equalsIgnoreCase(((SpeechSignal)s).getReference())) ||
                (s instanceof ParametricSignal && reference.equalsIgnoreCase(((ParametricSignal)s).getReference())));
    }

}
