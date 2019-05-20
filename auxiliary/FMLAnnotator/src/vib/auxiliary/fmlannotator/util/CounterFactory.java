/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.fmlannotator.util;

import java.util.HashMap;

/**
 *
 * @author David Panou
 */
public class CounterFactory {

    HashMap<String, Counter> value;

    public CounterFactory(HashMap<String, Counter> stock) {
        value = stock;
    }

    public Counter createCompteur(String name, int value) {
        Counter temp = new Counter(value);
        this.value.put(name, temp);
        return temp;
    }

    public HashMap<String, Counter> createCompteur(String[] names, int value) {
        for (String e : names) {
            Counter temp = new Counter(value);
            this.value.put(e, temp);
        }
        return this.value;
    }
}
