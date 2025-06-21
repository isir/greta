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
 * This class contains informations about torso Signals.
 *
 * @author Andre-Marie Pez
 * @author Quoc Anh Le
 * @author Brice Donval
 */
public class TorsoSignal extends SpineSignal {

    // variable to know if we have to move just the shoulder or also the rest of the torso
    public boolean shoulder = false;

    public TorsoSignal(String id) {
        super(id);
    }

    @Override
    public String getModality() {
        return "torso";
    }

    public boolean getShoulder() {
        return shoulder;
    }

    public void setModality(String modality) {
    }

}
