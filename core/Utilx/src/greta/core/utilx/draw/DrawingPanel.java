/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.core.utilx.draw;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author Ken Prepin
 */
public class DrawingPanel extends JPanel{
    List <IDrawable> listDrawables;
    protected AffineTransform transform;
    protected int margin = 5;

    public DrawingPanel(){
        super();
        listDrawables = new LinkedList<IDrawable>();
        transform = new AffineTransform();
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform save = g2.getTransform();
        g2.transform(transform);
//        g2.drawLine(0, -10000, 0, 10000);
//        g2.drawLine(-10000, 0, 10000, 0);
        Rectangle2D visible = getVisibleBounds();
        for(IDrawable drawable : listDrawables){
            if(visible.intersects(drawable.getShape().getBounds2D()))
                drawable.draw(g);
        }
        g2.setTransform(save);
    }

    public double getScaleX(){
        return transform.getScaleX();
    }

    public double getScaleY(){
        return transform.getScaleY();
    }

    public void scale(double scaleX, double scaleY){
        transform.scale(scaleX, scaleY);
    }

    public void scale(double scale){
        transform.scale(scale, scale);
    }
    public void scaleFrom(double scale, Point2D pivot){
        scaleFrom(scale, scale, pivot);
    }
    public void scaleFrom(double scaleX, double scaleY, Point2D pivot){
        scale(scaleX, scaleY);
        translate(
                pivot.getX() / scaleX - pivot.getX()
                ,
                pivot.getY() / scaleY - pivot.getY()
        );
    }

    public void translate(Point2D offset){
        translate(offset.getX(), offset.getY());
    }

    public void translate(double offsetX, double offsetY){
        transform.translate(offsetX, offsetY);
    }

    public void resetTransform(){
        transform.setToIdentity();
    }

    public Rectangle2D getDrawablesBounds(){
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for(IDrawable drawable : listDrawables){
            if(drawable instanceof IMovableDrawable){
                Rectangle2D bounds = ((IMovableDrawable)drawable).getBounds();
                minX = Math.min(minX, bounds.getMinX());
                minY = Math.min(minY, bounds.getMinY());
                maxX = Math.max(maxX, bounds.getMaxX());
                maxY = Math.max(maxY, bounds.getMaxY());
            }
        }
        if(minX > maxX || minY > maxY){
            return null;
        }
        return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
    }

    public Rectangle2D getVisibleBounds(){
        try{
            Point2D min = transform.inverseTransform(new Point2D.Double(0, 0), null);
            Point2D max = transform.inverseTransform(new Point2D.Double(getWidth(), getHeight()), null);
            return new Rectangle2D.Double(min.getX(), min.getY(), max.getX()-min.getX(), max.getY()-min.getY());
        }
        catch(Exception e){}
        return new Rectangle2D.Double(0, 0, getWidth(), getHeight());
    }

    public void addDrawable(IDrawable drawable){
        listDrawables.add(drawable);
        repaint();
    }
    public void removeDrawable(IDrawable drawable){
        listDrawables.remove(drawable);
        repaint();
    }
    public void clear(){
        listDrawables.clear();
        repaint();
    }
    public List<IDrawable> findDrawables(Point2D p){
        List list = new ArrayList<IDrawable>();
        for(IDrawable drawable:listDrawables){
            if(drawable.intersects(p)){
                list.add(drawable);
            }
        }
        return list;
    }

    public boolean isFree(Shape shape){
        for(IDrawable drawable:listDrawables){
            if(shape.intersects(drawable.getShape().getBounds())){
                return false;
            }
        }
        return true;
    }

    public boolean isAlone(IDrawable drawable){
        Rectangle bounds = drawable.getShape().getBounds();
        for(IDrawable element:listDrawables){
            if(!element.equals(drawable) && element.getShape().intersects(bounds)){
                return false;
            }
        }
        return true;
    }

    public List<IDrawable> getDrawables(){
        return this.listDrawables;
    }

    public void centerView(double maximumScale){
        resetTransform();
        //get the space used by the drawables
        Rectangle2D bounds = getDrawablesBounds();
        if(bounds == null){
            return;
        }
        //add a margin to this space
        bounds.setRect(bounds.getMinX()-margin, bounds.getMinY()-margin, bounds.getWidth()+2*margin, bounds.getHeight()+2*margin);

        //compute the target scale factor to apply
        double targetScale = Math.min(Math.min(getWidth()/bounds.getWidth(), getHeight()/bounds.getHeight()), maximumScale);
        scale(targetScale);

        //translate the center of the view to the center of the network
        translate(getWidth()/(2*targetScale)-bounds.getCenterX(), getHeight()/(2*targetScale)-bounds.getCenterY());
    }

    public void centerView(){
        centerView(1.0);
    }

 }
