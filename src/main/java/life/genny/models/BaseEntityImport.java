package life.genny.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.vavr.Tuple2;

public class BaseEntityImport implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String code;
	String name;
	
	List<Tuple2<String,String>> attributeValuePairList = new ArrayList<Tuple2<String,String>>();

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the attributeValuePairList
	 */
	public List<Tuple2<String,String>> getAttributeValuePairList() {
		return attributeValuePairList;
	}

	/**
	 * @param attributeValuePairList the attributeValuePairList to set
	 */
	public void setAttributeValuePairList(List<Tuple2<String,String>> attributeValuePairList) {
		this.attributeValuePairList = attributeValuePairList;
	}

	@Override
	public String toString() {
		return "BaseEntityImport [" + (code != null ? "code=" + code + ", " : "")
				+ (name != null ? "name=" + name + ", " : "")
				+ (attributeValuePairList != null ? "attributeValuePairList=" + attributeValuePairList : "") + "]";
	}
	
	
}
