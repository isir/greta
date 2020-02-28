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
