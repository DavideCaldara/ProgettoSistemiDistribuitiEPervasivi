package Droni;

import java.util.Scanner;

/** Thread per lettura quit
 */

public class ThreadReadQuit extends Thread{
    @Override
    public void run() {
        Scanner scan = new Scanner(System.in);
        String input = scan.next();

        if(input.equals("quit")){
            synchronized (DroneMain.lockStartQuit){
                DroneMain.lockStartQuit.notify();
            }
        }
    }
}
