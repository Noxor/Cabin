package cz.muni.fi.ib053.elevator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import cz.muni.fi.ib053.elevator.ElevatorCabin.CabinState;
import cz.muni.fi.ib053.elevator.ElevatorCabin.LoadState;
import cz.muni.fi.ib053.elevator.ElevatorCabin.DoorState;
import cz.muni.fi.ib053.elevator.ElevatorCabin.LightState;

public class CabinClient implements PropertyChangeListener {

	private TCPConnection connection;
	private ElevatorCabin cabin;
	private boolean listening;

	public CabinClient(String server, int port, ElevatorCabin cabin) {
		listening = false;
		this.cabin = cabin;
		try {
			connection = new TCPConnection(server, port);
			start();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		cabin.addConnectionChangeListener(this); // pak odregistrovat
	}

	private void send(String message) {
		System.out.println("Cabin: Trying to send: " + message);
		try {
			connection.write(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void lvlBtnPressed(int level) {
		send("TLACITKO;" + level + "\n");
	}

	public void openBtnPressed() {
		send("TLACITKO_OTEVRI\n");
	}

	public void closeBtnPressed() {
		send("TLACITKO_ZAVRI\n");
	}

	public void sensorAction() {
		send("PRUCHOD\n");
	}
	
	public void errorState() {
		send("CHYBOVY_STAV;D\n");
	}

	public void occupancyChanged(LoadState occupancy) {
		switch (occupancy) {
		case EMPTY:
			send("ZATIZENI;0\n");
			break;
		case OCCUPIED:
			send("ZATIZENI;10\n");
			break;
		case FULL:
			send("ZATIZENI;100\n");
			break;
		case OVERLOAD:
			send("ZATIZENI;101\n");
			break;
		}
	}

	public void doorStateChanged(DoorState state) {
		switch (state) {
		case CLOSE:
			send("DVERE;Z\n");
			break;
		case OPEN:
			send("DVERE;O\n");
			break;
		default:
			; // nothing is send for opening & closing
		}
	}

	public void initialize() {
		send("INICIALIZACE;" + cabin.getLevelCount() + "\n");
	}
	
	public void start(){
		if(listening)
			return;
		listening = true;
		(new Thread(new TcpListeningThread())).start();;
	}
	
	public void stop(){
		listening = false;
	}
	
	public void quit(){
		stop();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// well...
		}
		try {
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {

		//System.out.println("Conn event " + event.getPropertyName());

		switch (event.getPropertyName()) {
		case ElevatorCabin.BUTTON:
			lvlBtnPressed((int)event.getNewValue());
			break;
		case ElevatorCabin.SENSOR:
			sensorAction();
			break;
		case ElevatorCabin.DOOR:
			doorStateChanged((DoorState)event.getNewValue());
			break;
		case ElevatorCabin.LOAD:
			occupancyChanged((LoadState)event.getNewValue());
			break;
		case ElevatorCabin.OPEN_BUTTON:
			openBtnPressed();
			break;
		case ElevatorCabin.CLOSE_BUTTON:
			closeBtnPressed();
			break;
		case ElevatorCabin.ERROR:
			errorState();
			break;
		}

	}

	private class TcpListeningThread implements Runnable {

		public TcpListeningThread() {
			// Nothing to do...
		}

		@Override
		public void run() {
			try {
				while (listening) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// well...
					}

					if (connection.readerReady()) {
						String str = connection.readLine();
						System.out.println("Cabin: Received: " + str);
						executeMessage(str);
					}
				}
			} catch (IOException e) {
				System.err.println("Connection problem");
			}
		}

		private void executeMessage(String message) {
			String[] tokens = message.split(";");
			if (tokens.length < 1)
				return;
			
			int level;

			switch (tokens[0]) {
			case "OTEVRI":
				cabin.setDoorState(DoorState.OPENING);
				break;
			case "ZAVRI":
				cabin.setDoorState(DoorState.CLOSING);
				break;
			case "ROZSVIT":
				cabin.turnOnTheLights();
				break;
			case "ZHASNI":
				cabin.turnOffTheLights();
				break;
			case "PANEL":
				if (tokens.length != 3)
					return; // maybe throw or log
				
				try {
					level = Integer.parseInt(tokens[2]);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					return;
				}
				cabin.setLevel(level);
				switch (tokens[1]) {
				case "N":
					cabin.setCabinState(CabinState.MOVE_UP);
					break;
				case "D":
					cabin.setCabinState(CabinState.MOVE_DOWN);
					break;
				case "S":
					cabin.setCabinState(CabinState.DOOR_OPEN);
					break;
				case "K":
					cabin.setCabinState(CabinState.STAND_EMPTY);
					break;
				case "P":
					cabin.setCabinState(CabinState.OVERLOAD);
					break;
				default:
					return; // +log something
				}
				break;
			case "INDIKACE":
				if (tokens.length != 3)
					return; // maybe throw or log
				
				try {
					level = Integer.parseInt(tokens[1]);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					return;
				}
				switch (tokens[2]) {
				case "S":
					cabin.changeBtnLight(level, LightState.SHINE);
					break;
				case "N":
					cabin.changeBtnLight(level, LightState.DARK);
					break;
				case "B":
					cabin.changeBtnLight(level, LightState.FLASH);
					break;
				default:
					return; // +log something
				}
				break;
			default: // log something
			}
		}
	}
}