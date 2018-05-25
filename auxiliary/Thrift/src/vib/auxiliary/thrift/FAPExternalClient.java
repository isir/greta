/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift;

import vib.auxiliary.thrift.gen_java.Message;
import vib.auxiliary.thrift.services.ExternalClient;
import vib.auxiliary.thrift.services.Sender;
import vib.core.animation.mpeg4.fap.FAPFrame;
import vib.core.animation.mpeg4.fap.FAPFrameEmitter;
import vib.core.animation.mpeg4.fap.FAPFramePerformer;
import vib.core.util.animationparameters.AnimationParametersFrame;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.log.Logs;
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
                performer.performFAPFrames(getVibAPFrameList(m), id);
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


