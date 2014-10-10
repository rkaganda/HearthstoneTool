package rk.hearthstone.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HearthstoneCardZone {
	protected Map<String,HearthstoneCard> cards;
	protected String zoneName;
	protected List<HearthstoneCardZoneListener> listeners;
	
	public HearthstoneCardZone(String n) {
		cards = new HashMap<String,HearthstoneCard>();
		listeners = new ArrayList<HearthstoneCardZoneListener>();
		zoneName = n;
	}
	
	public void addListener(HearthstoneCardZoneListener l) {
		listeners.add(l);
	}
	
	public boolean addCard(HearthstoneCard card) {
		boolean addOK = false;
		
		if(!cards.containsKey(card.cardAttributes.get("id"))) { //check if zone already contains card with same id
			cards.put(card.getAttributes().get("id"), card);
			
			for(HearthstoneCardZoneListener l:listeners) {
				l.cardAdded(card);	//notify listener
			}
			addOK = true; //add finished
		}
		return addOK;
	}
	
	public HearthstoneCard removeCard(String cardId) {
		HearthstoneCard removedCard = cards.remove(cardId);
		if(removedCard!=null) { //if card existed
			for(HearthstoneCardZoneListener l:listeners) {
				l.cardRemoved(removedCard);	//notify listener
			}
		}
		return removedCard;
	}
	
	public void clearZone() {
		cards.clear();
		
	}

	public String getName() {
		return zoneName;
	}
}
