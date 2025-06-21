/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
