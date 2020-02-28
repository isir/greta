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
import greta.auxiliary.player.ogre.OgreThread;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import vib.auxiliary.player.ogre.natives.SceneManager;

/**
 * Class for performing Fades (fade-in and fade-out effects)
 * TODO : check when a new fade arrives when one is not finished...
 * @author Mathieu
 */
public class Fader {
    private boolean type=false;//true = fadeout (transparent to black), false ) fadein
    private long startTime; //last fade start time
    private long duration; //last fade duration
//    private ITextureUnitState tus; //duration
    private SceneManager sceneManager;
    private float fadeAmount=0f;

    public Fader(SceneManager sceneManager)
    {
        super();
        this.sceneManager =sceneManager;
        this.fadeAmount=0f;
        this.startTime=System.currentTimeMillis()+1;
        this.duration=1;
        this.type=false;
        createFaderOverlay();

        Runnable r = new Runnable(){
            @Override
            public void run()
            {
                final float prevAmount=fadeAmount;
                fadeAmount = (float)Math.min(((float)(System.currentTimeMillis()-startTime))/((float)duration),1.0);
                fadeAmount = type ? fadeAmount : 1-fadeAmount;
                Ogre.callSync(Ogre.waitAndRefresh);
                Ogre.callSync(new OgreThread.Callback() {

                @Override
                public void run() {
                    //######
                    //For some weird reason, the thread won't run if we don't do something before... so we print this
                    if(prevAmount!=fadeAmount)
                    {
                        String s = "Fading ";
                        s+= type ? "Out (" : "In (";
                        s+=(prevAmount*100)+"%)";
                        System.out.println(s);
                    }
                    //######
                    updateFader(fadeAmount);
                }
            });
        }};

        ScheduledExecutorService execserv = Executors.newSingleThreadScheduledExecutor();
        Future<?> f =execserv.scheduleAtFixedRate(r, 0, 50, TimeUnit.MILLISECONDS);
    }

    public void fade(long dur, boolean fadeOut)
    {
        this.type=fadeOut;
        this.startTime=System.currentTimeMillis();
        this.duration=dur;
    }


    private void createFaderOverlay()//boolean fadeIn)
    {
        /*  this part must be redo : we don't use ogre4j anymore
        Rectangle2D rect = new Rectangle2D(true);
        Ogre.dontDelete(rect);
        rect.setCorners(-1.0f, 1.0f, 1.0f, -1.0f);
        rect.setMaterial("OverlayMaterial");
        MaterialPtr mptr = new MaterialPtr(rect.getMaterial().getInstancePointer());
        Ogre.dontDelete(mptr);
        tus = mptr.get().getTechnique(0).getPass(0).getTextureUnitState(0);
        Ogre.dontDelete(tus);
        mptr.get().getTechnique(0).getPass(0).setSceneBlending(SceneBlendType.SBT_TRANSPARENT_ALPHA);

        // Use infinite AAB to always stay visible
        AxisAlignedBox aab = new AxisAlignedBox();
        Ogre.dontDelete(aab);
        aab.setInfinite();
        rect.setBoundingBox(aab);
        rect.setRenderQueueGroup((short)RenderQueueGroupID.RENDER_QUEUE_OVERLAY.getValue());

        // Attach background to the scene
        SceneNode node = sceneManager.getRootSceneNode().createChildSceneNode(IDProvider.createID("TestOverlay").toString());
        node.attachObject(rect);
        */
    }

    public void updateFader(float amount)
    {
        /*  this part must be redo : we don't use ogre4j anymore
        tus.setAlphaOperation(LayerBlendOperationEx.LBX_SOURCE1, LayerBlendSource.LBS_MANUAL, LayerBlendSource.LBS_CURRENT, amount,0,0);
        */
    }
}
