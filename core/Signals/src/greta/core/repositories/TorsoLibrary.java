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

import greta.core.signals.SpineDirection;
import greta.core.signals.SpinePhase;
import greta.core.signals.TorsoSignal;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.IniManager;
import greta.core.util.log.Logs;
import greta.core.util.math.Quaternion;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 * @author Brice Donval
 */
public class TorsoLibrary extends SignalLibrary<TorsoSignal> implements CharacterDependent {

    private static final String TORSO_LIBRARY_PARAM_NAME;
    private static final String TORSO_LIBRARY_XSD;
    public static TorsoLibrary globalLibrary;
    private CharacterManager characterManager;

    /**
     * @return the characterManager
     */
    @Override
    public CharacterManager getCharacterManager() {
        if(characterManager==null)
            characterManager = CharacterManager.getStaticInstance();
        return characterManager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        if(this.characterManager!=null)
            this.characterManager.remove(this);
        this.characterManager = characterManager;
        characterManager.add(this);
    }

    static {
        TORSO_LIBRARY_PARAM_NAME = "TORSOGESTURES";
        TORSO_LIBRARY_XSD = IniManager.getGlobals().getValueString("XSD_TORSOGESTURES");
        CharacterManager cm = CharacterManager.getStaticInstance();
        globalLibrary = new TorsoLibrary(cm);
        cm.add(globalLibrary);
    }

    /**
     * @return The global {@code TorsoLibrary}.
     */
    public static TorsoLibrary getGlobalLibrary() {
        return globalLibrary;
    }

    private TorsoIntervals intervals = new TorsoIntervals();

    public TorsoIntervals getTorsoIntervals() {
        return intervals;
    }

    public TorsoLibrary(CharacterManager cm) {
        super();
        setCharacterManager(cm);
        setDefaultDefinition(getCharacterManager().getDefaultValueString(TORSO_LIBRARY_PARAM_NAME));
        setDefinition(getCharacterManager().getValueString(TORSO_LIBRARY_PARAM_NAME));
        System.out.println("GRETA TORSO LIBRARY OPENED"+TORSO_LIBRARY_PARAM_NAME );
    }

    @Override
    protected List<SignalEntry<TorsoSignal>> load(String definition) {

        List<SignalEntry<TorsoSignal>> torsos = new LinkedList<SignalEntry<TorsoSignal>>();
        XMLParser parser = XML.createParser();
        System.out.println("LOADING TORSO GESTURE: "+TORSO_LIBRARY_XSD+"  "+ definition);
        XMLTree torsotree = parser.parseFileWithXSD(definition, TORSO_LIBRARY_XSD);
        if (torsotree != null) {
            for (XMLTree torso : torsotree.getChildrenElement()) {
                if (torso.isNamed("torso")) {
                    TorsoSignal torsoSignal = new TorsoSignal(torso.getAttribute("id"));
                    torsoSignal.setLexeme(torso.getAttribute("lexeme"));
                    double amount = torso.getAttributeNumber("amount");
                    for (XMLTree phase : torso.getChildrenElement()) {
                        SpinePhase torsoPhase = new SpinePhase(phase.getAttribute("type"), 0, 0);
                        for (XMLTree direction : phase.getChildrenElement()) {
                            SpineDirection torsoDirection = null;
                            if (direction.isNamed("VerticalTorsion")) {
                                torsoDirection = torsoPhase.verticalTorsion;
                            }
                            if (direction.isNamed("SagittalTilt")) {
                                torsoDirection = torsoPhase.sagittalTilt;
                            }
                            if (direction.isNamed("LateralRoll")) {
                                torsoDirection = torsoPhase.lateralRoll;
                            }
                            if (direction.isNamed("Collapse")) {
                                torsoDirection = torsoPhase.collapse;
                            }
                            if (torsoDirection != null) {
                                torsoDirection.direction = SpineDirection.Direction.valueOf(direction.getAttribute("direction").toUpperCase());
                                torsoDirection.flag = true;
                                if (direction.hasAttribute("amount")) {
                                    torsoDirection.value = direction.getAttributeNumber("amount");
                                } else {
                                    torsoDirection.value = amount;
                                }
                                torsoDirection.valueMax = direction.getAttributeNumber("max");
                                torsoDirection.valueMin = direction.getAttributeNumber("min");
                            }
                            if (direction.getName().equalsIgnoreCase("Rotations")) {
                                for (XMLTree rotates : direction.getChildrenElement()) {
                                    String name = rotates.getName();
                                    float x = (float) (rotates.getAttributeNumber("x"));
                                    float y = (float) (rotates.getAttributeNumber("y"));
                                    float z = (float) (rotates.getAttributeNumber("z"));
                                    float w = (float) (rotates.getAttributeNumber("w"));
                                    Quaternion q = new Quaternion(x, y, z, w);
                                    torsoPhase._rotations.put(name, q);
                                }
                            }
                        }
                        torsoSignal.getPhases().add(torsoPhase);
                    }
                    for (int i = 0; i < torsoSignal.getRepetitions() - 1; i++) {
                        int sum = torsoSignal.getPhases().size();
                        for (int j = 0; j < sum; j++) {
                            SpinePhase torsoPhase = new SpinePhase(torsoSignal.getPhases().get(j).getType(), 0, 0);
                            torsoPhase.collapse = torsoSignal.getPhases().get(j).collapse;
                            torsoPhase.lateralRoll = torsoSignal.getPhases().get(j).lateralRoll;
                            torsoPhase.sagittalTilt = torsoSignal.getPhases().get(j).sagittalTilt;
                            torsoPhase.verticalTorsion = torsoSignal.getPhases().get(j).verticalTorsion;
                            torsoSignal.getPhases().add(sum + j, torsoPhase);
                        }
                    }
                    torsos.add(new SignalEntry<TorsoSignal>(torsoSignal.getId(), torsoSignal));
                }
                if (torso.isNamed("torsoDirectionShift")) {
                    TorsoSignal torsoSignal = new TorsoSignal(torso.getAttribute("id"));
                    torsoSignal.setDirectionShift(true);
                    SpinePhase torsoPhase = new SpinePhase("strokeEnd", 0, 0);
                    for (XMLTree direction : torso.getChildrenElement()) {
                        SpineDirection torsoDirection = null;
                        if (direction.isNamed("VerticalTorsion")) {
                            torsoDirection = torsoPhase.verticalTorsion;
                        }
                        if (direction.isNamed("SagittalTilt")) {
                            torsoDirection = torsoPhase.sagittalTilt;
                        }
                        if (direction.isNamed("LateralRoll")) {
                            torsoDirection = torsoPhase.lateralRoll;
                        }
                        if (direction.isNamed("Collapse")) {
                            torsoDirection = torsoPhase.collapse;
                        }
                        if (torsoDirection != null) {
                            torsoDirection.direction = SpineDirection.Direction.valueOf(direction.getAttribute("direction").toUpperCase());
                            torsoDirection.flag = true;
                            torsoDirection.value = direction.getAttributeNumber("amount");
                            torsoDirection.valueMax = direction.getAttributeNumber("max");
                            torsoDirection.valueMin = direction.getAttributeNumber("min");
                        }
                    }
                    torsoSignal.getPhases().add(torsoPhase);
                    torsos.add(new SignalEntry<TorsoSignal>(torsoSignal.getId(), torsoSignal));
                }
            }
        }
        return new ArrayList<SignalEntry<TorsoSignal>>(torsos);
    }

    @Override
    protected void save(String definition, List<SignalEntry<TorsoSignal>> paramToSave) {
        Logs.info("Torso movement cannot be save");
    }

    @Override
    public void onCharacterChanged() {
        setDefinition(getCharacterManager().getValueString(TORSO_LIBRARY_PARAM_NAME));
    }

}
