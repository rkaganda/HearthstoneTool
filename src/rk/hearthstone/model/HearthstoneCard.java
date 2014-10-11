package rk.hearthstone.model;

import java.util.HashMap;
import java.util.Map;

public class HearthstoneCard {
	protected Map<String,String> cardAttributes;
	
	
	public HearthstoneCard(Map<String,String> event) {
		cardAttributes = new HashMap<String,String>();
		cardAttributes.put("id", event.get("id"));
		
		if(event.get("from").equals("unknown") &&
				( event.get("to").equals("FRIENDLY PLAY") ||
				event.get("to").equals("OPPOSING PLAY")) ) {
			cardAttributes.put("summon", "true");
		}else {
			cardAttributes.put("summon", "false");
		}
	}
	
	public String get(String s) {
		return cardAttributes.get(s);
	}
	
	public void put(String k,String v) {
		cardAttributes.put(k, v);
	}
}
