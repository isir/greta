/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.FlipperDemo.dm.managers;



import java.util.concurrent.atomic.AtomicLong;

/**

 *
 */
public class Say {
    private static final AtomicLong ID = new AtomicLong(0);

    private boolean typed;
    private long id;
    private long timestamp;
    private String text;
    private String actorName;
    private long length;
    private String language;
    private boolean talking = false;
	public String strategy;
    /*public String restpose;
	public String gesture;
	public String smile;
	public String _text;*/
	
    /*
    public boolean random;
    public double reward;
    public double[] qTable;*/

    public Say(){
        id = ID.incrementAndGet();
        this.text = null;
        this.timestamp = 0;
        this.length = 0;
        this.typed = false;
        this.talking = false;
        this.actorName = null;
        this.language = null;
		this.strategy = "";
        /*this.restpose="";
		this.gesture="";
		this.smile="";
		this._text="";*/
        /*
        this.random = true;
        this.reward =0.0;   */
    }

    public Say(String text, String actorName, boolean typed) {
        id = ID.incrementAndGet();
        this.text = text.trim();
        this.timestamp = System.currentTimeMillis();
        this.length = 0;
        this.typed = typed;
        this.talking = false;
        this.actorName = actorName;
        this.language = "English";
    }

    public Say(String text, String actorName, boolean typed, long timestamp, long duration, boolean isTalking, String language) {
        id = ID.incrementAndGet();
        this.text = text.trim();
        this.timestamp = timestamp;
        this.length = duration;
        this.typed = typed;
        this.actorName = actorName;
        this.talking = isTalking;
        this.language = language;
    }
    public long getId() {
        return id;
    }
    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getLength(){
        return length;
    }

    public boolean isTyped() {
        return typed;
    }

    public String getActorName(){
        return actorName;
    }

    public String getLanguage() { return language; }

    public boolean isTalking(){
        return talking;
    }

    @Override
    public String toString() {
        return "\""+getActorName()+"\":{ \"id\":" + getId() + ", \"timestamp\":" + getTimestamp() + ", \"duration\":" + this.getLength() + " \"text\":\"" + getText() + "\" }";
    }

    public void setTyped(boolean typed) {
        this.typed = typed;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setTalking(boolean talking) {
        this.talking = talking;
    }
}
