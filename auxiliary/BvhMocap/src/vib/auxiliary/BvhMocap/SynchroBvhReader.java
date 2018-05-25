/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.BvhMocap;

//import vib.core.signals.EventPerformer;

/**
 *
 * @author Nesrine Fourati
 * @author Radoslaw Niewiadomski
 */


public class SynchroBvhReader extends BvhReader //implements EventPerformer
{

    public SynchroBvhReader() {
        this("");
    }

    public SynchroBvhReader(String fname) {
        filename = fname;
        dictionary = new Dictionary();
        dictionary.Initialize();
    }

//   // @Override
//    public void load(String bvhfilename) {
//        filename = bvhfilename;
//    }
//
//    public void performEvent(String message) {   //filename = message;
//        process();
//    }
}
