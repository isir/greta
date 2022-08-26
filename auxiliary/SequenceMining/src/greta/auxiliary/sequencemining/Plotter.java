/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.sequencemining;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.battelle.clodhopper.Cluster;
import org.faceless.graph2.AxesGraph;
import org.faceless.graph2.ImageOutput;
import org.faceless.graph2.Marker;
import org.faceless.graph2.ScatterSeries;
import org.faceless.graph2.Style;
import greta.auxiliary.sequencemining.structures.AttitudeVariationEvent;

/**
 *
 * @author Mathieu
 */
public class Plotter {
    
    //default image
    public static void plotAttitudeVariationsPoints(List<AttitudeVariationEvent> normalizedVariations, String diagramPath)
    {
        plotAttitudeVariationsPoints(normalizedVariations, diagramPath, 2500, 2500);
    }
    
    public static void plotAttitudeVariationsPoints(List<AttitudeVariationEvent> normalizedVariations, String diagramPath, int w, int h)
    {
        ScatterSeries ss = new ScatterSeries("variations", "cross", 3);
        AxesGraph gra = new AxesGraph();
        for(AttitudeVariationEvent avec : normalizedVariations)
        {
            if(!avec.type.equals(AttitudeVariationEvent.VariationType.PLATEAU))
            {
                ss.set(avec.normalizedDuration, avec.value.doubleValue());
            }
        }
        
        //add points
        gra.addSeries(ss);
        ImageOutput out = new ImageOutput(2500,2500);
        gra.draw(out);
        try {
            out.writePNG(new FileOutputStream(diagramPath), 256);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }

    }
    
    public static void plotAttitudeClusters(List<AttitudeVariationEvent> normalizedVariations,double[][] clusterCenters, String outputFilePath)
    {
        plotAttitudeClusters(normalizedVariations,clusterCenters, outputFilePath, 2500, 2500);
    }
    
    public static void plotAttitudeClusters(List<AttitudeVariationEvent> normalizedVariations,double[][] clusterCenters, String outputFilePath, int w, int h)
    {
        ScatterSeries ss2 = new ScatterSeries("variations", "cross", 3);
        for(AttitudeVariationEvent avec : normalizedVariations)
        {
            if(!avec.type.equals(AttitudeVariationEvent.VariationType.PLATEAU))
            {
                ss2.set(avec.normalizedDuration, avec.value.doubleValue());
            }
        }
        AxesGraph gra2 = new AxesGraph();
        ScatterSeries clustersss = new ScatterSeries("clusters", "star", 3);
        Style st = new Style(Color.RED);
        clustersss.setStyle(st);

        {int i=0;
            for(double[] c : clusterCenters)//Object c : orderedCluster)
            {
                //Cluster cl = (Cluster) c;
                Marker m = new Marker("star",100);//cl.getMemberCount()+20);
                m.setStyle(st);
                m.setName("m"+i);
                clustersss.addMarker(m, c[0],c[1]);//cl.getCenter()[0], cl.getCenter()[1]);
                i++;
            }
        }
        gra2.addSeries(ss2);
        gra2.addSeries(clustersss);
        ImageOutput out2 = new ImageOutput(2500,2500);
        gra2.draw(out2);
        try {
            out2.writePNG(new FileOutputStream(outputFilePath), 256);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
