package rk.hearthstone.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import rk.hearthstone.model.HearthstoneCard;

public class CardZoneCellRenderer extends JLabel implements ListCellRenderer<Object>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<? extends Object> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		HearthstoneCard card = (HearthstoneCard)value;
				
		if(card.get("name")!=null) {
			setText(card.get("name"));
		}else {
			setText(card.get("id"));
		}
		

        Color background;
        Color foreground;

        // check if this cell represents the current DnD drop location
        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsert()
                && dropLocation.getIndex() == index) {

            background = Color.BLUE;
            foreground = Color.WHITE;

        // check if this cell is selected
        } else if (isSelected) {
            background = Color.BLACK;
            foreground = Color.WHITE;
            System.out.println("cell");

        // unselected, and not the DnD drop location
        } else {
            background = Color.WHITE;
            foreground = Color.BLACK;
        };

        setBackground(background);
        setForeground(foreground);

        return this;
	}
}
