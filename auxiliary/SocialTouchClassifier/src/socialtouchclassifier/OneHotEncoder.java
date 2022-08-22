package socialtouchclassifier;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Michele
 */

import java.util.Arrays;
import java.util.HashMap;
public class OneHotEncoder {

    private int numberOfClasses;

    public OneHotEncoder(int numberOfClasses) {
        this.numberOfClasses = numberOfClasses;
    }

    public int getNumberOfClasses() {
        return numberOfClasses;
    }

    public int[] encode(int label) {
        int[] oneHot = new int[numberOfClasses];
        oneHot[label] = 1;
        return oneHot;
    }
    
    public int decode(int[] oneHot) {
        return Arrays.binarySearch(oneHot, 1);
    }
    
    String[] stringLabels = {"Sunday", "Monday", "Tuesday"};
/*
LabelEncoder<String> stringEncoder = new LabelEncoder<>(stringLabels);

int numClasses = stringEncoder.getClasses.size();

OneHotEncoder oneHotEncoder = new oneHotEncoder(numClasses);

for (Datum datum : data) {
    int classNumber = stringEncoder.encode(datum.getLabel);
    int[] oneHot    = oneHotEncoder.encode(classNumber);
    // do something with classes i.e. add to List or Matrix
}
    
    //REVERSE
    for(Integer[] prediction: predictions) {
    int classLabel = oneHotEncoder.decode(prediction);
    String label = labelEncoder.decode(classLabel); 
}
*/
}