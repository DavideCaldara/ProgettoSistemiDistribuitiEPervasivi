package Droni;

/** Thread che preleva un ordine dalla coda, cerca il drone target e gliela assegna
 */

import Dronazon.Ordine;
import ServerAmministratore.DroneRecord;
import com.project.grpc.DroneServiceGrpc;
import com.project.grpc.DroneServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class ThreadAssegnamentoConsegne extends Thread{

    @Override
    public void run() {
        while(true) {
            Ordine ordine = DroneMain.queueConsegne.take(); //prendo un ordine dalla lista

            //cerco un drone non già impegnato in una richiesta, più vicino alla consegna e con maggior livello di batteria, o con ID maggiore cercando nella mia lista locale con tutte le info
            int IDDroneTarget = DroneMain.ID;//inizializzo me stesso, se non c'è nessun'altro e sono già in cosegna rimetto nella lista.
            int PORTDroneTarget = DroneMain.PORT;

            int Xr = ordine.getPuntoRitiroX();
            int Yr = ordine.getPuntoRitiroY();
            int Xc = ordine.getPuntoConsegnaX();
            int Yc = ordine.getPuntoConsegnaY();


            //cerco drone più vicino nella lista
            synchronized (DroneMain.infoDroneConsegna) {
                for (InfoDroneConsegna info : DroneMain.infoDroneConsegna) {
                    if (!info.inConsegna) { //se il drone non è già in consegna
                        if (distanza(Xr, Yr, info.getX(), info.getY()) < distanza(Xr, Yr, DroneMain.X, DroneMain.Y)) { //drone più vicino a posizione di ritiro
                            IDDroneTarget = info.getID();
                            if (info.getBatteria() > DroneMain.batteria) { //drone con batteria maggiore
                                IDDroneTarget = info.getID();
                                if (info.getID() > DroneMain.ID) //drone con ID maggiore
                                    IDDroneTarget = info.getID();
                            }
                        }
                    }
                }
            }

            //se non ho trovato drone target oltre a me, se sono già in consegna o se ho batteria sotto 15% (quindi dovrei uscire, ma non ci sono droni disponibili), rimetto in coda l'ordine
            if ((IDDroneTarget == DroneMain.ID && DroneMain.inConsegna) || (IDDroneTarget == DroneMain.ID && DroneMain.batteria<15 )) {
                DroneMain.queueConsegne.put(ordine);
            } else { // rimetto in coda e continuo a prenderla finchè non si libera un drone

                System.out.println("Ordine "+ ordine.getID() +" prelevato.");

                //se devo consegnare io e non sono già in consegna mi setto subito in consegna
                if (IDDroneTarget == DroneMain.ID && !DroneMain.inConsegna) {
                    DroneMain.inConsegna = true;
                }

                //ho ottenuto il drone target (anche me stesso)
                //gli assegno la consegna. Apro client gRPC e invio ordine (devo prendere anche la porta dalla lista locale non dettagliata)
                synchronized (DroneMain.listaDroni) {
                    for (DroneRecord d : DroneMain.listaDroni) {
                        if (d.getId() == IDDroneTarget)
                            PORTDroneTarget = d.getPort();
                    }
                }

                //aggiorno lo stato consegna del drone target nella mia lista dettagliata
                synchronized (DroneMain.infoDroneConsegna) {
                    for (InfoDroneConsegna d : DroneMain.infoDroneConsegna) {
                        if (d.getID() == IDDroneTarget)
                            d.setInConsegna(true);
                    }
                }

                final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + PORTDroneTarget).usePlaintext().build();
                DroneServiceGrpc.DroneServiceStub stub = DroneServiceGrpc.newStub(channel);
                DroneServiceOuterClass.Ordine consegna = DroneServiceOuterClass.Ordine.newBuilder().setID(ordine.getID()).setXr(Xr).setYr(Yr).setXc(Xc).setYc(Yc).build();

                System.out.println("Consegna assegnata al drone: " + IDDroneTarget);

                int finalIDDroneTarget = IDDroneTarget;
                stub.assegnaconsegna(consegna, new StreamObserver<DroneServiceOuterClass.Info>() {
                    @Override
                    public void onNext(DroneServiceOuterClass.Info info) {
                        //aggiorno tutte quello che devo aggiornare
                        DroneMain.totConsegneEffettuateDaTuttiIDroni += 1;
                        DroneMain.totChilometriPercorsiDaiDroni += info.getKm();
                        synchronized (DroneMain.misurazioniRicevuteDaiDroni) {
                            DroneMain.misurazioniRicevuteDaiDroni.add(info.getMediastatPM10());
                        }
                        // aggiorno livello batteria e nuova posizione drone
                        synchronized (DroneMain.infoDroneConsegna) {
                            for (InfoDroneConsegna i : DroneMain.infoDroneConsegna) {
                                if (finalIDDroneTarget == i.getID()) {
                                    i.setBatteria(info.getBatteria());
                                    i.setX(info.getNewX());
                                    i.setY(info.getNewY());
                                    i.setInConsegna(false); //drone non più in consegna
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        //se non trovo il drone perchè per qualche motivo ha quittato aggiorno le liste locali e rimetto ordine in coda
                        System.out.println("Drone target " + finalIDDroneTarget + " non trovato. Tolgo da lista locale e dettagliata.");
                        synchronized (DroneMain.listaDroni){
                            synchronized (DroneMain.infoDroneConsegna){
                                for(int i=0; i < DroneMain.listaDroni.size(); i++){
                                    if(finalIDDroneTarget == DroneMain.listaDroni.get(i).getId()){
                                        DroneMain.listaDroni.remove(i);
                                    }
                                }
                                for(int i=0; i < DroneMain.infoDroneConsegna.size(); i++){
                                    if(finalIDDroneTarget == DroneMain.infoDroneConsegna.get(i).getID()){
                                        DroneMain.infoDroneConsegna.remove(i);
                                    }
                                }

                            }
                        }
                        //rimetto l'ordine in coda
                        DroneMain.queueConsegne.put(ordine);
                        channel.shutdown();
                    }

                    @Override
                    public void onCompleted() {
                        channel.shutdown();
                    }
                });

                try {
                    channel.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }



    }

    private float distanza(int x1, int y1, int x2, int y2){
        return (float) Math.sqrt(Math.pow((x2-x1),2)+Math.pow((y2-y1),2));
    }
}
