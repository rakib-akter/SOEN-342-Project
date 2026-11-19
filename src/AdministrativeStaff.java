public class AdministrativeStaff {
    private String employeeId;
    private String firstName;
    private String lastName;

    public AdministrativeStaff(String employeeId, String firstName, String lastName) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // The activity log is not part of the PoC, so these stay empty.
    public void logAction(String actionName) {
    }

    public void logAction(String actionName, Patient patient) {
    }

    public String toString() {
        return firstName + " " + lastName + " (" + employeeId + ")";
    }
}
