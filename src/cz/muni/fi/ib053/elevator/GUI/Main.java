package cz.muni.fi.ib053.elevator.GUI;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
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
		// TODO: propper initialization
		ElevatorCabin cabin = null;
		CabinClient client = null;
		Object[] cabinAndClient = executeSettingFile("SETTINGS.txt");
		if(cabinAndClient == null || cabinAndClient.length <2)
			return;
		
		cabin = (ElevatorCabin)cabinAndClient[0];
		client = (CabinClient)cabinAndClient[1];

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
		int groundLevel;
		int capacity;

		try {
			List<String> lines = Files.readAllLines(Paths.get(fileName),
					Charset.defaultCharset());
			String[] variables = lines.get(0).split(";");
			server = variables[0];
			port = Integer.parseInt(variables[1]);
			levels = Integer.parseInt(variables[2]);
			groundLevel = Integer.parseInt(variables[3]);
			capacity = Integer.parseInt(variables[4]);
			btnLabels = lines.get(1).split(";");

			if (btnLabels.length != levels) {
				throw new Exception();
			}
		} catch (Exception e) {

			JDialog dialog = new JDialog();
			JLabel label = new JLabel(
					"SETTING.txt file is missing or contains wrong data.");
			dialog.setLocationRelativeTo(null);
			dialog.setTitle("ERROR");
			dialog.add(label);
			dialog.pack();

			dialog.setVisible(true);

			return null;
		}
		ElevatorCabin cabin = new ElevatorCabin(btnLabels, capacity, groundLevel);
		CabinClient client = new CabinClient(server, port, cabin);
		
		Object[] out = new Object[] {cabin,client};
		return out;
	}

}
