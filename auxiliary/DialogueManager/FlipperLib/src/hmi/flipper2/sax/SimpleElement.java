package hmi.flipper2.sax;

import java.util.HashMap;
import java.util.Vector;

import org.xml.sax.Attributes;

public class SimpleElement {

	public String tag;
	public HashMap<String,String> attr;
	public Vector<SimpleElement> children;
	public StringBuffer characters;
	
	
    public SimpleElement(String tag, Attributes attr) {
    	this.tag = tag;
    	this.attr = new HashMap<String, String>();
    	if ( attr != null )
    		for (int i = 0; i < attr.getLength(); i++) {
    			this.attr.put(attr.getQName(i), attr.getValue(i));
    	}
    	this.children = new Vector<SimpleElement>();
    	this.characters =  new StringBuffer();
    }
    
    public void addChild(SimpleElement ch) {
    	this.children.addElement(ch);
    }
    
    public void addCharacters(char ch[], int start, int length) {
    	this.characters.append(ch, start, length);
    }
    
	public String toString() {
		StringBuffer b = new StringBuffer();

		b.append("<" + tag);
		for (String key : this.attr.keySet()) {
		    b.append(',');
		    b.append(key);
		    b.append('=');
		    b.append('"');
		    b.append(attr.get(key));
		    b.append('"');
		}
		b.append(",children{");
		for (int j = 0; j < this.children.size(); j++) {
			b.append(" " + this.children.get(j).tag);
		}
		b.append(" }");
		b.append(">");
		return b.toString();
	}
    
}
