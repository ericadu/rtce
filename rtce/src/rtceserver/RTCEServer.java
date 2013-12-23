package rtceserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class RTCEServer {
	private final ServerSocket serverSocket;
	private final ConcurrentHashMap<String, ArrayList<String>> deltaLogs = new ConcurrentHashMap<String, ArrayList<String>>();
	private ArrayList<String> edits;
	private int version;

	public RTCEServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		edits = new ArrayList<String>();
		version = 0;
	}

	/**
	 * Run the server, listening for client connections and handling them. Never
	 * returns unless an exception is thrown.
	 * 
	 * @throws IOException
	 *             if the main server socket is broken (IOExceptions from
	 *             individual clients do *not* terminate serve()).
	 */
	public void serve() throws IOException {
		while (true) {
			// block until a client connects
			final Socket socket = serverSocket.accept();
			Thread thread = new Thread(new Runnable() {
				public void run() {
					// handle the client
					try {
						handleConnection(socket);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});

			thread.start();

		}
	}

	/**
	 * Handles a single client connection. Will either receive a "push" or
	 * "pull" message from client, and responds in 2 different ways. 1. PUSH:
	 * Server will add client's string to the server queue 2. PULL: Server will
	 * keep sending the client strings until client's version matches current
	 * server version
	 * 
	 * @param socket
	 *            socket where the client is connected
	 * @throws IOException
	 *             if connection has an error or terminates unexpectedly
	 */
	private void handleConnection(Socket socket) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

		try {
			for (String line = in.readLine(); line != null; line = in
					.readLine()) {

				String[] tokens = line.split("\\s");

				if (tokens[0].equals("pull")) {
					int clientVersion = Integer.parseInt(tokens[1]);
					int difference = version - clientVersion;

					for (int i = 0; i < difference; i++) {
						String currentEdit;
						synchronized (edits) {
							currentEdit = edits.get(clientVersion + i);
						}
						System.out.println("pulled: " + currentEdit);
						out.println(currentEdit);
						out.flush();
					}
				} else if (tokens[0].equals("push")) {
					System.out.println("handleConnection: " + line);
					handlePush(line, tokens);
				} else {
					System.out.println("invalid message from client");
				}

				// for (String edit : edits) {
				// System.out.println("Edit in the list server side: " + edit);
				// }

			}

		} finally {
			out.close();
			in.close();
		}
	}

	/**
	 * Handles
	 * 
	 * @param line
	 * @return
	 */
	private String handlePush(String line, String[] tokens) {
		// System.out.println("serve: vers numb in handle: " + version);
		// System.out.println(line);
		// System.out.println("server edits:" + edits);
		int clientVer = Integer.parseInt(tokens[1]);
		if (clientVer == version) {
			synchronized (edits) {
				edits.add(line);
				System.out.println("normal delta push");
			}
		} else {
			handleMerge(line);
			System.out.println("merged");
		}
		version++;
		return "";
	}

	private ArrayList<Object> getTokens(String delta) {
		int numSpaces = 4; // total # of spaces separating tokens
		int last_version_number = 0;
		int startIndex = 0;
		int length = 0;
		String replacement = "";
		String buffer = "";
		for (int i = 0; i < delta.length(); i++) {
			if (numSpaces == 0) {
				replacement = delta.substring(i);
				break;
			}
			if (delta.substring(i, i + 1).equals(" ")) {
				numSpaces += -1;
				if (numSpaces == 2) {
					last_version_number = Integer.parseInt(buffer);
				} else if (numSpaces == 1) {
					startIndex = Integer.parseInt(buffer);
				} else if (numSpaces == 0) {
					length = Integer.parseInt(buffer);
				}
				buffer = "";
				continue;
			}
			buffer += delta.substring(i, i + 1);
		}
		ArrayList<Object> tokens = new ArrayList<Object>();
		tokens.add(last_version_number);
		tokens.add(startIndex);
		tokens.add(length);
		tokens.add(replacement);
		return tokens;
	}

	private String handleMerge(String delta) {

		ArrayList<Object> tokens = getTokens(delta);
		int last_version_number = (Integer) tokens.get(0);
		int startIndex = (Integer) tokens.get(1);
		int length = (Integer) tokens.get(2);
		String replacement = (String) tokens.get(3);
		int difference = version - last_version_number;

		for (int i = 0; i < difference; i++) {
			String currentEdit;
			synchronized (edits) {
				System.out.println("Server: current verion server side:"
						+ version);
				System.out.println("Server: thisDelta's last_version_number: "
						+ last_version_number);
				currentEdit = edits.get(last_version_number + i);
			}
			ArrayList<Object> tempTokens = getTokens(currentEdit);
			int tempStartIndex = (Integer) tempTokens.get(1);
			int tempLength = (Integer) tempTokens.get(2);
			String tempReplacement = (String) tempTokens.get(3);
			int tempReplacementLength = tempReplacement.length();
			if (tempStartIndex <= startIndex) {
				if (tempStartIndex + tempLength > startIndex) {
					startIndex = tempStartIndex;
				} else {
					startIndex += tempReplacementLength - tempLength;
				}
			}
		}
		last_version_number += difference + 1;

		edits.add("push " + last_version_number + " " + startIndex + " "
				+ length + " " + replacement);
		return "merge success";
	}

	/**
	 * Start a document server.
	 */
	public static void main(int Port) {
		try {
			RTCEServer server = new RTCEServer(Port);
			server.serve();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
