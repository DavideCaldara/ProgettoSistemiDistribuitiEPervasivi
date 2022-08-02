package Droni;

/** Thread client gRPC per invio messaggio elected(batteria, ID)
 */

import com.project.grpc.DroneServiceGrpc;
import com.project.grpc.DroneServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class SendElectedMessageThread extends Thread{
    private int batteria;
    private int ID;

    public SendElectedMessageThread(int batteria, int ID) {
        this.batteria = batteria;
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

        stub.elected(params, new StreamObserver<DroneServiceOuterClass.ACK>() {
            @Override
            public void onNext(DroneServiceOuterClass.ACK value) {
                System.out.println("Il mio drone successivo [Drone " + DroneMain.droneSucc.getId() + "] ha ricevuto il messaggio [Elected " + ID + ", " + batteria + "].");
            }

            @Override
            public void onError(Throwable t) {
                //da non gestire, un drone non può uscire con un'elezione in corso
            }

            @Override
            public void onCompleted() {
                channel.shutdown();
            }
        });
    }
}
