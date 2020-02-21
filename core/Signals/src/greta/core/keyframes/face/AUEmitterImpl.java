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
package greta.core.keyframes.face;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Philippe Gauthier <philippe.gauthier@sorbonne-universite.fr>
 */


public class AUEmitterImpl implements AUEmitter {
    protected List<AUPerformer> performers = new ArrayList<>();
    @Override
    public void addAUPerformer(AUPerformer performer) {
        if(performer!=null && !performers.contains(performer))
            performers.add(performer);
        
    }

    @Override
    public void removeAUPerformer(AUPerformer performer) {
        if(performer!=null && performers.contains(performer))
            performers.remove(performer);
    }
    
}
