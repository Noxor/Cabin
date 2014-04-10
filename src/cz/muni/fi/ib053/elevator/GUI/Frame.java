package cz.muni.fi.ib053.elevator.GUI;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cz.muni.fi.ib053.elevator.CabinClient;
import cz.muni.fi.ib053.elevator.ElevatorCabin;
import cz.muni.fi.ib053.elevator.ElevatorCabin.DoorState;
import cz.muni.fi.ib053.elevator.ElevatorCabin.LightState;

public class Frame extends JFrame implements PropertyChangeListener {
	private ElevatorCabin cabin;
	private JPanel mainPanel;
	private JButton[] lvlButtons;
	private JButton doorClose, doorOpen;
	private JLabel lightBulb; // temporal from now on down
	private JButton enter, leave;
	private JLabel peopleLabel;
	private JLabel doorLabel;

	public Frame(ElevatorCabin cabin) {		
		this.cabin = cabin;
		mainPanel = new JPanel();
		this.add(mainPanel);
		cabin.addGUIChangeListener(this); // pak hlavne odregistrovat

		lvlButtons = new JButton[cabin.getLevelCount()];
		for (int i = 0; i < cabin.getLevelCount(); i++) {
			lvlButtons[i] = new JButton("Button " + cabin.getLevelLabel(i));

			lvlButtons[i].addActionListener(new LevelButtonListener(i));

			mainPanel.add(lvlButtons[i]);
		}

		doorClose = new JButton("Zavri dvere");
		doorOpen = new JButton("Otevri dvere");

		doorClose.addActionListener(new CloseButtonListener());
		doorOpen.addActionListener(new OpenButtonListener());

		mainPanel.add(doorClose);
		mainPanel.add(doorOpen);

		// TODO re-do bellow completely
		lightBulb = new JLabel();
		lightBulb.setText("Light Bulb");
		lightBulb.setBackground(Color.BLACK);
		lightBulb.setForeground(Color.WHITE);
		lightBulb.setOpaque(true);

		mainPanel.add(lightBulb);

		// ////////////////
		enter = new JButton("+");
		leave = new JButton("-");
		peopleLabel = new JLabel("People: " + cabin.getOccupancy());

		enter.addActionListener(new PeopleButtonListener(1));
		leave.addActionListener(new PeopleButtonListener(-1));

		doorLabel = new JLabel("DOOR: CLOSED");

		mainPanel.add(leave);
		mainPanel.add(peopleLabel);
		mainPanel.add(enter);

		cabin.initializeConnection();
		
	}

	private class LevelButtonListener implements ActionListener {
		private int level;

		public LevelButtonListener(int level) {
			this.level = level;
		}

		public void actionPerformed(ActionEvent e) {
			cabin.lvlBtnPressed(level);
		}
	}

	private class CloseButtonListener implements ActionListener {

		public CloseButtonListener() {
		}

		public void actionPerformed(ActionEvent e) {
			cabin.closeBtnPressed();
		}
	}

	private class OpenButtonListener implements ActionListener {

		public OpenButtonListener() {
		}

		public void actionPerformed(ActionEvent e) {
			cabin.openBtnPressed();
		}
	}

	private class PeopleButtonListener implements ActionListener {
		int change;

		public PeopleButtonListener(int change) {
			this.change = change;
		}

		public void actionPerformed(ActionEvent e) {
			if (cabin.getOccupancy() + change < 0
					|| cabin.getOccupancy() + change >= 6)
				return;

			if (change == 1) {
				cabin.enter();
			} else {
				cabin.leave();
			}
			peopleLabel.setText("People: " + cabin.getOccupancy());
			// mainPanel.validate(); //asi netreba
			// mainPanel.repaint();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
	
		System.out.println("GUI event " + event.getPropertyName());

		switch (event.getPropertyName()) {
		case ElevatorCabin.LIGHT:
			if ((boolean) event.getNewValue())
				lightBulb.setBackground(Color.YELLOW);
			else
				lightBulb.setBackground(Color.BLACK);
			break;
		case ElevatorCabin.DOOR: // /start separate Thread for opening/closing -
									// REDO
			if ((DoorState) event.getNewValue() == DoorState.OPENING) {
				cabin.setDoorState(DoorState.OPEN);
				doorLabel.setText("DOOR: OPENED");
			} else if ((DoorState) event.getNewValue() == DoorState.CLOSING) {
				cabin.setDoorState(DoorState.CLOSE);
				doorLabel.setText("DOOR: CLOSED");
			}
			break;
		case ElevatorCabin.BUTTON:
			int index = ((IndexedPropertyChangeEvent) event).getIndex();
			if ((LightState) event.getNewValue() == LightState.SHINE) {
				lvlButtons[index].setBackground(Color.YELLOW);
			} else if ((LightState) event.getNewValue() == LightState.FLASH) {
				lvlButtons[index].setBackground(Color.ORANGE);
			} else {
				lvlButtons[index].setBackground(null);
			}

			break;
		case ElevatorCabin.PANEL:
			break;
		}
	}

}
