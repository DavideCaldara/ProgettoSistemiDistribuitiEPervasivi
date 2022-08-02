package Dronazon;


import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/** Thread Dronazon
 * genera un nuovo ordine ogni 5 secondi e pubbliz il messaggio sul broker MQTT
 */

public class DronazonThread extends Thread{
    MqttClient publisher;
    String topic;
    int waitingtime = 5000;

    public DronazonThread(MqttClient publisher, String topic){
        this.publisher=publisher;
        this.topic=topic;
    }


    public void run() {
        while (true) {

            try {
                Thread.sleep(waitingtime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //genera nuovo ordine ogni 5 secondi
            Ordine payload = generateOrdine(); //creazione nuovo ordine
            MqttMessage message = new MqttMessage(payload.toString().getBytes()); //Stringa pubblicata: "ID,Xritiro,Yritiro,Xconsegna,Yconsegna"
            message.setQos(2);
            System.out.println("Dronazon sta pubblicando il messaggio: " + payload + " ...");
            try {
                publisher.publish(topic, message);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            System.out.println("Nuovo Messaggio Pubblicato.");

        }
    }

    private static Ordine generateOrdine() {
        int ID = (int)(Math.random()*1000);
        Coordinate pr = new Coordinate((int)(Math.random()*10),(int)(Math.random()*10));
        Coordinate pc = new Coordinate((int)(Math.random()*10),(int)(Math.random()*10));
        return new Ordine(ID, pr, pc);
    }


}
