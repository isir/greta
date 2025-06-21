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
package greta.core.animation.common.Frame;

import greta.core.util.math.Function;

/**
 *
 * @author Andre-Marie Pez
 */
public class ExtendedKeyFrame extends KeyFrame{

    private Function function = new greta.core.util.math.easefunctions.Linear();

    public ExtendedKeyFrame(double time){
        super(time);
    }

    public void setFunction(Function function){
        this.function = function;
    }

    public Function getFunction(){
        return function;
    }
}
