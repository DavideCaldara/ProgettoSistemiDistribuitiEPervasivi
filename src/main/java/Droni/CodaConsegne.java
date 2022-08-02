package Droni;

/** Buffer per gestione consegne in arrivo da dronazon
 */

import Dronazon.Ordine;
import java.util.ArrayList;

public class CodaConsegne {

    public ArrayList<Ordine> buffer = new ArrayList<>();

    public synchronized void put(Ordine ordine) {
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        buffer.add(ordine);
        notify();
    }

    public synchronized Ordine take(){
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        Ordine ordine = null;

        while(buffer.size() == 0){
            try {
                System.out.println("Aspetto arrivo di un ordine");
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

//        System.out.println("Ordine arrivato. buffer.size() = "+ buffer.size());
        if(buffer.size()>0){
            ordine = buffer.get(0);
            buffer.remove(0);
        }

        if(buffer.size()==0){ //notifico permesso di uscita quando la coda è vuota
            synchronized (DroneMain.lockCodaConsegneVuota){
                DroneMain.lockCodaConsegneVuota.notify();
            }
        }


        return ordine;
    }
}
