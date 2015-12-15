package alpv.mwp;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MyClient {

	Server server;
	String text = "Syphilis (Aussprache in Deutschland nur [ˈzʏ⁠fɪlɪs], in Österreich auch [ˈzɪ⁠fɪlɪs] bzw. [ˈsɪ⁠fɪlɪs]; auch Lues, Lues venerea, harter Schanker oder Franzosenkrankheit (maladie française) genannt) ist eine Infektionskrankheit, die zur Gruppe der sexuell übertragbaren Erkrankungen gehört. Der Erreger der Syphilis ist das Bakterium Treponema pallidum ssp. pallidum. Die Syphilis wird hauptsächlich bei sexuellen Handlungen durch Schleimhautkontakt und ausschließlich von Mensch zu Mensch übertragen. Während der Schwangerschaft und bei der Geburt kann eine erkrankte Mutter ihr Kind infizieren (Syphilis connata).Das Erscheinungsbild der Krankheit ist vielfältig. Typisch ist ein Beginn mit schmerzlosen Schleimhautgeschwüren und Lymphknotenschwellungen. Bei einem Teil der Infizierten kommt es zu einem chronischen Verlauf, der durch vielfältigen Haut- und Organbefall gekennzeichnet ist. Im Endstadium kommt es zur Zerstörung des zentralen Nervensystems. Die Diagnose wird hauptsächlich durch den Nachweis von Antikörpern erstellt. Die Syphilis ist durch die Gabe von Antibiotika, unter anderem Penicillin, heilbar. Die Entdeckung und die spätere Verfügbarkeit von Antibiotika in ausreichenden Mengen führte zu einem deutlichen Rückgang der Syphilis im 20. Jahrhundert.Seit den 1990er Jahren ist jedoch wieder ein Anstieg der erkannten Erkrankungen feststellbar. 2011 wurden in Deutschland 3.698 Neuerkrankungen erkannt (93,6 Prozent Männer, 6,4 Prozent Frauen); das waren fast 22 Prozent mehr als 2010. In den Jahren 2012 und 2013 wurden 4.410 und 5017 Neuerkrankungen gemeldet; das waren zuletzt 65 Prozent mehr als 2010. Die Zahlen stammen aus der statistischen Erhebung des Robert-Koch-Instituts (RKI).[1][2][3] Der direkte oder indirekte Nachweis des Erregers Treponema pallidum ist in Deutschland nichtnamentlich nach dem Infektionsschutzgesetz zu melden. Eine Meldepflicht besteht auch in der Schweiz[4] und in Österreich. Syphilidologie ist die Lehre von den syphilitischen Krankheiten.";
	String searchedString = "Syphilis";

	//Constructor
	public MyClient(String host, int port) {
		super();
		try {		
			Registry reg = LocateRegistry.getRegistry(host, port);

			this.server = (Server) reg.lookup("MyMaster");
			System.out.println("Client starting job.");
			Job newJob = new MyJob(text, searchedString, new MyTask());
			RemoteFuture rf =  server.doJob(newJob);
			while(!rf.isFinished()){
				System.out.println("Result update: "+ rf.get());
			}
			System.out.println("Final Result: "+ rf.get());

		} catch (RemoteException e1) {
			e1.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println("ERROR: Server not bound");
		}
	}

}
