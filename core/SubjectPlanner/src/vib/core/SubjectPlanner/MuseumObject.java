/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vib.core.SubjectPlanner;

import vib.core.util.parameter.Parameter;

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
