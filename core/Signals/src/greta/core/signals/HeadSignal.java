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
package greta.core.signals;

/**
 * This class contains informations about head Signals.
 *
 * @author Andre-Marie Pez
 * @author Brice Donval
 */
public class HeadSignal extends SpineSignal {

    /**
     * Construct a new {@code HeadSignal}.
     *
     * @param id The identifier of this {@code HeadSignal}.
     */
    public HeadSignal(String id) {
        super(id);
    }

    @Override
    public String getModality() {
        return "head";
    }

}
