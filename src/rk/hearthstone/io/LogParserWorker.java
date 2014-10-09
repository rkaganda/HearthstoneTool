package rk.hearthstone.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import rk.hearthstone.HearthTool;

public class LogParserWorker extends SwingWorker<ArrayList<String>,Void> {	
	protected File theFile;
	protected HearthTool theTool;
	
	public LogParserWorker(File file, HearthTool tool) {
		theFile = file;
		theTool = tool;
	}
	
	@Override
	protected ArrayList<String> doInBackground() throws Exception {
		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(theFile));
			String line = new String();
			while((line = br.readLine())!=null) {
				lines.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	protected void done() {
		try {
			theTool.logLoaded(get(),theFile.getName());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
