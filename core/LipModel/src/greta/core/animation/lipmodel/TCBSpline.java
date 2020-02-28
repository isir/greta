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
 * @author ding
 */
public class TCBSpline {

    double[] val = null;
    double[] time = null;
    float[] tension = null;
    float[] continuity = null;
    float[] bias = null;
    float startSpeed = 0.0f;
    float endSpeed = 0.0f;

    double timeEnd = 0.0f;
    // this interpolation operation ends at timeEnd
    // timeEnd may not be last time of control points

    public double getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(double timeEnd) {
        this.timeEnd = timeEnd;
    }

    int len = 0; // original number of control points

    public double[] getVal() {
        return val;
    }

    public void setVal(double[] val) {
        this.val = val;
    }

    public double[] getTime() {
        return time;
    }

    public void setTime(double[] time) {
        this.time = time;
    }

    public float[] getTension() {
        return tension;
    }

    public void setTension(float[] tension) {
        this.tension = tension;
    }

    public float[] getContinuity() {
        return continuity;
    }

    public void setContinuity(float[] continuity) {
        this.continuity = continuity;
    }

    public float[] getBias() {
        return bias;
    }

    public void setBias(float[] bias) {
        this.bias = bias;
    }

    public float getStartSpeed() {
        return startSpeed;
    }

    public void setStartSpeed(float startSpeed) {
        this.startSpeed = startSpeed;
    }

    public float getEndSpeed() {
        return endSpeed;
    }

    public void setEndSpeed(float endSpeed) {
        this.endSpeed = endSpeed;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public double[] getA() {
        return A;
    }

    public void setA(double[] A) {
        this.A = A;
    }

    public double[] getB() {
        return B;
    }

    public void setB(double[] B) {
        this.B = B;
    }

    public double[] getC() {
        return C;
    }

    public void setC(double[] C) {
        this.C = C;
    }

    public double[] getD() {
        return D;
    }

    public void setD(double[] D) {
        this.D = D;
    }

    public double[] getDelta() {
        return delta;
    }

    public void setDelta(double[] delta) {
        this.delta = delta;
    }

    public double[] getTi() {
        return Ti;
    }

    public void setTi(double[] Ti) {
        this.Ti = Ti;
    }

    public double[] getTo() {
        return To;
    }

    public void setTo(double[] To) {
        this.To = To;
    }

    double[] A = null;
    double[] B = null;
    double[] C = null;
    double[] D = null;
    double[] delta = null;
    double[] Ti = null;
    double[] To = null;

    //float timeGo = 0.0f;

    public TCBSpline(double[] val, double[] time, float t, float c, float b, float startSpeed, float endSpeed){
        this.len = val.length;

        // set parameters
        tension = new float[len-1];
        continuity = new float[len-1];
        bias = new float[len-1];
        A = new double[len-1];
        B = new double[len-1];
        C = new double[len-1];
        D = new double[len-1];
        delta = new double[len-1];

        for (int f = 0; f < tension.length; f++)
        {
            tension[f] = t;
            continuity[f] = c;
            bias[f] = b;
        }
        this.val = val;
        this.time = time;
        this.startSpeed = startSpeed;
        this.endSpeed = endSpeed;

        Ti = new double[this.val.length];
        To = new double[this.val.length];

        //System.out.println("in construction fonction: time[end]:  "+ time[time.length-1]);

        this.calculateABCD();
    }

    public void calculateABCD()
    {
        // calculate delta
        this.calculateDelta();
        // calculate Ti and To
        this.calculateTiAndTo();
        for (int p = 0; p < A.length; p++)
        {
            // calculate A
            A[p] = val[p];
            // calculate B
            B[p] = delta[p]*To[p];
            // calculate C
            C[p] = 3*(val[p+1]-val[p]) - delta[p]*(2*To[p] + Ti[p+1]);
            // calculate D
            D[p] = -2*(val[p+1]-val[p]) + delta[p]*(To[p]+Ti[p+1]);
        }

    }

    public void calculateDelta()
    {
        for (int p = 0; p < delta.length; p++)
        {
             delta[p] = time[p+1]-time[p];
        }
    }

    public void calculateTiAndTo()
    {
        // 1st control point
        int p = 0;
        Ti[p] = startSpeed;//(((1-tension[p])*(1+continuity[p])*(1-bias[p]))/2)*(val[p+1]-val[p])/delta[p] + (((1-tension[p])*(1-continuity[p])*(1+bias[p]))/2)*startSpeed;
        To[p] = startSpeed;//(((1-tension[p])*(1-continuity[p])*(1-bias[p])/2*(val[p+1]-val[p])/delta[p]))+(((1-tension[p])*(1+continuity[p])*(1-bias[p]))/2)*startSpeed;
        // the end control point
        p = val.length-1;
        Ti[p] = endSpeed;// + (((1-tension[p])*(1-continuity[p])*(1+bias[p]))/2)*((val[p]-val[p-1]))/delta[p-1];
        To[p] = endSpeed;//(((1-tension[p])*(1-continuity[p])*(1-bias[p])/2*endSpeed))+(((1-tension[p])*(1+continuity[p])*(1-bias[p]))/2)*((val[p]-val[p-1]))/delta[p-1];

        // the other control point
        for (p = 1; p < (Ti.length-1); p++){
             Ti[p] = this.calculateOneTi(p);
             To[p] = this.calculateOneTo(p);
        }
    }

    public double calculateOneTi(int p)
    {
        double ti;
        //ti = (((1-tension[p])*(1+continuity[p])*(1-bias[p]))/2)*(val[p+1]-val[p])/delta[p] + (((1-tension[p])*(1-continuity[p])*(1+bias[p]))/2)*((val[p]-val[p-1]))/delta[p-1];
        ti = (2*delta[p]/(delta[p]+delta[p-1]))*((((1-tension[p])*(1+continuity[p])*(1-bias[p]))/2)*(val[p+1]-val[p]) + (((1-tension[p])*(1-continuity[p])*(1+bias[p]))/2)*((val[p]-val[p-1])));
        //ti = 0.5f*(val[p+1]-val[p-1]);
        return ti;
    }

    public double calculateOneTo(int p)
    {
        double to;
        //to = (((1-tension[p])*(1-continuity[p])*(1-bias[p])/2*(val[p+1]-val[p])/delta[p]))+(((1-tension[p])*(1+continuity[p])*(1-bias[p]))/2)*((val[p]-val[p-1]))/delta[p-1];
        to = (2*delta[p-1]/(delta[p]+delta[p-1]))*((((1-tension[p])*(1-continuity[p])*(1-bias[p])/2*(val[p+1]-val[p])))+(((1-tension[p])*(1+continuity[p])*(1-bias[p]))/2)*((val[p]-val[p-1])));
        //to = 0.5f*(val[p+1]-val[p-1]);
        return to;
    }

    //public double[] getOutputStream(float duration, float timeStep)
    //public double[] getOutputStream(int nbOutput, float timeStep)
    public double[] getOutputStream(double timeStart, float timeStep)
    {
        //int nbOuput = (int) (duration / timeStep);
        //nbOuput = nbOuput + 1;
        //double[] output = new double[nbOutput];
        //float timeTemp = time[0];

        double timeTemp = timeStart;
        int nbOutput = 0;
        //System.out.println(" out of while structure  "+ timeTemp + " < " + +time[time.length-1]);
        while(timeTemp <= time[time.length-1])
        {
            //System.out.println(" "+ timeTemp + " < " + +time[time.length-1]);
            nbOutput++;
            timeTemp = timeTemp + timeStep;
        }
        //System.out.println("nbOutput = " + nbOutput);
        double[] output = new double[nbOutput];

        int seg = 0;
        timeTemp = timeStart;


        for (int f = 0; f < output.length; f++)
        {
             timeTemp = timeStart + f * timeStep;

                      while (timeTemp > time[seg+1])
                      {
                          seg ++ ; //
                      }
                      output[f] = A[seg] + ((timeTemp-time[seg])/delta[seg])*B[seg]+Math.pow((double)(timeTemp-time[seg])/delta[seg],2.0)*C[seg]+Math.pow((double)(timeTemp-time[seg])/delta[seg],3.0)*D[seg];

                      //double segTime = (timeTemp-time[seg])/delta[seg];
                      //System.out.println("segTimePorportion = "+segTime+ "  timeTemp = " + timeTemp + "time[seg] = " + time[seg] + "    delta[seg] = "+delta[seg]);
                      //System.out.println("A[seg] = " + A[seg] + " = " + " Pi " + val[seg]);
                      //double ABCD = A[seg]+B[seg]+C[seg]+D[seg];
                      //System.out.println("A+B+C+D = " + ABCD + " = " + val[seg+1] + "A,B,C,D="+ A[seg] + " "+B[seg]+ " "+C[seg]+ " "+D[seg]);
                      //System.out.println("output = " + output[f] );
        }

        this.timeEnd = timeTemp;
        return output;

    }


}
