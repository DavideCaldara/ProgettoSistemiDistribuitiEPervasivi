package Droni;

/** "riga" lista info dettagliate dei droni per il drone master
 */

public class InfoDroneConsegna {
    int ID;
    int X;
    int Y;
    boolean inConsegna;
    int batteria;

    public InfoDroneConsegna(int ID, int X, int Y, boolean inConsegna, int batteria) {
        this.ID = ID;
        this.X = X;
        this.Y = Y;
        this.inConsegna=inConsegna;
        this.batteria=batteria;
    }

    public InfoDroneConsegna(int ID, int X, int Y, boolean inConsegna) {
        this.ID = ID;
        this.X = X;
        this.Y = Y;
        this.inConsegna=inConsegna;
        this.batteria=100;
    }



    public int getID() {
        return ID;
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    public void setX(int x) {
        X = x;
    }

    public void setY(int y) {
        Y = y;
    }

    public boolean isInConsegna() {
        return inConsegna;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setInConsegna(boolean inConsegna) {
        this.inConsegna = inConsegna;
    }

    public int getBatteria() {
        return batteria;
    }

    public void setBatteria(int batteria) {
        this.batteria = batteria;
    }


}
