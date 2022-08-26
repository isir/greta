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
package greta.core.signals.gesture;

/**
 *
 * @author Jing Huang
 */
public class UniformPosition implements Position {

    private double x, y, z;

    private boolean xFixed = false;
    private boolean yFixed = false;
    private boolean zFixed = false;

    private boolean xOverridable = false;
    private boolean yOverridable = false;
    private boolean zOverridable = false;

    public UniformPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public UniformPosition(){
        this(0.5, 0.5, 0.5);
    }

    public UniformPosition(UniformPosition p) {
        this(p.x, p.y, p.z);
        xFixed = p.xFixed;
        yFixed = p.yFixed;
        zFixed = p.zFixed;
        xOverridable = p.xOverridable;
        yOverridable = p.yOverridable;
        zOverridable = p.zOverridable;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public void applySpacial(double spc) {
        //magic numbers come from old codes
        if(!xFixed) {
            x *= (spc * (spc>0 ? 1.3 : 0.7)) + 1;
        }
        if(!yFixed) {
            y*= (spc * (spc>0 ? 0.6 : 0.25)) + 1;
        }
        if(!zFixed) {
            z*= (spc * 0.25) + 1;
        }
    }

    @Override
    public String getStringPosition() {
        String returnString = "       <horizontalLocation>" + getX() + "</horizontalLocation>\n";
        returnString += "       <verticalLocation>" + getY() + "</verticalLocation>\n";
        returnString += "       <frontalLocation>" + getZ() + "</frontalLocation>\n";
        return returnString;
    }

    @Override
    public Position getCopy() {
        return new UniformPosition(this);
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public boolean isXFixed() {
        return xFixed;
    }

    public boolean isYFixed() {
        return yFixed;
    }

    public boolean isZFixed() {
        return zFixed;
    }

    public void setXFixed(boolean xFixed) {
        this.xFixed = xFixed;
    }

    public void setYFixed(boolean yFixed) {
        this.yFixed = yFixed;
    }

    public void setZFixed(boolean zFixed) {
        this.zFixed = zFixed;
    }

    public boolean isXOverridable() {
        return xOverridable;
    }

    public boolean isYOverridable() {
        return yOverridable;
    }

    public boolean isZOverridable() {
        return zOverridable;
    }

    public void setXOverridable(boolean xOverridable) {
        this.xOverridable = xOverridable;
    }

    public void setYOverridable(boolean yOverridable) {
        this.yOverridable = yOverridable;
    }

    public void setZOverridable(boolean zOverridable) {
        this.zOverridable = zOverridable;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"["+(xFixed?x:"("+x+")")+ " "+(yFixed?y:"("+y+")")+ " "+(zFixed?z:"("+z+")")+"]";
    }

}
