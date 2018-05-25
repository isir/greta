/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre;
import vib.auxiliary.player.ogre.natives.SceneNode;

/**
 *
 * @author Radoslaw Niewiadomski
 */

public class OgreAR extends OgreAwt{

    @Override
    public void initializeOgre() {
        super.initializeOgre();
        SceneNode parent = getCamera().getOgreCamera().getParentSceneNode();

        //add Rectangle with texture

    };
}
