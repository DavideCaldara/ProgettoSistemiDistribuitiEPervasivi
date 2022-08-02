package Droni;

/** Thread che ogni 10 secondi invia un ping al drone master e controlla se è ancora up
 */

import com.project.grpc.DroneServiceGrpc;
import com.project.grpc.DroneServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

import static Droni.DroneMain.lockAwakeMasterPing;
import static Droni.DroneMain.lockStartNewElection;

public class ThreadPingMaster extends Thread{
    @Override
    public void run() {
        while(true) {

            if (DroneMain.elezioniInCorso){ //se ci sono elezioni fermo ping perchè altrimenti farebbe partire altre elezioni se il nuovo drone master non viene aggiornato prima che questo pinghi
                synchronized (DroneMain.lockAwakeMasterPing) {
                    try {
                        lockAwakeMasterPing.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                Thread.sleep(10000); //ogni 10 secondi controllo se c'è il master
                System.out.println("pingo il master " + DroneMain.IDMaster + " con porta " + DroneMain.PORTMaster + ".");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + DroneMain.PORTMaster).usePlaintext().build();
            DroneServiceGrpc.DroneServiceStub stub = DroneServiceGrpc.newStub(channel);
            DroneServiceOuterClass.ACK ping = DroneServiceOuterClass.ACK.newBuilder().build();
            stub.ping(ping, new StreamObserver<DroneServiceOuterClass.ACK>() {
                @Override
                public void onNext(DroneServiceOuterClass.ACK ack) {

                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println(throwable.toString());
                    System.out.println("DRONE MASTER DOWN!");
                    synchronized (lockStartNewElection) {
                        lockStartNewElection.notify();
                    }
                    channel.shutdown();
                }

                @Override
                public void onCompleted() {

                    System.out.println("canale ping chiuso");
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