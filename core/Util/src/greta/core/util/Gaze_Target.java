
package greta.core.util;

/**
 *
 * @author Michele
 */


public class Gaze_Target {

    public Gaze_Target() {
        this.posX = 0;
        this.posY = 1.55;
        this.posZ = 1;
        gaze_object=false;
        
    }
    
    public Gaze_Target(double x, double y, double z){
        
        this.posX=x;
        this.posY=y;
        this.posZ=z;
        
    }
    
    public double posZ;
    public double posX;
    public double posY;
    public boolean gaze_object;

    public boolean isGaze_object() {
        return gaze_object;
    }

    public void setGaze_object(boolean gaze_object) {
        this.gaze_object = gaze_object;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public void setPosZ(double posZ) {
        this.posZ = posZ;
    }

    
    
    
}
