package Droni;

import CodiceSimulatori.PM10Simulator;
import ServerAmministratore.DroneRecord;
import ServerAmministratore.DroneRecordComparator;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.project.grpc.DroneServiceGrpc;
import com.project.grpc.DroneServiceOuterClass;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**  processo per il singolo drone. Ogni drone deve essere predisposto a diventare drone master.
 * Si coordina con gli altri droni tramite gRPC per:
 * - eleggere il drone master tramite l'algoritmo ring-based, inviare le statistiche al server amministratore;
 * - effettuare le consegne assegnate dal drone master;
 * - uscire dal sistema quando il proprio livello di batteria<15%
 */

public class DroneMain {

    static int ID;
    static int PORT;

    static volatile boolean isMaster;

    static ArrayList<DroneRecord> listaDroni;

    static volatile int batteria;

    static volatile int X;
    static volatile int Y;

    static volatile int IDMaster;
    static volatile int PORTMaster;

    static DroneRecord droneSucc;

    static Object lockStartNewElection;
    static volatile boolean elezioniInCorso;
    static volatile boolean partecipante;

    static Object lockAvvioFunzionalitaMaster;

    static ArrayList<InfoDroneConsegna> infoDroneConsegna;
    static CodaConsegne queueConsegne;
    static volatile boolean inConsegna;
    static volatile int totConsegneEffettuate;
    static volatile float totChilometriPercorsi;
    //variabili per master
    static volatile int totConsegneEffettuateDaTuttiIDroni;
    static volatile int totChilometriPercorsiDaiDroni;
    static ArrayList<Float> misurazioniRicevuteDaiDroni;

    static ArrayList<MediaMisurazione> acquisizioniSensore;

    static Object lockDisconnectMQTT;
    static Object lockinConsegna;
    static Object lockCodaConsegneVuota;
    static Object lockStatGlobaliComunicate;
    static Object lockElezioniInCorso;
    static Object lockStartQuit;
    static Object quit;
    static Object lockAwakeMasterPing;

    static boolean contattatoDuranteElezioni;




    public static void main(String[] args) throws InterruptedException {

        System.out.println("Avvio procedura creazione nuovo Drone ...");

        Drone drone = new Drone();

        listaDroni = new ArrayList<>();
        infoDroneConsegna = new ArrayList<>();
        ID = drone.ID;
        PORT = drone.PORT;
        elezioniInCorso = false;
        partecipante = false;
        batteria = 100;
        X = 0;
        Y = 0;
        queueConsegne = new CodaConsegne();
        totConsegneEffettuate = 0;
        totChilometriPercorsi = 0;
        totConsegneEffettuateDaTuttiIDroni = 0;
        totChilometriPercorsiDaiDroni = 0;
        misurazioniRicevuteDaiDroni = new ArrayList<>();
        inConsegna = false;

        lockAvvioFunzionalitaMaster = new Object();
        lockStartNewElection = new Object();
        lockDisconnectMQTT = new Object();
        lockinConsegna = new Object();
        lockCodaConsegneVuota = new Object();
        lockStatGlobaliComunicate = new Object();
        lockElezioniInCorso = new Object();
        lockStartQuit = new Object();
        quit = new Object();
        lockAwakeMasterPing = new Object();

        acquisizioniSensore = new ArrayList<>();

        contattatoDuranteElezioni = false;

        IDMaster=0;
        PORTMaster=0;


        //TODO FASE DI INIZIALIZZAZIONE DRONE E RICHIESTA ACCESSO A SMART-CITY

        //aggiungi drone al sistema (invia dati a server amministratore) http://localhost:1337/drone/add (costruire json)
        Client client = Client.create();
        WebResource webResource2 = client.resource("http://localhost:1337/drone/add");
        String DroneRequestJSON = "{\"id\":\""+ drone.ID +"\",\"ip\": \"localhost\",\"port\":\""+ drone.PORT +"\"}";
        System.out.println("Richiesta di accesso alla smart-city inoltrata al server amministratore ...");
        ClientResponse addingresponse = webResource2.type("application/json").post(ClientResponse.class, DroneRequestJSON);

//        System.out.println(addingresponse.getStatus());
        if (addingresponse.getStatus() != 200) { //chiudere drone se c'è già un drone con lo stesso ID
            System.out.println("ERRORE! ID GIA' PRESENTE. CHIUSURA DRONE IN CORSO ...");
            System.exit(0);
        }

        //ricezione e inizializzazione coordinate nella smart-city tramite richiesta rest al server amministratore
        String coord = addingresponse.getEntity(String.class); // coord = text ricevuto da drone/coord
        String[] coordArr = coord.split(",");
        X = Integer.parseInt(coordArr[0]);
        Y = Integer.parseInt(coordArr[1]);


        System.out.println("Richiesta accettata. \n" + "Drone correttamente registrato nel sistema.");


        //ricezione elenco di droni già presenti nella smart-city tramite richiesta rest al server amministratore
        System.out.println("Richiesta ricezione lista droni a server amministratore inoltrata...");
        WebResource webResource = client.resource("http://localhost:1337/admin/lista");
        ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
        String output = response.getEntity(String.class);
        System.out.println("Lista droni ricevuta correttamente.");

        //convertire stringa JSON in Arraylist java. ESEMPIO STRINGA RICEVUTA [{"id":"453","ip":"localhost","port":156},{"id":"666","ip":"localhost","port":1235}]
        Gson gson = new Gson();
        Type dronesListType = new TypeToken<ArrayList<DroneRecord>>(){}.getType();
        listaDroni = gson.fromJson(output, dronesListType); //listaDroni conterrà tutti i droni presenti nella rete e deve essere aggiornata quando un nuovo drone entra nella rete. Il messaggio arriverà dal nuovo drone
        listaDroni.sort(new DroneRecordComparator()); //riordino lista droni per ID (mi serve dopo per trovare successivo)

        //ho ricevuto anche me stesso nella lista, voglio togliermi.
        for(int i=0; i<listaDroni.size(); i++){
            if(drone.ID == listaDroni.get(i).getId())
                listaDroni.remove(i);
        }

        System.out.println("Elenco droni ricevuti: ");
        for(DroneRecord d : listaDroni) //prova
            System.out.println(d.getId());


        //se la lista è vuota mi eleggo drone master
        if(listaDroni.isEmpty()) {
            System.out.println("Sono il master");
            isMaster = true;
            IDMaster = ID;
            PORTMaster = PORT;
        }

        //cerco e salvo drone successivo per costruire rete ad anello. SE SONO IL PRIMO DRONE NON HO UN SUCCESSIVO?? --> sistemato: si aggiorna ogni volta che un nuovo drone appena entrato nella rete mi contatta
        if(!listaDroni.isEmpty()) {
            droneSucc = getDroneSucc(drone.ID);
            System.out.println("Mio ID: " + drone.ID); //prova
            System.out.println("Drone successivo: " + droneSucc.getId());
        }




        System.out.println("Drone "+ID+" con porta "+PORT+" immesso nella smart-city alle coordinate [" + X + "," + Y +"]");


        // TODO DRONE COMPLETAMENTE INIZIALIZZATO E REGISTRATO SUL SERVER AMMINISTRATORE


        // TODO FASE DI PRESENTAZIONE AGLI ALTRI DRONI DELLA RETE
        // invio in broadcast dei propri parametri di inizializzazione per immissione nelle proprie liste del nuovo drone
        // comunicazione in broadcast con gRPC a tutti gli altri droni dei propri dati di inizializzazione per essere immessi nelle proprie liste, comunicando la propria posizione di partenza. Deve anche capire chi è il drone master
        // Sistema p2p: ogni drone funge sia da client che da server

        // Avvio le funzionalità di server peer del mio drone tramite thread, devo restare sempre in ascolto in caso un altro drone abbia bisogno di contattarmi. Implementa tutti i servizi gRPC
        grpcServerThread server = new grpcServerThread(PORT);
        server.start();
        System.out.println("Server Thread avviato.");

        //Thread che avvierà la procedura di elezione in caso ce ne sia bisogno, contiene metodo che forwarda il messaggio, viene richiamato anche dal server gRPC
        ElectionThread electionThread = new ElectionThread();
        electionThread.start();
        /** Quando mi accorgo che è caduto il drone master mi basterà fare
         *
         * synchronized(lockStartNewElection){
         *     lockStartNewElection.notify();
         * }
         *
         * il drone che si è accorto manderà il primo messaggio che avvia l'elezione
         */

        System.out.println("Avvio procedura di presentazione agli altri droni...");

        //scorro la lista locale dei droni, contatto ogni drone e invio i miei dati, quando contatto il drone master salvo le sue info
        for(int i=0; i<listaDroni.size(); i++){
            contactDrone(listaDroni.get(i));
        }


        System.out.println("Presentazione completata");


        //TODO THREAD SENSORE E GESTIONE RACCOLTA DATI DEL SENSORE
        Buffer buff = new Buffer();
        PM10Simulator sensorThread = new PM10Simulator(buff);
        sensorThread.start();
        System.out.println("Sensore di inquinamento PM10 avviato...");
        //thread che svuota il buffer e salva la media dei valori registrati in aquisizioniSensore
        PM10BufferReader pm10BufferReader = new PM10BufferReader(buff);
        pm10BufferReader.start();

        //TODO THREAD PING MASTER PER CONTROLLO PERIODICO
        ThreadPingMaster threadPingMaster = new ThreadPingMaster();
        threadPingMaster.start();

        //TODO FUNZIONALITA' DRONE MASTER - quello che devo fare se io sono master
        //avvio thread funzionalità master e lo metto in wait. Lo attivo solo se il drone diventa master
        FunzionalitaMaster funzionalitaMaster = new FunzionalitaMaster();
        funzionalitaMaster.start();

        //TODO THREAD PER STAMPA PERIODICA INFO DRONE
        ThreadStampaInfo threadStampaInfo = new ThreadStampaInfo();
        threadStampaInfo.start();

        //TODO THREAD GESTIONE USCITA DRONE
        ThreadQuit threadQuit = new ThreadQuit();
        threadQuit.start();

        //TODO THREAD LETTURA QUIT DA INPUT
        ThreadReadQuit threadReadQuit = new ThreadReadQuit();
        threadReadQuit.start();

        synchronized (quit){
            quit.wait();
        }

        System.out.println("Drone in chiusura...");
        System.exit(0);



    }//end main




    //Metodo getDroneSucc(): dato un ID trova e ritorna il drone successivo da listaDroni
    public static DroneRecord getDroneSucc(int ID) {
        synchronized (listaDroni) {
            int i = 0;
            try {
                while (ID > listaDroni.get(i).getId()) { //la lista ricevuta è già ordinata, mi fermo quando trovo un ID maggiore
                    i++;
                }
                return listaDroni.get(i);
            } catch (IndexOutOfBoundsException e) {
                if(listaDroni.isEmpty())
                    return new DroneRecord(ID, "localhost", PORT);
                return listaDroni.get(0); //sono il drone con l'ID più alto, il mio successivo è il primo per chiudere il cerchio
            }
        }
    }


    //Metodo contactDrone(): Dato me stesso e un drone da contattare fornisce funzionalità di client per la presentazione
    private static void contactDrone(DroneRecord droneToContact) { //FUNZIONANTE CONTROLLATO

        //creo un canale di comunicazione e specifico con forTarget indirizzo del "drone server" e porta del servizio
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + droneToContact.getPort()).usePlaintext().build();

        //creo stub per chiamata asincrona sul canale
        DroneServiceGrpc.DroneServiceStub stub = DroneServiceGrpc.newStub(channel);

        //creo la richiesta da inviare al server (ID,IP,porta,posizione)
        DroneServiceOuterClass.DroneMe request = DroneServiceOuterClass.DroneMe.newBuilder().setID(ID).setIP("localhost").setPORT(PORT).setPosizione(X + "," + Y).build();
        System.out.println("Sto cercando di mettermi in contatto con il drone " + droneToContact.getId() + " ...");

        //chiamo servizio grpc sullo stub e gestisco diverse risposte
        stub.welcome(request, new StreamObserver<DroneServiceOuterClass.MasterDrone>() {
            @Override
            public void onNext(DroneServiceOuterClass.MasterDrone value) {
                if(value.getIsMaster()){
                    System.out.println("Risposta ricevuta dal drone master. [Drone " + droneToContact.getId() + "].");
                    //salvo info utili che ho ricevuto dal drone master
                    IDMaster = value.getID();
                    PORTMaster = value.getPORT();
                    System.out.println("Drone Master [ID: " + value.getID() + " PORT: " + value.getPORT() + "] salvato.");
                }
            }

            @Override
            public void onError(Throwable t) {
                //mi rispondono sicuramente tutti i droni perchè un drone per uscire deve prima passare dal server e quindi la lista che ricevo è aggiornata,
                //non so però se tra questi c'è il master --> controllo dopo se non ho ricevuto un master
            }

            @Override
            public void onCompleted() {
                channel.shutdown(); //chiudo canale
            }
        });


        return;

    }



}
