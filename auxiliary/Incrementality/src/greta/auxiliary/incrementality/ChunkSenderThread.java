package greta.auxiliary.incrementality;

import greta.core.keyframes.Keyframe;
import greta.core.keyframes.KeyframePerformer;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author Sean
 */
public class ChunkSenderThread extends Thread {

    private List<KeyframePerformer> keyframePerformers;
    private TreeMap<Integer, List<Keyframe>> treeList;
    private ID requestId;
    private Mode mode;

    private boolean open;

    public ChunkSenderThread() {
        super();
    }

    public ChunkSenderThread(List<KeyframePerformer> parKeyframePerformers) {
        this.keyframePerformers = parKeyframePerformers;
        treeList = new TreeMap<Integer, List<Keyframe>>();
        this.open = false;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!open || treeList.size() == 0) {
                    sleep(100);
                } else {
                    //System.out.println("\u001B[34m" + treeList);
                    System.out.println("\u001B[34m---------------------------------   THREAD SCHEDULING CHUNKS   -----------------------------");
                    while (open && treeList.size() > 0) {
                        System.out.println("\u001B[34m" + treeList.firstEntry().getKey() + " --- " + treeList.firstEntry().getValue() + " --- IN THREAD " + Thread.currentThread().getId());
                        this.sendKeyframes(treeList.firstEntry().getValue(), requestId, mode);

                        if (treeList.size() > 1) {
                            List<Keyframe> currentBurstList = treeList.firstEntry().getValue();
                            List<Keyframe> nextBurstList = treeList.entrySet().stream().skip(1).map(map -> map.getValue()).findFirst().get();

                            double currentFirst = currentBurstList.get(0).getOffset();
                            double nextFirst = nextBurstList.get(0).getOffset();

                            long sleepTime = (long) (nextFirst * 1000) - (long) (currentFirst * 1000);
                            if (sleepTime > 0) {
                                Thread.sleep(sleepTime);
                                System.out.println("\u001B[34m WAITED : " + nextFirst + " - " + currentFirst + " = " + sleepTime);
                            }
                        }
                        treeList.remove(treeList.firstKey());
                    }

                    Thread.sleep(500); //Wait to make sure agent goes back to rest pose*/
                    this.close();
                }
            } catch (Exception e) {
            }
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

    public synchronized void open() {
        System.out.println("\u001B[31m RECEIVED OPEN COMMAND");
        this.open = true;
    }

    public synchronized void close() {
        System.out.println("\u001B[31m RECEIVED CLOSE COMMAND");
        this.open = false;
    }

    public synchronized void setChunkList(TreeMap<Integer, List<Keyframe>> parTreeList) {
        this.treeList = parTreeList;
    }

    public synchronized void setRequestId(ID parId) {
        this.requestId = parId;
    }

    public synchronized void setMode(Mode parMode) {
        this.mode = parMode;
    }

    public synchronized void send(TreeMap<Integer, List<Keyframe>> parTreeList, ID id, Mode mode) {
        this.open();
        this.setChunkList(parTreeList);
        this.setRequestId(id);
        this.setMode(mode);
    }

    public void addKeyframePerformer(KeyframePerformer parKeyframePerformer) {
        this.keyframePerformers.add(parKeyframePerformer);
    }

    public void removeKeyframePerformer(KeyframePerformer parKeyframePerformer) {
        int i = 0;
        for (KeyframePerformer kfp : keyframePerformers) {
            if (kfp.equals(parKeyframePerformer)) {
                keyframePerformers.remove(i);
            }
            i++;
        }
    }
}
