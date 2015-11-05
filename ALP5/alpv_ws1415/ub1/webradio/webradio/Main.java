package alpv_ws1415.ub1.webradio.webradio;
//New line
import alpv_ws1415.ub1.webradio.communication.ClientTCP;
import alpv_ws1415.ub1.webradio.communication.ServerTCP;
import alpv_ws1415.ub1.webradio.ui.ServerGUI;

public class Main {
	private static final String	USAGE	= String.format("usage: java -jar UB%%X_%%NAMEN [-options] server tcp|udp|mc PORT%n" +
														"         (to start a server)%n" +
														"or:    java -jar UB%%X_%%NAMEN [-options] client tcp|udp|mc SERVERIPADDRESS SERVERPORT USERNAME%n" +
														"         (to start a client)");

	/**
	 * Starts a server/client according to the given arguments, using a GUI or
	 * just the command-line according to the given arguments.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			boolean useGUI = false;
			int i = -1;

			// Parse options. Add additional options here if you have to. Do not
			// forget to mention their usage in the help-string!
			while(args[++i].startsWith("-")) {
				if(args[i].equals("-help")) {
					System.out.println(USAGE + String.format("%n%nwhere options include:"));
					System.out.println("  -help      Show this text.");
					System.out.println("  -gui       Show a graphical user interface.");
					System.exit(0);
				}
				else if(args[i].equals("-gui")) {
					useGUI = true;
				}
			}

			if(args[i].equals("server")) {
				if(useGUI){
					Thread gui = new Thread(new ServerGUI(Integer.parseInt(args[i+2])));
					gui.start();
				}else{
				Thread server = new Thread(new ServerTCP(Integer.parseInt(args[i+2])));
				server.start();
				}
			}
			else if(args[i].equals("client")) {
			    Thread  client = new Thread(new ClientTCP(args[i+2],Integer.parseInt(args[i+3]),args[i+4]));
				client.start();
			}
			else
				throw new IllegalArgumentException();
		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.err.println(USAGE);
		}
		catch(NumberFormatException e) {
			System.err.println(USAGE);
		}
		catch(IllegalArgumentException e) {
			System.err.println(USAGE);
		}
	}
}
