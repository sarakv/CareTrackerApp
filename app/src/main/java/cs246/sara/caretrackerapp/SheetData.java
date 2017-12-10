package cs246.sara.caretrackerapp;

/**
 * Groups information necessary to send data to Google Sheets.
 * Facilitates using gson to store the data.
 */
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

    /**
     * Initializes the data to be sent
     * @param user the user name
     * @param timestamp the timestamp
     * @param label the entry name
     * @param description the entry description
     * @param saidToClient what was said to the client
     * @param clientSaid what the client said
     * @param imgLink the link to an associated image
     */
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

    /**
     * @return An array of the information to be sent
     */
    public String[] getValues() {
        return toArray(user, timestamp, label, description, saidToClient, clientSaid, imgLink);
    }

    /**
     * Internal helper function. Returns an array of strings from individual objects
     * @param vals the values to add to the array
     * @return the array with the strings
     */
    private String[] toArray(String... vals) {
        String[] values = vals;
        return values;
    }

    // getters and setters
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
