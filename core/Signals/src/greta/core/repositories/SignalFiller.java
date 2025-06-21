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

import greta.core.signals.FaceSignal;
import greta.core.signals.HeadSignal;
import greta.core.signals.ShoulderSignal;
import greta.core.signals.Signal;
import greta.core.signals.SpinePhase;
import greta.core.signals.TorsoSignal;
import greta.core.signals.gesture.GesturePose;
import greta.core.signals.gesture.GestureSignal;
import greta.core.util.log.Logs;

/**
 *
 * @author Andre-Marie Pez
 */
public class SignalFiller {

    private SignalFiller() {
    }

    public static void fillSignal(Signal signal) {
        if (signal instanceof GestureSignal) {
            fill((GestureSignal) signal);
        }
        if (signal instanceof HeadSignal) {
            fill((HeadSignal) signal);
        }
        if (signal instanceof FaceSignal) {
            fill((FaceSignal) signal);
        }
        if (signal instanceof TorsoSignal) {
            fill((TorsoSignal) signal);
        }
        if (signal instanceof ShoulderSignal) {
            ((ShoulderSignal) signal).updateFromLibrary();
        }
    }

    public static void fill(FaceSignal face) {
        if (face.isFilled()) {
            return;
        }
        AUExpression flexpression = FaceLibrary.global_facelibrary.get(face.getReference());
        if (flexpression != null) {
            for (AUItem auitem : flexpression.getActionUnits()) {
                AUItem new_auitem = new AUItem(auitem.getAUnum(), auitem.getIntensity(), auitem.getSide());
                face.add(new_auitem);
            }
            face.setFilled(true);
        } else {
            face.setFilled(false);
            System.out.println("greta.core.repositories.SignalFiller.fill(): There is no entry in the FaceLibrary for " + face.getReference());
        }
    }

    public static void fill(GestureSignal gesture) {
        if (gesture.isFilled()) {
            return;
        }
        GestureSignal g = Gestuary.global_gestuary.getSignal(gesture.getReference());

        if (g != null) {
            gesture.setCategory(g.getCategory());
            for (GesturePose phase : g.getPhases()) {
                gesture.addPhase(new GesturePose(phase));
            }
            if (g.getCategory().equalsIgnoreCase("rest") && !g.getPhases().isEmpty()) {
                gesture.setEndRestPose(new GesturePose(g.getPhases().get(g.getPhases().size() - 1)));
            }
            gesture.setFilled(true);
            return;
        }
        else{
            gesture.setFilled(false);
            System.out.println("greta.core.repositories.SignalFiller.fill(): There is no entry in the Gestuary for " + gesture.getReference());
        }
    }

    public static void fill(HeadSignal head) {
        if (head.isFilled()) {
            return;
        }
        String headID = head.getReference().substring(head.getReference().indexOf("=") + 1).trim();
        greta.core.repositories.SignalEntry<HeadSignal> entry = HeadLibrary.getGlobalLibrary().get(headID);
        if (entry == null) {
            entry = HeadLibrary.getGlobalLibrary().findOneForLexeme(head.getLexeme());
        }
        if (entry != null) {
            HeadSignal refHeadSignal = entry.getSignal();
            head.setLexeme(refHeadSignal.getLexeme());
            head.getPhases().add(new SpinePhase("start", 0, 0)); //TODO set the current shift position
            int rep = head.isDirectionShift() ? 1 : Math.max(1, head.getRepetitions());
            for (int i = 0; i < rep; ++i) {
                for (SpinePhase phase : refHeadSignal.getPhases()) {
                    head.getPhases().add(new SpinePhase(phase));
                }
            }
            if (!head.isDirectionShift()) {
                head.getPhases().add(new SpinePhase("end", 0, 0));
            }
            head.setFilled(true);
        } else {
            head.setFilled(false);
            System.out.println("greta.core.repositories.SignalFiller.fill(): There is no entry in the HeadLibrary for " + headID);
        }
    }

    public static void fill(TorsoSignal torso) {
        // get torso id from one indicated in BML and look up its description in lexicon
        String torsoID = torso.getReference().substring(torso.getReference().indexOf("=") + 1).trim();
        torso.getPhases().clear();
        double start = torso.getStartValue();
        double end = torso.getEndValue();
        TorsoSignal t = TorsoLibrary.getGlobalLibrary().getSignal(torsoID);
        if (t != null) {
            torso.setCategory(torsoID);
            SpinePhase startPhase = new SpinePhase("start", 0, 0);
            torso.getPhases().add(startPhase);
            for (SpinePhase phase : t.getPhases()) {

                // calculate the allocated duration
                double allocatedDuration = end - start;
                // there are three phase. Hence, the allocated duration is divided by three.
                double duration = allocatedDuration * 0.33;
                // set start time and duration for start phase

                // set start time and duration for stroke phase
                SpinePhase strokePhase = new SpinePhase(phase);
                strokePhase.setStartTime(start + duration);
                strokePhase.setEndTime(start + duration + duration);

                strokePhase.collapse.value = torso.getIntensity() * phase.collapse.value;
                strokePhase.lateralRoll.value = torso.getIntensity() * phase.lateralRoll.value;
                strokePhase.sagittalTilt.value = torso.getIntensity() * phase.sagittalTilt.value;
                strokePhase.verticalTorsion.value = torso.getIntensity() * phase.verticalTorsion.value;

                // build the torso
                torso.getPhases().add(strokePhase);

            }
            // add a post stroke hold phase
            SpinePhase holdPhase = new SpinePhase(torso.getPhases().get(torso.getPhases().size() - 1));
            torso.getPhases().add(holdPhase);
            torso.setFilled(true);
        } else {
            torso.setFilled(false);
            System.out.println("greta.core.repositories.SignalFiller.fill(): There is no entry in the TorsoLibrary for " + torsoID);
        }
    }

}
