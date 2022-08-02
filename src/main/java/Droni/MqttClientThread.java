package Droni;

/** Thread client MQTT
 * si connette al broker MQTT, legge i messaggi in arrivo e mette gli ordini nella coda queueCosegne
 */

import Dronazon.Coordinate;
import Dronazon.Ordine;
import org.eclipse.paho.client.mqttv3.*;

import java.sql.Timestamp;

public class MqttClientThread extends Thread{

    @Override
    public void run() {
        MqttClient client;
        String broker = "tcp://localhost:1883";
        String clientId = MqttClient.generateClientId();
        String topic = "dronazon/smartcity/orders";
        int qos = 2;

        try{
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            //Connetto il client
            System.out.println(clientId + " Connessione al broker " + broker + "...");
            client.connect(connOpts);
            System.out.println(clientId + " Connessione Completata.");

            //Callback
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println(clientId + " Connectionlost! cause:" + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    //aggiungo messaggio alla lista locale di consegne da assegnare ai droni
                    String time = new Timestamp(System.currentTimeMillis()).toString();
                    String receivedMessage = new String(message.getPayload());
                    String[] msg = receivedMessage.split(",");
                    DroneMain.queueConsegne.put(new Ordine(Integer.parseInt(msg[0]), new Coordinate(Integer.parseInt(msg[1]), Integer.parseInt(msg[2])), new Coordinate(Integer.parseInt(msg[3]), Integer.parseInt(msg[4]))));
                    System.out.println("Nuova consegna ricevuta da dronazon e messa in coda.");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });

            System.out.println(clientId + " Sottoscrizione in corso ... ");
            client.subscribe(topic,qos);
            System.out.println(clientId + " Sottoscritto al topic: " + topic);

            //mi metto in wait, aspetto che qualcuno mi dia il via per disconnettermi
            synchronized (DroneMain.lockDisconnectMQTT){
                try {
                    DroneMain.lockDisconnectMQTT.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Disconnessione da Dronazon in corso...");
            client.disconnect();
            System.out.println("Drone disconnesso.");

        } catch (MqttException e) {
            e.printStackTrace();
        }


    }


}
