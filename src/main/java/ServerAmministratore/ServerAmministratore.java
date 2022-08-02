package ServerAmministratore;

/** Permette di inserire e rimuovere droni, riceve statistiche globali dai droni,
 * consente al client amministratore di ottenere la statistiche con architettura REST.
 * devo modellare lista dei droni e statistiche.
 * Quando inserisco/rimuovo un drone, se c'è già un ID uguale messaggio di errore, se non c'è viene aggiunto e il server
 * amministratore invia la posizione iniziale nella smart city e la lista dei droni già presenti
 * Richiesta sincronizzazione sulla lista dei droni (synchronized su add e delete drone)
 * Statistiche globali:
 * il drone master deve periodicamente inoltrare al server amministratore le statistiche dei vari droni
 * client amministratore dovra predisporre interfacce che permettono di leggere queste statistiche.
 * DRONAZON e MQTT
 * Dronazon client mqtt con ruolo di publisher. Faccio partire il broker e connetto dronazon, che genera periodiacamente
 * consegne e pubblicandole. Logica del drone master e subrscriber a mqtt su ogni drone perchè potenzialmente
 * potrebbe diventare master.
*/

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;

public class ServerAmministratore { //server REST per la gestione degli ordini

    private static final String HOST = "localhost";
    private static final int PORT = 1337;

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServerFactory.create("http://"+HOST+":"+PORT+"/");
        server.start();

        System.out.println("Server Amministratore avviato su: http://"+HOST+":"+PORT);

    }
}
