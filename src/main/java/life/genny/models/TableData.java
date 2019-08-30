package life.genny.models;

import java.io.Serializable;
import java.util.List;

import life.genny.qwanda.Ask;
import life.genny.qwanda.message.QDataBaseEntityMessage;

public class TableData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<QDataBaseEntityMessage> themeMsgList ;
	Ask ask;
	/**
	 * @param themeMsgList
	 * @param ask
	 */
	public TableData(List<QDataBaseEntityMessage> themeMsgList, Ask ask) {
		this.themeMsgList = themeMsgList;
		this.ask = ask;
	}
	/**
	 * @return the themeMsgList
	 */
	public List<QDataBaseEntityMessage> getThemeMsgList() {
		return themeMsgList;
	}
	/**
	 * @param themeMsgList the themeMsgList to set
	 */
	public void setThemeMsgList(List<QDataBaseEntityMessage> themeMsgList) {
		this.themeMsgList = themeMsgList;
	}
	/**
	 * @return the ask
	 */
	public Ask getAsk() {
		return ask;
	}
	/**
	 * @param ask the ask to set
	 */
	public void setAsk(Ask ask) {
		this.ask = ask;
	}
	@Override
	public String toString() {
		return "TableData [themeMsgList=" + themeMsgList + ", ask=" + ask + "]";
	}
	
	
}
