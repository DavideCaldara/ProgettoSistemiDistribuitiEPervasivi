package ServerAmministratore;

/** classe implementazione comparator tra DroniRecord
 *
 */

import java.util.Comparator;

public class DroneRecordComparator implements Comparator<DroneRecord> {
    @Override
    public int compare(DroneRecord o1, DroneRecord o2) {
        if (o1.getId()<o2.getId())
            return -1;
        if (o1.getId()>o2.getId())
            return 1;
        return 0;
    }
}
