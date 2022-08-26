/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package greta.auxiliary.multilayerattitudeplanner.structures;

/**
 *
 * @author Mathieu
 */
public enum AttitudeCluster {
    BigIncr(2),
    BigDecr(3),
    SmallDecr(0),
    SmallIncr(1),
    Null(-1);
    
    private int clusterID;
    
    private AttitudeCluster(int clusterID)
    {
        this.clusterID=clusterID;
    }
    
    public int getClusterID()
    {
        return clusterID;
    }
    
}
