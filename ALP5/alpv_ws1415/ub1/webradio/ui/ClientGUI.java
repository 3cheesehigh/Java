package alpv_ws1415.ub1.webradio.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import alpv_ws1415.ub1.webradio.communication.ClientTCP;

public class ClientGUI implements ClientUI {
<<<<<<< HEAD
=======
	
>>>>>>> origin/master

	private JFrame frame;
	private JTextField textField;
	private JTextArea log;
	private JButton buttonSend;
	private JButton buttonQuit;
	private ClientTCP client;
	private JScrollPane scrollPaneLog;

	public ClientGUI(String addr, int port, String username) throws InvocationTargetException, InterruptedException {
		this.client = new ClientTCP(addr, port, username);
		new Thread(client).start();
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				setUI();
			}
		});
		pushChatMessage("Welcome");
	}

	@Override
	public void run() {

	}

	private void setUI() {
		// mainFrame
		frame = new JFrame("The client");
<<<<<<< HEAD
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
=======
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
>>>>>>> origin/master
		frame.setLayout(new BorderLayout());
		// TextLog to show Chat and stuff
		log = new JTextArea();
		log.setEditable(false);
		log.setMargin(new Insets(5, 5, 5, 5));
		JScrollPane scrollPaneLog = new JScrollPane(log);

		JPanel chatInputPanel = new JPanel();
		chatInputPanel.setLayout(new BorderLayout());
		chatInputPanel.add(textField = new JTextField(), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(buttonSend = new JButton("SEND"), BorderLayout.WEST);
		buttonPanel.add(buttonQuit = new JButton("QUIT"), BorderLayout.EAST);

		chatInputPanel.add(buttonPanel, BorderLayout.EAST);

		frame.add(scrollPaneLog, BorderLayout.CENTER);
		frame.add(chatInputPanel, BorderLayout.PAGE_END);

		frame.setLocationRelativeTo(null);
		frame.getContentPane().setPreferredSize(new Dimension(400, 400));
<<<<<<< HEAD
		
=======

>>>>>>> origin/master
		buttonQuit.addActionListener(new OnClickQuitClient());

		// Display the window.
		frame.pack();
		frame.setVisible(true);
<<<<<<< HEAD
=======
		
>>>>>>> origin/master
	}

	@Override
	public String getUserName() {
		// TODO Auto-generated method stub
		return client.getUsername();
	}

	@Override
	public void pushChatMessage(String message) {
<<<<<<< HEAD
		log.append(message+"\n");
	}
	
=======
		log.append(message + "\n");
	}

>>>>>>> origin/master
	private class OnClickQuitClient implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
<<<<<<< HEAD
			client.close();
=======
			if (client.socketIsOpen()) {
				client.close();
			}
>>>>>>> origin/master
			System.exit(0);
		}

	}

}
