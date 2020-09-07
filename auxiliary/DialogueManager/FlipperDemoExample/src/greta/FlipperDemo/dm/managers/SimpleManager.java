/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.FlipperDemo.dm.managers;


public class SimpleManager  {

    protected long interval;
    protected long previousTime;
    protected String name;
    protected String id;
  

    public SimpleManager(){

    }

    public void process() {

    }

    public String getName() {
        return this.name;
    }


    public void setName(String name) {
        this.name = name;
    }

  
    public String getID() {
        return this.id;
    }

  
    public void setID(String id) {
        this.id = id;

    }
}
