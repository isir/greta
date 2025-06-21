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
package greta.core.animation.common.easingcurve;

/**
 *
 * @author Jing Huang
 */
public abstract class EasingCurveFuntion {
    String _t;  //type
    double _p = 0.3f; // period
    double _a = 1.0f;  //amplitude
    double _o = 1.70158f;  //overshoot

    abstract double getValue(double t);

}
