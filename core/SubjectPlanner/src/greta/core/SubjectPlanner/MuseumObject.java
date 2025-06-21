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
