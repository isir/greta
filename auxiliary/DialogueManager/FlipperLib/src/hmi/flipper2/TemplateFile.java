package hmi.flipper2;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import hmi.flipper2.dataflow.DataFlow;
import hmi.flipper2.effect.EffectList;
import hmi.flipper2.effect.JavaEffect;
import hmi.flipper2.javascript.JsStringValue;
import hmi.flipper2.javascript.JsValue;
import hmi.flipper2.postgres.Database;
import hmi.flipper2.sax.SimpleElement;
import hmi.flipper2.sax.SimpleSAXParser;

public class TemplateFile extends FlipperObject {
	
	public int    tfid = -1;
	public long sync_is;
	public String path;
	public String xml_str;
	public TemplateController tc;
	private SimpleElement xml_root;
	
	public TemplateFile(TemplateController tc, String path, String xml_str, String db_is_value, long sync_is) throws FlipperException  {
		super((new File(path)).getName());
		this.tc = tc;
		this.path = path;
		this.xml_str = xml_str;
		this.sync_is = sync_is;
		this.xml_root = SimpleSAXParser.parseString(this.path, xml_str);
		for (int i = 0; i < this.xml_root.children.size(); i++) {
			handle_section(this.xml_root.children.get(i));
		}
		activate(db_is_value);
	}
	
	public List<String> init_js = new ArrayList<String>();
	
	private void handle_section(SimpleElement el) throws FlipperException {
		if (el.tag.equals("is")) {
			handle_is(el);
		} else if (el.tag.equals("database")) {
			handle_database(el);
		} else if (el.tag.equals("javascript")) {
			String js = el.characters.toString();
			this.init_js.add(js);
			this.tc.is.execute(js);
		} else if (el.tag.equals("template")) {
			handle_template(el);
		} else
			throw new RuntimeException("INCOMPLETE: "+el.tag);
	}
	
	public String is_name = null;
	public boolean is_updated = false;
	public String is_json_value = null;
	
	private void handle_is(SimpleElement el) {
		if ( is_name != null ) 
			throw new RuntimeException("INCOMPLETE: multiple is");
		this.is_name = el.attr.get("name");
		this.is_json_value = el.characters.toString();
	}
	
	public List<String> db_init_sql = new ArrayList<String>();
	public EffectList db_init_java = new EffectList();
	
	public List<String> db_cleanup_sql = new ArrayList<String>();
	public EffectList db_cleanup_java = new EffectList();
	
	private void handle_database(SimpleElement el) throws FlipperException {
		for (SimpleElement db_el : el.children) {
			if ( db_el.tag.equals("init")) {
				for (SimpleElement db_iel : db_el.children) {
					if (db_iel.tag.equals("function") || db_iel.tag.equals("method")) {
						this.db_init_java.add(Template.handle_effect(null, tc.is, db_iel));
					} else if (db_iel.tag.equals("sql")) {
						this.db_init_sql.add(db_iel.characters.toString());
					} else
						throw new FlipperException("UNEXPECTED db:init tag: "+db_iel.tag);
				}
			} else if ( db_el.tag.equals("cleanup")) {
					// INCOMPLETE
			} else
				throw new RuntimeException("INCOMPLETE: bad database");
		}
	}
	
	List<Template> templates = new ArrayList<Template>();
	
	private void handle_template(SimpleElement el) throws FlipperException {
		Template new_t = new Template(this, el);
		templates.add( new_t );
	}
	
	/*
	 * 
	 */
	
	private void activate(String db_is_value) throws FlipperException {
		tc.is.activate_tf(this, (db_is_value==null?this.is_json_value:db_is_value));	
		if ( (this.db_init_sql.size() > 0) || (this.db_init_java.size() > 0) ) {
			Database db = tc.is.getDatabase();
			if ( db == null )
				throw new FlipperException("<db> section in database without default Database");
			if ( this.db_init_sql != null ) {
				for(String sql : this.db_init_sql)
					db.executeScript(sql);
			} if ( this.db_init_java != null )
				this.db_init_java.doIt(tc.is);
		}
	}
	
	public void deactivate() throws FlipperException {
		tc.is.deactivate_tf(this);
		if ((this.db_cleanup_sql.size() > 0) || (this.db_cleanup_java.size() > 0)) {
			Database db = tc.is.getDatabase();
			if (db == null)
				throw new FlipperException("<db> section in database without default Database");
			if (this.db_cleanup_sql != null) {
				for (String sql : this.db_cleanup_sql)
					db.executeScript(sql);
			}
			if (this.db_cleanup_java != null)
				this.db_cleanup_java.doIt(tc.is);
		}
	}
	
	/*
	 * 
	 * 
	 */
	
	public JsValue isValue() {
		return new JsStringValue("INCOMPLETE");
	}
	
	public String toString() {
		return "TemplateFile["+id()+"]";
	}
	
	public Set<TemplateFile> dfin_tf = null;
	public Set<TemplateFile> dfout_tf = null;
	
	private Set<TemplateFile> is_var2templatefile(Set<String> dfx) {
		Set<TemplateFile> res = new HashSet<TemplateFile>();
		for(String is_var : dfx) { 
			TemplateFile tfdep = tc.is.tf_from_is_var(is_var);
			if ( tfdep != null )
				res.add(tfdep);	
		}
		return res;
	}
	
	public Set<String> flowIn() {
		Set<String> res = new HashSet<String>();
		for(String js : this.init_js)
			res.addAll(DataFlow.extractRefs(js));
		res.addAll(this.db_init_java.flowIn());
		res.addAll(this.db_cleanup_java.flowIn());
		for(Template t : this.templates)
			res.addAll(t.flowIn());
		this.dfin = res;
		this.dfin_tf = is_var2templatefile(res);
		System.out.println("TF-IN["+id()+"]="+dfin_tf);
		return res;
	}
	
	public Set<String> flowOut() {
		Set<String> res = new HashSet<String>();
		res.addAll(this.db_init_java.flowOut());
		res.addAll(this.db_cleanup_java.flowOut());
		for(Template t : this.templates)
			res.addAll(t.flowOut());
		this.dfout = res;
		this.dfout_tf = is_var2templatefile(res);
		System.out.println("TF-OUT["+id()+"]="+dfout_tf);
		return res;
	}
}
