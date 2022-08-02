package Droni;

import ServerAmministratore.DroneRecord;
import ServerAmministratore.DroneRecordComparator;
import com.project.grpc.DroneServiceGrpc.*;
import com.project.grpc.DroneServiceOuterClass.*;
import io.grpc.stub.StreamObserver;

/** Implementazione servizi gRPC
 */


public class DroneServiceImpl extends DroneServiceImplBase {

    @Override
    public void welcome(DroneMe droneme, StreamObserver<MasterDrone> responseObserver) {

        System.out.println("Nuovo Drone ricevuto!");

        //la richiesta è di tipo DroneMe (definito in .proto)
        // System.out.println(droneme);

        //smonto richiesta, ricostruisco droneRecord
        DroneRecord msg = new DroneRecord(droneme.getID(), droneme.getIP(), droneme.getPORT());

        //lo aggiungo alla mia lista locale
        synchronized (DroneMain.listaDroni) { //acquisisco lock, sto modificando la lista all'interno di un thread
            DroneMain.listaDroni.add(msg);
            DroneMain.listaDroni.sort(new DroneRecordComparator());


            //Controllo lista aggiornata correttamente
            System.out.println("Lista droni locale aggiornata");
            System.out.println("ID \t    IP \t    PORT \n");
            for (DroneRecord drone : DroneMain.listaDroni) { //stampo nuova lista aggiornata
                System.out.println(drone.getId() + "\t" + drone.getIP() + "\t" + drone.getPort() + "\n");
            }

            //un nuovo drone è entrato nella rete -> aggiorno drone successivo per ricorstuire l'anello
            DroneMain.droneSucc = DroneMain.getDroneSucc(DroneMain.ID);
            System.out.println("Drone successivo aggiornato: [Drone " + DroneMain.droneSucc.getId() + "].");
        }


        //se sono il drone master devo aggiungere un campo anche alla lista dettagliata
        String[] pos = droneme.getPosizione().split(",");
        if(DroneMain.isMaster){
            synchronized (DroneMain.infoDroneConsegna){
                DroneMain.infoDroneConsegna.add(new InfoDroneConsegna(droneme.getID(), Integer.parseInt(pos[0]), Integer.parseInt(pos[1]), false));
            }
        }

        //se c'è un'elezione in corso setto contattato durante elezioni a true
        if(DroneMain.elezioniInCorso)
            DroneMain.contattatoDuranteElezioni = true;

        //costruisco la richiesta di tipo MasterDrone (sempre definito in .proto)
        //creo la risposta
        MasterDrone response = MasterDrone.newBuilder().setID(DroneMain.ID).setPORT(DroneMain.PORT).setIsMaster(DroneMain.isMaster).build();

        //passo la risposta all'observer che la invierà al client
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    @Override
    public void election(ElectionParams params, StreamObserver<ACK> responseObserver) {

        System.out.println("Elezione in corso...");
        DroneMain.elezioniInCorso = true;
//        System.out.println(params.getBatteria() +","+ params.getID() );
//        System.out.println(DroneMain.batteria +","+DroneMain.ID );


        //Ho ricevuto messaggio election(ID, batteria)
        //se la batteria che ricevo è maggiore della mia, inoltro il messaggio e mi marco come PARTECIPANTE
        if (params.getBatteria() > DroneMain.batteria) {
            DroneMain.partecipante = true;
//            System.out.println("rispondo");
            ACK response = ACK.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            SendElectionMessageThread thread = new SendElectionMessageThread(params.getBatteria(), params.getID());
            thread.start();
        }
        else{
//            System.out.println("else 1");
        }
        //se la batteria che ricevo è minore della mia, se sono NON PARTECIPANTE cambio il mio stato in PARTECIPANTE e invio il messaggio con mio ID e batteria. Se sono già PARTECIPANTE non invio il messaggio
        if (params.getBatteria() < DroneMain.batteria) {
            if (DroneMain.partecipante == false) {
                DroneMain.partecipante = true;
//                System.out.println("rispondo");
                ACK response = ACK.newBuilder().build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                SendElectionMessageThread thread = new SendElectionMessageThread(DroneMain.batteria, DroneMain.ID);
                thread.start();
            } else {
                //do nothing
                ACK response = ACK.newBuilder().build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        }
        else{
//            System.out.println("else 2");
        }
        //se ricevo un valore uguale di batteria devo controllare ID
        if (params.getBatteria() == DroneMain.batteria) {
//            System.out.println("if batt uguale");
            //In questo punto sono sicuro che ho ricevuto un uguale valore di batteria
            //Se l'ID che ricevo è maggiore del mio, inoltro il messaggio e mi marco come PARTECIPANTE
            if (params.getID() > DroneMain.ID) {
                DroneMain.partecipante = true;
//                System.out.println("rispondo");
                ACK response = ACK.newBuilder().build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                SendElectionMessageThread thread = new SendElectionMessageThread(params.getBatteria(), params.getID());
                thread.start();
            }
            //se l'ID che ricevo è minore del mio, se sono NON PARTECIPANTE cambio il mio stato in PARTECIPANTE e invio il messaggio con mio ID e batteria. Se sono già PARTECIPANTE non invio il messaggio
            if (params.getID() < DroneMain.ID) {
                if (DroneMain.partecipante = false) {
                    DroneMain.partecipante = true;
//                    System.out.println("rispondo");
                    ACK response = ACK.newBuilder().build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                    SendElectionMessageThread thread = new SendElectionMessageThread(DroneMain.batteria, DroneMain.ID);
                    thread.start();
                } else {
                    //do nothing
                    ACK response = ACK.newBuilder().build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }
            }
            //Se l'ID ricevuto è uguale al mio (quindi anche batteria) sono il nuovo drone master. Marco me stesso come non partecipante e invio il messaggio elected(batteria, ID) ai processi successivi
            if (params.getID() == DroneMain.ID) {
                System.out.println("Sono il nuovo drone master con ID: " + DroneMain.ID + " e batteria: " + DroneMain.batteria);
                DroneMain.isMaster = true;
                DroneMain.partecipante = false;
//                System.out.println("rispondo");
                ACK response = ACK.newBuilder().build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                SendElectedMessageThread thread = new SendElectedMessageThread(DroneMain.batteria, DroneMain.ID); //forward messaggio drone master eletto
                thread.start();
            }
        }
        else{
//            System.out.println("else 3");
        }


//        responseObserver.onCompleted();

    }

    @Override
    public void elected(ElectionParams params, StreamObserver<ACK> responseObserver) {

        //ho ricevuto un messaggio di master eletto, marco me stesso come NON PARTECIPANTE, salvo il nuovo drone master e inoltro il messaggio a meno che io non sia il nuovo drone master

        DroneMain.IDMaster = params.getID(); //aggiorno ID master
        //aggiorno porta master
        synchronized (DroneMain.listaDroni){ //sto leggendo la lista droni da un thread, voglio il lock
            for(DroneRecord d : DroneMain.listaDroni){
                if(DroneMain.IDMaster == d.getId())
                    DroneMain.PORTMaster = d.getPort();
            }
            //se sono io il drone master non mi trovo nella lista, aggiorno autonomamente
            if(DroneMain.isMaster)
                DroneMain.PORTMaster = DroneMain.PORT;
        }

        System.out.println("Salvato nuovo drone master "+ DroneMain.IDMaster+ " con porta " + DroneMain.PORTMaster + ".");

        DroneMain.partecipante = false;

        if (!(params.getID() == DroneMain.ID)) { //se non sono il nuovo drone master appena eletto inoltro il messaggio, altrimenti mi fermo
            DroneMain.elezioniInCorso = false;
            DroneMain.contattatoDuranteElezioni = false;
            System.out.println("Elezione terminata");

            ACK response = ACK.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            SendElectedMessageThread thread = new SendElectedMessageThread(params.getBatteria(), params.getID());
            thread.start();
        } else {
            if(DroneMain.contattatoDuranteElezioni){ // per gestire il caso in cui un nuovo drone entri a elezioni in corso
                DroneMain.elezioniInCorso = false;
                DroneMain.contattatoDuranteElezioni = false;
                ACK response = ACK.newBuilder().build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                SendElectedMessageThread thread = new SendElectedMessageThread(params.getBatteria(), params.getID()); //faccio girare una seconda volta il messaggio elected nell'anello, così anche il nuovo drone sa chi è il nuovo master
                thread.start();
            }
            else { //se non mi hanno contattato, nessun altro lo sarà stato
                DroneMain.elezioniInCorso = false;
                ACK response = ACK.newBuilder().build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
            //avvio funzionalità master
            synchronized (DroneMain.lockAvvioFunzionalitaMaster){
                DroneMain.lockAvvioFunzionalitaMaster.notify();
            }
        }

        //riprendo ping al master
        synchronized (DroneMain.lockAwakeMasterPing){
            DroneMain.lockAwakeMasterPing.notify();
        }
        
//        responseObserver.onCompleted();
    }


    @Override
    public void position(ACK request, StreamObserver<Position> responseObserver) {
        //Costruisco risposta con le mie info
        Position reponse = Position.newBuilder().setID(DroneMain.ID).setX(DroneMain.X).setY(DroneMain.Y).setInConsegna(DroneMain.inConsegna).setBatteria(DroneMain.batteria).build();
        responseObserver.onNext(reponse);
        responseObserver.onCompleted();
    }

    @Override
    public void assegnaconsegna(Ordine consegna, StreamObserver<Info> responseObserver) {
        //ho ricevuto la consegna da effettuare
        //effettuo la consegna e resituisco tutte le informazioni (*) al drone master

        double timestampArrivoConsegna;
        int newX = consegna.getXc(); //nuova posizione del drone dopo consegna *
        int newY = consegna.getYc();
        float chilometriPercorsi = 0;
        float mediaMisurazioniPM10 = 0;
        int batteriaResidua;

        DroneMain.inConsegna = true; //cambio mio stato inConsegna
        System.out.println("------Drone in consegna!------");
        System.out.println("------Consegna "+ consegna.getID()+" in corso------");
        System.out.println("Ordine "+ consegna.getID() +" - Posizione attuale [" + DroneMain.X + "," + DroneMain.Y + "] Punto ritiro [" + consegna.getXr() + "," + consegna.getYr() + "] Punto Consegna [" + consegna.getXc() + "," + consegna.getYc() + "]");



        //effettua consegna
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //consegna effettuata

        DroneMain.totConsegneEffettuate += 1;

        timestampArrivoConsegna = System.currentTimeMillis(); //timesatmp di arrivo al luogo di consegna *

//        System.out.println("Distanza consegna in corso " + (distanza(DroneMain.X, DroneMain.Y, consegna.getXr(), consegna.getYr())+distanza(consegna.getXr(), consegna.getYr(), newX, newY)));
        chilometriPercorsi = distanza(DroneMain.X, DroneMain.Y, consegna.getXr(), consegna.getYr())+distanza(consegna.getXr(), consegna.getYr(), newX, newY);//chilometri percorsi per effettuare la consegna (Pattuale-Pritiro)+(Pritiro-Pconsegna) *
        DroneMain.totChilometriPercorsi += chilometriPercorsi;

        //movimento da posizione attuale a punti di ritiro e poi consegna
        //aggiorno posizione attuale drone in main
        DroneMain.X=newX; // *
        DroneMain.Y=newY; // *

        //livello di batteria residuo
        DroneMain.batteria-=10;
        batteriaResidua = DroneMain.batteria; // *
//        System.out.println("Batteria residua: "+ batteriaResidua);
        if(batteriaResidua < 15){ //se sono sotto 15% di batteria avvio procedura di uscita
            synchronized (DroneMain.lockStartQuit){
                DroneMain.lockStartQuit.notify();
            }
        }


        float sum = 0;
        //raccolgo misurazioni e svuoto struttura dati
        synchronized (DroneMain.acquisizioniSensore){
            for(MediaMisurazione m : DroneMain.acquisizioniSensore){
                //faccio la media di tutte le misurazioni presenti
                sum += m.getMedia();
            }
            mediaMisurazioniPM10 = sum/DroneMain.acquisizioniSensore.size(); // *

            //svuoto la struttura dati
            DroneMain.acquisizioniSensore.clear();
        }

        //costruisco risposta
        Info response = Info.newBuilder().setTimestamp(timestampArrivoConsegna).setNewX(newX).setNewY(newY).setKm(chilometriPercorsi).setMediastatPM10(mediaMisurazioniPM10).setBatteria(batteriaResidua).build();

        //mi setto come non più in consegna
        DroneMain.inConsegna = false;
        synchronized (DroneMain.lockinConsegna){ //notifico se c'è un drone in uscita
            DroneMain.lockinConsegna.notify();
        }

        //invio risposta al drone master
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    private float distanza(int x1, int y1, int x2, int y2){
        return (float) Math.sqrt(Math.pow((x2-x1),2)+Math.pow((y2-y1),2));
    }

    @Override
    public void ping(ACK request, StreamObserver<ACK> responseObserver) {
        ACK response = ACK.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void sendInfo(Position request, StreamObserver<ACK> responseObserver) {
        InfoDroneConsegna p = new InfoDroneConsegna(request.getID(), request.getX(), request.getY(), request.getInConsegna(), request.getBatteria());
        System.out.println("Info ricevute. Inserimento in lista locale.");
        synchronized (DroneMain.infoDroneConsegna) { //richiedo il lock perchè sto aggiorando la lista
            DroneMain.infoDroneConsegna.add(p);
        }
        responseObserver.onNext(ACK.newBuilder().build());
        responseObserver.onCompleted();
    }
}
