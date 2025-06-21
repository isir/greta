/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
