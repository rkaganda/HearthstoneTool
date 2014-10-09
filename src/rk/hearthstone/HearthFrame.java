package rk.hearthstone;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

public class HearthFrame extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected HearthTool hearthTool;
	
	protected JMenuBar menubar;
	protected JMenu fileMenu, watchMenu;
	protected JMenuItem loadFileItem;
	
	protected JButton watchButton, recordButton;
	
	protected CardListView playerPlayed;
	protected CardListView opposingPlayed;
	
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
		recordButton = new JButton("Record Decks");
		recordButton.setEnabled(false);
		recordButton.addActionListener(this);
		toolBar.add(recordButton);
		
		JPanel cardViews = new JPanel();
		cardViews.setLayout(new BoxLayout(cardViews, BoxLayout.X_AXIS));
		playerPlayed = new CardListView("Player Played");
		opposingPlayed = new CardListView("Opposing Played");
		cardViews.add(playerPlayed);
		cardViews.add(Box.createHorizontalGlue());
		cardViews.add(opposingPlayed);
		
		
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
			watchButton.setText("Stop Watching");
			watchButton.setBackground(Color.RED);
			recordButton.setEnabled(true); //record enabled
		}else {
			watchButton.setText("Start Watching");
			watchButton.setBackground(Color.GREEN);
			recordButton.setEnabled(false); //record disabled
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
			if(recordButton.isEnabled() && isWatching ) { //sanity check
				hearthTool.startRecord(); //start record process 
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

	public void addFriendlyCard(String string) {
		playerPlayed.addCard(string);
	}
	
	public void addOpposingCard(String string) {
		opposingPlayed.addCard(string);
	}
}
