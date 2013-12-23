package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientModel implements Runnable {

	// private ConcurrentMap<Integer, rtceGUI> rtceGUImap;
	private final int DELAY = 10;
	private int versionNum = 0;
	private ClientConnection connection;
	private String document = "";
	private static ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
	private final rtceGUI gui;
	private int caretPosition = 0;

	public ClientModel(ClientConnection connection, rtceGUI gui)
			throws IOException {
		this.connection = connection;
		this.gui = gui;
	}

	public void run() {

		new Thread(new ClientQueueRunnable()).start();
		new Thread(new StartClientRunnable()).start();

	}

	public class ClientQueueRunnable implements Runnable {
		public ClientQueueRunnable() {
		}

		public void run() {
			while (true) {

				while (!queue.isEmpty()) {
					// gui.lock();
					System.out.println("Servicing the queue!");
					String delta = queue.poll();
					handleDelta(delta);
					versionNum++;
					gui.setVersionNumber(versionNum);
					System.out.println("clientside vers number: " + versionNum);
					try {
						System.out.println("Thread Sleeping");
						Thread.sleep(DELAY);
						System.out.println("Thread Done Sleeping");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// gui.unlock();
				}
				System.out.println("client version num:" + versionNum);
				connection.sendMessage("pull " + versionNum);
				try {
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
	}

	// public void setClientText(String document) {
	// gui.getDocument().setText(document);
	// }

	/**
	 * Starts the client. Client requests a pull from the server every DELAY
	 * milliseconds. Sends the deltas it receives from the server to the
	 * client-side queue.
	 * 
	 * @throws IOException
	 */
	public class StartClientRunnable implements Runnable {
		public StartClientRunnable() {
		}

		public void run() {
			System.out.println("Client has started");
			// while (true) {
			try {
				BufferedReader in = connection.getIn();
				for (String input = in.readLine(); input != null; input = in
						.readLine()) {
					System.out.println("Received in clientmodel queue: "
							+ input);
					queue.add(input);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			// }
		}
	}

	public void handleDelta(String delta) {
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
		handleEdit(startIndex, length, replacement);
	}

	public void handleEdit(int startIndex, int length, String replacement) {
		if (replacement.equals("\u0015")) {
			replacement = "\n";
		}

		if (startIndex >= document.length()) { // startIndex should never be
												// greater
			document += replacement;

		} else {
			document = document.substring(0, startIndex) + replacement
					+ document.substring(startIndex + length);
			// assumes that given a string "abc" startIndex of 1 means
			// inserting/replacing starting after "a"
		}

		if (startIndex <= caretPosition) {
			if (startIndex + length > caretPosition) {
				caretPosition = startIndex;
			} else {
				caretPosition += replacement.length() - length;
			}
		}
		//document.replaceAll("\u0015", "\n");
		System.out.println("Client's Doc: " + document);
		// System.out.println("cpos " + gui.getDocument().getCaretPosition());
		// System.out.println("doc length " + document.length());

		gui.updateGUI(document, caretPosition);
		// gui.getDocument().setCaretPosition(
		// gui.getDocument().getDocument().getLength());

	}
	// /**
	// * Constantly checks to see if the queue is empty. If its not applies the
	// * edit to the document.
	// *
	// * @author AndrewHuang
	// *
	// */
	// public class ClientQueueRunnable imple ments Runnable {
	// public ClientQueueRunnable() {
	// }
	//
	// public void handleEdit(int startIndex, int length, String replacement) {
	// if (startIndex >= document.length()) { // startIndex should never be
	// // greater
	// document = document + replacement;
	// } else {
	// document = document.substring(0, startIndex) + replacement
	// + document.substring(startIndex + length);
	// // assumes that given a string "abc" startIndex of 1 means
	// // inserting/replacing starting after "a"
	// }
	// System.out.println("Client's Doc: " + document);
	//
	// }
	//
	// public void run() {
	// while (true) {
	// while (!queue.isEmpty()) {
	// int numSpaces = 3; // total # of spaces separating tokens
	// int last_version_number = 0;
	// int startIndex = 0;
	// int length = 0;
	// String replacement = "";
	// String buffer = "";
	// String delta = queue.poll();
	// for (int i = 0; i < delta.length(); i++) {
	// if (numSpaces == 0) {
	// replacement = delta.substring(i);
	// break;
	// }
	// if (delta.substring(i, i + 1).equals(" ")) {
	// numSpaces += -1;
	// if (numSpaces == 2) {
	// System.out.println("buffer: " + buffer);
	// last_version_number = Integer.parseInt(buffer);
	// System.out.println("buffer: " + buffer);
	// } else if (numSpaces == 1) {
	// startIndex = Integer.parseInt(buffer);
	// } else if (numSpaces == 0) {
	// length = Integer.parseInt(buffer);
	// }
	// buffer = "";
	// continue;
	// }
	// buffer += delta.substring(i, i + 1);
	// }
	// handleEdit(startIndex, length, replacement);
	// }
	//
	// }
	// }
	// }
}
