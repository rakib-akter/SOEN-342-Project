import java.util.ArrayList;
import java.util.List;

// A medical service offered by the clinic. Services are loaded when the system starts (use-case assumption 21).
public class MedicalService {
    private String serviceId;
    private String name;
    private boolean requiresReferral;
    private List<Resource> resources;

    public MedicalService(String serviceId, String name, boolean requiresReferral) {
        this(serviceId, name, requiresReferral, new ArrayList<>());
    }

    public MedicalService(String serviceId, String name, boolean requiresReferral, List<Resource> resources) {
        this.serviceId = serviceId;
        this.name = name;
        this.requiresReferral = requiresReferral;
        this.resources = resources;
    }

    // Information Expert: the service knows the resources it requires (Requires).
    public boolean resourcesFreeDuring(AvailabilitySlot slot) {
        for (Resource resource : resources) {
            if (!resource.isFreeDuring(slot)) {
                return false;
            }
        }
        return true;
    }

    public List<Resource> getResources() {
        return resources;
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