package client;

import java.io.IOException;
import java.net.Socket;
import javax.swing.SwingWorker;

/**
 * A swing worker class which performs a login operation in the background as
 * the gui waits for a response. First, it gets the connection with a socket on
 * the server, then it sends a login user command.
 */
public class LoginWorker extends SwingWorker<Void, Void> {

	private String username;
	private int portNumber;
	private String hostName;
	private Socket socket;
	private rtceStartGUI rtceStartGUI;

	public LoginWorker(String username, int portNumber, String hostName,
			rtceStartGUI rtceStartGUI) {
		this.username = username;
		this.portNumber = portNumber;
		this.hostName = hostName;
		this.rtceStartGUI = rtceStartGUI;

	}

	protected Void doInBackground() throws Exception {

		this.socket = new Socket(hostName, portNumber);
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					ClientConnection connection = new ClientConnection(socket);
					synchronized (rtceStartGUI) {
						rtceStartGUI.setConnection(connection);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		thread.start();
		return null;
	}

	public Socket getSocket() {
		return socket;
	}
}
