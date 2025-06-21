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
package greta.auxiliary.player.ogre;

import greta.core.util.audio.AudioOutput;
import vib.auxiliary.player.ogre.natives.SceneNode;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class OgreAR extends OgreAwt{

    public OgreAR(AudioOutput audioOutput) {
        super( audioOutput);
    }

    @Override
    public void initializeOgre() {
        super.initializeOgre();
        SceneNode parent = getCamera().getOgreCamera().getParentSceneNode();

        //add Rectangle with texture

    };
}
