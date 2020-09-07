package hmi.flipper2.javascript;

public class JsStringValue extends JsValue {

		private String value;
		
		public JsStringValue(String value) {
			this.value = value;
		}
		
		public String stringValue() {
			return value;
		}
		
		public String toString() {
			return "JSV["+this.value+"]";
		}
}
