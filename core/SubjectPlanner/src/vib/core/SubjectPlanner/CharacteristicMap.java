package vib.core.SubjectPlanner;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import vib.core.util.parameter.EngineParameterSetOfSet;
import vib.core.util.log.Logs;
/**
 *
 * @author Nadine
 */
public class CharacteristicMap extends EngineParameterSetOfSet{
    
        public static final String CHAR_MAP = "SubjectPlanner/Data/CharacteristicMaps.xml";
    

    public static CharacteristicMap global_characteristic;
    
    
    public CharacteristicMap(String filename){
        //get the default Lexicon :
        super(filename);
  
    }
    
  //  public double GetCharacteristicDistance(String Characteristic){
  //      double CharacteristicDistance;
  //      CharacteristicDistance = CurrentCharacteristic - NewCharacteristic();
   //     return CharacteristicDistance;
    
   
    
}
