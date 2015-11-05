package alpv_ws1415.ub1.webradio.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ServerGUI implements ServerUI {

	/*
	 * *(non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 * 
	 * Basic features : Change sound files and exits the program
	 */

	// GUI frames
	// Main menu
	private JFrame frame;
	private JFileChooser fileChooser; // To change the song path
	private JButton b1;
	private JButton b2;
	private JTextArea log;
	
	private String titleName = "•?((¯°·._.• ǗĹŤĮϻÃŤẸ ŴẸβŘÃĎĮỖ •._.·°¯))؟•";


	private volatile boolean serverRunning;
	private volatile boolean songPathChanged;

	private String newPath;

	public boolean isServerRunning() {
		return serverRunning;
	}

	public boolean isSongPathChanged() {
		return songPathChanged;
	}

	public ServerGUI() throws InvocationTargetException, InterruptedException {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				setUI();
			}
		});
		serverRunning = true;
	}

	private void shutdownServer() {
		serverRunning = false;
	}

	private class OnClickChangeSong implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JFileChooser fc = new JFileChooser("./Res/Wav");
					FileNameExtensionFilter filter = new FileNameExtensionFilter(".wav", "wav");
					fc.setFileFilter(filter);
					int returnVal = fc.showOpenDialog(ServerGUI.this.frame);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						System.out.println("You chose : " + fc.getSelectedFile().getName() + ". Good choice!");
					}
				}
			});

		}

	}

	public String getNewPath() {
		return newPath;
	}

	private class OnClickShutdownServer implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			shutdownServer();
			System.exit(0);
		}

	}

	@Override
	public void run() {
	}

	private void setUI() {
		// Imagines for the buttons
		ImageIcon shutdownServerIcon = new ImageIcon(ServerGUI.class.getResource("/Res/Img/shutdownServerIcon.gif"));
		ImageIcon changeSongIcon = new ImageIcon(ServerGUI.class.getResource("/Res/Img/changeSongIcon.gif"));

		// Create and set up the window.
		frame = new JFrame(titleName);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.getContentPane().setPreferredSize(new Dimension(400, 400));
		// frame.setResizable(false);
		frame.getContentPane().setBackground(Color.MAGENTA);

		// Add a log for console stuff
		log = new JTextArea();
		log.setEditable(false);
		log.setMargin(new Insets(5, 5, 5, 5));
		JScrollPane scrollPaneLog = new JScrollPane(log);

		// Create buttons
		b1 = new JButton(shutdownServerIcon);
		b1.setToolTipText("Click this button to shutdown server");
		b1.setBounds(10, 10, 128, 128);
		b1.setBackground(Color.CYAN);

		b2 = new JButton(changeSongIcon);
		b2.setToolTipText("Click this button to change song streaming on server");
		b2.setBounds(260, 10, 128, 128);
		b2.setBackground(Color.CYAN);

		// Listener for button clicks
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(b1);
		buttonPanel.add(b2);
		buttonPanel.setBackground(Color.MAGENTA);
		frame.add(scrollPaneLog, BorderLayout.CENTER);
		//frame.add(buttonPanel, BorderLayout.SOUTH);

		b1.addActionListener(new OnClickShutdownServer());
		b2.addActionListener(new OnClickChangeSong());

		// Add the buttons
		frame.getContentPane().add(buttonPanel);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}


}
