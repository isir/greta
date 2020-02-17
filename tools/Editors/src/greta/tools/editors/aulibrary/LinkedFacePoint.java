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
package greta.tools.editors.aulibrary;

/**
 *
 * @author Andre-Marie Pez
 */
public class LinkedFacePoint extends FacePoint{

    private FapComponents linkedTo;
    private double percentOfMovementX;
    private double percentOfMovementY;
    private LinkedFacePoint(double originalX, double originalY){
        this(originalX, originalY, null);
    }

    public LinkedFacePoint(double originalX, double originalY, FapComponents linkedTo){
        this(originalX, originalY, linkedTo, 1);
    }

    public LinkedFacePoint(double originalX, double originalY, FapComponents linkedTo, double percentOfMovement){
        this(originalX, originalY, linkedTo, percentOfMovement, percentOfMovement);
    }

    public LinkedFacePoint(double originalX, double originalY, FapComponents linkedTo, double percentOfMovementX, double percentOfMovementY){
        super(originalX, originalY);
        this.linkedTo = linkedTo;
        this.percentOfMovementX = percentOfMovementX;
        this.percentOfMovementY = percentOfMovementY;
    }

    @Override
    public double getX(){
        this.x = (linkedTo.x - linkedTo.originalX)*percentOfMovementX + originalX;
        return super.getX();
    }

    @Override
    public double getY(){
        this.y = (linkedTo.y - linkedTo.originalY)*percentOfMovementY + originalY;
        return super.getY();
    }
}
