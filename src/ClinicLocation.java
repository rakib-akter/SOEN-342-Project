// A clinic location. Locations are loaded when the system starts (use-case assumption 21).
public class ClinicLocation {
    private String locationId;
    private String name;
    private String address;

    public ClinicLocation(String locationId, String name, String address) {
        this.locationId = locationId;
        this.name = name;
        this.address = address;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
