package rk.hearthstone;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class HearthTool {
	protected HearthFrame theFrame;
	protected boolean watchingFile;
	protected boolean fileWatcher;
	protected File watchedFile;
	protected ArrayList<String> lastLog;
	HearthstoneGame theGame;
	
	public static void main(String args[]) {
		HearthTool myTool = new HearthTool();
	}
	
	public HearthTool() {
		theFrame = new HearthFrame(this);
		watchingFile = false;
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
	
		final String TRANSITION_CARD = "[Zone] ZoneChangeList.ProcessChanges() - TRANSITIONING card";
		
		if(s.length()>6) {
			if(s.substring(0,6).equals("[Zone]")) { //check for [Zone]
				Map<String,String> event = HearthstoneGame.parseEvent(s);
				if(event.containsKey(("type"))) {
					theFrame.writeConsole("type="+event.get("type"));
					if(event.containsKey("name")) {
						theFrame.writeConsole("name="+event.get("name"));
					}else {
						theFrame.writeConsole("name=?");
					}
					if(event.containsKey("from")) {
						theFrame.writeConsole("from="+event.get("from"));
					}else {
						theFrame.writeConsole("from=?");
					}
					if(event.containsKey("to")) {
						theFrame.writeConsole("to="+event.get("to"));
					}else {
						theFrame.writeConsole("to=?");
					}
					
					theGame.handleEvent(event);
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
		theFrame.watchingFile(watchingFile = true);
		parseLog(file);
		
		LogFileWatcher myWatcher = new LogFileWatcher(file, this);
		Thread myThread = new Thread(myWatcher);
		myThread.start();
		writeConsole("Watching "+file.getAbsolutePath());
		watchedFile = file;
	}

	public void notifyFileUpdated() {
		parseLog(watchedFile);
	}

	public void friendlyPlayed(Map<String, String> event) {
		theFrame.addFriendlyCard(event.get("name"));
	}

	public void opposingPlayed(Map<String, String> event) {
		theFrame.addOpposingCard(event.get("name"));
	}
}
