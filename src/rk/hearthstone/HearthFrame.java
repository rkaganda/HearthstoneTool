package rk.hearthstone;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class HearthFrame extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected HearthTool hearthTool;
	
	protected JMenuBar menubar;
	protected JMenu fileMenu, watchMenu;
	protected JMenuItem loadFileItem, startWatchItem, stopWatchItem;
	
	
	protected CardListView playerPlayed;
	protected CardListView opposingPlayed;

	protected JTextArea toolConsole = new JTextArea();
	
	public HearthFrame(HearthTool hT) {
		hearthTool = hT;
		
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
		startWatchItem = new JMenuItem("Start Watching Log");
		startWatchItem.addActionListener(this);
		watchMenu.add(startWatchItem);
		stopWatchItem = new JMenuItem("Stop Watching Log");
		stopWatchItem.setEnabled(false);
		stopWatchItem.addActionListener(this);
		watchMenu.add(stopWatchItem);
		menubar.add(fileMenu);
		menubar.add(watchMenu);
		setJMenuBar(menubar);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel,BoxLayout.Y_AXIS));
		this.getContentPane().add(contentPanel);
		
		
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
		
		contentPanel.add(cardViews);
		contentPanel.add(consolePanel);
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
		startWatchItem.setEnabled(false);
		stopWatchItem.setEnabled(true);
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
		}else if(e.getSource().equals(startWatchItem)) {
			final JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(this);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	            hearthTool.watchFile(file);
	        }
		}
	}


	public void addFriendlyCard(String string) {
		playerPlayed.addCard(string);
	}
	
	public void addOpposingCard(String string) {
		opposingPlayed.addCard(string);
	}
}
