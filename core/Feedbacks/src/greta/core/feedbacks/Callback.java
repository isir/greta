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
package greta.core.feedbacks;

import greta.core.util.id.ID;

/**
 *
 * @author Ken Prepin
 */
public class Callback {

    private String type;
    private double time;
    private ID animId;

    public Callback(Callback callback){
        this(callback.type(),callback.time(), callback.animId());
    }

    public Callback(String type, double time, ID animId) {
        this.type = type;
        this.time = time;
        this.animId = animId;
    }

    public String type() {
        return type;
    }

    public double time() {
        return time;
    }

    public ID animId() {
        return animId;
    }
    public void setType(String type){
        this.type = type;
    }
     public void setTime(double time){
        this.time = time;
    }
    public void setAnimId(ID animId){
        this.animId = animId;
    }
}
