package Droni;

/** Thread client gRPC per invio  messaggio election(batteria, ID)
 */

import com.project.grpc.DroneServiceGrpc;
import com.project.grpc.DroneServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class SendElectionMessageThread extends Thread{
    private int batteria;
    private int ID;

    public SendElectionMessageThread(int batteria, int ID) {
        this.batteria=batteria;
        this.ID = ID;
    }

    @Override
    public void run() {

        //crea client gRPC per contattare il drone successivo
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + DroneMain.droneSucc.getPort()).usePlaintext().build();

        //creo stub per chiamata asincrona sul canale
        DroneServiceGrpc.DroneServiceStub stub = DroneServiceGrpc.newStub(channel);

        //creo la richiesta da inviare al server (batteria, ID)
        DroneServiceOuterClass.ElectionParams params = DroneServiceOuterClass.ElectionParams.newBuilder().setID(ID).setBatteria(batteria).build();

        stub.election(params, new StreamObserver<DroneServiceOuterClass.ACK>() {
            @Override
            public void onNext(DroneServiceOuterClass.ACK value) {
                System.out.println("Il mio drone successivo [Drone " + DroneMain.droneSucc.getId() + "] ha ricevuto il messaggio [Election ID" + ID + ", BATT " + batteria + "].");
            }

            @Override
            public void onError(Throwable t) {
                synchronized (DroneMain.listaDroni) {
                    System.out.println(t.toString());
                    System.out.println("Drone successivo non trovato. Ne cerco un altro e riprovo");
                    for (int i = 0; i < DroneMain.listaDroni.size(); i++) { //eliminio il drone che non c'è più dalla lista locale
                        if (DroneMain.droneSucc.getId() == DroneMain.listaDroni.get(i).getId()) {
                            System.out.println("rimosso drone " + DroneMain.listaDroni.get(i).getId() + " da lista locale");
                            DroneMain.listaDroni.remove(i);

                        }
                    }
                    //trovo un altro drone successivo
                    DroneMain.droneSucc=DroneMain.getDroneSucc(DroneMain.ID);
                    System.out.println("nuovo drone successivo: "+ DroneMain.droneSucc.getId());
                }
                SendElectionMessageThread thread = new SendElectionMessageThread(batteria, ID);
                thread.start();
                channel.shutdown();
            }

            @Override
            public void onCompleted() {
                channel.shutdown();
            }
        });
    }
}
