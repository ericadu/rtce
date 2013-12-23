package client;

import java.awt.Color;
import java.awt.Event;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * The GUI class -- creates the GUI for every user that joins a document. Also
 * contains the action and event listeners that detect changes made to the
 * document.
 * 
 * This contains extra features modeled off of the Java example here:
 * http://docs.oracle.com/javase/tutorial/uiswing/components/generaltext.html
 * but these features do not assist the real time nature of our project.
 * 
 * @author Connie Huang
 * 
 */

@SuppressWarnings("serial")
public class rtceGUI extends JFrame {
	private final int DELAY = 10; // How much to sleep for
	private int clientID;
	private String username;
	private ClientConnection clientConnection;
	private int clientVersion;
	HashMap<Object, Action> actions;

	JTextArea changeLog;
	private javax.swing.JTextPane Document;
	private javax.swing.JScrollPane DocumentScroll;
	private javax.swing.JButton Exit;
	private javax.swing.JLabel GUITitle;
	private javax.swing.JToolBar Toolbar;
	private javax.swing.JList UserList;
	private javax.swing.JLabel UserTitle;
	private ConcurrentLinkedQueue<String> queue;
	JTextPane textPane;
	AbstractDocument doc;

	static final int MAX_CHARACTERS = 300;

	protected UndoAction undoAction;
	protected RedoAction redoAction;
	protected UndoManager undo = new UndoManager();


	public rtceGUI(String username, ClientConnection clientConnection) {
		super("editr " + username);
		this.username = username;
		this.clientConnection = clientConnection;
		initComponents();
		UserTitle.setText("Logged in as: " + username);
		this.queue = new ConcurrentLinkedQueue<String>();
		this.clientVersion = 0;
		this.Document.setCaretPosition(0);
		new Thread(new GUIQueueRunnable()).start();
	}

	public void lock() {
		this.Document.setEditable(false);
	}

	public void unlock() {
		this.Document.setEditable(true);
	}

	public int getCaretPosition() {
		return Document.getCaretPosition();
	}

	/**
	 * Creates the thread that serves the clientside queue.
	 *
	 */
	public class GUIQueueRunnable implements Runnable {
		public GUIQueueRunnable() {
		}

		public void run() {
			while (true) {
				while (!queue.isEmpty()) {
					System.out.println("Sending from GUI");
					clientConnection.sendMessage(queue.poll());
				}
				try {
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * updates the client version number
	 * @param v, version number we are updating it to
	 */
	public void setVersionNumber(int v) {
		this.clientVersion = v;
	}

	
	/**
	 * Creates all the SWING Components necessary for GUI
	 */
	private void initComponents() {
		UserTitle = new javax.swing.JLabel();
		Exit = new javax.swing.JButton();
		UserList = new javax.swing.JList();
		Toolbar = new javax.swing.JToolBar();
		GUITitle = new javax.swing.JLabel();
		DocumentScroll = new javax.swing.JScrollPane();
		Document = new javax.swing.JTextPane();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Editr | Document " + clientID);
		setBackground(new java.awt.Color(204, 255, 204));
		setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
		setName("editr");
		setResizable(false);

		// Create the text pane and configure it.
		textPane = new JTextPane();
		textPane.setCaretPosition(0);
		StyledDocument styledDoc = textPane.getStyledDocument();
		if (styledDoc instanceof AbstractDocument) {
			doc = (AbstractDocument) styledDoc;
		} else {
			System.err
					.println("Text pane's document isn't an AbstractDocument!");
			System.exit(-1);
		}

		UserTitle.setText("Signed in as " + username);

		UserList.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		UserList.setModel(new javax.swing.AbstractListModel() {
			String[] strings = { "Andrew", "Erica", "Connie" };

			public int getSize() {
				return strings.length;
			}

			public Object getElementAt(int i) {
				return strings[i];
			}
		});

		Toolbar.setRollover(true);
		Toolbar.setFloatable(false);

		GUITitle.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
		GUITitle.setForeground(new java.awt.Color(51, 51, 51));
		GUITitle.setText("editr");

		Document.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		Document.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
		// Document.setWrapStyleWord(true);
		// Document.setLineWrap(true);

		DocumentScroll.setViewportView(Document);

		// Set up the menu bar.
		actions = createActionTable(textPane);
		JMenu editMenu = createEditMenu();
		JMenu styleMenu = createStyleMenu();
		JMenuBar mb = new JMenuBar();
		mb.add(editMenu);
		mb.add(styleMenu);
		setJMenuBar(mb);

		// Add some key bindings.
		addBindings();

		// Start watching for undoable edits and caret changes.
		doc.addUndoableEditListener(new MyUndoableEditListener());
		doc.addDocumentListener(new MyDocumentListener());


		Document.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
		         if (e.getKeyCode() == KeyEvent.VK_ENTER) {
		                System.out.println("ENTER");
		                insertToClient("\u0015", Document.getCaretPosition(), 0);
		            } 
		         else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
		                deleteToClient("",Document.getCaretPosition()-1, 1);
		            }
		         
		         else if (e.getKeyCode() == KeyEvent.VK_LEFT || 
		                 e.getKeyCode() == KeyEvent.VK_RIGHT || 
		                 e.getKeyCode() == KeyEvent.VK_UP ||
		                 e.getKeyCode() == KeyEvent.VK_DOWN ||
		                 e.getKeyCode() == KeyEvent.VK_SHIFT ||
		                 e.getKeyCode() == KeyEvent.VK_CAPS_LOCK){

		         }
			
			    else {
			        insertToClient(((Character) e.getKeyChar()).toString(),Document.getCaretPosition(),0);
			        
		         }
			}

			public void keyTyped(KeyEvent e) {

			}

			public void keyReleased(KeyEvent e) {
			}
		});

		// Document.getDocument().addDocumentListener(documentListener);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		DocumentScroll,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		527,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.LEADING)
																				.addGroup(
																						layout.createSequentialGroup()
																								.addGap(28,
																										28,
																										28)
																								.addComponent(
																										UserTitle)
																								.addGap(0,
																										0,
																										Short.MAX_VALUE))
																				.addGroup(
																						layout.createSequentialGroup()
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																								.addGroup(
																										layout.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.LEADING)
																												.addComponent(
																														UserList,
																														javax.swing.GroupLayout.DEFAULT_SIZE,
																														javax.swing.GroupLayout.DEFAULT_SIZE,
																														Short.MAX_VALUE)
																												.addComponent(
																														Exit,
																														javax.swing.GroupLayout.DEFAULT_SIZE,
																														javax.swing.GroupLayout.DEFAULT_SIZE,
																														Short.MAX_VALUE)))))
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		Toolbar,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		527,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		Short.MAX_VALUE)
																.addComponent(
																		GUITitle)))
								.addContainerGap()));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						javax.swing.GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		GUITitle)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																		15,
																		Short.MAX_VALUE)
																.addComponent(
																		UserTitle)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																.addComponent(
																		UserList,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		98,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addGap(231,
																		231,
																		231)
																.addComponent(
																		Exit))
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		Toolbar,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		25,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(
																		DocumentScroll)))
								.addContainerGap()));

		// Set up the menu bar.
		actions = createActionTable(textPane);
		mb.add(editMenu);
		mb.add(styleMenu);
		setJMenuBar(mb);

		// Add some key bindings.
		addBindings();

		// Put the initial text into the text pane.
		textPane.setCaretPosition(0);

		// Start watching for undoable edits and caret changes.
		doc.addUndoableEditListener(new MyUndoableEditListener());
		doc.addDocumentListener(new MyDocumentListener());

		pack();
	}

	// Add a couple of emacs key bindings for navigation.
	protected void addBindings() {
		InputMap inputMap = textPane.getInputMap();

		// Ctrl-b to go backward one character
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.backwardAction);

		// Ctrl-f to go forward one character
		key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.forwardAction);

		// Ctrl-p to go up one line
		key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.upAction);

		// Ctrl-n to go down one line
		key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.downAction);
	}

	// This one listens for edits that can be undone.
	protected class MyUndoableEditListener implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent e) {
			// Remember the edit and update the menus.
			undo.addEdit(e.getEdit());
			undoAction.updateUndoState();
			redoAction.updateRedoState();
		}
	}

	// This listens for and reports caret movements.
	protected class CaretListenerLabel extends JLabel implements CaretListener {
		public CaretListenerLabel(String label) {
			super(label);
		}

		// Might not be invoked from the event dispatch thread.
		public void caretUpdate(CaretEvent e) {
			displaySelectionInfo(e.getDot(), e.getMark());
		}

		// This method can be invoked from any thread. It
		// invokes the setText and modelToView methods, which
		// must run on the event dispatch thread. We use
		// invokeLater to schedule the code for execution
		// on the event dispatch thread.
		protected void displaySelectionInfo(final int dot, final int mark) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (dot == mark) { // no selection
						try {
							Rectangle caretCoords = textPane.modelToView(dot);
							// Convert it to view coordinates.
							setText("caret: text position: " + dot
									+ ", view location = [" + caretCoords.x
									+ ", " + caretCoords.y + "]" + "\n");
						} catch (BadLocationException ble) {
							setText("caret: text position: " + dot + "\n");
						}
					} else if (dot < mark) {
						setText("selection from: " + dot + " to " + mark + "\n");
					} else {
						setText("selection from: " + mark + " to " + dot + "\n");
					}
				}
			});
		}
	}

	// And this one listens for any changes to the document.
	protected class MyDocumentListener implements DocumentListener {
		public void insertUpdate(DocumentEvent e) {
			displayEditInfo(e);
		}

		public void removeUpdate(DocumentEvent e) {
			displayEditInfo(e);
		}

		public void changedUpdate(DocumentEvent e) {
			displayEditInfo(e);
		}

		private void displayEditInfo(DocumentEvent e) {

		}
	}

	private HashMap<Object, Action> createActionTable(
			JTextComponent textComponent) {
		HashMap<Object, Action> actions = new HashMap<Object, Action>();
		Action[] actionsArray = textComponent.getActions();
		for (int i = 0; i < actionsArray.length; i++) {
			Action a = actionsArray[i];
			actions.put(a.getValue(Action.NAME), a);
		}
		return actions;
	}

	class UndoAction extends AbstractAction {
		public UndoAction() {
			super("Undo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undo.undo();
			} catch (CannotUndoException ex) {
				System.out.println("Unable to undo: " + ex);
				ex.printStackTrace();
			}
			updateUndoState();
			redoAction.updateRedoState();
		}

		protected void updateUndoState() {
			if (undo.canUndo()) {
				setEnabled(true);
				putValue(Action.NAME, undo.getUndoPresentationName());
			} else {
				setEnabled(false);
				putValue(Action.NAME, "Undo");
			}
		}
	}

	class RedoAction extends AbstractAction {
		public RedoAction() {
			super("Redo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undo.redo();

			} catch (CannotRedoException ex) {
				System.out.println("Unable to redo: " + ex);
				ex.printStackTrace();
			}
			updateRedoState();
			undoAction.updateUndoState();
		}

		protected void updateRedoState() {
			if (undo.canRedo()) {
				setEnabled(true);
				putValue(Action.NAME, undo.getRedoPresentationName());
			} else {
				setEnabled(false);
				putValue(Action.NAME, "Redo");
			}
		}
	}

	// Create the edit menu.
	protected JMenu createEditMenu() {
		JMenu menu = new JMenu("Edit");

		// Undo and redo are actions of our own creation.
		undoAction = new UndoAction();
		menu.add(undoAction);

		redoAction = new RedoAction();
		menu.add(redoAction);

		menu.addSeparator();

		// These actions come from the default editor kit.
		// Get the ones we want and stick them in the menu.
		menu.add(getActionByName(DefaultEditorKit.cutAction));
		menu.add(getActionByName(DefaultEditorKit.copyAction));
		menu.add(getActionByName(DefaultEditorKit.pasteAction));

		menu.addSeparator();

		menu.add(getActionByName(DefaultEditorKit.selectAllAction));
		return menu;
	}

	// Create the style menu.
	protected JMenu createStyleMenu() {
		JMenu menu = new JMenu("Style");

		Action action = new StyledEditorKit.BoldAction();
		action.putValue(Action.NAME, "Bold");
		menu.add(action);

		action = new StyledEditorKit.ItalicAction();
		action.putValue(Action.NAME, "Italic");
		menu.add(action);

		action = new StyledEditorKit.UnderlineAction();
		action.putValue(Action.NAME, "Underline");
		menu.add(action);

		menu.addSeparator();

		menu.add(new StyledEditorKit.FontSizeAction("12", 12));
		menu.add(new StyledEditorKit.FontSizeAction("14", 14));
		menu.add(new StyledEditorKit.FontSizeAction("18", 18));

		menu.addSeparator();

		menu.add(new StyledEditorKit.FontFamilyAction("Serif", "Serif"));
		menu.add(new StyledEditorKit.FontFamilyAction("SansSerif", "SansSerif"));

		menu.addSeparator();

		menu.add(new StyledEditorKit.ForegroundAction("Red", Color.red));
		menu.add(new StyledEditorKit.ForegroundAction("Green", Color.green));
		menu.add(new StyledEditorKit.ForegroundAction("Blue", Color.blue));
		menu.add(new StyledEditorKit.ForegroundAction("Black", Color.black));

		return menu;
	}

	protected SimpleAttributeSet[] initAttributes(int length) {
		// Hard-code some attributes.
		SimpleAttributeSet[] attrs = new SimpleAttributeSet[length];

		attrs[0] = new SimpleAttributeSet();
		StyleConstants.setFontFamily(attrs[0], "SansSerif");
		StyleConstants.setFontSize(attrs[0], 16);

		attrs[1] = new SimpleAttributeSet(attrs[0]);
		StyleConstants.setBold(attrs[1], true);

		attrs[2] = new SimpleAttributeSet(attrs[0]);
		StyleConstants.setItalic(attrs[2], true);

		attrs[3] = new SimpleAttributeSet(attrs[0]);
		StyleConstants.setFontSize(attrs[3], 20);

		attrs[4] = new SimpleAttributeSet(attrs[0]);
		StyleConstants.setFontSize(attrs[4], 12);

		attrs[5] = new SimpleAttributeSet(attrs[0]);
		StyleConstants.setForeground(attrs[5], Color.red);

		return attrs;
	}

	/**
	 * Adds a desired edit to the client queue, to be sent to the server queue
	 * Formats the edits in the correct grammar
	 * 
	 * @param edits
	 *            , the string representation of the changes in the text area
	 *            made by the client since last pull
	 * @param position
	 *            , the location of where the edits begin
	 * @param editLength
	 *            , the length of the edit
	 */
	public void insertToClient(String edits, int position, int editLength) {
		String message = "push " + clientVersion + " " + position + " 0" + " "
				+ edits;
		queue.add(message);
	}

	/**
	 * Deals with deletions
	 * 
	 * @param edits
	 * @param position
	 * @param editLength
	 *            , the length of the edit
	 */
	public void deleteToClient(String edits, int position, int editLength) {

		String message = "push " + clientVersion + " " + position + " "
				+ editLength + " " + "";
		queue.add(message);
		// clientVersion++;
	}

	public void setTextArea(String newText) {
		Document.setText(newText);
	}

	private Action getActionByName(String name) {
		return actions.get(name);
	}

	/**
	 * Updates the GUI w/ the new document and caret position
	 * 
	 * @param newText
	 * @param newCaretPosition
	 */
	public void updateGUI(String newText, int newCaretPosition) {
		Document.setText(newText);
		Document.setCaretPosition(newCaretPosition);
	}

	public static void main(String args[]) {
		// Look and feel settings
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(rtceGUI.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(rtceGUI.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(rtceGUI.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(rtceGUI.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		}

		UIManager.put("swing.boldMetal", Boolean.FALSE);

	}
}
