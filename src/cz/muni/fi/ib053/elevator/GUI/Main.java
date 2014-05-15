package cz.muni.fi.ib053.elevator.GUI;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.net.ConnectException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;

import cz.muni.fi.ib053.elevator.CabinClient;
import cz.muni.fi.ib053.elevator.ElevatorCabin;

public class Main {
	public static void main(String[] args) {

		// neni nejhezci, ale jinak nejde jednou metodou inicializovat oba
		// obekty
		// asi lepsi nez dvakat otevirat soubor
		Object[] cabinAndClient = executeSettingFile("SETTINGS.txt");
		
		if (cabinAndClient == null || cabinAndClient.length < 2)
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// well...
			}
			System.exit(1);
			return;
		}

		ElevatorCabin cabin;
		try {
			cabin = (ElevatorCabin) cabinAndClient[0];
		} catch (IllegalArgumentException e) {
			return;
		}
		
		CabinClient client = (CabinClient) cabinAndClient[1];

		Frame jms = new Frame(cabin, client);
		jms.setSize(new Dimension(500, 500));
		jms.setVisible(true);
		jms.setResizable(false);
	}

	private static Object[] executeSettingFile(String fileName) {
		String server = "";
		int port = 0;
		int levels;
		String[] btnLabels;
		int capacity;

		try {
			List<String> lines = Files.readAllLines(Paths.get(fileName),
					Charset.defaultCharset());
			String[] variables = lines.get(0).split(";");
			server = variables[0];
			port = Integer.parseInt(variables[1]);
			levels = Integer.parseInt(variables[2]);
			capacity = Integer.parseInt(variables[3]);
			btnLabels = lines.get(1).split(";");

			if (btnLabels.length != levels) {
				throw new Exception();
			}
		} catch (Exception e) {

			JDialog dialog = new JDialog();
			JLabel label = new JLabel(
					"Chybi soubor SETTING.txt nebo neni ve spravnem formatu");
			dialog.setLocationRelativeTo(null);
			dialog.setTitle("ERROR");
			dialog.add(label);
			dialog.pack();

			dialog.setVisible(true);

			return null;
		}
		
		ElevatorCabin cabin = new ElevatorCabin(btnLabels, capacity);
		CabinClient client;
		
		try{
			client = new CabinClient(server, port, cabin);
		} catch (Exception e) {

			JDialog dialog = new JDialog();
			JLabel label = new JLabel(
					"Nejdrive je treba spustit server.");
			dialog.setLocationRelativeTo(null);
			dialog.setTitle("ERROR");
			dialog.add(label);
			dialog.pack();

			dialog.setVisible(true);

			return null;
		}

		Object[] out = new Object[] { cabin, client };
		return out;
	}

}
