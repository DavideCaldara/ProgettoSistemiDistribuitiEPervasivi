package ServerAmministratore;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * classe per la rappresentazione della struttura dati che conserva le statistiche ricevute dai droni
 */

@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class StatsDB {

    @XmlElement(name="stat")
    private List<StatRecord> Statistiche;

    private static StatsDB instance;

    //Costruttore
    private StatsDB(){
        Statistiche = new ArrayList<StatRecord>();
    }

    //singleton
    public synchronized static StatsDB getInstance(){
        if(instance==null)
            instance = new StatsDB();
        return instance;
    }

    //aggiungere nuovo record statistiche
    public synchronized void addStat (StatRecord stat){
        Statistiche.add(stat);
        System.out.println("Nuova statistica ricevuta.");
    }

    public synchronized List<StatRecord> getStatList() {
        return Statistiche;
    }

    //metodo che restituisce una lista degli ultimi n record
    public synchronized static List<StatRecord> getNStats(int n){
        List<StatRecord> response = getInstance().Statistiche.subList(getInstance().Statistiche.size()-n, getInstance().Statistiche.size());
        return response;
    }

    //metodo che restituisce la media delle consegne tra 2 timestamp
    public static float getConsegne(Timestamp t1, Timestamp t2) {
        List<StatRecord> copy = StatsDB.getInstance().getStatList();
        float consegne=0;
        int n=0;
        for(int i=0; i<copy.size();i++){
            if((copy.get(i).getTimestamp()>t1.getTime()) && (copy.get(i).getTimestamp()<=t2.getTime())){
                consegne=+copy.get(i).getConsegne();
                n++;
            }
        }
        return consegne/n;
    }

    //metodo che restituisce la media dei chilometri percorsi tra 2 timestamps
    public static float getChilometri(Timestamp t1, Timestamp t2) {
        List<StatRecord> copy = StatsDB.getInstance().getStatList();
        float chilometri=0;
        int n=0;
        for(int i=0; i<copy.size();i++){
            if((copy.get(i).getTimestamp()>t1.getTime()) && (copy.get(i).getTimestamp()<=t2.getTime())){
                chilometri=+copy.get(i).getConsegne();
                n++;
            }
        }
        return chilometri/n;
    }
}
