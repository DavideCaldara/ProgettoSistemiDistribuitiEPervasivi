package Droni;

/** Thread avvio gRPC server
 */

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class grpcServerThread extends Thread{
    int PORT;

    public grpcServerThread(int port) {
        this.PORT=port;
    }

    public void run() {
        try {
            //costruisco sulla mia porta server che fornisce servizio DroneServiceImpl()
            Server server = ServerBuilder.forPort(PORT).addService(new DroneServiceImpl()).build();

            //avvio server
            server.start();

            System.out.println("Drone pronto a ricevere connessioni dagli altri droni...");

            //drone in attesa di ricevere connessioni in ingresso

            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
