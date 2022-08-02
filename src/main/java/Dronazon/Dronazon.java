package Dronazon;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

/** Dronazon processo che simula un sito di e-commerce.
 * Genera un nuovo ordine ogni 5 secondi
 * Ordine:
 * - ID
 * - Punto di ritiro
 * - Punto di consegna
 * Ordini comunicati tramite protocollo MQTT su tcp://localhost:1883
 * con topic dronazon/smartcity/orders/
 */

public class Dronazon {
    public static void main(String[] args) {
        MqttClient publisher;
        String broker = "tcp://localhost:1883";
        String clientId = MqttClient.generateClientId();
        String topic = "dronazon/smartcity/orders";
        int qos = 2;

        try {
            publisher = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            //Connessione al broker
            System.out.println("Connessione Dronazon al broker " + broker);
            publisher.connect(connOpts);
            System.out.println("Dronazon connesso.");


            DronazonThread thread = new DronazonThread(publisher, topic);
            thread.start();

        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }


}
