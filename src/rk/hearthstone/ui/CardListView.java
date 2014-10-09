package rk.hearthstone.ui;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class CardListView extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected JList<String> cardList;
	protected JLabel listLabel;
	protected DefaultListModel<String> listModel;
	
	public CardListView(String l) {
		listLabel = new JLabel(l);
		listModel = new DefaultListModel<String>();
		
		cardList = new JList<String>(listModel);
		
		
		
		initUI();
	}
	
	protected void initUI() {
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		cardList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cardList.setLayoutOrientation(JList.VERTICAL);
		cardList.setVisibleRowCount(3);
		
		JScrollPane listScroller = new JScrollPane(cardList);
		listScroller.setPreferredSize(new Dimension(80, 250));
		
		add(listLabel);
		add(listScroller);
	}
	
	public void addCard(String c) {
		listModel.addElement(c);
	}
	
	public void removeCard(String c) {
		listModel.removeElement(c);
	}
}
