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
package greta.core.behaviorplanner.lexicon;

/*
 * This class of {@code Shape} is used by the behavior planner to read data in the {@code Lexicon}.<br/>
 * It describes the main shape and the alternative shape of each intention in the lexicon.
 * It is used in the signal selection, see {@code MultimodalSignalSelector}.<br/>
 * It contains no temporal information like ({@code TimeMarkers}), but it contains
 * the min and max duration a signal can have. Those infos are defined in the lexicon.
 *
 * @author Elisabetta Bevacqua
 */
public class Shape {

    private String name;
    private double min;
    private double max;
    private String content;
    private String intonation;
    private String voicequality;
    private String meaning;
    private double probability;

    //additional fields for MSE
    private int repetivity;
    private double probability_start;
    private double probability_stop;
    private String excludelist;


    /**
     * Construct a {@code Shape} specifying the name (or id).
     * @param shapeName the id of this {@code Shape}
     * @param min the minimum duration of this {@code Shape}
     * @param max the maximum duration of this {@code Shape}
     * @param content the speech content of this {@code Shape}
     * @param intonation the speech intonation of this {@code Shape}
     * @param voicequality the speech voicequality of this {@code Shape}
     * @param meaning the speech meaning of this {@code Shape}
     */
    public Shape(String shapeName, double min, double max, String content, String intonation, String voicequality, String meaning){
        this.name = shapeName;
        this.min = min;
        this.max = max;
        this.content = content;
        this.intonation = intonation;
        this.voicequality = voicequality;
        this.meaning = meaning;
        this.probability = 1;

    //in a case someone instist to use it
    this.repetivity=0;
    this.probability_start=1.0d;
    this.probability_stop=1.0d;
    this.excludelist="";

    }

    public String getName(){
        return name;
    }

    public void setProbability(double probability){
        this.probability = probability;
    }

    public double getProbability(){
        return probability;
    }

    public double getMin(){
        return min;
    }

    public double getMax(){
        return max;
    }

    public String getContent(){
        return content;
    }

    public String getIntonation(){
        return intonation;
    }

    public String getVoicequality(){
        return voicequality;
    }

    public String getMeaning(){
        return meaning;
    }


    public void setProbability_Start(double probability_start){
        this.probability_start = probability_start;
    }

    public double getProbability_Start(){
        return probability_start;
    }

    public void setProbability_Stop(double probability_stop){
        this.probability_stop = probability_stop;
    }

    public double getProbability_Stop(){
        return probability_stop;
    }

    public void setRepetivity(int repetivity){
        this.repetivity = repetivity;
    }

    public int getRepetivity(){
        return repetivity;
    }

    public void setExcludeList(String excludelist){
        this.excludelist = excludelist;
    }

    public String getExcludeList(){
        return excludelist;
    }


}
