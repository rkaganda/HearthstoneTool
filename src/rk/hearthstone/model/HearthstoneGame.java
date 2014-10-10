package rk.hearthstone.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rk.hearthstone.HearthTool;

public class HearthstoneGame {
	public final static int DISABLE = 0;
	public final static int WAITING_HERO_FRIENDLY = 1; //game has ended waiting for event type=move name=(Hero) from="" -> to="FRIENDLY PLAY" 
	public final static int EVENT_HERO_FRIENDLY_PLAY = 2; //occurs after event type=move name=(Hero) from="" -> to="FRIENDLY PLAY" 
	public final static int DEALING_FRIENDLY_DECK = 3; //game state while FRIENDLY DECK is dealt
	public final static int DEALING_OPPOSING_DECK = 5; 
	public final static int EVENT_HERO_GRAVEYARD = 100; // hero is in graveyard
	
	protected int gameState;
	
	//stores the card for each zone
	List<HearthstoneCardZone> zones;
	HearthstoneCardZone friendlyDeck;
	HearthstoneCardZone opposingDeck;
	HearthstoneCardZone friendlyHand;
	HearthstoneCardZone opposingHand;
	HearthstoneCardZone friendlyPlay;
	HearthstoneCardZone opposingPlay;
	HearthstoneCardZone friendlyGraveyard;
	HearthstoneCardZone opposingGraveyard;
	
	protected HearthTool theTool;
	
	public HearthstoneGame() {
		zones = new ArrayList<HearthstoneCardZone>(); //create zones
		friendlyDeck = new HearthstoneCardZone("friendlyDeck"); 
		opposingDeck = new HearthstoneCardZone("opposingDeck");
		friendlyHand = new HearthstoneCardZone("friendlyHand");
		opposingHand = new HearthstoneCardZone("opposingHand");
		friendlyPlay = new HearthstoneCardZone("friendlyPlay");
		opposingPlay = new HearthstoneCardZone("opposingPlay");
		friendlyGraveyard = new HearthstoneCardZone("friendlyGraveyard");
		opposingGraveyard = new HearthstoneCardZone("opposingGraveyard");
		
		zones.add(opposingDeck);
		zones.add(friendlyDeck);
		zones.add(opposingHand);
		zones.add(friendlyHand);
		zones.add(opposingPlay);
		zones.add(friendlyPlay);
		zones.add(opposingGraveyard);
		zones.add(friendlyGraveyard);
	}
	
	public HearthstoneGame(HearthTool tool) {
		this();
		theTool = tool;
		gameState = WAITING_HERO_FRIENDLY;
	}
	
	public static Map<String,String> parseEvent(String s) {
		HashMap<String,String> event = new HashMap<String,String>();
		s = s.substring(s.indexOf("[Zone]")+6); //remove [Zone]
		if(s.contains("] zone from")) { //parse event type=move params name, id, to, from
			event.put("type","move");
			String bracket = s.substring(s.indexOf("["),s.indexOf("]"));
			if(bracket.contains("name=")) { //parse name
				event.put("name", bracket.substring(bracket.indexOf("name=")+5,bracket.indexOf("id=")).trim());
			}else{
				event.put("name","unknown");
			}
			
			if(bracket.contains("id=")) { //parse id
				event.put("id", bracket.substring(bracket.indexOf("id=")+3,  // substring id= till whitespace
						bracket.substring(bracket.indexOf("id=")).indexOf(" ") + bracket.indexOf("id=")));
			}else {
				event.put("id","unknown");
			}
			
			//parse from
			event.put("from", s.substring(s.indexOf("] zone from ")+12, s.indexOf("-> ")).trim());
			if(event.get("from").equals("")) {
				event.put("from","unknown");
			}
			
			if(s.indexOf("-> ")+3!=s.length()-1) {
				event.put("to", s.substring(s.indexOf("-> ")+3,s.length()).trim());
			}else {
				event.put("to","unknown");
			}
		}
		return event;
	}
	
	
	
	public void handleEvent(Map<String, String> event) { 
		//TODO check if event has been handed over
		if(event.get("type").equals("move")) { //move events
			// if card is Hero
			if(isHero(event.get("name"))) { 
				if( event.get("to").equals("FRIENDLY PLAY (Hero)") // Hero -> FRIENDLY PLAY game start
						&& (gameState == WAITING_HERO_FRIENDLY 
						|| gameState == EVENT_HERO_GRAVEYARD) ) {
					doGameStart(event); //process starting game event
				}else if( event.get("to").equals("OPPOSING GRAVEYARD") || //Hero -> GRAVEYARD game over
						event.get("to").equals("FRIENDLY GRAVEYARD")) {
					doGameOver(event); //process game over event
				}
			}
		
			
			// move card from unknown to DECK
			if( event.get("from").equals("unknown") &&
				event.get("to").equals("FRIENDLY DECK")) {
					dealFriendlyDeck(event);
			}
			if( event.get("from").equals("unknown") &&
				event.get("to").equals("OPPOSING DECK")) {
					dealOpposingDeck(event);
			}
			
			// move unknown -> PLAY events
			if( event.get("from").equals("unknown") && 	// if card unknown -> FRIENDLY HAND
					event.get("to").equals("FRIENDLY HAND")) {
				moveUnknowToFriendlyHand(event);
			}else if( event.get("from").equals("unknown") && 	// if card unknown -> OPPOSING HAND
					event.get("to").equals("FRIENDLY HAND")) {
				moveUnknowToOpposingHand(event);
			}
			
			// move card DECK -> HAND events
			if( event.get("from").equals("FRIENDLY DECK") && 	// if card FRIENDLY DECK -> FRIENDLY HAND
					event.get("to").equals("FRIENDLY HAND")) {
				moveFriendlyDeckFriendlyHand(event);
			}else if( event.get("from").equals("OPPOSING DECK") && 	// if card OPPOSING DECK -> OPPOSING HAND
					event.get("to").equals("OPPOSING HAND")) {
				moveOpposingDeckOpposingHand(event);
			}
			
			// move card from HAND to DECK
			if( event.get("from").equals("FRIENDLY HAND") &&
					event.get("to").equals("FRIENDLY DECK")) {
				moveFriendlyHandFriendlyDeck(event);
			}
			if( event.get("from").equals("OPPOSING HAND") &&
				event.get("to").equals("OPPOSING DECK")) {
				moveOpposingHandOpposingDeck(event);
			}
			
			// move card HAND -> PLAY events
			if( event.get("from").equals("FRIENDLY HAND") && 	// if card FRIENDLY HAND -> FRIENDLY PLAY
				(event.get("to").equals("FRIENDLY PLAY") || 
				event.get("to").equals("FRIENDLY PLAY (Weapon)"))) {
				moveFriendlyHandFriendlyPlay(event);
			}else if( event.get("from").equals("OPPOSING HAND")  && //if card OPPOSING HAND -> OPPOSING PLAY
					(event.get("to").equals("OPPOSING PLAY") ||
					event.get("to").equals("OPPOSING PLAY (Weapon)"))) {
				moveOpposingHandOpposingPlay(event);
			}
		}
	}
	
	protected void moveFriendlyHandFriendlyDeck(Map<String, String> event) {
		HearthstoneCard card = friendlyHand.removeCard(event.get("id")); //remove card from deck
		updateCard(event,card); //update card with event data
		friendlyDeck.addCard(card);	//place card in opposing play
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	protected void moveOpposingHandOpposingDeck(Map<String, String> event) {
		HearthstoneCard card = opposingHand.removeCard(event.get("id")); //remove card from deck
		updateCard(event,card); //update card with event data
		opposingDeck.addCard(card);	//place card in opposing play
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	protected void moveFriendlyDeckFriendlyHand(Map<String, String> event) {
		HearthstoneCard card = friendlyDeck.removeCard(event.get("id")); //remove card from deck
		updateCard(event,card); //update card with event data
		friendlyHand.addCard(card);	//place card in opposing play
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	protected void moveOpposingDeckOpposingHand(Map<String, String> event) {
		HearthstoneCard card = opposingDeck.removeCard(event.get("id")); //remove card from  deck
		updateCard(event,card); //update card with event data
		opposingHand.addCard(card);	//place card in opposing play
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	protected void moveUnknowToFriendlyHand(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(); //create a new card
		updateCard(event,card); //update card with event data
		friendlyHand.addCard(card); //add card to deck
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	protected void moveUnknowToOpposingHand(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(); //create a new card
		updateCard(event,card); //update card with event data
		opposingHand.addCard(card); //add card to deck
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	protected void moveFriendlyHandFriendlyPlay(Map<String, String> event) {
		HearthstoneCard card = friendlyDeck.removeCard(event.get("id")); //remove card from friendly deck
		if(card==null) {	//TODO exception thrown if card is not found
			theTool.writeConsole("null friendly=");
			//theTool.logEvent(event);
		}
		updateCard(event,card); //update card with event data
		friendlyPlay.addCard(card);	//place card in friendly play
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	protected void moveOpposingHandOpposingPlay(Map<String, String> event) {
		HearthstoneCard card = opposingDeck.removeCard(event.get("id")); //remove card from opposing deck
		if(card==null) { //TODO exception thrown if card is not found
			theTool.writeConsole("null friendly \n");
			//theTool.logEvent(event);
		}
		updateCard(event,card); //update card with event data
		opposingPlay.addCard(card);	//place card in opposing play
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	protected void dealOpposingDeck(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(); //create a new card
		updateCard(event,card); //update card with event data
		opposingDeck.addCard(card); //add card to deck
		event.put("eventHandled", "true"); //flag event as handled
	}

	protected void dealFriendlyDeck(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(); //create a new card
		updateCard(event,card); //update card with event data
		friendlyDeck.addCard(card); //add card to deck
		event.put("eventHandled", "true"); //flag event as handled
	}

	protected void doGameStart(Map<String, String> event) {
		reset();
		theTool.notifyGameState(gameState = EVENT_HERO_FRIENDLY_PLAY); //notify Tool game started
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	protected void doGameOver(Map<String, String> event) {
		theTool.notifyGameState(gameState = EVENT_HERO_GRAVEYARD); //notify Tool game over
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	protected HearthstoneCard findCard(String id) {
		HearthstoneCard card = null; //null until found
		
		for(HearthstoneCardZone z:zones) {	//for each zone
			HearthstoneCard c = z.removeCard(id); // check if zone has card
			if(c!=null) {	//check if card is found
				break; //leave for
			}
		}
		
		return card; //card is null if not found
	}
	
	protected void updateCard(Map<String,String> event, HearthstoneCard card ) {
		if(event.containsKey("id")) {
			//TODO cleanup
		}
		card.getAttributes().put("id", event.get("id"));
		if(event.containsKey("name")) {
			card.getAttributes().put("name", event.get("name"));
		}else{
			card.getAttributes().put("name", "unknown");
		}
	}
	
	protected boolean isHero(String s) {
		  boolean isHero = false;
		  if(s.equals("Garrosh Hellscream")) {
			  isHero = true;
		  }else if(s.equals("Rexxar")) {
			  isHero = true;
		  }else if(s.equals("Jaina Proudmoore")) {
		   isHero = true;
		  }else if(s.equals("Valeera Sanguinar")) {
		   isHero = true;
		  }else if(s.equals("Thrall")) {
		   isHero = true;
		  }else if(s.equals("Malfurion Stormrage")) {
		   isHero = true;
		  }else if(s.equals("Uther the Lightbringer")) {
		   isHero = true;
		  }else if(s.equals("Anduin Wrynn")) {
		   isHero = true;
		  }else if(s.equals("Gul'dan")) {
		   isHero = true;
		  }
		  return isHero;
		 }
	
	public void reset() {
		opposingDeck.clearZone();	//empty zones
		friendlyDeck.clearZone();
		opposingHand.clearZone();
		friendlyHand.clearZone();
		opposingPlay.clearZone();
		friendlyPlay.clearZone();
		opposingGraveyard.clearZone();
		friendlyGraveyard.clearZone();
		
		
	}

	public List<HearthstoneCardZone> getZones() {
		return zones;
	}
	
	public int getState() {
		return gameState;
	}
}
