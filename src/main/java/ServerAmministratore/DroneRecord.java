package ServerAmministratore;

/**
 * classe per la rappresentazione della smart-city sul server amministratore
 * per ogni drone (record della lista) devo registrare:
 * - ID
 * - IndirizzoIP
 * - Porta
 * - posizione nella smart city???
 */


import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement //predispone la classe a marshalling e unmarshalling
public class DroneRecord{
    private int id;
    private String ip;
    private int port;

    public DroneRecord(){}

    //ID e PORT ricevuti alla creazione di un nuovo drone client REST
    public DroneRecord(int ID, String IP,int PORT){
        this.id =ID;
        this.ip=IP;
        this.port =PORT;
    }

    //metodi getter
    public int getId() {
        return id;
    }

    public String getIP() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}

