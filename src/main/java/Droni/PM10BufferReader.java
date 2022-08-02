package Droni;

/** Thread che legge dal buffer di misurazioni PM10
 * quando il buffer è pieno chiama il metodo readAllandClean, calcola la media dei valori nel buffer e la salva in aquisizioniSensore
 */

import CodiceSimulatori.Buffer;
import CodiceSimulatori.Measurement;

import java.util.List;

public class PM10BufferReader extends Thread{

    private final Buffer buffer;

    public PM10BufferReader(Buffer buffer){
        this.buffer = buffer;
    }

    @Override
    public void run() {
        //legge dal buffer quando è pieno

        while(true){
            List<Measurement> stats = buffer.readAllAndClean(); //aggiungo media buffer

            double sum = 0;
//            System.out.println("Record buffer prelevato: ");

            for(int i=0; i<stats.size(); i++) {
//                System.out.println(stats.get(i).getValue());
                sum += stats.get(i).getValue();
            }

        //salva media dei valori prelevati dal buffer nella struttura dati del drone
            DroneMain.acquisizioniSensore.add(new MediaMisurazione(sum/8, System.currentTimeMillis()));
//            System.out.println("Nuovo record registrato nella struttura dati.");

//            for(MediaMisurazione m : DroneMain.acquisizioniSensore){
//                System.out.println(m.getMedia() + " _ " + m.getTimestamp());
//            }
        }


    }
}
