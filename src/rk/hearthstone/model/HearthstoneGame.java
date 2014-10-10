package rk.hearthstone.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rk.hearthstone.HearthTool;

public class HearthstoneGame {
	public final static int RECORD_OFF = 0;
	public final static int WAITING_HERO_FRIENDLY = 1; //game has ended waiting for event type=move name=(Hero) from="" -> to="FRIENDLY PLAY" 
	public final static int EVENT_HERO_FRIENDLY_PLAY = 2; //occurs after event type=move name=(Hero) from="" -> to="FRIENDLY PLAY" 
	public final static int DEALING_FRIENDLY_DECK = 3; //game state while FRIENDLY DECK is dealt
	public final static int EVENT_HERO_OPPOSING_PLAY = 4; //TODO next event to create
	public final static int DEALING_OPPOSING_DECK = 5; 

	
	protected int gameState;
	
	//stores the state of each zone, stored in Map as id=>HearthstoneCard
	protected HearthstoneCardZone opposingDeck; //deck zones
	protected HearthstoneCardZone friendlyDeck;
	
	protected HearthstoneCardZone opposingHand; //hand zones
	protected HearthstoneCardZone friendlyHand;
	
	protected HearthstoneCardZone friendlyPlay; //play zone
	protected HearthstoneCardZone opposingPlay;
	
	protected HearthstoneCardZone friendlyGraveyard; //graveyard zone
	protected HearthstoneCardZone opposingGraveyard;
	
	protected HearthTool theTool;
	
	public HearthstoneGame() {
		friendlyDeck = new HearthstoneCardZone("friendlyDeck");
		opposingDeck = new HearthstoneCardZone("opposingDeck");
		friendlyHand = new HearthstoneCardZone("friendlyHand");
		opposingHand = new HearthstoneCardZone("opposingHand");
		friendlyPlay = new HearthstoneCardZone("friendlyPlay");
		opposingPlay = new HearthstoneCardZone("opposingPlay");
		friendlyGraveyard = new HearthstoneCardZone("friendlyGraveyard");
		opposingGraveyard = new HearthstoneCardZone("opposingGraveyard");
	}
	
	public HearthstoneGame(HearthTool tool) {
		this();
		theTool = tool;
		gameState = RECORD_OFF;
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
				event.put("name","");
			}
			if(bracket.contains("id=")) { //parse id
				event.put("id", bracket.substring(bracket.indexOf("id=")+3,  // substring id= till whitespace
						bracket.substring(bracket.indexOf("id=")).indexOf(" ") + bracket.indexOf("id=")));
			}else {
				event.put("id","");
			}
			
			//parse from
			event.put("from", s.substring(s.indexOf("] zone from ")+12, s.indexOf("-> ")).trim());
			
			if(s.indexOf("-> ")+3!=s.length()-1) {
				event.put("to", s.substring(s.indexOf("-> ")+3,s.length()).trim());
			}else {
				event.put("to","");
			}
		}
		return event;
	}
	
	
	
	public void handleEvent(Map<String, String> event) { 
		if(event.get("type").equals("move")) { //move events
			
			// if card is Hero
			if(isHero(event.get("name"))) { 
				if( event.get("to").equals("FRIENDLY PLAY (Hero)") // Hero -> FRIENDLY PLAY game start
						&& gameState == WAITING_HERO_FRIENDLY ) {
					doGameStart(event); //process starting game event
				}else if( event.get("to").equals("OPPOSING GRAVEYARD") || //Hero -> GRAVEYARD game over
						event.get("to").equals("FRIENDLY GRAVEYARD")) {
					doGameOver(event); //process game over event
				}
			}
		
		
			// move "" : "" -> "FRIENDLY DECK"
			if( event.get("name").equals("") &&
				event.get("from").equals("") &&
				event.get("to").equals("FRIENDLY DECK")) {
				if(gameState==EVENT_HERO_FRIENDLY_PLAY) { 
					theTool.notifyGameState(gameState = DEALING_FRIENDLY_DECK); //start DEALING_FRIENDLY_DECK state
					dealFriendlyDeck(event);
				}else if(gameState==DEALING_FRIENDLY_DECK) {
					dealFriendlyDeck(event);
				}
			}
			
			// move "" : "" -> "OPPOSING DECK"
			if( event.get("name").equals("") &&
				event.get("from").equals("") &&
				event.get("to").equals("OPPOSING DECK")) {
				if(gameState==DEALING_FRIENDLY_DECK) { 
					theTool.notifyGameState(gameState = DEALING_OPPOSING_DECK); //start DEALING_OPPOSING_DECK state
					dealOpposingDeck(event);
				}else if(gameState==DEALING_OPPOSING_DECK) {
					dealOpposingDeck(event);
				}	
			}
			
			
			// move HAND -> PLAY events
			if( event.get("from").equals("FRIENDLY HAND") && 	// if card FRIENDLY HAND -> FRIENDLY PLAY
				(event.get("to").equals("") || 
						event.get("to").equals("FRIENDLY PLAY") || 
						event.get("to").equals("FRIENDLY PLAY (Weapon)"))) {
				friendlyPlay(event);
			}else if( event.get("from").equals("OPPOSING HAND")  && //if card OPPOSING HAND -> OPPOSING PLAY
					(event.get("to").equals("") || 
						event.get("to").equals("OPPOSING PLAY") ||
						event.get("to").equals("OPPOSING PLAY (Weapon)"))) {
				opposingPlay(event);
			}
		}
	}
	
	protected void friendlyPlay(Map<String, String> event) {
		theTool.logEvent(event); //debug
		HearthstoneCard card = friendlyDeck.removeCard(event.get("id")); //remove card from friendly deck
		if(card==null) { //TODO this quickfix assumes card was played from hand, should take card from correct zone
			card = new HearthstoneCard(); //create a new card
			card.getAttributes().put("id", event.get("id")); //set card id
		}
		card.cardAttributes.put("name", event.get("name"));
		friendlyPlay.addCard(card);	//place card in friendly play
		
		//TODO notify tool
	}
	
	protected void opposingPlay(Map<String, String> event) {
		theTool.logEvent(event); //debug
		HearthstoneCard card = opposingDeck.removeCard(event.get("id")); //remove card from opposing deck
		if(card==null) { //TODO this quickfix assumes card was played from hand, should take card from correct zone
			card = new HearthstoneCard(); //create a new card
			card.getAttributes().put("id", event.get("id")); //set card id
		}
		card.cardAttributes.put("name", event.get("name"));
		opposingPlay.addCard(card);	//place card in opposing play
		
		//TODO notify tool
	}
	
	protected void dealOpposingDeck(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(); //create a new card
		card.getAttributes().put("id", event.get("id")); //set card id
		card.cardAttributes.put("name", "unknown"); //cards from DECK are unknown
		opposingDeck.addCard(card); //add card to deck
	}

	protected void dealFriendlyDeck(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(); //create a new card
		card.getAttributes().put("id", event.get("id")); //set card id
		card.cardAttributes.put("name", "unknown"); //cards from DECK are unknown
		friendlyDeck.addCard(card); //add card to deck
	}

	protected void doGameStart(Map<String, String> event) {
		theTool.notifyGameState(gameState = EVENT_HERO_FRIENDLY_PLAY); //notify Tool game started
	}
	
	protected void doGameOver(Map<String, String> event) {
		theTool.notifyGameState(gameState = WAITING_HERO_FRIENDLY); //notify Tool game started
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
		}
		return isHero;
	}

	public List<HearthstoneCardZone> reset() {
		List<HearthstoneCardZone> zones = new ArrayList<HearthstoneCardZone>();
		
		opposingDeck.clearZone();	//empty zones
		friendlyDeck.clearZone();
		opposingHand.clearZone();
		friendlyHand.clearZone();
		opposingPlay.clearZone();
		friendlyPlay.clearZone();
		opposingGraveyard.clearZone();
		friendlyGraveyard.clearZone();
		zones.add(opposingDeck);
		zones.add(friendlyDeck);
		zones.add(opposingHand);
		zones.add(friendlyHand);
		zones.add(opposingPlay);
		zones.add(friendlyPlay);
		zones.add(opposingGraveyard);
		zones.add(friendlyGraveyard);
	
		theTool.notifyGameState(gameState = WAITING_HERO_FRIENDLY); //set state to wait for player hero event
		
		return zones;
	}

	public int getState() {
		return gameState;
	}
}
