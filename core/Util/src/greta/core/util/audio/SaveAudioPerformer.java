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
package greta.core.util.audio;

import greta.core.util.Mode;
import greta.core.util.id.ID;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ken Prepin
 */
public class SaveAudioPerformer implements AudioPerformer{

    @Override
    public void performAudios(List<Audio> audios, ID requestId, Mode mode) {
        if(audios.size()==1){
            try {
                audios.get(0).save(requestId+".wav",false);
                System.out.println(requestId+".wav"+ "aaaaaaaaaaaaaaaa");
            } catch (IOException ex) {
                Logger.getLogger(SaveAudioPerformer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        else{
            for(int i=0; i<audios.size();++i){
                try {
                    audios.get(i).save(requestId+"_"+i+".wav",false);
                    System.out.println(requestId+"_"+i+".wav");
                } catch (IOException ex) {
                    Logger.getLogger(SaveAudioPerformer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }


}
