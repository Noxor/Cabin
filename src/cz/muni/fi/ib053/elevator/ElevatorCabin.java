package cz.muni.fi.ib053.elevator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;

///Predelat i spojeni s vyuzitim listeneru, jine udalosti aby se posilalo je serveru nebo GUI, dalsi PropertyChangeSupport,
// nebo obracene?? dum poslouchat udalosti od spojeni?
public class ElevatorCabin {

	private PropertyChangeSupport propChangeSup = new PropertyChangeSupport(
			this);
	private int levelCount;
	private int groundLevel;
	private int level;
	private String[] levelLabels;
	private LightState[] buttonLight;
	private CabinClient tcpClient;
	private boolean lightsOn;
	private int capacity;
	private int occupancy;
	private DoorState doorState;
	private CabinState cabinState;
	public static final String PANEL = "PANEL", DOOR = "DOOR", LIGHT = "LIGHT",
			BUTTON = "BUTTON";

	public ElevatorCabin(String[] labels, int capacity, int groundLevel,
			String server, int port) {
		if (labels.length == 0)
			throw new IllegalArgumentException(
					"Constructor of elevator cabin requires array of minimal length 1");

		levelCount = labels.length;
		levelLabels = new String[levelCount];
		System.arraycopy(labels, 0, levelLabels, 0, levelCount);

		buttonLight = new LightState[levelCount];
		Arrays.fill(buttonLight, LightState.DARK);

		this.capacity = capacity;
		occupancy = 0;

		level = -1; // Error state, should be rewritten by first message from
					// controller

		doorState = DoorState.CLOSE;
		cabinState = CabinState.STAND_EMPTY;

		tcpClient = new CabinClient(server, port, this);
		this.groundLevel = groundLevel;
	}

	public int getLevelCount() {
		return levelCount;
	}

	public int getGroundLevel() {
		return groundLevel;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getCapacity() {
		return capacity;
	}

	public int getOccupancy() {
		return occupancy;
	}

	// only method called from different Threads
	synchronized public void setDoorState(DoorState state) {
		DoorState old = doorState;
		this.doorState = state;
		propChangeSup.firePropertyChange(DOOR, old, doorState);// asi oboje bude
																// hayet udalost

		tcpClient.doorStateChanged(doorState);// ale do jineho listeneru
	}

	// je potreba?
	public DoorState getDoorState() {
		return doorState;
	}

	public LoadState getLoadState() {
		if (occupancy > capacity) {
			return LoadState.OVERLOAD;
		} else if (occupancy == capacity) {
			return LoadState.FULL;
		} else if (occupancy == 0) {
			return LoadState.EMPTY;
		} else {
			return LoadState.OCCUPIED;
		}
	}

	// mozna predelat, pamatovat occupancy LoadState v kabine, nebo metoda,so ho
	// vraci
	public void enter() {
		tcpClient.sensorAction();
		occupancy++;
		
		tcpClient.occupancyChanged(getLoadState());
	}

	public void leave() {
		if (occupancy <= 0)
			return;

		tcpClient.sensorAction();
		occupancy--;

		tcpClient.occupancyChanged(getLoadState());

	}

	public LightState getLightState(int level) {
		return buttonLight[level];
	}

	public String getLevelLabel(int level) {
		return levelLabels[level];
	}

	public boolean lightsOn() {
		return lightsOn;
	}

	public void turnOnTheLights() {
		boolean old = lightsOn;
		lightsOn = true;

		propChangeSup.firePropertyChange(LIGHT, old, lightsOn);
	}

	public void turnOffTheLights() {
		boolean old = lightsOn;
		lightsOn = false;

		propChangeSup.firePropertyChange(LIGHT, old, lightsOn);
	}

	public void lvlBtnPressed(int level) {
		tcpClient.lvlBtnPressed(level);
	}

	public void openBtnPressed() {
		tcpClient.openBtnPressed();
	}

	public void closeBtnPressed() {
		tcpClient.closeBtnPressed();
	}

	public void changeBtnLight(int level, LightState state) {
		if (level < 0 || level >= levelCount)
			throw new IllegalArgumentException();

		LightState old = buttonLight[level];
		buttonLight[level] = state;

		propChangeSup.fireIndexedPropertyChange(BUTTON, level, old,
				buttonLight[level]);
	}

	public void initializeConnection() {
		tcpClient.initialize();
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propChangeSup.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propChangeSup.removePropertyChangeListener(listener);
	}

	public enum LightState {
		DARK, SHINE, FLASH;
	}

	public enum LoadState {
		EMPTY, OCCUPIED, FULL, OVERLOAD;
	}

	public enum DoorState {
		OPEN, CLOSE, OPENING, CLOSING;
	}

	public enum CabinState {
		MOVE_UP, MOVE_DOWN, STAND_EMPTY, DOOR_OPEN;
	}

}
