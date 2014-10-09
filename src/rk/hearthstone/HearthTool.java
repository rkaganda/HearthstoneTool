package rk.hearthstone;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

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
			//System.out.println("wow");
		}
	}
	
	protected void parseNewLine(String s) {
		if(s.length()>6) {  //validate length
			if(s.substring(0,6).equals("[Zone]")) { //if [Zone] log
				Map<String,String> event = HearthstoneGame.parseEvent(s); //attempt to parse event
				if(event.containsKey(("type"))) { //if event:type was parsed
					if(theGame.getState()>0) { //if game is in recording decks state
						theGame.handleEvent(event); //pass event to record event 
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

	public void friendlyPlayed(Map<String, String> event) {
		theFrame.addFriendlyCard(event.get("name"));
	}

	public void opposingPlayed(Map<String, String> event) {
		theFrame.addOpposingCard(event.get("name"));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writeConsole("Stopped watching "+watchedFile.getAbsolutePath());
		theFrame.watchingFile(watchingFile = false); //update UI 
	}

	public void doRecord() {
		if(watchingFile){ //sanity check
			if(!recordingDecks ) { //not recording
				theGame.reset(); //reset prepare game for first event
				recordingDecks = true;
			}else if(recordingDecks) {
				theFrame.recordingStop(); //stop recording
				recordingDecks = false;
			}
		}
	}
	
	public void notifyGameState(int i) {
		if(i == HearthstoneGame.WAITING_HERO_FRIENDLY) {
			theFrame.recordWaiting(); //update the frame to show game state
		}
	}
}
