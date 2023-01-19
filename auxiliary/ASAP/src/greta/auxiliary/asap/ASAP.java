/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.asap;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.transport.udp.OSCPortOut;
import greta.core.util.CharacterManager;

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFrameEmitterImpl;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.keyframes.face.AUEmitter;
import greta.core.keyframes.face.AUPerformer;
import greta.core.repositories.AUAPFrame;
import greta.core.util.Constants;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import greta.core.keyframes.face.AUEmitter;
import greta.core.keyframes.face.AUEmitterImpl;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFrameEmitterImpl;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 *
 * @author Michele
 */
public class ASAP implements Runnable{

    
    protected static final Logger LOGGER = Logger.getLogger(ASAP.class.getName());
    
    protected ASAPFrame loader;
    
    public CharacterManager cm;
    
    protected Thread thread;
    private AUEmitterImpl auEmitter = new AUEmitterImpl();
    private BAPFrameEmitterImpl bapFrameEmitter = new BAPFrameEmitterImpl();
    protected final String threadName = ASAP.class.getSimpleName();
    protected double frameDuration = 0.0;
    protected ASAPFrame curFrame = new ASAPFrame();
    protected ASAPFrame prevFrame = new ASAPFrame();
    protected double fps = 0.0;
    protected double min_time = Double.MAX_VALUE;
    protected double max_time = 0.0;	
    protected boolean useFilter = true;	
    protected ArrayOfDoubleFilterPow filterAUs = new ArrayOfDoubleFilterPow(64,5,.5);
    protected ArrayOfDoubleFilterPow filterBAP = new ArrayOfDoubleFilterPow(3,5,.5);

    protected double alpha = 0.75; //1.0;
    
    private int startInputFrame = 0;
    private final int offsetFrame = 0;
    
    protected double prev_rot_X = 0.0;
    protected double prev_rot_Y = 0.0;
    protected double prev_rot_Z = 0.0;

    protected String[] selectedFeatures;
    /**
     * @param loader
     * @param cm
     * @param args the command line arguments
     */
    
    public ASAP(CharacterManager cm, ASAPFrame loader){
        this.cm=cm;
        this.loader=loader;
        this.cm.setAsap_enabled(true);
    }
    
    
    public void setSelected(String[] selected) {
        if (selected != null) {
            if (!Arrays.equals(selected, selectedFeatures)) {
                selectedFeatures = selected;
                setSelectedFeatures(selectedFeatures);
            }
            getLogger().info(String.format("Setting selected features to: %s", Arrays.toString(selected)));
        } else {
            getLogger().warning("No header selected");
        }
    }
    public boolean isUseFilter() {
        return useFilter;
    }

    /* ---------------------------------------------------------------------- */
    
    

    
        public String[] getSelectedFeatures() {
        return selectedFeatures;
    }

    /**
     * @param features the selected output features to set
     */
    public void setSelectedFeatures(String[] features) {
        selectedFeatures = features;
    }
    protected Logger getLogger() {
        return LOGGER;
    }
    
    

@Override
    public void run() {
        LOGGER.info(String.format("Thread: %s running", ASAP.class.getName()));
        // Socket Connection
    }
    


}
