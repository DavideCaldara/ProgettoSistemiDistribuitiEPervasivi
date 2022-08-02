package Droni;

import java.util.Scanner;

/** classe per variabili e metodi del singolo drone
 *
 */


public class Drone {

    boolean auto = true;

    final int ID = generaID(auto);
    final String IP = "localhost";
    final int PORT = generaPORT();



    //costruttore
    public void drone() { }


    //Genera nuova porta drone
    private int generaPORT() {
        return (int)(1000+Math.random()*9000);
    }

    //Genera nuovo ID drone auto = false -> manuale
    private static int generaID(boolean auto) {

        if(auto)
            return (int)(1+Math.random()*100); //genera ID casuale tra 1 e 100
        else{
            System.out.println("Inserisci un ID per il nuovo drone: ");
            Scanner input = new Scanner(System.in);
            int id=input.nextInt();

            while(!(id>0 && id<1000)){
                System.out.println("L'ID deve essere un numero compreso tra 0 e 1000. Inserire un nuovo ID: ");
                input.nextInt();
                id=input.nextInt();
            }
            return id;
        }
    }

}
