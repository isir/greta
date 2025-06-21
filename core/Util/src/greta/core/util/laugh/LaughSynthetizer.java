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
package greta.core.util.laugh;

import greta.core.util.audio.Audio;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 * @author Yu Ding
 */
public interface LaughSynthetizer {

    /**
     * Sets a {@code Laugh} to performe.
     * @param laugh the {@code Laugh} to performe
     * @see greta.core.util.laugh.Laugh Laugh
     */
    public void setLaugh(Laugh laugh);

    public void compute(boolean doTemporize, boolean doAudio, boolean doPhonemes);

    public List<LaughPhoneme> getPhonemes();

    public Audio getAudio();
}
