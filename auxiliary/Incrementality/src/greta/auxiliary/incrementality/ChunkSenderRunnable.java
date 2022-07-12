package greta.auxiliary.incrementality;

import greta.core.keyframes.Keyframe;
import greta.core.keyframes.KeyframeEmitter;
import greta.core.keyframes.KeyframePerformer;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author Sean
 */
public class ChunkSenderRunnable implements Runnable {

    private List<KeyframePerformer> keyframePerformers;
    private TreeMap<Integer, List<Keyframe>> treeList;
    private ID requestId;
    private Mode mode;
    private boolean exit;
    
    public ChunkSenderRunnable(){}

    public ChunkSenderRunnable(List<KeyframePerformer> parKeyframePerformers, TreeMap<Integer, List<Keyframe>> parTreeList, ID parRequestId, Mode parMode) {
        this.keyframePerformers = parKeyframePerformers;
        this.treeList = parTreeList;
        this.requestId = parRequestId;
        this.mode = parMode;
        this.exit = false;
    }

    @Override
    public void run() {
        System.out.println("\u001B[34m---------------------------------   THREAD SCHEDULING CHUNKS   -----------------------------");
        while (exit == false && treeList.size() > 0) {
            System.out.println("\u001B[34m" + treeList.firstEntry().getKey() + " --- " + treeList.firstEntry().getValue() + " --- IN THREAD " + Thread.currentThread().getId());
            this.sendKeyframes(treeList.firstEntry().getValue(), requestId, mode);

            if (treeList.size() > 1) {
                try {
                    List<Keyframe> currentBurstList = treeList.firstEntry().getValue();
                    List<Keyframe> nextBurstList = treeList.entrySet().stream().skip(1).map(map -> map.getValue()).findFirst().get();

                    double currentFirst = currentBurstList.get(0).getOffset();
                    double nextFirst = nextBurstList.get(0).getOffset();
                    //System.out.println("TEST WAIT = " + lastCurrent + " --- " + lastNext);

                    long sleepTime = (long) (nextFirst * 1000) - (long) (currentFirst * 1000);
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                        System.out.println("\u001B[34m WAITED : " + nextFirst + " - " + currentFirst + " = " + sleepTime);
                    }
                } catch (Exception e) {
                    System.out.println("\u001B[34m ERROR --- " + e);
                }
            }
            treeList.remove(treeList.firstKey());
        }

        try {
            Thread.sleep(1000); //Wait to make sure agent goes back to rest pose
        } catch (Exception e) {
            System.out.println("ERROR --- " + e);
        }
    }

    public void sendKeyframes(List<Keyframe> keyframes, ID id, Mode mode) {
        if (keyframes != null) {
            for (KeyframePerformer performer : keyframePerformers) {
                // TODO : Mode management in progress
                performer.performKeyframes(keyframes, id, mode);
            }
        }
    }
    
    public void shutdown(){
        this.exit = true;
    }
}
