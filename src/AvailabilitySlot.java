import java.time.LocalDate;
import java.time.LocalTime;

// A dated time slot offered by a professional at a clinic location.
public class AvailabilitySlot {
    private static int nextId = 1;

    private String slotId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private SlotStatus status;
    private HealthcareProfessional professional;
    private ClinicLocation location;

    public AvailabilitySlot(LocalDate date, LocalTime startTime, LocalTime endTime,
                            HealthcareProfessional professional, ClinicLocation location) {
        this.slotId = "S" + nextId++;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = SlotStatus.AVAILABLE;
        this.professional = professional;
        this.location = location;
    }

    // Creates the Appointment and marks the slot BOOKED. Returns null if the slot is not available.
    public Appointment book(Patient patient, MedicalService service, Referral referral, AdministrativeStaff staff) {
        if (!isAvailable()) {
            return null;
        }
        Appointment appointment = new Appointment(patient, this, professional, service, referral, staff);
        markBooked();
        return appointment;
    }

    public boolean isAvailable() {
        return status == SlotStatus.AVAILABLE;
    }

    // True if the slot is available and matches every given criterion (null = not given).
    public boolean matches(HealthcareProfessional professional, Specialty specialty, ClinicLocation location,
                           LocalDate fromDate, LocalDate toDate) {
        return isAvailable()
                && (professional == null || professional == this.professional)
                && (specialty == null || this.professional.practicesIn(specialty))
                && (location == null || location == this.location)
                && (fromDate == null || !date.isBefore(fromDate))
                && (toDate == null || !date.isAfter(toDate));
    }

    public boolean overlaps(LocalDate date, LocalTime startTime, LocalTime endTime) {
        return this.date.equals(date) && startTime.isBefore(this.endTime) && this.startTime.isBefore(endTime);
    }

    private void markBooked() {
        status = SlotStatus.BOOKED;
    }

    public String getSlotId() {
        return slotId;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public HealthcareProfessional getProfessional() {
        return professional;
    }

    public String toString() {
        return slotId + "  " + date + " " + startTime + "-" + endTime + "  " + professional
                + "  " + location.getName() + "  " + status;
    }
}
