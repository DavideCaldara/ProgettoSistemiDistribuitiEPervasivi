package ServerAmministratore;

/** Interfaccia amministratore per gestire le statistiche
 * Funzioni (richieste da client amministratore):
 * - Restituire lista droni //COMPLETATO FUNZIONANTE
 * - Restituire ultime n statistiche (con timestamp) //COMPLETATO
 * - Media del numero di consegne effettuate dai droni tra due timestamp t1 e t2 V //COMPLETATO
 * - Media dei chilometri percorsi dai droni tra due timestamp t1 e t2 //COMPLETATO
 * */

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;

@Path("admin")
public class AdministratorInterface {


    //restituisce la lista droni
    @Path("lista")
    @GET
    @Produces({"application/json"})
    public Response getListaDroni(){
        return Response.ok(ListaDroni.getInstance().getListaDroni()).build();
    }

    //metodo tutte le statistiche create
    @Path("get")
    @GET
    @Produces("application/json")
    public Response getAllStats(){
        return Response.ok(StatsDB.getInstance().getStatList()).build();
    }


    //metodo ultime n statistiche globali (con timestamp)
    @Path("get/{n}")
    @GET
    @Produces("application/json")
    public Response getLastNStats(@PathParam("n") int n){
        return Response.ok(StatsDB.getInstance().getNStats(n)).build();
    }


    //metodo media numero di consegne tra t1 e t2
    @Path("consegne/{timestamps}")
    @GET
    @Produces("application/json")
    public Response getConsegne(@PathParam("timestamps") String ts){
        String[] timestamps = ts.split(",");
        Timestamp t1 = Timestamp.valueOf(timestamps[0].replace("&"," "));
        Timestamp t2 = Timestamp.valueOf(timestamps[1].replace("&"," "));
        return Response.ok(StatsDB.getConsegne(t1,t2)).build();
    }


    //metodo media chilometri tra t1 e t2
    @Path("chilometri/{timestamps}")
    @GET
    @Produces("application/json")
    public Response getChilometri(@PathParam("timestamps") String ts){
        String[] timestamps = ts.split(",");
        Timestamp t1 = Timestamp.valueOf(timestamps[0].replace("&"," "));
        Timestamp t2 = Timestamp.valueOf(timestamps[1].replace("&"," "));
        return Response.ok(StatsDB.getChilometri(t1,t2)).build();
    }


}
