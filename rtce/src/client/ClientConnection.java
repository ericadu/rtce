package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * This class holds the connection to the socket of the server. It handles the
 * connection and all incoming messages from the server. It also sends all
 * messages from the client side to the server.
 * 
 * 
 */
public class ClientConnection {
	private static final String ClientModel = null;
	private int caretPosition = 0;
	private ArrayList<String> deltas = new ArrayList<String>();
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;

	public ClientConnection(Socket socket) throws UnknownHostException,
			IOException {
		this.socket = socket;
		this.out = new PrintWriter(socket.getOutputStream(), true);
		this.in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));

	}

	public void setCaretPosition(int position) {
		caretPosition = position;
	}

	public BufferedReader getIn() {
		return this.in;
	}

	/**
	 * Sends a message to the server
	 * 
	 * @param message
	 */
	public void sendMessage(String message) {
		System.out.println(message);
		out.println(message);
		out.flush();
	}

	/**
	 * Disconnects from the server.
	 * 
	 * @return true if disconnection was performed correctly
	 */
	public boolean disconnect() {
		try {
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	// public String getDocument() {
	// return document;
	// }

	// /**
	// * Private method which handles the connection, sending out commands from
	// * the messageQueue and handling input commands using the private
	// * handleRequest function
	// *
	// * It will also send out an initial message to the server, as specified by
	// * the String argument.
	// *
	// * @throws IOException
	// */
	// public ArrayList<String> handleConnection(String initialMessage)
	// throws IOException {
	// deltas = new ArrayList<String>();
	// sendMessage(initialMessage);
	// for (String input = in.readLine(); input != null; input = in.readLine())
	// {
	//
	// //System.out.println("Client Side Receiving: " + input);
	// if (!input.equals("welcome")) {
	// String[] tokens = input.split(" ");
	// if (tokens.length == 4) {
	// document += " ";
	// } else {
	// document += tokens[4];
	// }
	// System.out.println(document);
	// }
	// deltas.add(input);
	// }
	//
	// return deltas;
	// }

}
