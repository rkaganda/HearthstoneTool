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
				if(event.containsKey(("type"))) { //if event:type was parsed
					theFrame.writeConsole("'"+event.get("name").trim()+"':"+"'"+event.get("from").trim()+"'->"+"'"+event.get("to").trim()+"'");
					theGame.handleEvent(event); //pass event to game to handle
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
	
	public void writeConsole(String s) {
		theFrame.writeConsole(s);
	}

	
	public void watchFile(File file) {
		theFrame.watchingFile(watchingFile = true); //update UI
		parseLog(file); //do intial parse of file
		
		myWatcher = new LogFileWatcher(file, this); //create a Runnable for Thread
		watcherThread = new Thread(myWatcher); 
		watcherThread.start();	//start watching file
		
		writeConsole("Started watching "+file.getAbsolutePath());
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
		writeConsole("Stopped watching "+watchedFile.getAbsolutePath());
		theFrame.watchingFile(watchingFile = false); //update UI 
	}

	public void doRecord() {
		if(watchingFile){ //sanity check
			if(!recordingDecks ) { //not recording
				theFrame.updateZones(theGame.reset()); //reset the game, push new zones to view				
				recordingDecks = true;
			}else if(recordingDecks) {
				theFrame.recordingStop(); //stop recording
				recordingDecks = false;
			}
		}
	}
	
	//debug
	public void logEvent(Map<String,String> e) {
		System.out.print("event:");
		for(String key:e.keySet()) {
			System.out.print(key+"="+e.get(key));
		}
		System.out.print("\n");
	}
	
	public void notifyGameState(int i) {
		if(i == HearthstoneGame.WAITING_HERO_FRIENDLY) {
			theFrame.recordWaiting(); //update UI
			theFrame.writeConsole("Game State: WAITING_HERO_FRIENDLY");
		}else if(i==HearthstoneGame.EVENT_HERO_FRIENDLY_PLAY) {
			theFrame.gameStarted(); //update UI 
			theFrame.writeConsole("Game State: EVENT_HERO_FRIENDLY_PLAY");
		}else if(i==HearthstoneGame.DEALING_FRIENDLY_DECK) {
			theFrame.gameStarted(); //update UI 
			theFrame.writeConsole("Game State: DEALING_FRIENDLY_DECK");
		}else if(i==HearthstoneGame.DEALING_OPPOSING_DECK) {
			theFrame.gameStarted(); //update UI 
			theFrame.writeConsole("Game State: DEALING_OPPOSING_DECK");
		}
	}
}
