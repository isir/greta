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
package greta.auxiliary.socialparameters;

import greta.core.util.animationparameters.AnimationParameter;

/**
 *
 * @author Florian Pecune
 */
public class SocialParameter extends AnimationParameter {

    public static final double FLOAT_PRECISION = 1000000;
    private static final double INVALID_VALUE = 2;


    public SocialParameter(){
        super();
    }

    public SocialParameter(boolean mask, int value){
        super(mask, value);
    }

    public SocialParameter(SocialParameter sp){
        super(sp);
    }

    public double getDoubleValue() {
        return getValue()/FLOAT_PRECISION;
    }

    public void setDoubleValue (double value) {
        applyValue((int)(value*FLOAT_PRECISION));
    }

    public void setAsInvalid(){
        setDoubleValue(INVALID_VALUE);//or any value outside [-1, 1]
    }

    public boolean isInvalid(){
        return getValue()>FLOAT_PRECISION || getValue()<-FLOAT_PRECISION;
    }
}
