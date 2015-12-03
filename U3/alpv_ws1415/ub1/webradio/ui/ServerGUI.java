package alpv_ws1415.ub1.webradio.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import alpv_ws1415.ub1.webradio.communication.ServerTCP;

public class ServerGUI implements ServerUI {

	/*
	 * *(non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 * 
	 * Basic features : Change sound files and exits the program
	 */
	private ServerTCP server;
	// GUI frames
	// Main menu
	private JFrame frame;
	private JButton b1;
	private JButton b2;
	private JButton b3;
	private JTextArea log;

	private String titleName = "υℓтιмαтє ωєвяα∂ισ ";

	private volatile boolean songPathChanged;
	private final String welcome = "Please choose a song and press Play";
	private volatile String newPath;



	public boolean isSongPathChanged() {
		return songPathChanged;
	}

	public ServerGUI(int port) throws InvocationTargetException, InterruptedException {
		this.server = new ServerTCP(port);
		new Thread(server).start();
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				setUI();
			}
		});

	}


	private class OnClickChangeSong implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			songPathChanged = true;
			JFileChooser fc = new JFileChooser("./Res/Wav");
			FileNameExtensionFilter filter = new FileNameExtensionFilter(".wav", "wav");
			fc.setFileFilter(filter);
			int returnVal = fc.showOpenDialog(ServerGUI.this.frame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				log.append("You chose : " + file.getName() + ". Good choice!\n");
				try {
					String path = file.getCanonicalPath().toString();
					System.out.println(path);
					server.setSongPath(path);
					server.playSong(path);
				} catch (UnsupportedAudioFileException | IOException e) {
					// TODO Auto-generated catch block
					System.err.println("Can't choose new audio file.");
					e.printStackTrace();
				}
			}
			
		}
	}

	public String getNewPath() {
		return newPath;
	}

	public void setNewPath(String path) {
		this.newPath = path;

	}
	private class OnClickPlaySong implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				server.playSong(server.getSongPath());
			} catch (UnsupportedAudioFileException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}		
		}
		
	}

	private class OnClickShutdownServer implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			server.close();
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
		ImageIcon playSongIcon = new ImageIcon(ServerGUI.class.getResource("/Res/Img/playSong.gif"));

		// Create and set up the window.
		frame = new JFrame(titleName);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.getContentPane().setPreferredSize(new Dimension(600, 300));
		// frame.setResizable(false);
		frame.getContentPane().setBackground(Color.MAGENTA);

		// Add a log for console stuff
		log = new JTextArea();
		log.setEditable(false);
		log.setMargin(new Insets(5, 5, 5, 5));
		log.append(welcome);

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
		
		b3 = new JButton(playSongIcon);
		b3.setToolTipText("Click this button to play a song");
		b3.setBounds(460, 10, 128, 128);
		b3.setBackground(Color.CYAN);

		// Listener for button clicks
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(b1);
		buttonPanel.add(b2);
		buttonPanel.add(b3);
		buttonPanel.setBackground(Color.MAGENTA);
		frame.add(scrollPaneLog, BorderLayout.CENTER);
		frame.add(buttonPanel, BorderLayout.SOUTH);

		b1.addActionListener(new OnClickShutdownServer());
		b2.addActionListener(new OnClickChangeSong());
		b3.addActionListener(new OnClickPlaySong());

		// Add the buttons

		// Display the window.
		frame.pack();
		frame.setVisible(true);
		
	
	}

	public void printToLog(String string) {
		log.append(string);
	}

}
