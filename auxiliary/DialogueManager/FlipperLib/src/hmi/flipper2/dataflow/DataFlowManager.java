package hmi.flipper2.dataflow;

import hmi.flipper2.TemplateController;
import hmi.flipper2.TemplateFile;

public class DataFlowManager {

	TemplateController tc = null;
	
	public DataFlowManager(TemplateController tc) {
		this.tc = tc;
	}
	
	public String analyze() {
		for(TemplateFile tf: tc.tf_list) {
			tf.flowIn();
			tf.flowOut();
		}
		return "INCOMPLETE";
	}
}
