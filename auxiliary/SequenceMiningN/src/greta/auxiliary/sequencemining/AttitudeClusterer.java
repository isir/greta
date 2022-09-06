/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.sequencemining;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.task.TaskEvent;
import org.battelle.clodhopper.task.TaskListener;
import org.battelle.clodhopper.tuple.ArrayTupleList;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.xmeans.XMeansClusterer;
import org.battelle.clodhopper.xmeans.XMeansParams;
import greta.auxiliary.sequencemining.structures.AttitudeVariationEvent;

/**
 *
 * @author Mathieu
 */
public class AttitudeClusterer {

    
    //clusters attitude variations
    public List cluster(List<AttitudeVariationEvent> attitudesVars, XMeansParams params) {
        List<AttitudeVariationEvent> slopeList = new LinkedList<>();
        for (AttitudeVariationEvent avec : attitudesVars) {
            if (!avec.type.equals(AttitudeVariationEvent.VariationType.PLATEAU)) {
                slopeList.add(avec);
            }
        }
        int tupleLength = 2; //nb attributs slopes. 2: pente, hauteur
        int tupleCount = slopeList.size() / tupleLength;
        double[] attitudesVars1D = attitudesVarsTo1D(slopeList);
        TupleList tupleData = new ArrayTupleList(tupleLength, tupleCount, attitudesVars1D);
        XMeansClusterer xmeans = new XMeansClusterer(tupleData, params);
        xmeans.addTaskListener(new TaskListener() {
            @Override
            public void taskBegun(TaskEvent e) {
                System.out.printf("%s\n\n", e.getMessage());
            }

            @Override
            public void taskMessage(TaskEvent e) {
                System.out.println("  ... " + e.getMessage());
            }

            @Override
            public void taskProgress(TaskEvent e) {
                // Reports the progress.  Ignore for this example.
            }

            @Override
            public void taskPaused(TaskEvent e) {
                // Reports that the task has been paused. Won't happen in this example,
                // so ignore.
            }

            @Override
            public void taskResumed(TaskEvent e) {
                // Reports when a paused task has been resumed.
                // Ignore for this example.
            }

            @Override
            public void taskEnded(TaskEvent e) {
                System.out.printf("\n%s\n\n", e.getMessage());
            }
        });
        Thread t = new Thread(xmeans);
        t.start();
        List clusters = null;
        try {
            clusters = xmeans.get();
        } catch (InterruptedException ex) {
            System.err.println(ex.getMessage());
        } catch (ExecutionException ex) {
            System.err.println(ex.getMessage());
        }
        return clusters;
    }

    //takes a list of clusters and orders it with respect to number of events in the cluster
    List<Cluster> orderClusters(List clusters) {
        List<Cluster> orderedClusters = new LinkedList<>();
        for(Object c : clusters)
        {
            int i=0;
            boolean stillNotAdded=true;
            Cluster cl = (Cluster) c;
            if(orderedClusters.isEmpty())
            {
                orderedClusters.add(cl);
                continue;
            }
            for(Cluster clInList : orderedClusters)
            {
                if(cl.getMemberCount()>clInList.getMemberCount())
                {
                    orderedClusters.add(i, cl);
                    stillNotAdded=false;
                    break;
                }
                else
                {
                    i++;
                }
            }
            if(stillNotAdded)
                orderedClusters.add(cl);
        }
        return orderedClusters;
    }
    
    public static void writeToFile(List<Cluster> list, String outputPath) throws IOException
    {
        for(int i=0;i<list.size();i++)
        {
            String outputPathCluster=outputPath+i+".txt";
            Util.checkDeleteOutputFile(outputPathCluster);
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPathCluster))); //writer
            bw.write(i+","+list.get(i).getCenter()[0]+","+list.get(i).getCenter()[1]
                    +","+list.get(i).getMemberCount());
            bw.close();
        }
    }

    private double[] attitudesVarsTo1D(List<AttitudeVariationEvent> slopeList) {
        int fullsize = slopeList.size() * 2;
        double[] oneDimensionalData = new double[fullsize];
        for (int i = 0; i < fullsize; i++) {
            if (i % 2 == 0) {
                oneDimensionalData[i] = slopeList.get(i / 2).normalizedDuration;
            } else {
                oneDimensionalData[i] = slopeList.get(i / 2).value.doubleValue();
            }
        }
        return oneDimensionalData;
    }
    
}
