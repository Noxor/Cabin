package cz.muni.fi.ib053.elevator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import cz.muni.fi.ib053.elevator.ElevatorCabin.LoadState;
import cz.muni.fi.ib053.elevator.ElevatorCabin.DoorState;
import cz.muni.fi.ib053.elevator.ElevatorCabin.LightState;

public class CabinClient implements PropertyChangeListener {

	private TCPConnection connection;
	private ElevatorCabin cabin;

	public CabinClient(String server, int port, ElevatorCabin cabin) {
		this.cabin = cabin;
		try {
			connection = new TCPConnection(server, port);
			(new Thread(new TcpListeningThread())).start();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		cabin.addConnectionChangeListener(this); // pak odregistrovat
		//cabin.addGUIChangeListener(this);
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
		case ElevatorCabin.INITIALIZE:
			initialize();
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
				while (true) {
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
				break;
			case "INDIKACE":
				if (tokens.length != 3)
					return; // maybe throw or log
				int level;
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