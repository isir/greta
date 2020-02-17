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
