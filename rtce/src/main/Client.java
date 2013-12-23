package main;

import javax.swing.SwingUtilities;

import client.rtceStartGUI;

/**
 * GUI chat client runner.
 */
public class Client {

	/**
	 * Start a GUI rtce client.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				rtceStartGUI main = new rtceStartGUI();
				main.setVisible(true);
			}
		});
	}
}
