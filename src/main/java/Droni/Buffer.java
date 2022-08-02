package Droni;

/** Classe Buffer: implementa l'interfaccia Buffer in CodiceSimulatori, metodi implementati:
 * - addMeasurement(Measurement m) -> aggiugne una nuova misurazione del sensore al buffer
 * - readAllAndClean() -> legge il buffer quando raggiunge le 8 misurazioni (capienza massima), ne fa la media e scorre la sliding window
 */

import CodiceSimulatori.Measurement;

import java.util.ArrayList;
import java.util.List;

public class Buffer implements CodiceSimulatori.Buffer {

    public static ArrayList<Measurement> myBuffer = new ArrayList<>();

    @Override
    public synchronized void addMeasurement(Measurement m) {

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        myBuffer.add(m);
//        System.out.println("Nuovo valore aggiunto al buffer.");
//        for(Measurement value : myBuffer)
//            System.out.println(value.getValue());
        if(myBuffer.size()==8)
            notify();
    }

    @Override
    public synchronized List<Measurement> readAllAndClean() {

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        List<Measurement> StatsSet = new ArrayList<>(8);

        while(myBuffer.size() < 8){
            try {
                wait();
            } catch (InterruptedException e) { e.printStackTrace(); }
        }

        if(myBuffer.size() > 0){ //quando il buffer è pieno svuoto e scorro di 4
            StatsSet.addAll(myBuffer);
            for(int i=0; i<4; i++)
                myBuffer.remove(0); //tolgo i primi 4 valori

        }

        return StatsSet;
    }
}
