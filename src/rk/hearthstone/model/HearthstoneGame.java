package rk.hearthstone.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rk.hearthstone.HearthTool;

public class HearthstoneGame {
	public final static int DISABLE = 0;
	public final static int WAITING_HERO = 1; //game has ended waiting for a hero to enter play
	public final static int HERO_PLAY = 2; //hero as entered play
	public final static int HERO_GRAVEYARD = 100; // hero is in graveyard
	
	protected Map<String,String> gameFlags; //stores game state flags
	
	protected int gameState;
	
	//stores the card for each zone
	List<HearthstoneCardZone> zones;
	HearthstoneCardZone friendlyDeck;
	HearthstoneCardZone opposingDeck;
	HearthstoneCardZone friendlyHand;
	HearthstoneCardZone opposingHand;
	HearthstoneCardZone friendlyPlay;
	HearthstoneCardZone opposingPlay;
	HearthstoneCardZone friendlySecret;
	HearthstoneCardZone opposingSecret;
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
		friendlySecret = new HearthstoneCardZone("friendlySecret");
		opposingSecret = new HearthstoneCardZone("opposingSecret");
		friendlyGraveyard = new HearthstoneCardZone("friendlyGraveyard");
		opposingGraveyard = new HearthstoneCardZone("opposingGraveyard");
		
		zones.add(opposingDeck);
		zones.add(friendlyDeck);
		zones.add(opposingHand);
		zones.add(friendlyHand);
		zones.add(opposingPlay);
		zones.add(friendlyPlay);
		zones.add(friendlySecret);
		zones.add(opposingSecret);
		zones.add(opposingGraveyard);
		zones.add(friendlyGraveyard);
		
		gameFlags = new HashMap<String,String>();
		gameFlags.put("gameRunning","false");
	}
	
	public HearthstoneGame(HearthTool tool) {
		this();
		theTool = tool;
		gameState = WAITING_HERO; //waiting for hero to enter play zone
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
			
			if(s.indexOf("-> ")+2!=s.length()-1) {
				event.put("to", s.substring(s.indexOf("-> ")+3,s.length()).trim());
			}else {
				event.put("to","unknown");
			}
		}
		return event;
	}
	
	
	
	public void handleEvent(Map<String, String> event) { 
		if(event.get("type").equals("move")) { //move events
			
			// if card is Hero
			if(isHero(event.get("name"))) { 
				if(event.get("to").equals("FRIENDLY PLAY (Hero)")) {	// Hero -> FRIENDLY PLAY, possible game start
					moveHeroUnknownFriendlyPlay(event); //process starting game event
				}
				if(event.get("to").equals("OPPOSING PLAY (Hero)")) { // Hero -> OPPOSING PLAY, possible game start
					moveHeroUnknownOpposingPlay(event); //process starting game event
				}
				
				if( event.get("to").equals("OPPOSING GRAVEYARD") || //Hero -> GRAVEYARD game over
						event.get("to").equals("FRIENDLY GRAVEYARD")) {
					doGameOver(event); //process game over event
				}
			}
		
			// move * from * to FRIENDLY PLAY (Hero Power)
			if(	event.get("to").equals("FRIENDLY PLAY (Hero Power)")) {
				event.put("eventHandled", "true"); //flag event as handled
			}
			if( event.get("to").equals("OPPOSING PLAY (Hero Power)")) {
				event.put("eventHandled", "true"); //flag event as handled
			}
			
			// move * from PLAY (Weapon) to unknown
			if( event.get("from").equals("unknown") &&
				event.get("to").equals("FRIENDLY PLAY (Weapon)")) {
					event.put("eventHandled", "true"); //flag event as handled
			}
			if( event.get("from").equals("unknown") &&
				event.get("to").equals("OPPOSING PLAY (Weapon)")) {
					event.put("eventHandled", "true"); //flag event as handled
			}
			
			//move * from PLAY (Weapon) to GRAVEYARD
			//TODO move weapons to graveyard zone
			if( event.get("from").equals("FRIENDLY PLAY (Weapon)") &&
					event.get("to").equals("FRIENDLY GRAVEYARD")) {
				event.put("eventHandled", "true"); //flag event as handled
			}
			if( event.get("from").equals("OPPOSING PLAY (Weapon)") &&
					event.get("to").equals("OPPOSING GRAVEYARD")) {
				event.put("eventHandled", "true"); //flag event as handled
			}
			
			// move * from unknown to DECK
			if( event.get("from").equals("unknown") &&
				event.get("to").equals("FRIENDLY DECK")) {
					moveUnknownFriendlyDeck(event);
			}
			if( event.get("from").equals("unknown") &&
				event.get("to").equals("OPPOSING DECK")) {
					moveUnknownOpposingDeck(event);
			}
			
			// move * from unknown -> HAND
			if( event.get("from").equals("unknown") && 	// if card unknown -> FRIENDLY HAND
					event.get("to").equals("FRIENDLY HAND")) {
				moveUnknowFriendlyHand(event);
			}else if( event.get("from").equals("unknown") && 	// if card unknown -> OPPOSING HAND
					event.get("to").equals("OPPOSING HAND")) {
				moveUnknowOpposingHand(event);
			}
			
			//move * from unknown -> PLAY
			if( event.get("from").equals("unknown") && 	
					event.get("to").equals("FRIENDLY PLAY")) {
				moveUnknowFriendlyPlay(event);
			}else if( event.get("from").equals("unknown") && 	
					event.get("to").equals("OPPOSING PLAY")) {
				moveUnknowOpposingPlay(event);
			}
			
			//move * from unknown -> SECRET
			if( event.get("from").equals("unknown") && 	
					event.get("to").equals("FRIENDLY SECRET")) {
				moveUnknowToFriendlySecret(event);
			}else if( event.get("from").equals("unknown") && 	
					event.get("to").equals("OPPOSING SECRET")) {
				moveUnknowToOpposingSecret(event);
			}
			
			// move * from unknown to GRAVEYARD
			if( event.get("from").equals("unknown") &&
					event.get("to").equals("FRIENDLY GRAVEYARD")) {
				moveUnknownFrindlyGraveyard(event);
			}
			if( event.get("from").equals("unknown") &&
					event.get("to").equals("OPPOSING GRAVEYARD")) {
				moveUnknownOpposingGraveyard(event);
			}
			
			// move * DECK -> HAND events
			if( event.get("from").equals("FRIENDLY DECK") && 	
					event.get("to").equals("FRIENDLY HAND")) {
				moveFriendlyDeckFriendlyHand(event);
			}else if( event.get("from").equals("OPPOSING DECK") && 	
					event.get("to").equals("OPPOSING HAND")) {
				moveOpposingDeckOpposingHand(event);
			}
			
			// move * DECK -> SECRET
			if( event.get("from").equals("FRIENDLY DECK") && 	
					event.get("to").equals("FRIENDLY SECRET")) {
				moveFriendlyDeckFriendlySecret(event);
			}else if( event.get("from").equals("OPPOSING DECK") && 	
					event.get("to").equals("OPPOSING SECRET")) {
				moveOpposingDeckOpposingSecret(event);
			}
			
			// move * DECK -> PLAY events
			if( event.get("from").equals("FRIENDLY DECK") && 	
					event.get("to").equals("FRIENDLY PLAY")) {
				moveFriendlyDeckFriendlyPlay(event);
			}else if( event.get("from").equals("OPPOSING DECK") && 	
					event.get("to").equals("OPPOSING PLAY")) {
				moveOpposingDeckOpposingPlay(event);
			}
			
			//move * DECK -> GRAVEYARD events
			if( event.get("from").equals("FRIENDLY DECK") && 	
					event.get("to").equals("FRIENDLY GRAVEYARD")) {
				moveFriendlyDeckFriendlyGraveyard(event);
			}else if( event.get("from").equals("OPPOSING DECK") && 
					event.get("to").equals("OPPOSING GRAVEYARD")) {
				moveOpposingDeckOpposingGraveyard(event);
			}
			
			// move * from HAND to DECK
			if( event.get("from").equals("FRIENDLY HAND") &&
					event.get("to").equals("FRIENDLY DECK")) {
				moveFriendlyHandFriendlyDeck(event);
			}
			if( event.get("from").equals("OPPOSING HAND") &&
				event.get("to").equals("OPPOSING DECK")) {
				moveOpposingHandOpposingDeck(event);
			}
			
			//move * from HAND to SECRET
			if( event.get("from").equals("FRIENDLY HAND") &&
					event.get("to").equals("FRIENDLY SECRET")) {
				moveFriendlyHandFriendlySecret(event);
			}
			if( event.get("from").equals("OPPOSING HAND") &&
				event.get("to").equals("OPPOSING SECRET")) {
				moveOpposingHandOpposingSecret(event);
			}
			
			// move * HAND -> PLAY events
			if( event.get("from").equals("FRIENDLY HAND") && 	// if card FRIENDLY HAND -> FRIENDLY PLAY
				(event.get("to").equals("FRIENDLY PLAY") || 
				event.get("to").equals("FRIENDLY PLAY (Weapon)"))) {
				moveFriendlyHandFriendlyPlay(event);
			}else if( event.get("from").equals("OPPOSING HAND")  && //if card OPPOSING HAND -> OPPOSING PLAY
					(event.get("to").equals("OPPOSING PLAY") ||
					event.get("to").equals("OPPOSING PLAY (Weapon)"))) {
				moveOpposingHandOpposingPlay(event);
			}
			
			//move * from HAND to GRAVEYARD
			if( event.get("from").equals("FRIENDLY HAND") &&
					event.get("to").equals("FRIENDLY GRAVEYARD")) {
				moveFriendlyHandFriendlyGraveyard(event);
			}
			if( event.get("from").equals("OPPOSING HAND") &&
					event.get("to").equals("OPPOSING GRAVEYARD")) {
				moveOpposingHandOpposingGraveyard(event);
			}
			
			//move * from HAND to unknown
			if( event.get("from").equals("FRIENDLY HAND") &&
					event.get("to").equals("unknown")) {
				moveFriendlyHandUnknown(event);
			}
			if( event.get("from").equals("OPPOSING HAND") &&
					event.get("to").equals("unknown")) {
				moveOpposingHandUnknown(event);
			}
			
			// move * from PLAY to HAND
			if( event.get("from").equals("FRIENDLY PLAY") &&
					event.get("to").equals("FRIENDLY HAND")) {
				moveFriendlyPlayFriendlyHand(event);
			}
			if( event.get("from").equals("OPPOSING PLAY") &&
					event.get("to").equals("OPPOSING HAND")) {
				moveOpposingPlayOpposingHand(event);
			}
			
			// move * from PLAY to PLAY
			if( event.get("from").equals("FRIENDLY PLAY") &&
					event.get("to").equals("OPPOSING PLAY")) {
				moveFriendlyPlayOpposingPlay(event);
			}
			if( event.get("from").equals("OPPOSING PLAY") &&
					event.get("to").equals("FRIENDLY PLAY")) {
				moveOpposingPlayFriendlyPlay(event);
			}
			
			// move * from PLAY to unknown
			if( event.get("from").equals("FRIENDLY PLAY") &&
					event.get("to").equals("unknown")) {
				moveFriendlyPlayUnknown(event);
			}
			if( event.get("from").equals("OPPOSING PLAY") &&
					event.get("to").equals("unknown")) {
				moveOpposingPlayUnknown(event);
			}
			
			// move * from PLAY to GRAVEYARD
			if( event.get("from").equals("FRIENDLY PLAY") &&
					event.get("to").equals("FRIENDLY GRAVEYARD")) {
				moveFriendlyPlayFriendlyGraveyard(event);
			}
			if( event.get("from").equals("OPPOSING PLAY") &&
					event.get("to").equals("OPPOSING GRAVEYARD")) {
				moveOpposingPlayOpposingGraveyard(event);
			}
			
			//move * from SECRET to GRAVEYARD
			if( event.get("from").equals("FRIENDLY SECRET") &&
					event.get("to").equals("FRIENDLY GRAVEYARD")) {
				moveFriendlyPlayFrindlySecret(event);
			}
			if( event.get("from").equals("OPPOSING SECRET") &&
					event.get("to").equals("OPPOSING GRAVEYARD")) {
				moveOpposingPlayOpposingSecret(event);
			}
		}
	}
	
	//Deck -> Hand
	protected void moveFriendlyDeckFriendlyHand(Map<String, String> event) {
		HearthstoneCard card = friendlyDeck.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		friendlyHand.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
		
	protected void moveOpposingDeckOpposingHand(Map<String, String> event) {
		HearthstoneCard card = opposingDeck.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		opposingHand.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	//Deck -> Hand
	protected void moveFriendlyDeckFriendlySecret(Map<String, String> event) {
		HearthstoneCard card = friendlyDeck.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		friendlySecret.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
		
	protected void moveOpposingDeckOpposingSecret(Map<String, String> event) {
		HearthstoneCard card = opposingDeck.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		opposingSecret.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	// Deck -> Play
	protected void moveFriendlyDeckFriendlyPlay(Map<String, String> event) {
		HearthstoneCard card = friendlyDeck.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		friendlyPlay.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
		
	protected void moveOpposingDeckOpposingPlay(Map<String, String> event) {
		HearthstoneCard card = opposingDeck.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		opposingPlay.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	//Deck -> Graveyard
	protected void moveFriendlyDeckFriendlyGraveyard(Map<String, String> event) {
		HearthstoneCard card = friendlyDeck.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		friendlyGraveyard.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
		
	protected void moveOpposingDeckOpposingGraveyard(Map<String, String> event) {
		HearthstoneCard card = opposingDeck.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		opposingGraveyard.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}

	//Hand -> Deck
	protected void moveFriendlyHandFriendlyDeck(Map<String, String> event) {
		HearthstoneCard card = friendlyHand.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		friendlyDeck.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	protected void moveOpposingHandOpposingDeck(Map<String, String> event) {
		HearthstoneCard card = opposingHand.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		opposingDeck.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	//Hand -> Unknown
	protected void moveFriendlyHandUnknown(Map<String, String> event) {
		HearthstoneCard card = friendlyHand.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		event.put("eventHandled", "true"); //flag event as handled
	}
		
	protected void moveOpposingHandUnknown(Map<String, String> event) {
		HearthstoneCard card = opposingHand.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	//Hand -> Play
	protected void moveFriendlyHandFriendlyPlay(Map<String, String> event) {
		HearthstoneCard card = friendlyHand.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		friendlyPlay.addCard(card);	//place card in friendly play
		event.put("eventHandled", "true"); //flag event as handled
	}
			
	protected void moveOpposingHandOpposingPlay(Map<String, String> event) {
		HearthstoneCard card = opposingHand.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		opposingPlay.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	//Hand -> Secret
	protected void moveFriendlyHandFriendlyGraveyard(Map<String, String> event) {
		HearthstoneCard card = friendlyHand.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		friendlyGraveyard.addCard(card);	//place card in friendly play
		event.put("eventHandled", "true"); //flag event as handled
	}
			
	protected void moveOpposingHandOpposingGraveyard(Map<String, String> event) {
		HearthstoneCard card = opposingHand.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		opposingGraveyard.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	//Hand -> Secret
	protected void moveFriendlyHandFriendlySecret(Map<String, String> event) {
		HearthstoneCard card = friendlyHand.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		friendlySecret.addCard(card);	//place card in friendly play
		event.put("eventHandled", "true"); //flag event as handled
	}
			
	protected void moveOpposingHandOpposingSecret(Map<String, String> event) {
		HearthstoneCard card = opposingHand.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		opposingSecret.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	// Play -> Hand
	protected void moveFriendlyPlayFriendlyHand(Map<String, String> event) {
		HearthstoneCard card = friendlyPlay.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data	
		friendlyHand.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
		
	protected void moveOpposingPlayOpposingHand(Map<String, String> event) {
		HearthstoneCard card = opposingPlay.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		opposingHand.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	//Play -> Play
	protected void moveFriendlyPlayOpposingPlay(Map<String, String> event) {
		HearthstoneCard card = friendlyPlay.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data	
		opposingPlay.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
		
	protected void moveOpposingPlayFriendlyPlay(Map<String, String> event) {
		HearthstoneCard card = opposingPlay.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		friendlyPlay.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	//Play -> Unknown
	protected void moveFriendlyPlayUnknown(Map<String, String> event) {
		HearthstoneCard card = friendlyPlay.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data	
		event.put("eventHandled", "true"); //flag event as handled
	}
		
	protected void moveOpposingPlayUnknown(Map<String, String> event) {
		HearthstoneCard card = opposingPlay.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	//Play -> Graveyard
	protected void moveFriendlyPlayFriendlyGraveyard(Map<String, String> event) {
		HearthstoneCard card = friendlyPlay.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data	
		friendlyGraveyard.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
		
	protected void moveOpposingPlayOpposingGraveyard(Map<String, String> event) {
		HearthstoneCard card = opposingPlay.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		opposingGraveyard.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	//Secret -> Graveyard
	protected void moveFriendlyPlayFrindlySecret(Map<String, String> event) {
		HearthstoneCard card = friendlySecret.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data	
		friendlyGraveyard.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
		
	protected void moveOpposingPlayOpposingSecret(Map<String, String> event) {
		HearthstoneCard card = opposingSecret.removeCard(event.get("id")); //remove card from zone
		updateCard(event,card); //update card with event data
		opposingGraveyard.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	// Unknown -> Deck  
	protected void moveUnknownOpposingDeck(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(event); //create a new card
		updateCard(event,card); //update card with event data
		opposingDeck.addCard(card); //add card to deck
		event.put("eventHandled", "true"); //flag event as handled
	}

	protected void moveUnknownFriendlyDeck(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(event); //create a new card
		updateCard(event,card); //update card with event data
		friendlyDeck.addCard(card); //add card to deck
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	//Unknown -> Hand
	protected void moveUnknowFriendlyHand(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(event); //create a new card
		updateCard(event,card); //update card with event data
		friendlyHand.addCard(card); //add card to deck
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	protected void moveUnknowOpposingHand(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(event); //create a new card
		updateCard(event,card); //update card with event data
		opposingHand.addCard(card); //add card to deck
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	//Unknown -> Played
	protected void moveUnknowFriendlyPlay(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(event); //create a new card
		updateCard(event,card); //update card with event data
		friendlyPlay.addCard(card); //add card to deck
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	protected void moveUnknowOpposingPlay(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(event); //create a new card
		updateCard(event,card); //update card with event data
		opposingPlay.addCard(card); //add card to deck
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	// Unknown -> Secret
	protected void moveUnknowToFriendlySecret(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(event); //create a new card
		updateCard(event,card); //update card with event data
		friendlySecret.addCard(card); //add card to deck
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	protected void moveUnknowToOpposingSecret(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(event); //create a new card
		updateCard(event,card); //update card with event data
		opposingSecret.addCard(card); //add card to deck
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	//Unknown -> Graveyard
	protected void moveUnknownFrindlyGraveyard(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(event); //create a new card
		updateCard(event,card); //update card with event data
		friendlyGraveyard.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
			
	protected void moveUnknownOpposingGraveyard(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(event); //create a new card
		updateCard(event,card); //update card with event data
		opposingGraveyard.addCard(card);	//place card in zone
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	//Hero -> Play
	protected void moveHeroUnknownFriendlyPlay(Map<String, String> event) {
		if(gameFlags.get("gameRunning").equals("false")) { //make sure game isn't already running
			doGameStart(event);
		}
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	protected void moveHeroUnknownOpposingPlay(Map<String, String> event) {
		if(gameFlags.get("gameRunning").equals("false")) { //make sure game isn't already running
			doGameStart(event);
		}
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	
	protected void doGameStart(Map<String, String> event) {
		reset();	
		gameFlags.put("gameRunning","true");
		theTool.notifyGameState(gameState = HERO_PLAY); //notify Tool game started 
	}
	
	protected void doGameOver(Map<String, String> event) {
		gameFlags.put("gameRunning","false");
		theTool.notifyGameState(gameState = HERO_GRAVEYARD); //notify Tool game over
		event.put("eventHandled", "true"); //flag event as handled
	}
	
	protected HearthstoneCard findCard(String id) {
		HearthstoneCard card = null; //null until found
		
		for(HearthstoneCardZone z:zones) {	//for each zone
			HearthstoneCard c = z.removeCard(id); // check if zone has card
			if(c!=null) {	//check if card is found
				theTool.writeConsole("found card in zone="+z.getName());
				break; //leave for
			}
		}
		
		return card; //card is null if not found
	}
	
	protected void updateCard(Map<String,String> event, HearthstoneCard card ) {
		if(event.containsKey("name")) {
			card.put("name", event.get("name"));
		}else{
			card.put("name", "unknown");
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
		  }else if(s.equals("Uther Lightbringer")) {
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
		
		theTool.notifyGameState(gameState = WAITING_HERO);
	}

	public List<HearthstoneCardZone> getZones() {
		return zones;
	}
	
	public int getState() {
		return gameState;
	}
}
