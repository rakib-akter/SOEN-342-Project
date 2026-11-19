import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// A healthcare professional created by the CSV import
public class HealthcareProfessional {
    private String employeeId;
    private String firstName;
    private String lastName;
    private String title;
    private List<Specialty> specialties = new ArrayList<>();
    private List<ClinicLocation> locations = new ArrayList<>();
    private List<AvailabilitySlot> slots = new ArrayList<>();

    public HealthcareProfessional(String employeeId, String firstName, String lastName, String title) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
    }

    public boolean practicesIn(Specialty specialty) {
        return specialties.contains(specialty);
    }

    public boolean practicesAt(ClinicLocation location) {
        return locations.contains(location);
    }

    public void addSpecialty(Specialty specialty) {
        if (!specialties.contains(specialty)) {
            specialties.add(specialty);
        }
    }

    public void addLocation(ClinicLocation location) {
        if (!locations.contains(location)) {
            locations.add(location);
        }
    }

    // Creates the slot unless it overlaps one of this professional's slots. Returns null on a clash.
    public AvailabilitySlot addSlot(ClinicLocation location, LocalDate date, LocalTime startTime, LocalTime endTime) {
        for (AvailabilitySlot slot : slots) {
            if (slot.overlaps(date, startTime, endTime)) {
                return null;
            }
        }
        AvailabilitySlot slot = new AvailabilitySlot(date, startTime, endTime, this, location);
        slots.add(slot);
        return slot;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getTitle() {
        return title;
    }

    public List<Specialty> getSpecialties() {
        return specialties;
    }

    public String toString() {
        return title + " " + firstName + " " + lastName + " (" + employeeId + ")";
    }
}
