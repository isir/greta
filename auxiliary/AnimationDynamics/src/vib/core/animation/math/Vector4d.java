/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vib.core.animation.math;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author Jing Huang (http://perso.telecom-paristech.fr/~jhuang/)  jhuang@telecom-paristech.fr
 */
public class Vector4d  extends VectorNd<Vector4d>{
    public Vector4d(){
        super(4);
        this.set(0, 0, 0, 0);
    }
    
    public Vector4d(RealVector v) {
        super(4);
        this.setEntry(0,v.getEntry(0));
        this.setEntry(1,v.getEntry(1));
        this.setEntry(2,v.getEntry(2));
        this.setEntry(3,v.getEntry(3));
    }
    
    public Vector4d(double x, double y, double z, double w){
        super(4);
        this.set(x, y, z, w);
    }
    
    public void set(double x, double y, double z, double w) {
        this.setEntry(0, x);
        this.setEntry(1, y);
        this.setEntry(2, z);
        this.setEntry(3, w);
    }
    

    @Override
    public Vector4d copyData(RealVector arv) {
        return new Vector4d(arv);
    }
    
    public static Vector4d zero(){
        return new Vector4d();
    }
}
