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
