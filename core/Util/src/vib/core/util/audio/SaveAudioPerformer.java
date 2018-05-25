/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.audio;

import vib.core.util.Mode;
import vib.core.util.id.ID;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public class SaveAudioPerformer implements AudioPerformer{

    @Override
    public void performAudios(List<Audio> audios, ID requestId, Mode mode) {
        if(audios.size()==1){
            audios.get(0).save(requestId+".wav");
        }
        else{
            for(int i=0; i<audios.size();++i){
                audios.get(i).save(requestId+"_"+i+".wav");
            }
        }
    }


}
