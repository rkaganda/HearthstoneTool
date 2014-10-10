package rk.hearthstone;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rk.hearthstone.io.LogFileWatcher;
import rk.hearthstone.io.LogParserWorker;
import rk.hearthstone.model.HearthstoneCard;
import rk.hearthstone.model.HearthstoneCardZone;
import rk.hearthstone.model.HearthstoneGame;
import rk.hearthstone.ui.HearthFrame;

public class HearthTool {
	protected HearthFrame theFrame;
	protected boolean watchingFile, recordingDecks;
	protected File watchedFile;
	protected ArrayList<String> lastLog;
	protected HearthstoneGame theGame;
	protected LogFileWatcher myWatcher;
	protected Thread watcherThread;
	
	public static void main(String args[]) {
		HearthTool myTool = new HearthTool();
	}
	
	public HearthTool() {
		theFrame = new HearthFrame(this);
		watchingFile = false;
		recordingDecks = false;
		theGame = new HearthstoneGame(this);
	}
	
	public void parseLog(File file) {
		if(file!=null) {
			(new LogParserWorker(file,this)).execute(); //run thread
		}else {
			//null file, go figure
		}
	}
	
	protected void parseNewLine(String s) {
		if(s.length()>6) {  //validate length
			if(s.substring(0,6).equals("[Zone]")) { //if [Zone] log
				Map<String,String> event = HearthstoneGame.parseEvent(s); //attempt to parse event
				if(event.containsKey(("type"))) { //if event was parsed
					theGame.handleEvent(event); //pass event to game to handle
					if(!event.containsKey("eventHandled")) { //check if event was handled
						logEvent(event); //debug
					}
				}
			}
		}
	}
	
	public void logLoaded(ArrayList<String> arrayList,String name) {
		if(watchingFile) {
			if(lastLog!=null) {
				if(arrayList.size() > lastLog.size()) {
					for(int i = lastLog.size(); i < arrayList.size(); i++ ) {
						parseNewLine(arrayList.get(i));
					}
				}
			}
			lastLog = arrayList;
		}else{
			theFrame.writeConsole("Loaded file: "+name+" OK\n");
		}
	}
	
	public void watchFile(File file) {
		theFrame.watchingFile(watchingFile = true); //update UI
		parseLog(file); //do intial parse of file
		
		myWatcher = new LogFileWatcher(file, this); //create a Runnable for Thread
		watcherThread = new Thread(myWatcher); 
		watcherThread.start();	//start watching file
		
		theFrame.writeConsoleLine("Started watching "+file.getAbsolutePath());
		watchedFile = file; //save reference to watchedFile
	}

	public void notifyFileUpdated() {
		parseLog(watchedFile); //parse the updated file
	}

	public void stopWatching() {
		if(recordingDecks) { 	//if recording
			if(theGame.getState() > HearthstoneGame.WAITING_HERO_FRIENDLY) { //if game is active
				
			}else { // recording but game is not active
				theFrame.recordingStop();
			}
			recordingDecks = false; //recording stopped
		}
		
		myWatcher.stayAlive(false); //tell Runnable to die
		try {
			watcherThread.join(); //wait for Thread to die
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		theFrame.writeConsoleLine("Stopped watching "+watchedFile.getAbsolutePath());
		theFrame.watchingFile(watchingFile = false); //update UI 
	}

	public void doRecord() {
		if(watchingFile){ //sanity check
			if(!recordingDecks ) { //not recording
				theGame.reset(); //reset the game, push new zones to view				
				recordingDecks = true;
				theFrame.recordWaiting(); //update toolbar 
			}else if(recordingDecks) {
				theFrame.recordingStop(); //stop recording
				recordingDecks = false;
			}
		}
	}
	
	public void writeConsole(String s) {
		theFrame.writeConsole(s);
	}
	
	//debug
	public void logEvent(Map<String,String> e) {
		for(String key:e.keySet()) {
			theFrame.writeConsole(key+"='"+e.get(key)+"' ");
		}
		theFrame.writeConsole("\n");
	}
	
	public void notifyGameState(int i) {
		//TODO remove unneeded game states, only notify of game start and game end
		if(i == HearthstoneGame.WAITING_HERO_FRIENDLY) {
			theFrame.recordWaiting(); 	//update toolbar
			theFrame.writeConsoleLine("Game State: WAITING_HERO_FRIENDLY");
		}else if(i==HearthstoneGame.EVENT_HERO_FRIENDLY_PLAY) {
			theFrame.gameStarted(); //update UI
			theFrame.updateZones(theGame.getZones()); //update zone views
			theFrame.writeConsoleLine("Game State: EVENT_HERO_FRIENDLY_PLAY");
		}else if(i==HearthstoneGame.DEALING_FRIENDLY_DECK) {
			theFrame.gameStarted(); //update UI 
			theFrame.writeConsoleLine("Game State: DEALING_FRIENDLY_DECK");
		}else if(i==HearthstoneGame.DEALING_OPPOSING_DECK) {
			theFrame.gameStarted(); //update UI 
			theFrame.writeConsoleLine("Game State: DEALING_OPPOSING_DECK");
		}else if(i==HearthstoneGame.EVENT_HERO_GRAVEYARD) {
			theFrame.recordWaiting(); 	//update toolbar
			theFrame.writeConsoleLine("Game State: EVENT_HERO_GRAVEYARD");
		}
	}
}
