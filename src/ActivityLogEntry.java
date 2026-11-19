import java.time.LocalDateTime;

// One recorded activity, for accountability. Concerns a patient (0..1).
public class ActivityLogEntry {
    private LocalDateTime timestamp;
    private String action;
    private Patient patient;

    public ActivityLogEntry(String actionName, Patient patient) {
        this.timestamp = LocalDateTime.now();
        this.action = actionName;
        this.patient = patient;
    }

    public String toString() {
        return timestamp.withNano(0) + "  " + action
                + (patient == null ? "" : "  (patient " + patient.getPatientId() + ")");
    }
}