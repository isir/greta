/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.intentplanner.listener;

import vib.core.signals.ParametricSignal;
import vib.core.signals.Signal;
import vib.core.signals.SpeechSignal;

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
