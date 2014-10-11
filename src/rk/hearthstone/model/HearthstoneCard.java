package rk.hearthstone.model;

import java.util.HashMap;
import java.util.Map;

public class HearthstoneCard {
	protected Map<String,String> cardAttributes;
	
	
	public HearthstoneCard(Map<String,String> event) {
		cardAttributes = new HashMap<String,String>();
		cardAttributes.put("id", event.get("id"));
		
		if(event.get("type").equals("move")) { //if event type=move
			if(event.get("from").equals("unknown") && //if card from unknown to PLAY
					( event.get("to").equals("FRIENDLY PLAY") || 
					event.get("to").equals("OPPOSING PLAY")) ) {
				cardAttributes.put("summon", "true"); //card is summon
			}else {
				cardAttributes.put("summon", "false"); //card is not summon
			}
		}
	}
	
	public String get(String s) {
		return cardAttributes.get(s);
	}
	
	public void put(String k,String v) {
		cardAttributes.put(k, v);
	}
}
