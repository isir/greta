/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package greta.auxiliary.sequencemining.structures;


/**
 *
 * @author Mathieu
 */
public class NVBEvent {
    
    public Modality moda;
    public String type;
    public int start_time;
    public int end_time;
    
    public NVBEvent( NVBEvent nvbsource)
    {
        this.moda=nvbsource.moda;
        this.type=nvbsource.type;
        this.start_time=nvbsource.start_time;
        this.end_time=nvbsource.end_time;
    }
    
    public NVBEvent( Modality moda,String type,int start_time,int end_time)
    {
        this.moda=moda;
        this.type=type;
        this.start_time=start_time;
        this.end_time=end_time;
    }
    public NVBEvent( Modality moda,String type,int start_time)
    {
        this.moda=moda;
        this.type=type;
        this.start_time=start_time;
    }
    
}
