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
package greta.core.SubjectPlanner;

import greta.core.util.parameter.Parameter;

/**
 *
 * @author Nadine
 */
class MuseumObject implements Parameter {

    String name;
    String period;
    String type;
    String artist;

    public MuseumObject(MuseumObject object) {
        this(object.name, object.period, object.type, object.artist);
    }

    public MuseumObject(String name_, String period_, String type_, String artist_) {
        super();
        this.name = name_;
        this.period = period_;
        this.type = type_;
        this.artist = artist_;
    }

    @Override
    public String getParamName() {
        return this.name;
    }

    @Override
    public void setParamName(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean equals(Parameter p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
