package cz.muni.fi.ib053.elevator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

//ponechano temer v puvodnim stavu, protoze se zda, ze to funguje
public class TCPConnection {

	private Socket socket;
	private BufferedReader reader;
	private BufferedWriter writer;
	private InputStreamReader isReader;
	private OutputStreamWriter osWriter;

	private void createReaderWriter() throws IOException {
		isReader = new InputStreamReader(socket.getInputStream());
		reader = new BufferedReader(isReader);
		osWriter = new OutputStreamWriter(socket.getOutputStream());
		writer = new BufferedWriter(osWriter);
	}

	public TCPConnection(Socket socket) throws IOException {
		this.socket = socket;
		createReaderWriter();
	}

	public TCPConnection(String server, int port) throws UnknownHostException, IOException {
		try{		
			socket = new Socket(server, port);
		}catch(ConnectException e)
		{
			//chyti se pozdeji null pointer exception, tuhle vyjimku z nejakeho duvodu nejde chytit
			//nikde jinde. NullPointerException bude vyhozena nasledujici metodou
		}
		createReaderWriter();
	}

	public void write(String str) throws IOException {
		writer.append(str);
		writer.flush();
	}

	public String readLine() throws IOException {
		return reader.readLine();
	}

	public boolean readerReady() throws IOException {
		return socket.getInputStream().available() > 0 || reader.ready();
	}

	public void close() throws IOException {
		reader.close();
		writer.close();
		socket.close();
	}

}
