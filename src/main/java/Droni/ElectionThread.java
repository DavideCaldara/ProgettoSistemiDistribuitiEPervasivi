package Droni;

/** Thread per avvio elezioni in caso di necessità
 * Viene avviato e messo in attesa. Se il drone si accorge della mancanza del drone master, risveglia questo thread che inoltra il primo messaggio e avvia l'elezione
 */

public class ElectionThread extends Thread{

    public ElectionThread(){;}

    @Override
    public void run() {

        while(true) {
            synchronized (DroneMain.lockStartNewElection) {
                try {
                    System.out.println("Election thread in attesa");
                    DroneMain.lockStartNewElection.wait();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Election thread risvegliato");
                DroneMain.elezioniInCorso = true; //avvio processo di elezione
                DroneMain.partecipante = true; //marco me stesso come partecipante

                //avvio thread che manda un messaggio di elezione al successivo con miei ID e batteria
                SendElectionMessageThread sendElectionMessageThread = new SendElectionMessageThread(DroneMain.batteria, DroneMain.ID);
                sendElectionMessageThread.start();

            }
        }
    }
}
