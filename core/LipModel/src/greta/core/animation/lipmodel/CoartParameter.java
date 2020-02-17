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
