package hmi.flipper2.javascript;

public class JsObjectValue extends JsValue {

		// incomplete, do some nashorn stuff here
		public Object value;
		
		public JsObjectValue(Object value) {
			this.value = value;
		}
		
		public String stringValue() {
			throw new RuntimeException("INCOMPLETE");
		}
		
		public String toString() {
			return "JSV["+this.value.toString()+"]";
		}
}
