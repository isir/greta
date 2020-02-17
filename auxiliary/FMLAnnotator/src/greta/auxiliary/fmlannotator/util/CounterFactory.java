/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.fmlannotator.util;

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
