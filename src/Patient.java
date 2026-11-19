import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Patients are loaded when the system starts.
public class Patient {
    private String patientId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private List<String> insuranceNumbers;
    private List<Referral> referrals;
    private List<Appointment> appointments = new ArrayList<>();

    public Patient(String patientId, String firstName, String lastName, LocalDate dateOfBirth, String phoneNumber,
                   List<String> insuranceNumbers, List<Referral> referrals) {
        this.patientId = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.insuranceNumbers = insuranceNumbers;
        this.referrals = referrals;
    }

    // True if every given field matches (null = not given). Names can be partial.
    public boolean matches(String insuranceNumber, LocalDate dateOfBirth, String phoneNumber,
                           String firstName, String lastName) {
        return (insuranceNumber == null || insuranceNumbers.contains(insuranceNumber))
                && (dateOfBirth == null || dateOfBirth.equals(this.dateOfBirth))
                && (phoneNumber == null || phoneNumber.equals(this.phoneNumber))
                && (firstName == null || this.firstName.toLowerCase().contains(firstName.toLowerCase()))
                && (lastName == null || this.lastName.toLowerCase().contains(lastName.toLowerCase()));
    }

    public List<Referral> getActiveReferrals() {
        List<Referral> active = new ArrayList<>();
        for (Referral referral : referrals) {
            if (referral.isActive()) {
                active.add(referral);
            }
        }
        return active;
    }

    public Referral getReferral(String referralId) {
        for (Referral referral : referrals) {
            if (referral.getReferralId().equals(referralId)) {
                return referral;
            }
        }
        return null;
    }

    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
    }

    public String getPatientId() {
        return patientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String toString() {
        return patientId + "  " + firstName + " " + lastName + "  born " + dateOfBirth + "  " + phoneNumber;
    }
}
