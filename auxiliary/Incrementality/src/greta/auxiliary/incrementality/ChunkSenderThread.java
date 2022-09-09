package greta.auxiliary.incrementality;

import greta.core.keyframes.Keyframe;
import greta.core.keyframes.KeyframePerformer;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Thread that schedule chunk of keyframe after realizer processing
 *
 * @author Sean GRAUX
 */
public class ChunkSenderThread extends Thread {

    private List<KeyframePerformer> keyframePerformers;
    private TreeMap<Integer, List<Keyframe>> treeList;
    private ID requestId;
    private Mode mode;

    private boolean isRunning;
    private boolean isQueued;

    private boolean isReplaced;

    public ChunkSenderThread() {
        super();
    }

    public ChunkSenderThread(List<KeyframePerformer> parKeyframePerformers) {
        this.keyframePerformers = parKeyframePerformers;
        treeList = new TreeMap<Integer, List<Keyframe>>();
        this.isRunning = true;
        this.isQueued = false;
        this.isReplaced = false;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                //Wait to get keyframe to schedule and the open flag
                if (!isQueued || treeList.size() == 0) {
                    sleep(100);
                    //schedule keyframes chunk by chunk
                } else {
                    System.out.println("\u001B[34m---------------------------------   THREAD SCHEDULING CHUNKS   -----------------------------");
                    while (isQueued && treeList.size() > 0) {
                        System.out.println("\u001B[34m" + treeList.firstEntry().getKey() + " --- " + treeList.firstEntry().getValue() + " --- IN THREAD " + Thread.currentThread().getId());

                        for (Map.Entry<Integer, List<Keyframe>> entry : treeList.entrySet()) {
                            System.out.println(requestId + " --- " + entry.getKey());
                            for (Keyframe kf : entry.getValue()) {
                                System.out.println(kf.getParentId() + " --- " + kf.toString() + " --- " + kf.getOffset());
                            }
                        }

                        //send keyframes
                        this.sendKeyframes(treeList.firstEntry().getValue(), requestId, mode);

                        //found the difference between last keyframe of current chunk and first keyframe of next chunk
                        if (treeList.size() > 1) {
                            List<Keyframe> currentBurstList = treeList.firstEntry().getValue();
                            List<Keyframe> nextBurstList = treeList.entrySet().stream().skip(1).map(map -> map.getValue()).findFirst().get();

                            double currentFirst = currentBurstList.get(0).getOffset();
                            double nextFirst = nextBurstList.get(0).getOffset();

                            //wait calculated amount of time
                            long sleepTime = (long) (nextFirst * 1000) - (long) (currentFirst * 1000);
                            if (sleepTime > 0) {
                                Thread.sleep(sleepTime);
                                System.out.println("\u001B[34m WAITED : " + nextFirst + " - " + currentFirst + " = " + sleepTime);
                            }
                        }
                        this.removeFromList();
                        /*if (this.isReplaced == false) {
                            this.removeFromList();
                        } else {
                            this.isReplaced = false;
                            break;
                        }*/
                    }

                    Thread.sleep(500); //Wait to make sure agent goes back to rest pose*/
                    System.out.println(" ------------------------------------ END OF " + requestId + " ------------------------------------\n");
                    this.closeQueue();
                }
            } catch (Exception e) {
            }
        }
    }

    //send keyframes to next components
    public void sendKeyframes(List<Keyframe> keyframes, ID id, Mode mode) {
        if (keyframes != null) {
            for (KeyframePerformer performer : keyframePerformers) {
                performer.performKeyframes(keyframes, id, mode);
            }
        }
    }

    //flag the queue as being occupied
    public synchronized void putInQueue() {
        System.out.println("\u001B[31m RECEIVED PUT IN QUEUE COMMAND");
        this.isQueued = true;
    }

    //flag the queue as being empty
    public synchronized void closeQueue() {
        System.out.println("\u001B[31m RECEIVED CLOSE COMMAND");
        this.isQueued = false;
    }

    //stop the thread
    public synchronized void stopRunning() {
        this.isRunning = false;
    }

    //set the keyframe chunk list
    public synchronized void setChunkList(TreeMap<Integer, List<Keyframe>> parTreeList) {
        this.treeList = parTreeList;
    }

    public synchronized void emptyChunkList() {
        System.out.println("Emptying list");
        this.treeList = new TreeMap<Integer, List<Keyframe>>();
    }

    public synchronized void setRequestId(ID parId) {
        this.requestId = parId;
    }

    public synchronized void setMode(Mode parMode) {
        this.mode = parMode;
    }

    //let another object send a list of chunked keyframes to the thread
    public synchronized void send(TreeMap<Integer, List<Keyframe>> parTreeList, ID id, Mode mode) {
        /*if (mode.getCompositionType().toString().equals("blend")) {
            System.out.println("BLEND FOUND");
        } else if (mode.getCompositionType().toString().equals("append")) {
            System.out.println("APPEND FOUND");
            //this.setChunkList(this.treeList.append(parTreeList));
            TreeMap<Integer, List<Keyframe>> tempList = this.treeList;

            for (Map.Entry<Integer, List<Keyframe>> entry : parTreeList.entrySet()) {
                tempList.put(entry.getKey(), entry.getValue());
            }

            this.setChunkList(tempList);
        } else {
            System.out.println("REPLACE FOUND");

            if (!this.treeList.isEmpty()) {
                this.isReplaced = true;
            }
            this.emptyChunkList();
            this.setChunkList(parTreeList);
        }*/

        TreeMap<Integer, List<Keyframe>> tempList = this.treeList;

        for (Map.Entry<Integer, List<Keyframe>> entry : parTreeList.entrySet()) {
            tempList.put(entry.getKey(), entry.getValue());
        }

        this.setChunkList(tempList);

        this.setRequestId(id);
        this.setMode(mode);
        this.putInQueue();
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

    public void wakeUp() {
        this.interrupt();
    }

    private synchronized void removeFromList() {
        this.treeList.remove(treeList.firstKey());
    }
}
