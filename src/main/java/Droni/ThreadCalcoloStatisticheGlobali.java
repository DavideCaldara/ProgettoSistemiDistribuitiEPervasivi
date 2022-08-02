package Droni;

/** Thread per drone master, calcola le statistiche globali e le invia periodicamente al drone master
 */

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ThreadCalcoloStatisticheGlobali extends Thread{

    public ThreadCalcoloStatisticheGlobali() {;}

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //media n consegne effettuate dai droni
            float mediaConsegne = (float)DroneMain.totConsegneEffettuateDaTuttiIDroni/(float)(DroneMain.infoDroneConsegna.size()+1);

            //media chilometri percorsi dai droni
            float totchilometri = (float)DroneMain.totChilometriPercorsiDaiDroni/(float)(DroneMain.infoDroneConsegna.size()+1);

            //media livello di inquinamento rilevato dai droni
            float sum = 0;
            float mediainq = 0;
            synchronized (DroneMain.misurazioniRicevuteDaiDroni){
                for(int i=0; i<DroneMain.misurazioniRicevuteDaiDroni.size(); i++){
                    sum += DroneMain.misurazioniRicevuteDaiDroni.get(i);
                }
                mediainq = sum/DroneMain.misurazioniRicevuteDaiDroni.size();
                DroneMain.misurazioniRicevuteDaiDroni.clear();
            }

            //media livello batteria residuo dei droni
            int sumbatt = 0;
            float mediabatt = 0;
            synchronized (DroneMain.infoDroneConsegna){
                for(InfoDroneConsegna info : DroneMain.infoDroneConsegna){
                    sumbatt += info.getBatteria();
                }
                mediabatt = sumbatt/(DroneMain.infoDroneConsegna.size()+1);
            }

            //invio tutto al server amministratore tramite chiamata REST (JSON)
            Client client = Client.create();
            WebResource webresource = client.resource("http://localhost:1337/drone/addstat");
            String StatJSON = "{\"consegne\":\""+ mediaConsegne +"\",\"chilometripercorsi\":\""+ totchilometri +"\",\"livelloinquinamento\":\""+ mediainq +"\",\"livellobatteria\":\""+ mediabatt +"\",\"timestamp\":\""+ System.currentTimeMillis() +"\"}";
            System.out.println("Statistiche globali calcolate e inviate al sever amministratore");
//            System.out.println(StatJSON);
            ClientResponse response = webresource.type("application/json").post(ClientResponse.class, StatJSON);

            synchronized (DroneMain.lockStatGlobaliComunicate){ //notifico drone in uscita stat comunicate
                DroneMain.lockStatGlobaliComunicate.notify();
            }


        }
    }
}
