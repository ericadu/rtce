package client;

// creates a new thread

public class NewDocument implements Runnable {

	private rtceGUI rtceGUI;

	public NewDocument(rtceGUI rtceGUI) {
		this.rtceGUI = rtceGUI;
	}

	@Override
	public void run() {
		rtceGUI.setVisible(true);
	}

}
