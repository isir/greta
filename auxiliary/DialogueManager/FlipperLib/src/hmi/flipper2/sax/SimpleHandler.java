package hmi.flipper2.sax;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SimpleHandler extends DefaultHandler {

	private Vector<SimpleElement> stack = null;
	
	private void push(SimpleElement el) {
		if ( stack.size() > 0 )
			top().addChild(el);		
		stack.addElement(el);
	}
	
	private SimpleElement top() {
		return stack.lastElement();
	}
	
	private SimpleElement pop() {
		SimpleElement top = top();
		stack.remove(top);
		return top;
	}
	
	public  SimpleElement root = null;
	
    public void startDocument() throws SAXException {
        stack =  new Vector<SimpleElement>();
        push(new SimpleElement("ROOT",null));
    }

    public void endDocument() throws SAXException {
    	if ( top().children.size() != 1 )
    		throw new RuntimeException("UNEXPECTED");
    	this.root = top().children.firstElement();
    	stack = null;
    	
    }

    public void startElement(String uri, String localName,
        String qName, Attributes attributes)
    throws SAXException {
    	push(new SimpleElement(qName, attributes));
    }

    public void endElement(String uri, String localName, String qName)
    throws SAXException {
    	pop();
    }

    public void characters(char ch[], int start, int length)
    throws SAXException {
    	top().addCharacters(ch, start, length);
    }

    //
    private String getParseExceptionInfo(SAXParseException spe) {
        String systemId = spe.getSystemId();

        if (systemId == null) {
            systemId = "null";
        }

        String info = "URI=" + systemId + " Line=" 
            + spe.getLineNumber() + ": " + spe.getMessage();

        return info;
    }
    
    public void warning(SAXParseException spe) throws SAXException {
        System.out.println("Warning: " + getParseExceptionInfo(spe));
    }
        
    public void error(SAXParseException spe) throws SAXException {
        String message = "Error: " + getParseExceptionInfo(spe);
        throw new SAXException(message);
    }

    public void fatalError(SAXParseException spe) throws SAXException {
        String message = "Fatal Error: " + getParseExceptionInfo(spe);
        throw new SAXException(message);
    }
}
