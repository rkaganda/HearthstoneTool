package rk.hearthstone.model;

import java.util.HashMap;
import java.util.Map;

public class HearthstoneCard {
	protected Map<String,String> cardAttributes;
	
	
	public HearthstoneCard() {
		cardAttributes = new HashMap<String,String>();
	}
	
	public Map<String,String> getAttributes() {
		return cardAttributes;
	}
}
