package cs246.sara.caretrackerapp;

public class SheetData {
    public String user;
    public String timestamp;
    public String label;
    public String description;
    public String saidToClient;
    public String clientSaid;
    public String imgLink;

    public SheetData() {
        //intentionally empty for Gson compatibility
    }

    public SheetData(String user, String timestamp, String label, String description,
                     String saidToClient, String clientSaid, String imgLink) {
        this.user = user;
        this.timestamp = timestamp;
        this.label = label;
        this.description = description;
        this.saidToClient = saidToClient;
        this.clientSaid = clientSaid;
        this.imgLink = imgLink;
    }

    public String[] getValues() {
        return toArray(user, timestamp, label, description, saidToClient, clientSaid, imgLink);
    }

    private String[] toArray(String... vals) {
        String[] values = vals;
        return values;
    }
}
