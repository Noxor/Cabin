package cz.muni.fi.ib053.elevator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;

///Predelat i spojeni s vyuzitim listeneru, jine udalosti aby se posilalo je serveru nebo GUI, dalsi PropertyChangeSupport,
// nebo obracene?? dum poslouchat udalosti od spojeni? zkusit upne nove udalosti
// http://castever.wordpress.com/2008/07/31/how-to-create-your-own-events-in-java/
// http://stackoverflow.com/questions/6270132/create-a-custom-event-in-java
// http://www.codeproject.com/Articles/677591/Defining-Custom-Source-Event-Listener-in-Java  asi nejlepsi
// http://www.java2s.com/Code/Java/Event/CreatingaCustomEvent.htm ofiko
public class ElevatorCabin {

	private PropertyChangeSupport eventsForGUI;
	private PropertyChangeSupport eventsForConnection;
	private int levelCount;
	private int groundLevel;
	private int level;
	private String[] levelLabels;
	private LightState[] buttonLight;
	private boolean lightsOn;
	private int capacity;
	private int occupancy;
	private DoorState doorState;
	private CabinState cabinState;
	public static final String LEVEL = "LEVEL", DOOR = "DOOR", LIGHT = "LIGHT",
			BUTTON = "BUTTON", SENSOR = "SENSOR", LOAD = "LOAD",
			OPEN_BUTTON = "OPEN_BUTTON", CLOSE_BUTTON = "CLOSE_BUTTON",
			STATE = "STATE";

	public ElevatorCabin(String[] labels, int capacity, int groundLevel,
			String server, int port) {
		if (labels.length == 0)
			throw new IllegalArgumentException(
					"Constructor of elevator cabin requires array of minimal length 1");

		eventsForGUI = new PropertyChangeSupport(this);
		eventsForConnection = new PropertyChangeSupport(this);

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
		if(level < 0 || level >= levelCount)
			return;//log
		int old = this.level;
		this.level = level;
		eventsForGUI.firePropertyChange(LEVEL, old, level);		
	}

	public CabinState getCabinState() {
		return cabinState;
	}

	public void setCabinState(CabinState state) {
		CabinState old = cabinState;
		this.cabinState = state;
		eventsForGUI.firePropertyChange(STATE, old, cabinState);	
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
		eventsForGUI.firePropertyChange(DOOR, old, doorState);

		eventsForConnection.firePropertyChange(DOOR, old, doorState);
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
		sensorEvent(1);
	}

	public void leave() {
		if (occupancy <= 0)
			return;

		sensorEvent(-1);
	}

	private void sensorEvent(int change) {
		eventsForConnection.firePropertyChange(SENSOR, -2, -1);
		LoadState old = getLoadState();
		occupancy += change;

		eventsForConnection.firePropertyChange(LOAD, old, getLoadState());
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

		eventsForGUI.firePropertyChange(LIGHT, old, lightsOn);
	}

	public void turnOffTheLights() {
		boolean old = lightsOn;
		lightsOn = false;

		eventsForGUI.firePropertyChange(LIGHT, old, lightsOn);
	}

	public void lvlBtnPressed(int level) {
		eventsForConnection.firePropertyChange(BUTTON, -1, level);
	}

	public void openBtnPressed() {
		eventsForConnection.firePropertyChange(OPEN_BUTTON, -2, -1);
	}

	public void closeBtnPressed() {
		eventsForConnection.firePropertyChange(CLOSE_BUTTON, -2, -1);
	}

	public void changeBtnLight(int level, LightState state) {
		if (level < 0 || level >= levelCount)
			throw new IllegalArgumentException();

		LightState old = buttonLight[level];
		buttonLight[level] = state;

		eventsForGUI.fireIndexedPropertyChange(BUTTON, level, old,
				buttonLight[level]);
	}

	public void addGUIChangeListener(PropertyChangeListener listener) {
		eventsForGUI.addPropertyChangeListener(listener);
	}

	public void removeGUIChangeListener(PropertyChangeListener listener) {
		eventsForGUI.removePropertyChangeListener(listener);
	}

	public void addConnectionChangeListener(PropertyChangeListener listener) {
		eventsForConnection.addPropertyChangeListener(listener);
	}

	public void removeConnectionChangeListener(PropertyChangeListener listener) {
		eventsForConnection.removePropertyChangeListener(listener);
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
		MOVE_UP, MOVE_DOWN, STAND_EMPTY, DOOR_OPEN, OVERLOAD;
	}

}
