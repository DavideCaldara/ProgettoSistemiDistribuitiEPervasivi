package Droni;

import ServerAmministratore.DroneRecord;
import com.project.grpc.DroneServiceGrpc;
import com.project.grpc.DroneServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

import static Droni.DroneMain.lockAvvioFunzionalitaMaster;


/** Thread che esegue tutte le funzionalità di drone master
 *  - Contatta tutti i droni per ricevere la posizioni e salvarla in una struttura dati apposita
 *  - Si sottoscrive al topic di MQTT pronto a ricevere gli ordini generati da Dronazon per salvarli in un buffer
 *  - Prende un ordine dal buffer e lo assegna a un drone
 *  - Quando un drone finisce una consegna riceve tutte le statistiche e le manda al server
 */

public class FunzionalitaMaster extends Thread{

    @Override
    public void run() {

        while (!DroneMain.isMaster) {
            synchronized (lockAvvioFunzionalitaMaster) {
                try {
                    lockAvvioFunzionalitaMaster.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if(DroneMain.isMaster){

            System.out.println("Funzionalità Master Avviate!");

            //contatto tutti i droni e ricevo le loro posizioni correnti, me le salvo in una struttura dati apposita
            synchronized (DroneMain.listaDroni){
                for(int i=0; i<DroneMain.listaDroni.size(); i++){
                    try {
                        askForPosition(DroneMain.listaDroni.get(i), i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            //lista dettagliata completamente aggiornata

            //threaa sottoscritto al topic MQTT. Ogni volta che arriva una consegna la mette nel buffer delle consegne
            MqttClientThread mqttClientThread = new MqttClientThread();
            mqttClientThread.start();

            //Thread che prende un'ordine dalla coda delle consgne. Assegna l'ordine al drone più vicino, con maggiore batteria, o ID maggiore. Al termine della consegna riceve statistiche e nuovi paramteri drone
            ThreadAssegnamentoConsegne assegnamentoConsegneThread = new ThreadAssegnamentoConsegne();
            assegnamentoConsegneThread.start();

            //thread che ogni 10 secondi calcola le satistiche globali e le invia al server amministratore
            ThreadCalcoloStatisticheGlobali threadCalcoloStatisticheGlobali = new ThreadCalcoloStatisticheGlobali();
            threadCalcoloStatisticheGlobali.start();

        }
    }

    private static void askForPosition(DroneRecord d, int i) throws InterruptedException {

        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:"+ d.getPort()).usePlaintext().build();
        DroneServiceGrpc.DroneServiceStub stub = DroneServiceGrpc.newStub(channel);
        DroneServiceOuterClass.ACK request = DroneServiceOuterClass.ACK.newBuilder().build();
        System.out.println("Sto richiedendo info dettagliate al drone [Drone "+ d.getId() +"] ...");

        stub.position(request, new StreamObserver<DroneServiceOuterClass.Position>() {
            @Override
            public void onNext(DroneServiceOuterClass.Position value) {
                InfoDroneConsegna p = new InfoDroneConsegna(value.getID(), value.getX(), value.getY(), value.getInConsegna(), value.getBatteria());
                System.out.println("Info ricevute. Inserimento in lista locale.");
                synchronized (DroneMain.infoDroneConsegna) { //richiedo il lock perchè sto aggiorando la lista
                    DroneMain.infoDroneConsegna.add(p);
                }
            }

            @Override
            public void onError(Throwable t) {
                //non sono risucto a contattare il drone d, significa che il drone è down rimuovo dalla lista locale, ho già il lock
                System.out.println("Drone " + DroneMain.listaDroni.get(i).getId() + " non trovato, rimuovo dalla lista locale");
                DroneMain.listaDroni.remove(i);
                channel.shutdown();
            }

            @Override
            public void onCompleted() {
                channel.shutdown();
            }
        });

        channel.awaitTermination(10, TimeUnit.SECONDS);

        return;

    }
}
