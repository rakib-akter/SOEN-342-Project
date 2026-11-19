// An appointment. It takes its date, time and location from the slot it occupies.
public class Appointment {
    private static int nextId = 1;

    private String appointmentId;
    private AppointmentStatus status;
    private Patient patient;
    private AvailabilitySlot slot;
    private HealthcareProfessional professional;
    private MedicalService service;
    private Referral referral;
    private AdministrativeStaff staff;

    public Appointment(Patient patient, AvailabilitySlot slot, HealthcareProfessional professional,
                       MedicalService service, Referral referral, AdministrativeStaff staff) {
        this.appointmentId = "A" + nextId++;
        this.status = AppointmentStatus.BOOKED;
        this.patient = patient;
        this.slot = slot;
        this.professional = professional;
        this.service = service;
        this.referral = referral;
        this.staff = staff;
    }

    public String toString() {
        return appointmentId + " (" + status + ") " + service.getName() + " for "
                + patient.getFirstName() + " " + patient.getLastName() + " with " + professional
                + (referral == null ? "" : ", referral " + referral.getReferralId())
                + ", booked by " + staff + "\n  slot " + slot;
    }
}
