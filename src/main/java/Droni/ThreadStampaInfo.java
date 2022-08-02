package Droni;

/** Thread che ogni 10 secondi stampa info drone
 */

public class ThreadStampaInfo extends Thread{

    public ThreadStampaInfo() {;}

    @Override
    public void run() {
        while(true){
            //ogni 10 secondi stampo ntot consegne effettuate, chilometri percorsi, % batteria residua

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("-------------------- \n [Info Drone " + DroneMain.ID + "] \n - Totale Consegne Effettuate: " +
                    DroneMain.totConsegneEffettuate + "\n - Chilometri Percorsi Totali: " +
                    DroneMain.totChilometriPercorsi + " km \n - Batteria residua: " + DroneMain.batteria + "% \n --------------------" );

        }
    }
}
