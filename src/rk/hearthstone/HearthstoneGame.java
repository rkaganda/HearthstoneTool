package rk.hearthstone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HearthstoneGame {
	ArrayList<String> opposingDeck;
	ArrayList<String> friendlyDeck;
	ArrayList<String> opposingHand;
	ArrayList<String> friendlyHand;
	
	HearthTool theTool;
	
	public HearthstoneGame(HearthTool tool) {
		theTool = tool;
	}
	
	public static Map<String,String> parseEvent(String s) {
		System.out.println(s);
		HashMap<String,String> event = new HashMap<String,String>();
		s = s.substring(s.indexOf("[Zone]")+6);
		//String some = "";
		if(s.contains("] zone from")) {
			event.put("type","move");
			String bracket = s.substring(s.indexOf("["),s.indexOf("]"));
			if(bracket.contains("name=")) {
				event.put("name", bracket.substring(bracket.indexOf("name=")+5,bracket.indexOf("id=")));
			}
			//Integer id = Integer.parseInt(bracket.substring(s.indexOf("id=")+3, bracket.indexOf("zone=")));
			
			event.put("from", s.substring(s.indexOf("] zone from ")+12, s.indexOf("-> ")));
			
			if(s.indexOf("-> ")+3!=s.length()-1) {
				event.put("to", s.substring(s.indexOf("-> ")+3,s.length()));
			}else {
				event.put("to","");
			}
		}
		return event;
	}

	public void handleEvent(Map<String, String> event) {
		if(event.containsKey("name") && event.containsKey("to") ) {
			if(isHero(event.get("name"))  && event.get("to").equals("FRIENDLY PLAY (Hero)")) {
				
			}
		}
		if(event.containsKey("name") && event.containsKey("from") && event.containsKey("to") ) {
			if( event.get("from").trim().equals("FRIENDLY HAND") &&
				(event.get("to").trim().equals("") || 
						event.get("to").trim().equals("FRIENDLY PLAY") || 
						event.get("to").trim().equals("FRIENDLY PLAY (Weapon)"))) {
				theTool.friendlyPlayed(event);
			}else if( event.get("from").trim().equals("OPPOSING HAND")  && 
					(event.get("to").trim().equals("") || 
							event.get("to").trim().equals("OPPOSING PLAY") ||
							event.get("to").trim().equals("OPPOSING PLAY (Weapon)"))) {
				theTool.opposingPlayed(event);
			}
		}
	}
	
	protected boolean isHero(String s) {
		boolean isHero = false;
		if(s.trim().equals("Garrosh Hellscream")) {
			isHero = true;
		}else if(s.trim().equals("Rexxar")) {
			isHero = true;
		}
		return isHero;
	}
}
