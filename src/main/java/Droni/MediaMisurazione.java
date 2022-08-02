package Droni;

/** ELemento lista misurazioni raccolte dal sensore
 */

public class MediaMisurazione {
    private double media;
    private long timestamp;

    public MediaMisurazione(double media, long timestamp) {
        this.media = media;
        this.timestamp = timestamp;
    }

    public double getMedia() {
        return media;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
