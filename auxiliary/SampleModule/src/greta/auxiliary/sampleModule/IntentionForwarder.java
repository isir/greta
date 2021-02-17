/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.sampleModule;

import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Philippe Gauthier <philippe.gauthier@sorbonne-universite.fr>
 */
public class IntentionForwarder implements IntentionPerformer, IntentionEmitter {

     protected List<IntentionPerformer> performerList = new ArrayList<>();
     
    @Override
    public void performIntentions(List<Intention> list, greta.core.util.id.ID id, greta.core.util.Mode mode) {
        for(IntentionPerformer sp : performerList){
            System.out.println(String.format("IntentionForwarder: sending %s to performer %s",id.toString(),sp.toString()));
            sp.performIntentions(list, id, mode);
        }
    }

    @Override
    public void addIntentionPerformer(IntentionPerformer ip) {
        performerList.add(ip);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer ip) {
        performerList.remove(ip);
    }
    
}
