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
package greta.core.behaviorplanner.lexicon;

import greta.core.signals.Signal;
import greta.core.util.time.TimeMarker;
import java.util.ArrayList;
import java.util.List;

/**
 * This class of {@code Signal} intended to be used by the behavior planner in
 * the {@code Lexicon}.<br/> It can not be used other than to assist in the
 * signal selection, describing the abstract content a {@code Signal}.<br/> It
 * contains no temporal information (no {@code TimeMarkers}), And can not be
 * temporized.
 *
 * @author Andre-Marie Pez
 */
public class SignalItem implements Signal {

    private String modality;
    private String id;
    private Shape mainShape;
    private ArrayList<Shape> aternatives;
    private double totalProb;
    private double minSignalDuration;

    /**
     * Construct a {@code SignalItem} specifying the id, the modality used and
     * the main shape.
     *
     * @param id the id of this {@code SignalItem}
     * @param modality the modality used by this {@code SignalItem}
     * @param shape the main {@code Shape} object of this {@code SignalItem}
     */
    public SignalItem(String id, String modality, Shape shape) {
        this.id = id;
        this.modality = modality;
        mainShape = shape;
        if (mainShape != null){
            totalProb = mainShape.getProbability();
            aternatives = new ArrayList<Shape>();
            minSignalDuration = shape.getMin();
        } else {
            double inf = Double.POSITIVE_INFINITY;
            mainShape = new Shape(modality, 0.0, inf,"" ,"" ,"" ,"");
            totalProb = 0.0;
            aternatives = null;
            minSignalDuration = 0.0;
        }
    }

    /**
     * Empty constructor
     */
    public SignalItem() {
        this.id = "-1";
        this.modality = "";
        mainShape = null;
        totalProb = -1;
        aternatives = null;
        minSignalDuration = -1;
    }

    //Copy constructor
    public SignalItem(SignalItem source) {
        this.id = source.getId();
        this.modality = source.getModality();

        Shape sourceMain = source.getMainShape();

        Shape copyMain = new Shape(sourceMain.getName(), sourceMain.getMin(), sourceMain.getMax(), sourceMain.getContent(), sourceMain.getIntonation(), sourceMain.getVoicequality(), sourceMain.getMeaning());
        copyMain.setProbability(sourceMain.getProbability());
        mainShape = copyMain;
        totalProb = source.totalProb;
        aternatives = new ArrayList<Shape>();
        double min = copyMain.getMin();
        for (Shape sh : source.getAlternativeShapes()) {
            Shape copysh = new Shape(sh.getName(), sh.getMin(), sh.getMax(), sh.getContent(), sh.getIntonation(), sh.getVoicequality(), sh.getMeaning());
            copysh.setProbability(sh.getProbability());
            if (sh.getMin() < min) {
                min = sh.getMin();
            }
            aternatives.add(copysh);
        }
    }

    /**
     * Adds a new shape specifying its probability of occurrence.
     *
     * @param alternativeShape the {@code Shape} object
     * @param probability the probability of occurrence
     */
    public void addAlternative(Shape alternativeShape, double probability) {
        alternativeShape.setProbability(probability);
        aternatives.add(alternativeShape);
        totalProb += probability - mainShape.getProbability();
        mainShape.setProbability(mainShape.getProbability() - (mainShape.getProbability() > probability ? probability : 0)); //try to keep mainShape.probability > 0
        totalProb += mainShape.getProbability();
        if (alternativeShape.getMin() < minSignalDuration) {
            minSignalDuration = alternativeShape.getMin();
        }
    }

    /**
     * Getter for the alternative shape list
     *
     * @return a List of alternative shape of a SignalItem
     */
    public List<Shape> getAlternativeShapes() {
        return aternatives;
    }

    public Shape getMainShape() {
        return mainShape;
    }

    /**
     * Retreives a {@code Shape} by its name.<br/> May return {@code null} if
     * the target {@code Shape} is not found.
     *
     * @param name the name of the {@code Shape} to found
     * @return the target {@code Shape} or {@code null} if not found
     */
    public Shape getShape(String name) {
        if (mainShape.getName().equalsIgnoreCase(name)) {
            return mainShape;
        }
        for (Shape shape : aternatives) {
            if (shape.getName().equalsIgnoreCase(name)) {
                return shape;
            }
        }
        return null;
    }

    /**
     * Returns randomly one of shapes of this {@code SignalItem}.<br/> Only
     * shape that can fit in the holes of the interval are considered It use the
     * probabilties indicated with shapes to weight the random.
     *
     * @param maxHole is the maximum free hole in the interval
     * @return a random shape
     */
    public Shape getRandomShape(double maxHole) {
        double choose = Math.random() * totalProb;
        double base = 0;
        //first, the main shape
        //if(Math.random() < mainShape.probability)
        if (choose < mainShape.getProbability() && mainShape.getMin() <= maxHole) {
            return mainShape;
        }
        base += mainShape.getProbability();
        //try one alternative :
        for (Shape s : aternatives) {
            //if(Math.random() < s.probability)
            if (choose < base + s.getProbability() && s.getMin() <= maxHole) {
                return s;
            }
            base += s.getProbability();
        }
        //finaly return the main shape
        if (mainShape.getMin() <= maxHole) {
            return mainShape;
        } else {
            return null;
        }
    }

    @Override
    public String getModality() {
        return modality;
    }

    @Override
    public List<TimeMarker> getTimeMarkers() {
        //an empty list
        return new ArrayList<TimeMarker>();
    }

    @Override
    public TimeMarker getTimeMarker(String name) {
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    public double getMinSignalDuration() {
        return minSignalDuration;
    }

    @Override
    public void schedule() {
        //no TimeMarkers here...
    }

    @Override
    public TimeMarker getStart() {
        return null;//no TimeMarkers here...
    }

    @Override
    public TimeMarker getEnd() {
        return null;//no TimeMarkers here...
    }
}
