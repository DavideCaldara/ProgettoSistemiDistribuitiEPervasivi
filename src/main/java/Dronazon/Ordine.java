package Dronazon;

/** Classe ordine per rappresentare singolo ordine generato da Dronazon. Campi:
 * - ID
 * - Punto di ritiro
 * - Punto di consegna
 */

public class Ordine {
    private int ID;
    private Coordinate PuntoRitiro;
    private Coordinate PuntoConsegna;

    public Ordine(int ID, Coordinate pr, Coordinate pc){
        this.ID=ID;
        this.PuntoRitiro=pr;
        this.PuntoConsegna=pc;
    }

    @Override
    public String toString() {
        return (ID+","+PuntoRitiro.x+","+PuntoRitiro.y+","+PuntoConsegna.x+","+ PuntoConsegna.y);
    }

    public int getID() {
        return ID;
    }

    public int getPuntoRitiroX() {
        return PuntoRitiro.x;
    }

    public int getPuntoRitiroY() {
        return PuntoRitiro.y;
    }

    public int getPuntoConsegnaX() {
        return PuntoConsegna.x;
    }

    public int getPuntoConsegnaY() {
        return PuntoConsegna.y;
    }

}
