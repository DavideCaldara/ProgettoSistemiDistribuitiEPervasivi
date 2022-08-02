package Droni;

/** comparatore classe Drone
 */

import java.util.Comparator;

public class DroneComparator implements Comparator<Drone> {
    @Override
    public int compare(Drone o1, Drone o2) {
        if (o1.ID<o2.ID)
            return -1;
        if (o1.ID>o2.ID)
            return 1;
        return 0;
    }
}
