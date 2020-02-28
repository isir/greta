/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.player.ogre.overlay;

import greta.auxiliary.player.ogre.Ogre;

/**
 *
 * @author Mathieu
 */
public class OgreOverlayManager {

    private Fader fader=null;
    private String sceneName="";

    public OgreOverlayManager(String sceneName)
    {
        this.sceneName=sceneName;
    }

    public void fadeOut(long duration)
    {
        if(fader==null)
        {
            fader = new Fader(Ogre.getSceneManager(sceneName));
        }
        fader.fade(duration, true);
    }

    public void fadeIn(long duration)
    {
        if(fader==null)
        {
            fader = new Fader(Ogre.getSceneManager(sceneName));
        }
        fader.fade(duration, false);
    }
}
