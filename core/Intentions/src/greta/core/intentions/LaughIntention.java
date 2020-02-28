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
package greta.core.intentions;

import greta.core.util.laugh.Laugh;
import greta.core.util.time.TimeMarker;

/**
 *
 * @author Andre-Marie Pez
 * @author Yu Ding
 */
public class LaughIntention extends Laugh implements Intention{


    public LaughIntention(){
        super();
    }

    public LaughIntention(String id, TimeMarker start, TimeMarker end){
        super(id, start, end);
    }

    public LaughIntention(Laugh other){
        super(other);
    }

    @Override
    public String getName() {
        return "laugh";
    }

    @Override
    public String getType() {
        return "laugh";
    }

    @Override
    public double getImportance() {
        return 0.5;
    }

    @Override
    public boolean hasCharacter() {
        return false;
    }

    @Override
    public String getCharacter() {
        return null;
    }

    @Override
    public String getTarget (){
        return null;
    }
}
