package ServerAmministratore;

/**
 * classe che contiene la lista dei droni conservata sul server amministratore
 */

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD) //per consentire marshalling e unmarshalling dei droni contenuti nella lista
public class ListaDroni {

    @XmlElement(name="drone")
    private List<DroneRecord> ListaDroni;

    private static ListaDroni instance;

    //costruttore
    private ListaDroni(){
        ListaDroni = new ArrayList<DroneRecord>();
    }

    //singleton
    public synchronized static ListaDroni getInstance(){ //sincronizzare sul metodo della classe non sincronizza realmente perchè crea una nuova istanza di user a ogni richiesta -> creo singleton
        if(instance==null)
            instance = new ListaDroni();
        return instance;
    }

    ///METODI PER RICHIESTE REST
    //get ListaDroni
    public synchronized List<DroneRecord> getListaDroni() {
        return ListaDroni;
    }

    //set ListaDroni
    public synchronized void setListaDroni(List<DroneRecord> listaDroni) {
        ListaDroni = listaDroni;
    }

    //aggiungere nuovo drone
    public synchronized boolean addDrone (DroneRecord d){
        List<DroneRecord> check = getListaDroni();
        for(int i=0; i<check.size(); i++){ //controllo drone id duplicato
            if(check.get(i).getId()==d.getId()) {
                System.out.println("ERRORE! E' già presente un drone con questo ID.");
                return false; //drone non inserito
            }
        }
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        ListaDroni.add(d);
        System.out.println("Aggiunto drone "+d.getId()+".");
        return true; //inserimento andato a buon fine
    }

    //rimuovere drone
    public synchronized void removeDrone (int ID){
        for(int i=0; i<ListaDroni.size(); i++) {
            if(ListaDroni.get(i).getId()==ID) {
                System.out.println("Drone "+ListaDroni.get(i).getId()+" rimosso.");
                ListaDroni.remove(i);
            }
        }
    }
}

