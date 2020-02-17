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

/**
 *
 * @author David Panou
 */
public class Counter {

    private Integer value;

    public Counter(int value) {
        this.value = value;
    }

    public void increase() {
        ++value;
    }

    public void decrease() {
        --value;
    }

    public void set(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String toString() {
        return value.toString();
    }

}
