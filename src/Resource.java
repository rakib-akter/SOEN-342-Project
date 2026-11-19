import java.util.ArrayList;
import java.util.List;

// A predefined resource a medical service may require: a room, equipment, or a role such as Nurse.
// Roles are not specific people, so a role is never in conflict (domain model assumption 5).
public class Resource {
    private String name;
    private ResourceType type;
    private List<Appointment> appointments = new ArrayList<>();

    public Resource(String name, ResourceType type) {
        this.name = name;
        this.type = type;
    }

    // Information Expert: the resource knows the appointments it is allocated to (Allocates).
    // A room or equipment is free when none of its non-cancelled appointments occupies an overlapping slot.
    public boolean isFreeDuring(AvailabilitySlot slot) {
        if (type == ResourceType.ROLE) {
            return true;
        }
        for (Appointment appointment : appointments) {
            AvailabilitySlot taken = appointment.getSlot();
            if (appointment.getStatus() != AppointmentStatus.CANCELLED
                    && taken.overlaps(slot.getDate(), slot.getStartTime(), slot.getEndTime())) {
                return false;
            }
        }
        return true;
    }

    public void allocate(Appointment appointment) {
        appointments.add(appointment);
    }

    public String getName() {
        return name;
    }

    public ResourceType getType() {
        return type;
    }

    public String toString() {
        return name + " (" + type + ")";
    }
}