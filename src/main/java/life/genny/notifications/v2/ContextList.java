package life.genny.notifications.v2;

import java.util.HashMap;
import java.util.Map;

import life.genny.qwanda.attribute.EntityAttribute;

public class ContextList {
	
	private Map<String, String> contexts = new HashMap< >();
	
	
	public void addContext(EntityAttribute entityAttribute) {
 		contexts.put(entityAttribute.getAttributeCode(), entityAttribute.getAsString());
	}
	
	public void addContext(String key, String value) {
		contexts.put(key, value);
	}
	 
	
	public String getEntityAttribute(String code) {
		return contexts.get(code);
	}
	
	public void print() {
		contexts.forEach((k,v) ->{
			System.out.println(k+" : "+v);
		} );
	} 
}
