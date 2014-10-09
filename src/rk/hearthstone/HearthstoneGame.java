package rk.hearthstone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HearthstoneGame {
	public final static int RECORD_OFF = 0;
	public final static int WAITING_HERO_FRIENDLY = 1; //game has ended waiting for event type=move name=(Hero) from="" -> to="FRIENDLY PLAY" 
	public final static int EVENT_HERO_FRIENDLY_PLAY = 2; //occurs after event type=move name=(Hero) from="" -> to="FRIENDLY PLAY" 
	public final static int DEALING_FRIENDLY_DECK = 3; //game state while FRIENDLY DECK is dealt
	public final static int EVENT_HERO_OPPOSING_PLAY = 4; //TODO 
	public final static int DEALING_OPPOSING_DECK = 5; 

	
	protected int gameState;
	
	protected ArrayList<HearthstoneCard> opposingDeck;
	protected ArrayList<HearthstoneCard> friendlyDeck;
	protected ArrayList<HearthstoneCard> opposingHand;
	protected ArrayList<HearthstoneCard> friendlyHand;
	
	protected HearthTool theTool;
	
	public HearthstoneGame() {
		opposingDeck = new ArrayList<HearthstoneCard>();
		friendlyDeck = new ArrayList<HearthstoneCard>();
		opposingHand = new ArrayList<HearthstoneCard>();
		friendlyHand = new ArrayList<HearthstoneCard>();
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
			//System.out.println(s); 
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
				theTool.friendlyPlayed(event); //notify tool friendly card played event
			}else if( event.get("from").equals("OPPOSING HAND")  && //if card OPPOSING HAND -> OPPOSING PLAY
					(event.get("to").equals("") || 
						event.get("to").equals("OPPOSING PLAY") ||
						event.get("to").equals("OPPOSING PLAY (Weapon)"))) {
				theTool.opposingPlayed(event);	//notify tool opposing card played event
			}
		}
	}
	
	protected void dealOpposingDeck(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(); //create a new card
		card.getAttributes().put("id", event.get("id")); //set card id
		opposingDeck.add(card); //add card to deck
	}

	protected void dealFriendlyDeck(Map<String, String> event) {
		HearthstoneCard card = new HearthstoneCard(); //create a new card
		card.getAttributes().put("id", event.get("id")); //set card id
		friendlyDeck.add(card); //add card to deck
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

	public void reset() {
		opposingDeck.clear();	//empty decks
		friendlyDeck.clear();
		opposingHand.clear();
		friendlyHand.clear();
		
		theTool.notifyGameState(gameState = WAITING_HERO_FRIENDLY); //set state to wait for player hero event
	}

	public int getState() {
		return gameState;
	}
}
