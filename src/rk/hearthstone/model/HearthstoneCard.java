package rk.hearthstone.model;

import java.util.HashMap;
import java.util.Map;

public class HearthstoneCard {
	protected Map<String,String> cardAttributes;
	
	
	public HearthstoneCard(Map<String,String> event) {
		cardAttributes = new HashMap<String,String>();
		cardAttributes.put("id", event.get("id"));
	}
	
	public String get(String s) {
		return cardAttributes.get(s);
	}
	
	public void put(String k,String v) {
		cardAttributes.put(k, v);
	}
}
