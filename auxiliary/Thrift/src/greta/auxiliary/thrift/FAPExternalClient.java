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
package greta.auxiliary.thrift;

import greta.auxiliary.thrift.gen_java.Message;
import greta.auxiliary.thrift.services.ExternalClient;
import greta.auxiliary.thrift.services.Sender;
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.animation.mpeg4.fap.FAPFrameEmitter;
import greta.core.animation.mpeg4.fap.FAPFramePerformer;
import greta.core.util.animationparameters.AnimationParametersFrame;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ken Prepin
 */
public class FAPExternalClient extends APExternalClient implements FAPFrameEmitter{

   private ArrayList<FAPFramePerformer> fapFramesPerfList;
   private FAPExternalClientThread fapExtClientThread;

    public FAPExternalClient(){
        super();
        fapFramesPerfList =  new ArrayList<FAPFramePerformer>();

    }
    public FAPExternalClient(String host,int port){
        super(host,port);
        fapFramesPerfList =  new ArrayList<FAPFramePerformer>();

   }
    @Override
    protected AnimationParametersFrame newAnimParamFrame(int frameNumber) {
        return new FAPFrame(frameNumber);
    }

    @Override
    public void perform(Message m) {
        if(m.APFrameList!=null){
            ID id = IDProvider.createID(m.getId());
            for(FAPFramePerformer performer:fapFramesPerfList){
                performer.performFAPFrames(getGretaAPFrameList(m), id);
            }
        }
    }

    @Override
    public void startConnector(ExternalClient extClient) {
        super.startConnector(extClient);
         fapExtClientThread = new FAPExternalClientThread(this);
        fapExtClientThread.start();

    }

    @Override
    public void stopConnector(ExternalClient externalClient) {
        super.stopConnector(externalClient);
    }


    @Override
    public void addFAPFramePerformer(FAPFramePerformer fapfp) {
        fapFramesPerfList.add(fapfp);
    }

    @Override
    public void removeFAPFramePerformer(FAPFramePerformer fapfp) {
        fapFramesPerfList.remove(fapfp);
    }
    private class FAPExternalClientThread extends Thread {
        final FAPExternalClient externalClient;

        public FAPExternalClientThread(FAPExternalClient externalClientToUpdate){
            Logs.debug("externalClientThread created");
            this.setDaemon(true);
            this.externalClient=externalClientToUpdate;

            this.setName("ThriftExternalClient "+externalClient.getHost()+ " "+ externalClient.getPortString());
        }

        public void run() {
            Logs.debug("externalClientThread started" );
            while (externalClient.isConnected() && externalClient.fapExtClientThread==this) {
                externalClient.updateMessage();
                if (externalClient.isNewMessage()) {
                           perform(externalClient.getMessage());
                }

                try {
                    sleep(40);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
