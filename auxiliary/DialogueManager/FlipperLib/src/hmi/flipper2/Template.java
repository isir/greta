package hmi.flipper2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hmi.flipper2.conditions.ConditionList;
import hmi.flipper2.conditions.JavaCondition;
import hmi.flipper2.conditions.JsCondition;
import hmi.flipper2.dataflow.DataFlow;
import hmi.flipper2.effect.AssignEffect;
import hmi.flipper2.effect.BehaviourJavaEffect;
import hmi.flipper2.effect.DerefAssignEffect;
import hmi.flipper2.effect.DirectAssignEffect;
import hmi.flipper2.effect.Effect;
import hmi.flipper2.effect.EffectList;
import hmi.flipper2.effect.FunctionJavaEffect;
import hmi.flipper2.effect.JavaEffect;
import hmi.flipper2.effect.MethodJavaEffect;
import hmi.flipper2.effect.SystemEffect;
import hmi.flipper2.effect.TemplateEffect;
import hmi.flipper2.javascript.JsExpression;
import hmi.flipper2.sax.SimpleElement;
import hmi.flipper2.value.ConstantJavaValue;
import hmi.flipper2.value.DbJavaValue;
import hmi.flipper2.value.IsJavaValue;
import hmi.flipper2.value.JavaValueList;
import hmi.flipper2.value.PersistentJavaValue;

public class Template extends FlipperObject {

	public TemplateFile tf;
	
	public String priv_name;

	public Template(TemplateFile tf, SimpleElement el) throws FlipperException {
		super(el.attr.get("id"));
		this.tf = tf;
		this.priv_name = el.attr.get("name");
		String conditionalString = el.attr.get("conditional");
		if ( conditionalString != null && conditionalString.toLowerCase().equals("true"))
			this.conditional = true;
		tf.tc.registerCurrentTemplate(tf.id(), this.id(), this.priv_name);
		for (SimpleElement coe : el.children) {
			if (coe.tag.equals("preconditions")) {
				handle_preconditions(coe);
			} else if (coe.tag.equals("initeffects")) {
				listOfInitializeEffectList.add(handle_effects(coe));
			} else if (coe.tag.equals("effects")) {
				listOfEffectList.add(handle_effects(coe));
			} else if (coe.tag.equals("javascript")) {
				String js = coe.characters.toString();
				this.init_js.add(js);
				this.tf.tc.is.execute(js);
			} else
				throw new RuntimeException("INCOMPLETE: bad template: "+coe.tag);
		}
	}
	
	/**
	 * 
	 */
	
	public boolean conditional	= false;
	public Template next_conditional	= null; // used for building a stack of conditional Templates
	
	public Template push(Template oldtop) {
		this.next_conditional = oldtop;
		return this; // the new top
	}
	
	public Template pop() {
		Template res = this.next_conditional;
		this.next_conditional = null;	
		return res;
	}
	
	public Template popfirst() {
		if (this.next_conditional == null)
			return null;
		else {
			Template t = this.next_conditional, prev_t = null;
			while (t.next_conditional != null) {
				prev_t = t;
				t = t.next_conditional;
			}
			if (prev_t == null)
				this.next_conditional = null;
			else
				prev_t.next_conditional = null;
			return t;
		}
	}
	
	/**
	 * 
	 */

	ConditionList preconditions = null;
	
	public List<String> init_js = new ArrayList<String>();
	
	private void handle_preconditions(SimpleElement el) throws FlipperException {
		preconditions = new ConditionList(el.attr.get("mode"));
		for (SimpleElement pc : el.children) {
			if (pc.tag.equals("condition")) {
				preconditions.add(new JsCondition(pc.attr.get("id"), new JsExpression(tf.tc.is,"",pc.characters.toString(),"return %s")));
			} else if (pc.tag.equals("function") || pc.tag.equals("method")) {
				preconditions.add(new JavaCondition(pc.attr.get("id"), (JavaEffect)handle_effect(this, tf.tc.is, pc)));
			} else if (pc.tag.equals("javascript")) {
				String js = pc.characters.toString();
				this.init_js.add(js);
				this.tf.tc.is.execute(js);
			} else
				throw new RuntimeException("INCOMPLETE: bad precondition: "+pc.tag);	
		}
		if ( preconditions.size() == 0 )
			throw new FlipperException("preconditions list cannot be empty");
	}

	List<EffectList> listOfInitializeEffectList = new ArrayList<EffectList>();
	List<EffectList> listOfEffectList = new ArrayList<EffectList>();
	
	private EffectList handle_effects(SimpleElement el) throws FlipperException {
		String a_effect_mode = el.attr.get("mode");
		String a_dynamic_mode = el.attr.get("dynamic");
		EffectList effects = new EffectList(
								( a_effect_mode != null && a_effect_mode.equals("weighted")),
								( a_dynamic_mode != null && a_dynamic_mode.equals("true"))
						);
		for (SimpleElement ee : el.children) {
			if ( ee.tag.equals("javascript")) {
				String js = ee.characters.toString();
				this.init_js.add(js);
				this.tf.tc.is.execute(js);
			} else 
				effects.add(handle_effect(this, tf.tc.is, ee));
		}
		return effects;
	}
	
	public static Effect handle_effect(Template template, Is is, SimpleElement ee) throws FlipperException {
		Effect result = null;
		
		String id;
		
		if ( template == null )
			id = "DB-TEMPLATE";
		else
			// id = template.id()+":"+template.name;
			id = ee.attr.get("id");
		String effect_weight = ee.attr.get("weight");
	    if (ee.tag.equals("assign")) {
	    	String is_name = ee.characters.toString();
	    	if ( is_name.startsWith("*"))
	    		result = new DerefAssignEffect(id, ee.attr.get("is"), is_name);
	    	else {
	    		if ( ee.attr.get("raw")!=null && ee.attr.get("raw").toLowerCase().equals("true"))
	    			result = new DirectAssignEffect(id, ee.attr.get("is"), is_name);
	    		else
	    			result = new AssignEffect(id, ee.attr.get("is"), is_name);
	    	}
		} else if (ee.tag.equals("db")) {
			throw new RuntimeException("INCOMPLETE: DB ELEMENT: " + ee);
		} else if (ee.tag.equals("checktemplates")) {
			result = new TemplateEffect(id, ee.attr.get("regexpr"),ee.attr.get("isregexpr"));
		} else if (ee.tag.equals("function") || ee.tag.equals("method") || ee.tag.equals("behaviour")) {
			boolean isFunction = ee.tag.equals("function");
			String a_is = ee.attr.get("is");
			String a_is_type = ee.attr.get("is_type");
			String a_class = ee.attr.get("class");
			if ( !isFunction && a_class != null )
				throw new FlipperException("OLD SYNTAX class attribute must be specified in <object> not <method> or <behaviour>");
			String a_name = ee.attr.get("name");
			String a_mode = ee.attr.get("mode");
			JavaValueList arguments = null;
			String a_persistent = null;
			JavaValueList constructors = null;
			for (SimpleElement lists : ee.children) {
				if (lists.tag.equals("arguments"))
					arguments = handle_value_list(template, is, lists);
				else if (lists.tag.equals("constructors"))
					throw new FlipperException("OLD SYNTAX <constructors> tag must be inside <object>");
				else if (lists.tag.equals("object")) {
					a_class = lists.attr.get("class");
					a_persistent = lists.attr.get("persistent");
					for (SimpleElement constr : lists.children) {
						if ( constr.tag.equals("constructors") ) {
							constructors = handle_value_list(template, is, constr);
						} else
							throw new FlipperException("bad tag inside <object> tag: " + constr.tag);
					}
				}
				else
					throw new FlipperException("INCOMPLETE: bad tag: " + lists.tag);
			}
			Effect newEffect = null;
			if (ee.tag.equals("function")) {
				newEffect = new FunctionJavaEffect(id, template, a_is, a_is_type, a_class, a_name, arguments);
			} else if (ee.tag.equals("method")) {
				newEffect = new MethodJavaEffect(id, template, a_is, a_is_type, a_class, a_persistent, constructors, a_name, arguments, a_mode);
			} else if (ee.tag.equals("behaviour")) {
				newEffect = new BehaviourJavaEffect(id, template, a_is, a_is_type, a_class, a_persistent, constructors, a_name, arguments, a_mode);
			}
			result = newEffect;
		} else if (ee.tag.equals("system")) {
			result = new SystemEffect("system", ee.attr.get("command"),ee.attr.get("arg"));
		} else
			throw new FlipperException("UNKNOWN effect: " + ee.tag);
	    if (effect_weight != null)
			result.setWeight(effect_weight);
	    return result;
	}
	
	private static JavaValueList handle_value_list(Template template, Is is, SimpleElement list) throws FlipperException {
		JavaValueList jvl = new JavaValueList();	
		for (SimpleElement val : list.children) {
			if (  val.attr.get("constant") != null || // Value of constant defined in body:
					(val.attr.get("class") != null && val.characters != null && val.characters.length() > 0)) { 
				if (val.attr.get("constant") != null) {
					jvl.add( new ConstantJavaValue(val.attr.get("class"),val.attr.get("constant")));
				} else {
					jvl.add( new ConstantJavaValue(val.attr.get("class"), val.characters.toString()));
				}
			} else if (  val.attr.get("system") != null ) {	
				String sys = val.attr.get("system");	
				if ( sys.equals("template_id")) {	
					if ( template == null )
						throw new FlipperException("ERROR: use "+sys+" system variable outside Template");
					jvl.add( new ConstantJavaValue("String",template.id()));
				} else if ( sys.equals("template_name")) {
					if ( template == null )
						throw new FlipperException("ERROR: use "+sys+" system variable outside Template");
					jvl.add( new ConstantJavaValue("String",template.id()));
				} else 
					throw new FlipperException("ERROR: bad system value: "+sys);
			} else if (  val.attr.get("is") != null ) {
				jvl.add( new IsJavaValue(is, val.attr.get("is"), val.attr.get("is_type"),val.attr.get("class")));
			} else if (  val.attr.get("db") != null ) {
				jvl.add( new DbJavaValue(is, val.attr.get("db")));
			} else if (  val.attr.get("persistent") != null ) {
				jvl.add( new PersistentJavaValue(template, val.attr.get("class"), val.attr.get("persistent")));
			} else 
				throw new FlipperException("INCOMPLETE: bad value tag: "+val.tag);
		}
		return jvl;
	}
	
	public boolean checkPreconditions(Is is, boolean executeEffectImmediate) throws FlipperException {
		boolean res;
		
		if ( preconditions.checkIt(is) ) {
			if ( executeEffectImmediate )
				executeSelectedEffects(is);
			res = true;
		} else
			res = false;
		return res;
	}
	
	public void executeSelectedEffects(Is is) throws FlipperException {
		if ( preconditions.lastCheck ) {
				for(EffectList effects: listOfEffectList)
					effects.doIt(is);
		}
	}
	
	public void doInitializeEffects() throws FlipperException {
		for(EffectList effects: listOfInitializeEffectList)
			effects.doIt(this.tf.tc.is);		
	}
	
	public Set<String> flowIn() {
		Set<String> res = new HashSet<String>();
		for(String js : this.init_js)
			res.addAll(DataFlow.extractRefs(js));
		res.addAll(preconditions.flowIn());
		for(EffectList l : listOfInitializeEffectList)
			res.addAll(l.flowIn());
		for(EffectList l : listOfEffectList)
			res.addAll(l.flowIn());
		this.dfin = res;
		System.out.println("TEMPLATE-IN["+id()+"]="+dfin);
		return res;
	}
	
	public Set<String> flowOut() {
		Set<String> res = new HashSet<String>();
		res.addAll(preconditions.flowOut());
		for (EffectList l : listOfInitializeEffectList)
			res.addAll(l.flowOut());
		for (EffectList l : listOfEffectList)
			res.addAll(l.flowOut());
		this.dfout = res;
		System.out.println("TEMPLATE-OUT["+id()+"]="+dfout);
		return res;
	}
	
}
