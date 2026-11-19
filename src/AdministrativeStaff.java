import java.util.ArrayList;
import java.util.List;

public class AdministrativeStaff {
    private String employeeId;
    private String firstName;
    private String lastName;
    private List<ActivityLogEntry> activityLog = new ArrayList<>();

    public AdministrativeStaff(String employeeId, String firstName, String lastName) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Creator: the staff member records the activities it performs (Performs).
    public void logAction(String actionName) {
        logAction(actionName, null);
    }

    public void logAction(String actionName, Patient patient) {
        ActivityLogEntry entry = new ActivityLogEntry(actionName, patient);
        activityLog.add(entry);
    }

    public List<ActivityLogEntry> getActivityLog() {
        return activityLog;
    }

    public String toString() {
        return firstName + " " + lastName + " (" + employeeId + ")";
    }
}