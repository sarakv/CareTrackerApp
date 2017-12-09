package cs246.sara.caretrackerapp;

public class SheetData {
    private String user;
    private String timestamp;
    private String label;
    private String description;
    private String saidToClient;
    private String clientSaid;
    private String imgLink;

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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSaidToClient() {
        return saidToClient;
    }

    public void setSaidToClient(String saidToClient) {
        this.saidToClient = saidToClient;
    }

    public String getClientSaid() {
        return clientSaid;
    }

    public void setClientSaid(String clientSaid) {
        this.clientSaid = clientSaid;
    }

    public String getImgLink() {
        return imgLink;
    }

    public void setImgLink(String imgLink) {
        this.imgLink = imgLink;
    }
}
