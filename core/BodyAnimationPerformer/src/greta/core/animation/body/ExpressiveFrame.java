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
package greta.core.animation.body;

import greta.core.animation.Frame;
import greta.core.keyframes.ExpressivityParameters;

/**
 *
 * @author Jing Huang
 */
public class ExpressiveFrame extends Frame{
    protected double _time;
    protected ExpressivityParameters _exp;
    public double getTime() {
        return _time;
    }

    public void setTime(double time) {
        this._time = time;
    }

      public ExpressivityParameters getExpressivityParameters() {
        return _exp;
    }

    public void setExpressivityParameters(ExpressivityParameters exp) {
        this._exp = exp;
    }
}
