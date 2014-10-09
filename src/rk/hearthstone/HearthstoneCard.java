package rk.hearthstone;

import java.util.HashMap;
import java.util.Map;

public class HearthstoneCard {
	protected Map<String,String> cardProperties;
	
	
	public HearthstoneCard() {
		cardProperties = new HashMap<String,String>();
		
		cardProperties.put("name","unknown");
	}
	
	public HearthstoneCard(String n) {
		cardProperties.put("name",n);
	}
}
