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
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Brian Ravenet
 */
public class SocketAudioPerformer implements AudioPerformer{

    private String host = "localhost";
    private int port = 5555;

    @Override
    public void performAudios(List<Audio> audios, ID requestId, Mode mode) {
        if(audios.size()==1){
            try {
                Socket s = new Socket(host, port);
                audios.get(0).save(s.getOutputStream(), Audio.GRETA_AUDIO_FORMAT);
                s.close();
            } catch (UnknownHostException ex) {
                Logger.getLogger(SocketAudioPerformer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SocketAudioPerformer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        else{
            for(int i=0; i<audios.size();++i){
                try {
                Socket s = new Socket(host, port);
                audios.get(i).save(s.getOutputStream(), Audio.GRETA_AUDIO_FORMAT);
                s.close();
            } catch (UnknownHostException ex) {
                Logger.getLogger(SocketAudioPerformer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SocketAudioPerformer.class.getName()).log(Level.SEVERE, null, ex);
            }

            }
        }
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

}
