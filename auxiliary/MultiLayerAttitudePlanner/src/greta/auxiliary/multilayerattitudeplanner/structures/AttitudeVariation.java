/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package greta.auxiliary.multilayerattitudeplanner.structures;

/**
 *
 * @author Mathieu
 */
public class AttitudeVariation {
        
    private AttitudeDimension dim;
    private AttitudeCluster cluster;
        
    public AttitudeVariation(AttitudeDimension dimension)
    {
        this.dim=dimension;
        this.cluster=AttitudeCluster.Null;
        
    }
    
    public AttitudeVariation(AttitudeDimension dimension, AttitudeCluster clu)
    {
        this.dim=dimension;
        this.cluster=clu;
    }

    /**
     * @return the dim
     */
    public AttitudeDimension getDimension() {
        return dim;
    }

    /**
     * @param dim the dim to set
     */
    public void setDimension(AttitudeDimension dim) {
        this.dim = dim;
    }

    /**
     * @return the cluster
     */
    public AttitudeCluster getCluster() {
        return cluster;
    }

    /**
     * @param cluster the cluster to set
     */
    public void setCluster(AttitudeCluster cluster) {
        this.cluster = cluster;
    }
    
}
