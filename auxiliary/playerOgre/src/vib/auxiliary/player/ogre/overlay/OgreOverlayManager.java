/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.overlay;

import vib.auxiliary.player.ogre.Ogre;

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
