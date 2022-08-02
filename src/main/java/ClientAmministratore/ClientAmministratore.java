package ClientAmministratore;

import ServerAmministratore.DroneRecord;
import ServerAmministratore.StatRecord;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;

/** Client amministratore è un'interfaccia per inoltrare richiestre dei servizi
 * erogati dal Server Amministratore:
 * - Restituire lista droni
 * - Restituire ultime n statistiche
 * - Media del numero di consegne effettuate dai droni tra due timestamp
 * - Media del numero di chilometri percorsi dai droni tra due timestamp
 * */

public class ClientAmministratore {
    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);
        int servizio;

        System.out.println("----------INTERFACCIA AMMINISTRATORE----------\n");
        System.out.println("Servizi disponibili:\n" +
                "1. Lista droni\n" +
                "2. Richiesta ultime n Statistiche\n" +
                "3. Media consegne effettuate\n" +
                "4. Media chilometri percorsi\n");
        while(true){
            System.out.println("Inserisci numero e premi invio per richiedere il servizio desiderato: ");
            servizio = input.nextInt();

            Client client = Client.create();

            switch(servizio){ //ricevo JSON devo convertire
                case 1:{ //lista droni http://localhost:1337/admin/lista

                    WebResource webResource = client.resource("http://localhost:1337/admin/lista");
                    ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
                    String output = response.getEntity(String.class);
                    System.out.println("Lista droni ricevuta...");
                    //Ricostruzione lista e stampa
                    Gson gson = new Gson();
                    Type dronesListType = new TypeToken<ArrayList<DroneRecord>>(){}.getType();
                    ArrayList<DroneRecord> lista = gson.fromJson(output, dronesListType);

                    String leftAlignFormat = "| %-5d | %-9s | %-4d |%n";

                    System.out.format("+-------+-----------+------+%n");
                    System.out.format("| ID    | IP        | PORT |%n");
                    System.out.format("+-------+-----------+------+%n");
                    for (int i = 0; i < lista.size(); i++) {
                        System.out.format(leftAlignFormat,  lista.get(i).getId(), lista.get(i).getIP(), lista.get(i).getPort());
                    }
                    System.out.format("+-------+-----------+------+%n");

                    break;
                }
                case 2:{ //ultime n statistiche http://localhost:1337/admin/get/{n}
                    System.out.println("Inserire numero di statistiche desiderate: ");
                    int n = input.nextInt();
                    WebResource webResource = client.resource("http://localhost:1337/admin/get/"+n);
                    ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
                    String output = response.getEntity(String.class);
                    System.out.println("Lista statistiche ricevuta...");
                    Gson gson = new Gson();
                    Type Statlist = new TypeToken<ArrayList<StatRecord>>(){}.getType();
                    ArrayList<StatRecord> listaricevuta = gson.fromJson(output, Statlist); //listaDroni conterrà tutti i droni presenti nella rete e deve essere aggiornata quando un nuovo drone entra nella rete. Il messaggio arriverà dal nuovo drone

                    String leftAlignFormat = "| %-10f | %-10f | %-12f | %-10f | %-20s |%n";

                    System.out.format("+------------+------------+--------------+------------+-------------------------+%n");
                    System.out.format("| Consegne   | Chilometri | Inquinamento | Batterie   |        Timestamp        |%n");
                    System.out.format("+------------+------------+--------------+------------+-------------------------+%n");
                    for (int i = 0; i < listaricevuta.size(); i++) {
                        System.out.format(leftAlignFormat,  listaricevuta.get(i).getConsegne(), listaricevuta.get(i).getChilometripercorsi(), listaricevuta.get(i).getLivelloinquinamento(), listaricevuta.get(i).getLivellobatteria(), new Timestamp((long) listaricevuta.get(i).getTimestamp()));
                    }
                    System.out.format("+------------+------------+--------------+------------+-------------------------+%n");

                    break;
                }
                case 3:{ //http://localhost:1337/admin/consegne/{timestamp}
                    System.out.println("Inserire timestamps desiderati nel seguente formato separati da ',': yyyy-mm-dd hh:mm:ss");
                    System.out.println("Inserire primo timestamp: ");
                    Scanner in = new Scanner(System.in);
                    String t1 = in.nextLine();
                    while (!isTimeStampValid(t1)){
                        System.out.println("Timestamp inserito non valido. Inserirne uno nuovo (formato yyyy-mm-dd hh:mm:ss): ");
                        t1 = in.nextLine();
                    }
                    String t1replaced = t1.replace(" ", "&");
                    System.out.println("Inserire secondo timestamp: ");
                    String t2 = in.nextLine();
                    while (!isTimeStampValid(t2)){
                        System.out.println("Timestamp inserito non valido. Inserirne uno nuovo (formato yyyy-mm-dd hh:mm:ss): ");
                        t2 = in.nextLine();
                    }
                    String t2replaced = t1.replace(" ", "&");
                    WebResource webResource = client.resource("http://localhost:1337/admin/consegne/"+t1replaced+","+t2replaced);
                    ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
                    String output = response.getEntity(String.class);
                    System.out.println("Consegne effettuate tra "+t1+" e "+t2+": " + output);
                    break;
                }
                case 4:{ //http://localhost:1337/admin/chilometri/{timestamp}
                    System.out.println("Inserire timestamps desiderati nel seguente formato separati da ',': yyyy-mm-dd hh:mm:ss");
                    System.out.println("Inserire primo timestamp: ");
                    Scanner in = new Scanner(System.in);
                    String t1 = in.nextLine();
                    while (!isTimeStampValid(t1)){
                        System.out.println("Timestamp inserito non valido. Inserirne uno nuovo (formato yyyy-mm-dd hh:mm:ss): ");
                        t1 = in.nextLine();
                    }
                    String t1replaced = t1.replace(" ", "&");
                    System.out.println("Inserire secondo timestamp: ");
                    String t2 = in.nextLine();
                    while (!isTimeStampValid(t2)){
                        System.out.println("Timestamp inserito non valido. Inserirne uno nuovo (formato yyyy-mm-dd hh:mm:ss): ");
                        t2 = in.nextLine();
                    }
                    String t2replaced = t1.replace(" ", "&");
                    WebResource webResource = client.resource("http://localhost:1337/admin/chilometri/"+t1replaced+","+t2replaced);
                    ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
                    String output = response.getEntity(String.class);
                    System.out.println("Chilometri percorsi tra "+t1+" e "+t2+": " + output);
                    break;
                }
                default:{ //Gestione numero errato
                    System.out.println("ERRORE! Il numero inserito non è valido o non corrisponde a nessun servizio.");
                    break;
                }

                
            }
        }


    }

    public static boolean isTimeStampValid(String inputString) {
        SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            format.parse(inputString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }


}
