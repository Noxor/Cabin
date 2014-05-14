package cz.muni.fi.ib053.elevator.GUI;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;
import cz.muni.fi.ib053.elevator.CabinClient;
import cz.muni.fi.ib053.elevator.ElevatorCabin;
import cz.muni.fi.ib053.elevator.ElevatorCabin.CabinState;
import cz.muni.fi.ib053.elevator.ElevatorCabin.DoorState;
import cz.muni.fi.ib053.elevator.ElevatorCabin.LightState;


//trida obsluhuje GUI a specialni vlakno, ktere hybe dvermi a blika svetly 
public class Frame extends JFrame implements PropertyChangeListener,
		WindowListener {
	private ElevatorCabin cabin;
	private CabinClient client;
	private JPanel mainPanel;
	private JPanel iOPanel;
	private JPanel cabinPanel;
	private JButton[] lvlButtons;
	private JToggleButton[] people;
	private JButton doorClose, doorOpen;
	private JLabel levelLabel;
	private JLabel stateLabel;
	private ImageIcon closeImg;
	private ImageIcon openImg;
	private ImageIcon idleImage;
	private ImageIcon bleepImage;
	private ImageIcon downImage;
	private ImageIcon upImage;
	private ImageIcon cleanImage;
	private ImageIcon overloadImage;
	private ImageIcon personImage;
	private ImageIcon noPersonImage;
	private JPanel doorPosts;
	private JPanel doorPanel;
	private ConcurrentLinkedQueue<String> doorQueue;

	public Frame(ElevatorCabin cabin, CabinClient client) {
		addWindowListener(this);

		this.cabin = cabin;
		this.client = client;
		this.doorQueue = new ConcurrentLinkedQueue<String>();
		(new Thread(new GUIMovingThread())).start();
		mainPanel = new JPanel();
		mainPanel.setLayout(new MigLayout("", "", ""));
		this.add(mainPanel);
		
		//panel s tlacitky
		iOPanel = new JPanel();
		iOPanel.setLayout(new MigLayout("", "", ""));		
		JScrollPane scrollPanel = new JScrollPane(iOPanel);		
		mainPanel.add(scrollPanel, "width 130::130");		
		
		//panel s kabinou a lidmi
		cabinPanel = new JPanel();
		cabinPanel.setLayout(new MigLayout("", "", ""));
		mainPanel.add(cabinPanel, "width 130::");
		

		closeImg = new ImageIcon("res/BUTTON_CLOSE.png");
		openImg = new ImageIcon("res/BUTTON_OPEN.png");
		idleImage = new ImageIcon("res/BUTTON_OFF.png");
		bleepImage = new ImageIcon("res/BUTTON_ON.png");
		downImage = new ImageIcon("res/SIGN_DOWN.png");
		upImage = new ImageIcon("res/SIGN_UP.png");
		cleanImage = new ImageIcon("res/EMPTY.png");
		overloadImage = new ImageIcon("res/SIGN_OVERLOAD.png");
		personImage = new ImageIcon("res/PERSON_YES.png");
		noPersonImage = new ImageIcon("res/PERSON_NO.png");

		//pokud kabina zacina prazdna a nehybe se, jednodussi nez dlouhy switch
		stateLabel = new JLabel(cleanImage);
		iOPanel.add(stateLabel);

		levelLabel = new JLabel(cabin.getLevelLabel(cabin.getLevel()));
		iOPanel.add(levelLabel, "alignx center, wrap");
		levelLabel.setForeground(Color.BLUE);

		//tlacitka pro patra
		lvlButtons = new JButton[cabin.getLevelCount()];
		for (int i = cabin.getLevelCount() - 1; i >= 0; i--) {
			lvlButtons[i] = new JButton(idleImage);
			lvlButtons[i].setBorder(BorderFactory.createEmptyBorder());

			lvlButtons[i].addActionListener(new LevelButtonListener(i));

			iOPanel.add(new JLabel(cabin.getLevelLabel(i)), "alignx center");
			iOPanel.add(lvlButtons[i], "wrap");
		}

		//tlacitka pro ovladani dveri
		doorOpen = new JButton(openImg);
		doorOpen.setBorder(BorderFactory.createEmptyBorder());
		doorOpen.addActionListener(new OpenButtonListener());
		iOPanel.add(doorOpen);

		doorClose = new JButton(closeImg);
		doorClose.setBorder(BorderFactory.createEmptyBorder());
		doorClose.addActionListener(new CloseButtonListener());
		iOPanel.add(doorClose);
		

		//pokud je na zacatku zhasnuto
		cabinPanel.setBackground(Color.BLACK);

		//dvere
		doorPosts = new JPanel();
		doorPosts.setLayout(new MigLayout("", "", ""));
		doorPosts
				.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
		doorPosts.setBackground(Color.BLACK);
		cabinPanel.add(doorPosts,
				"width 200::200, height 300::300, wrap,alignx center");

		doorPanel = new JPanel();
		doorPanel.setBackground(Color.BLUE);
		doorPosts.add(doorPanel,
				"width 0::0, height 285::285, wrap, alignx center,push");

		
		//lidi
		JPanel peoplePanel = new JPanel();
		peoplePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1,
				true));
		cabinPanel.add(peoplePanel, "alignx center");
		
		people = new JToggleButton[5];
		for (int i = 0; i < people.length; i++) {
			people[i] = new JToggleButton(noPersonImage);
			people[i].setBorder(BorderFactory.createEmptyBorder());

			people[i].addActionListener(new PeopleButtonListener(people[i]));

			peoplePanel.add(people[i]);
		}

		cabin.addGUIChangeListener(this);
		
		//nechat nakonci jinak prichazi zpravy nehotovemu GUI
		this.client.initialize();
	}

	
	//LISTENERY
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

		private JToggleButton button;

		public PeopleButtonListener(JToggleButton button) {
			this.button = button;
		}

		public void actionPerformed(ActionEvent e) {

			if (button.isSelected()) {
				if (cabin.enter())
					button.setIcon(personImage);
				else
					button.setSelected(!button.isSelected());
			} else {
				if (cabin.leave())
					button.setIcon(noPersonImage);
				else
					button.setSelected(!button.isSelected());
			}

			doorQueue.add("SENSOR");
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {

		System.out.println("GUI event " + event.getPropertyName());

		switch (event.getPropertyName()) {
		case ElevatorCabin.LIGHT:
			if ((boolean) event.getNewValue())
				cabinPanel.setBackground(Color.YELLOW);
			else
				cabinPanel.setBackground(Color.BLACK);
			break;
		case ElevatorCabin.DOOR:
			if ((DoorState) event.getNewValue() == DoorState.OPENING) {
				doorQueue.add("OPENING");
			} else if ((DoorState) event.getNewValue() == DoorState.CLOSING) {
				doorQueue.add("CLOSING");
			}
			break;
		case ElevatorCabin.BUTTON:
			int index = ((IndexedPropertyChangeEvent) event).getIndex();
			if ((LightState) event.getNewValue() == LightState.SHINE) {
				lvlButtons[index].setIcon(bleepImage);
			} else if ((LightState) event.getNewValue() == LightState.DARK) {
				lvlButtons[index].setIcon(idleImage);
			}
			break;
		case ElevatorCabin.LEVEL:
			levelLabel.setText(cabin.getLevelLabel(cabin.getLevel()));
			break;
		case ElevatorCabin.STATE:
			if (cabin.getCabinState() == CabinState.MOVE_UP) {
				stateLabel.setIcon(upImage);
			} else if (cabin.getCabinState() == CabinState.MOVE_DOWN) {
				stateLabel.setIcon(downImage);
			} else if (cabin.getCabinState() == CabinState.OVERLOAD) {
				stateLabel.setIcon(overloadImage);
			} else {
				stateLabel.setIcon(cleanImage);
			}
			break;
		}
	}

	//vlakno obsluhujici dvere a svetla
	private class GUIMovingThread implements Runnable {

		private int counter;
		private int progress;
		private boolean lightUp;
		private boolean opening;
		private boolean closing;
		private boolean doorMoved;
		private final int maxProgress = 185;

		public GUIMovingThread() {
			lightUp = false;
			opening = false;
			closing = false;
			doorMoved = false;
		}

		@Override
		public void run() {

			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// well...
				}

				if (counter == 5) {
					for (int i = 0; i < cabin.getLevelCount(); i++) {
						if (cabin.getLightState(i) == LightState.FLASH) {
							counter = 0;
							if (lightUp) {
								lvlButtons[i].setIcon(idleImage);
							} else {
								lvlButtons[i].setIcon(bleepImage);
							}
						}
					}
					counter = 0;
					lightUp = !lightUp;
				}

				if (!doorQueue.isEmpty()) {
					String command = doorQueue.poll();
					if (command.equals("OPENING")
							&& cabin.getDoorState() != DoorState.OPEN) {
						closing = false;
						opening = true;
					}
					if (command.equals("CLOSING")
							&& cabin.getDoorState() != DoorState.CLOSE) {
						opening = false;
						closing = true;
					}
					if (command.equals("SENSOR") && closing) {
						closing = false;
						opening = true;
					}
				}
				if (opening) {
					if (progress >= 185) {
						opening = false;
						cabin.setDoorState(DoorState.OPEN);
						continue;
					}
					progress += 5;
					doorMoved = true;
				}
				if (closing) {
					if (progress <= 0) {
						closing = false;
						cabin.setDoorState(DoorState.CLOSE);
						continue;
					}
					progress -= 5;
					doorMoved = true;
				}

				if (doorMoved) {
					doorPosts.removeAll();
					doorPosts.add(doorPanel, "width " + progress + "::"
							+ progress
							+ ", height 285::285, wrap, alignx center,push");
					doorPosts.validate();
				}

				doorMoved = false;
				counter++;
			}
		}

	}

	// musi byt vsechny, aby bylo implementovano rozhrani
	@Override
	public void windowClosing(WindowEvent arg0) {
		client.quit();
		System.exit(0); // jinak neskonci
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
