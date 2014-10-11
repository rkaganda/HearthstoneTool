package rk.hearthstone.ui;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import rk.hearthstone.model.HearthstoneCard;
import rk.hearthstone.model.HearthstoneCardZone;
import rk.hearthstone.model.HearthstoneCardZoneListener;

public class CardZoneView extends JPanel implements HearthstoneCardZoneListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected JList<String> cardList;
	protected JLabel listLabel;
	protected DefaultListModel<String> listModel;
	protected HearthstoneCardZone zone;
	
	public CardZoneView(HearthstoneCardZone z) {
		listLabel = new JLabel(z.getName());
		listModel = new DefaultListModel<String>();
		
		cardList = new JList<String>(listModel);
		
		zone = z;
		zone.addListener(this);
		
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

	@Override
	public void cardAdded(HearthstoneCard card) {
		//TODO fix and render
		resetListModel();
	}

	@Override
	public void cardRemoved(HearthstoneCard card) {
		//TODO fix and render
		resetListModel();
	}
	
	
	protected void resetListModel() {
		listModel.removeAllElements();
		for(HearthstoneCard card:zone.getCards()) {
			if(card.get("name").equals("unknown")) {
				listModel.addElement(card.get("id"));
			}else {
				listModel.addElement(card.get("name"));
			}
		}
	}
}
