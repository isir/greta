/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.lipmodel;

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
