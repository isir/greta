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
