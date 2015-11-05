package alpv_ws1415.ub1.webradio.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class ClientGUI implements ClientUI{
	
	private JFrame frame;
	private JTextField textField;
	private JTextPane textPane;
	private JButton buttonSend;
	private JButton buttonQuit;


	public ClientGUI(){
		setUI();
	}
	@Override
	public void run() {
		
	}

	private void setUI() {
		//mainFrame
		frame = new JFrame("The client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		//TextLog to show Chat and stuff
		textPane = new JTextPane();
		textPane.setEditable(false);
		JScrollPane scrollPaneLog = new JScrollPane(textPane);

		
        JPanel chatInputPanel = new JPanel();
        chatInputPanel.setLayout(new BorderLayout());
        chatInputPanel.add(textField = new JTextField(),BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(buttonSend = new JButton("SEND"),BorderLayout.WEST);
        buttonPanel.add(buttonQuit = new JButton("QUIT"),BorderLayout.EAST);

        chatInputPanel.add(buttonPanel,BorderLayout.EAST);
        
		frame.add(scrollPaneLog, BorderLayout.CENTER);
		frame.add(chatInputPanel,BorderLayout.PAGE_END);

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
