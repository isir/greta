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
