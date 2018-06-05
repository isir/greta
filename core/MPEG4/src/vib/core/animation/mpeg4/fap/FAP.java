/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.mpeg4.fap;

import vib.core.util.animationparameters.AnimationParameter;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class FAP extends AnimationParameter {

    public FAP(FAP fap) {
        super(fap);
    }

    public FAP() {
        super();
    }

    public FAP(boolean mask, int value) {
        super(mask, value);
    }

    @Override
    public FAP clone() {
        FAP fap = new FAP(this);
        return fap;
    }
}
