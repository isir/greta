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
package greta.core.animation.lipmodel;

/**
 *
 * @author Andre-Marie Pez
 */
public class CoartParameter {

    double apexULO = 0;
    double apextimeULO = 0;
    double middleULO = 0;
    double apexLLO = 0;
    double apextimeLLO = 0;
    double middleLLO = 0;
    double apexJAW = 0;
    double apextimeJAW = 0;
    double middleJAW = 0;
    double apexLW = 0;
    double apextimeLW = 0;
    double middleLW = 0;
    double apexULP = 0;
    double apextimeULP = 0;
    double middleULP = 0;
    double apexLLP = 0;
    double apextimeLLP = 0;
    double middleLLP = 0;
    double apexCR = 0;
    double apextimeCR = 0;
    double middleCR = 0;

    public double getapexULO() {
        return apexULO;
    }

    public void setULO(double apex, double apexTime, double middle) {
        apexULO = apex;
        apextimeULO = apexTime;
        middleULO = middle;
    }

    public void setLLO(double apex, double apexTime, double middle) {
        apexLLO = apex;
        apextimeLLO = apexTime;
        middleLLO = middle;

    }

    public void setJAW(double apex, double apexTime, double middle) {
        apexJAW = apex;
        apextimeJAW = apexTime;
        middleJAW = middle;

    }

    public void setLW(double apex, double apexTime, double middle) {
        apexLW = apex;
        apextimeLW = apexTime;
        middleLW = middle;

    }

    public void setULP(double apex, double apexTime, double middle) {
        apexULP = apex;
        apextimeULP = apexTime;
        middleULP = middle;

    }

    public void setLLP(double apex, double apexTime, double middle) {
        apexLLP = apex;
        apextimeLLP = apexTime;
        middleLLP = middle;

    }

    public void setCR(double apex, double apexTime, double middle) {
        apexCR = apex;
        apextimeCR = apexTime;
        middleCR = middle;

    }
}
