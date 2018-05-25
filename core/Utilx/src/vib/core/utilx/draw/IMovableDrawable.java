/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.utilx.draw;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Ken Prepin
 */
public interface IMovableDrawable extends IDrawable{
    
    void setPosition(Point2D p);
    
    void setCenter(Point2D p);
    
    public Point2D getPosition();
    
    public Point2D getCenter();
    
    public Rectangle2D getBounds();
      
    void setMousePressedPosition(Point2D pos);
}
