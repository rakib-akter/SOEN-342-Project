// A medical service offered by the clinic. Services are loaded when the system starts (use-case assumption 21).
public class MedicalService {
    private String serviceId;
    private String name;
    private boolean requiresReferral;

    public MedicalService(String serviceId, String name, boolean requiresReferral) {
        this.serviceId = serviceId;
        this.name = name;
        this.requiresReferral = requiresReferral;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getName() {
        return name;
    }

    public boolean requiresReferral() {
        return requiresReferral;
    }
}
