package cz.muni.fi.ib053.elevator.GUI;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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

public class Frame extends JFrame implements PropertyChangeListener, WindowListener {
	private ElevatorCabin cabin;
	private CabinClient client;
	private JPanel mainPanel;
	private JButton[] lvlButtons;
	private JButton doorClose, doorOpen;
	private JLabel lightBulb; // temporal from now on down
	private JButton enter, leave;
	private JLabel peopleLabel;
	private JLabel doorLabel;
	private JLabel levelLabel;
	private JLabel stateLabel;

	public Frame(ElevatorCabin cabin, CabinClient client) {		
		this.cabin = cabin;
		this.client = client;
		mainPanel = new JPanel();
		this.add(mainPanel);
		
		addWindowListener(this);
		
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

		
		levelLabel = new JLabel("Level: " + cabin.getLevel());
		mainPanel.add(levelLabel);
		
		stateLabel = new JLabel("State: " + cabin.getCabinState());
		mainPanel.add(stateLabel);
		
		cabin.addGUIChangeListener(this); // pak hlavne odregistrovat
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
		case ElevatorCabin.LEVEL:
			levelLabel.setText("Level: " + cabin.getLevel());
			break;
		case ElevatorCabin.STATE:
			stateLabel.setText("State: " + cabin.getCabinState());
			break;
		}
	}
	
	///Follows WindowListener methods
	@Override
	public void windowClosing(WindowEvent arg0) {
		//client.stop();
		System.exit(0); //no need to call stop, thread ends anyway
	}
	
	@Override
	public void windowClosed(WindowEvent arg0) {
		// nada	
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// nada		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// nada		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// nada		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// nada		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// nada		
	}

}
