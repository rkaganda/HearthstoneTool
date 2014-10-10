package rk.hearthstone.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import rk.hearthstone.HearthTool;
import rk.hearthstone.model.HearthstoneCard;
import rk.hearthstone.model.HearthstoneCardZone;

public class HearthFrame extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected HearthTool hearthTool;
	
	protected JMenuBar menubar;
	protected JMenu fileMenu, watchMenu;
	protected JMenuItem loadFileItem;
	
	protected JPanel cardViews;
	protected JButton watchButton, recordButton;
	
	
	protected Map<String,CardZoneView> cardListViews;
	
	protected boolean isWatching;

	protected JTextArea toolConsole = new JTextArea();
	
	public HearthFrame(HearthTool hT) {
		hearthTool = hT;
		
		isWatching = false;
		
		setupUI();
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	
	public void setupUI() {
		try {
		    UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
		 } catch (Exception e) {
		            e.printStackTrace();
		 }
		menubar = new JMenuBar(); 
		fileMenu = new JMenu("File");
		watchMenu = new JMenu("Watch");
		loadFileItem = new JMenuItem("Open Log");
		loadFileItem.addActionListener(this);
		fileMenu.add(loadFileItem);
		menubar.add(fileMenu);
		menubar.add(watchMenu);
		//setJMenuBar(menubar);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		this.getContentPane().add(contentPanel);
		
		
		JToolBar toolBar = new JToolBar("main");
		watchButton = new JButton("Start Watching");
		watchButton.addActionListener(this);
		watchButton.setBackground(Color.GREEN);
		toolBar.add(watchButton);
		recordButton = new JButton("Record Games");
		recordButton.setBackground(Color.LIGHT_GRAY);
		recordButton.setEnabled(false);
		recordButton.addActionListener(this);
		toolBar.add(recordButton);
		
		cardViews = new JPanel();
		cardViews.setLayout(new BoxLayout(cardViews, BoxLayout.X_AXIS));
		cardViews.add(Box.createHorizontalGlue());
		
		
		JPanel consolePanel = new JPanel();
		toolConsole = new JTextArea();
		JScrollPane sp = new JScrollPane(toolConsole);
		sp.setPreferredSize(new Dimension(700,100));
		consolePanel.add(sp);
		
		contentPanel.add(toolBar,BorderLayout.PAGE_START);
		contentPanel.add(cardViews,BorderLayout.CENTER);
		contentPanel.add(consolePanel,BorderLayout.PAGE_END);
		
		toolConsole.setEditable(false);
		
		pack();
	}

	public void writeConsole(String s) {
		final String f = s;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				toolConsole.append(f);
			}
		});
	}
	
	public void writeConsoleLine(String s) {
		final String f = s;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				toolConsole.append(f+"\n");
			}
		});
	}
	
	
	public void clearConsole() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				toolConsole.setText("");
			}
		});
	}
	
	public void watchingFile(boolean b) {
		isWatching = b;
		if(b) {
			SwingUtilities.invokeLater(new Runnable() { //watching running 
				public void run() {
					watchButton.setText("Stop Watching"); 
					watchButton.setBackground(Color.RED);
					recordButton.setEnabled(true); //record enabled
				}
			});
		}else {
			SwingUtilities.invokeLater(new Runnable() { //watching stopped
				public void run() {
					watchButton.setText("Start Watching");
					watchButton.setBackground(Color.GREEN);
					recordButton.setEnabled(false); //record disabled
				}
			});
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(loadFileItem)) {
			final JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(this);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	            hearthTool.parseLog(file);
	        }
		}else if(e.getSource().equals(watchButton)) { //watch button
			if(!isWatching) { 
				startWatching(); 
			}else {
				watchButton.setText("Stopping Watch..");
				watchButton.setBackground(Color.YELLOW);
				hearthTool.stopWatching();
			}
		}else if(e.getSource().equals(recordButton)) {
			if(recordButton.isEnabled()) {  //sanity check
				hearthTool.doRecord(); //notify tool record button 
			}
		}
	}

	protected void startWatching() {
		final JFileChooser fc = new JFileChooser(); //get the file 
		int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            hearthTool.watchFile(file); //send File to tool for watching
        }
	}
	
	public void updateZones(List<HearthstoneCardZone> zones) {
		cardViews.removeAll(); //clear view
		for(HearthstoneCardZone z:zones) {
			CardZoneView view = new CardZoneView(z); //create new view
			cardViews.add(view); //add view to panel
		}
		pack();
	}
	
	public void recordWaiting() {
		SwingUtilities.invokeLater(new Runnable() { 
			public void run() {
				recordButton.setText("Waiting Game");
				recordButton.setBackground(Color.YELLOW);
				watchButton.setText("Stop Watching"); 
				watchButton.setBackground(Color.RED);
				watchButton.setEnabled(true);
			}
		});
	}

	public void recordingStop() {
		SwingUtilities.invokeLater(new Runnable() { 
			public void run() {
				recordButton.setText("Record Games");
				recordButton.setBackground(Color.LIGHT_GRAY);
			}
		});
	}


	public void gameStarted() {
		SwingUtilities.invokeLater(new Runnable() { //watching stopped
			public void run() {
				recordButton.setText("Recording Game");
				recordButton.setBackground(Color.RED);
				watchButton.setBackground(new Color(80,0,0));
				watchButton.setEnabled(false); //record disabled
			}
		});
	}
}
