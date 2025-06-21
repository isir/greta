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

import greta.core.util.CharacterManager;
import greta.core.util.speech.Speech;

/**
 *
 * @author Andre-Marie Pez
 */
public class SpeechSignal extends Speech implements Signal{


    public SpeechSignal(CharacterManager cm){
        super(cm);
    }

    public SpeechSignal(CharacterManager cm,Speech s){
        super(s);
    }

    public String getModality() {
        return "speech";
    }
}
