package rk.hearthstone.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import rk.hearthstone.HearthTool;


public class LogFileWatcher implements Runnable {
	protected Path thePath;
	protected String fileName;
	protected boolean stayAlive;
	protected WatchService watchService;
	protected HearthTool theTool;
	
	public LogFileWatcher(File file, HearthTool tool) {
		theTool = tool;
		stayAlive = true;
		
		fileName = file.getName();
		
		thePath = Paths.get(file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf(File.separator)));
		
		// Register events
		try {
			watchService = FileSystems.getDefault().newWatchService();
			
			thePath.register(watchService, 
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_MODIFY,
					StandardWatchEventKinds.ENTRY_DELETE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stayAlive(boolean b) {
		stayAlive = b;
	}
	
	public void run() {
		while(stayAlive) {
			theTool.notifyFileUpdated();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*
	public void run() {
		while(stayAlive) {
			final WatchKey key = watchService.poll();
			
			if(key!=null) {
				for (WatchEvent<?> watchEvent : key.pollEvents()) {
					final Kind<?> kind = watchEvent.kind();
					// Overflow event
					if (StandardWatchEventKinds.OVERFLOW == kind) {
						continue; // loop
					}else if(StandardWatchEventKinds.ENTRY_MODIFY == kind) {
						final WatchEvent<Path> wePath = (WatchEvent<Path>) watchEvent;
						final Path newPath = wePath.context();
						System.out.println("Path modified: " + newPath);
						if(newPath.toString().equals(fileName)) {
							System.out.println("match");
							theTool.notifyFileUpdated();
						}
					}
				}
				if(!key.reset()) {
					break;
				}
			}	
		}
	}*/
}
