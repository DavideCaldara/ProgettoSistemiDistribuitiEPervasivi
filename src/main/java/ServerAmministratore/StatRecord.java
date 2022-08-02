package ServerAmministratore;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * classe singolo record statistiche droni ricevute dal drone master:
 * - media numero consegne effettuate dai droni
 * - media chilometri percorsi dai droni
 * - media del livelllo di inquinamento rilevato dai droni
 * - media del livello di batteria residuo dei droni
 * - timpestamp
 */

@XmlRootElement
public class StatRecord {
    private float consegne;
    private float chilometripercorsi;
    private float livelloinquinamento;
    private float livellobatteria;
    private double timestamp;

    public StatRecord(){}

    public StatRecord(float consegne, float chilometripercorsi, float livelloinquinamento, float livellobatteria, double timestamp){
        this.consegne=consegne;
        this.chilometripercorsi=chilometripercorsi;
        this.livelloinquinamento=livelloinquinamento;
        this.livellobatteria=livellobatteria;
        this.timestamp = timestamp;
    }

    //Metodi getter
    public float getConsegne() {
        return consegne;
    }

    public float getChilometripercorsi() {
        return chilometripercorsi;
    }

    public float getLivelloinquinamento() {
        return livelloinquinamento;
    }

    public float getLivellobatteria() {
        return livellobatteria;
    }

    public double getTimestamp() {
        return timestamp;
    }

}
