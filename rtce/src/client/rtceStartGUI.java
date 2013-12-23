package client;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;

/**
 * The StartGUI class -- creates the starting interface for any user that opens
 * our application. Contains a field for a username and a document ID to connect
 * to an existing document or to create a new one.
 * 
 * @author Connie Huang
 */

public class rtceStartGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private String username;
	private ClientConnection clientConnection;
	private int port;
	private String server;

	private javax.swing.JLabel editrTitle;
	private javax.swing.JButton joinButton;
	private javax.swing.JTextField portNumber;
	private javax.swing.JLabel portNumberLabel;
	private javax.swing.JTextField serverName;
	private javax.swing.JLabel serverNameLabel;
	private javax.swing.JTextField userName;
	private javax.swing.JLabel userNameLabel;

	public rtceStartGUI() {
		super("Editr Login");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		initComponents();
	}

	private void initComponents() {

		editrTitle = new javax.swing.JLabel();
		joinButton = new javax.swing.JButton();
		userName = new javax.swing.JTextField();
		portNumber = new javax.swing.JTextField();
		serverName = new javax.swing.JTextField();
		userNameLabel = new javax.swing.JLabel();
		serverNameLabel = new javax.swing.JLabel();
		portNumberLabel = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		editrTitle.setFont(new java.awt.Font("Verdana", 0, 36)); // NOI18N
		editrTitle.setText("editr");

		joinButton.setText("Join");

		joinButton.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				joinButtonMouseClicked(evt);
			}
		});
		portNumber.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {

			}

			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					joinButtonEnterPressed(e);
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		joinButton.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					joinButtonEnterPressed(e);
				}
			}

			// unused abstract methods
			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		userNameLabel.setText("Name");

		serverNameLabel.setText("Server");

		portNumberLabel.setText("Port");

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(
														layout.createSequentialGroup()
																.addGap(128,
																		128,
																		128)
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.CENTER)
																				.addComponent(
																						joinButton)
																				.addComponent(
																						editrTitle)))
												.addGroup(
														layout.createSequentialGroup()
																.addGap(30, 30,
																		30)
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.TRAILING)
																				.addComponent(
																						userNameLabel)
																				.addComponent(
																						serverNameLabel))
																.addGap(18, 18,
																		18)
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.LEADING,
																				false)
																				.addComponent(
																						userName,
																						javax.swing.GroupLayout.PREFERRED_SIZE,
																						199,
																						javax.swing.GroupLayout.PREFERRED_SIZE)
																				.addGroup(
																						layout.createSequentialGroup()
																								.addComponent(
																										serverName,
																										javax.swing.GroupLayout.PREFERRED_SIZE,
																										99,
																										javax.swing.GroupLayout.PREFERRED_SIZE)
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										Short.MAX_VALUE)
																								.addComponent(
																										portNumberLabel)
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																								.addComponent(
																										portNumber,
																										javax.swing.GroupLayout.PREFERRED_SIZE,
																										52,
																										javax.swing.GroupLayout.PREFERRED_SIZE)))))
								.addContainerGap(38, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addComponent(editrTitle)
								.addGap(18, 18, 18)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(
														userName,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(userNameLabel))
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED,
										29, Short.MAX_VALUE)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(
														portNumber,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														serverName,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(serverNameLabel)
												.addComponent(portNumberLabel))
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(joinButton).addContainerGap()));

		pack();
	}

	protected void joinButtonEnterPressed(KeyEvent e) {
		username = userName.getText();
		port = Integer.parseInt(portNumber.getText());
		server = serverName.getText();

		String clientMessage = "joined as " + username + " on server " + server
				+ " and port " + port;

		System.out.println(clientMessage);

		LoginWorker worker = new LoginWorker(username, port, server,
				new rtceStartGUI());

		try {
			worker.doInBackground();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// close start gui, open main gui
		this.dispose();

		try {

			Socket socket = worker.getSocket();
			ClientConnection connection = new ClientConnection(socket);
			rtceGUI gui = new rtceGUI(username, connection);

			// TODO: Check that these edit the same guis
			new Thread(new NewDocument(gui)).start();
			System.out.println("rtcestartgui: starting clientModel");
			new Thread(new ClientModel(connection, gui)).start();
		} catch (UnknownHostException evt) {
			evt.printStackTrace();
		} catch (IOException evt) {
			evt.printStackTrace();
		}
	}

	private void joinButtonMouseClicked(java.awt.event.MouseEvent evt) {
		username = userName.getText();
		port = Integer.parseInt(portNumber.getText());
		server = serverName.getText();

		String clientMessage = "joined as " + username + " on server " + server
				+ " and port " + port;

		System.out.println(clientMessage);

		LoginWorker worker = new LoginWorker(username, port, server,
				new rtceStartGUI());

		try {
			worker.doInBackground();
			System.out.println(worker.getSocket());

		} catch (Exception e) {
			e.printStackTrace();
		}
		// close start gui, open main gui
		this.dispose();

		try {

			Socket socket = worker.getSocket();
			ClientConnection connection = new ClientConnection(socket);
			rtceGUI gui = new rtceGUI(username, connection);

			// TODO: Check that these edit the same guis
			new Thread(new NewDocument(gui)).start();
			System.out.println("rtcestartgui: starting clientModel");
			new Thread(new ClientModel(connection, gui)).start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void setConnection(ClientConnection connection) {
		this.clientConnection = connection;
	}

}
