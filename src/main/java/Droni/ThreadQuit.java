package Droni;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


/** se non master:
 * - completo consegna e comunico info al market --> aspetto inConsegna == false;
 * - chiudere le connessioni con gli altri droni
 * - chiedere al server amministratore di uscire dalla rete
 * se master:
 * - termino consegna e info --> aspetto inConsegna == false;
 * - disconnettersi dal broker mqtt
 * - assegnare consegen pendenti
 * - chiudere connessioni con gli altri droni
 * - inviare al server amministratore statistiche globali
 * - chiedere al server amministratore di uscire dalla rete
 */

public class ThreadQuit extends Thread{

    public ThreadQuit() {;}

    @Override
    public void run() {


        synchronized (DroneMain.lockStartQuit){
            try {
                DroneMain.lockStartQuit.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        System.out.println("Inizata procedura di uscita dalla rete");

        if(DroneMain.elezioniInCorso){ //blocco uscita drone durante le elezioni
            synchronized (DroneMain.lockElezioniInCorso){
                try {
                    System.out.println("Elezioni in corso, aspetto la fine per uscire...");
                    DroneMain.lockElezioniInCorso.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //continuo uscita
        }

        //termino consegna --> aspetto inConsegna == false
        if(DroneMain.inConsegna){
            synchronized (DroneMain.lockinConsegna){
                try {
                    DroneMain.lockinConsegna.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //continuo uscita
        }

        if(DroneMain.isMaster){

            //mi disconnetto dal broker MQTT
            synchronized (DroneMain.lockDisconnectMQTT){
                DroneMain.lockDisconnectMQTT.notify();
            }

            //assegna le consegne pendenti --> notifico quando buffer.size() == 0
            if(DroneMain.queueConsegne.buffer.isEmpty()){ //se il buffer è già vuoto
                //continuo uscita
            }else{ //altrimenti resto in attesa
                synchronized (DroneMain.lockCodaConsegneVuota){
                    try {
                        DroneMain.lockCodaConsegneVuota.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            //aspetto comunicazione stat globali
            synchronized (DroneMain.lockStatGlobaliComunicate){
                try {
                    DroneMain.lockStatGlobaliComunicate.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //continua uscita
            }
        }

        //chiedo al server amministratore di uscire
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:1337/drone/delete/"+DroneMain.ID);
        ClientResponse resp = webResource.delete(ClientResponse.class);

        //chiudo connessione con gli altri droni
        //esco
        synchronized (DroneMain.quit){
            DroneMain.quit.notify();
        }




    }


}
