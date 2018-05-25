package vib.core.SubjectPlanner;

// package vib.core.intentionplanner;


import vib.core.util.parameter.EngineParameterSetOfSet;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nadine
 */
public class Sentences extends EngineParameterSetOfSet{

    public static final String SENTENCE_LIB = "SubjectPlanner/Data/Sentences.xml";
    
    public static Sentences global_sentence_library;
     
    public Sentences(String filename){
      super(filename);
    }

    
}
