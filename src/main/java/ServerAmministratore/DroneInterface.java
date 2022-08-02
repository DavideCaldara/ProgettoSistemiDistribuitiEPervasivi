package ServerAmministratore;

/**
 * Interfaccia gestione droni da Server Amministratore
 * Funzioni:
 * - aggiungere droni //COMPLETATO FUNZIONANTE
 * - rimuovere droni //COMPLETATO FUNZIONANTE
 * - ricevere le statistiche globali dal drone master e memorizzarle nella struttura dati //COMPLETATO
 */

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("drone")
public class DroneInterface {

    //metodo inserimento nuovo drone //TODO FUNZIONANTE CON ARC
    @Path("add")
    @POST
    @Consumes({"application/json"})
    @Produces("application/text")
    public Response addDrone(DroneRecord d){
        if(ListaDroni.getInstance().addDrone(d)){
            return Response.ok(generateCoord()).build();
        }else{
            return Response.serverError().build();
        }
    }

    //metodo rimozione drone //TODO FUNZIONANTE CON ARC
    @Path("delete/{ID}")
    @DELETE
    public Response removeDrone(@PathParam("ID") int ID){
        ListaDroni.getInstance().removeDrone(ID);
        return Response.ok().build();
    }

    //aggiunge statistiche al DB ricevute dal drone master
    @Path("addstat") //TODO Controllare se funziona
    @POST
    @Consumes("application/json")
    public Response addStat(StatRecord sr) {
        StatsDB.getInstance().addStat(sr);
        return Response.ok().build();
    }

    private String generateCoord() {
        int x = (int)(Math.random()*10);
        int y = (int)(Math.random()*10);
        return (x + "," + y);
    }

}
