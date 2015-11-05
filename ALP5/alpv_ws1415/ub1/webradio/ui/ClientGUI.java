package alpv_ws1415.ub1.webradio.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class ClientGUI implements ClientUI{
	
	private JFrame frame;
	private JTextField textField;
	private JTextPane textPane;
	private JButton buttonSend;

	public ClientGUI(){
		setUI();
	}
	@Override
	public void run() {
		
	}

	private void setUI() {
		frame = new JFrame("The client");
		frame.setLayout(new BorderLayout());
		frame.add(textPane = new JTextPane(), BorderLayout.CENTER);
		frame.add(textField = new JTextField(),BorderLayout.PAGE_END);
		frame.add(buttonSend = new JButton("SEND"),BorderLayout.EAST);
		frame.setLocationRelativeTo( null );
		frame.getContentPane().setPreferredSize(new Dimension(400, 400));


		
		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public String getUserName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void pushChatMessage(String message) {
		// TODO Auto-generated method stub
		
	}

}
