import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Predicate;

// Facade controller: receives every system operation and keeps the data in memory.
public class AppointmentSystem {
    private List<Patient> patients;
    private List<MedicalService> services;
    private List<AvailabilitySlot> slots = new ArrayList<>();
    private List<HealthcareProfessional> professionals = new ArrayList<>();
    private List<Specialty> specialties = new ArrayList<>();
    private List<ClinicLocation> locations;
    private AdministrativeStaff staff;
    private Patient currentPatient;
    private MedicalService currentService;

    public AppointmentSystem(AdministrativeStaff staff, List<ClinicLocation> locations,
                             List<Patient> patients, List<MedicalService> services) {
        this.staff = staff;
        this.locations = locations;
        this.patients = patients;
        this.services = services;
    }

    // CO1
    public List<Patient> findPatient(String insuranceNumber, LocalDate dateOfBirth, String phoneNumber,
                                     String firstName, String lastName) {
        List<Patient> found = new ArrayList<>();
        for (Patient patient : patients) {
            if (patient.matches(insuranceNumber, dateOfBirth, phoneNumber, firstName, lastName)) {
                found.add(patient);
            }
        }
        return found;
    }

    // CO2. Returns the active referrals if the service requires one, or null if the patient or service is unknown.
    public List<Referral> selectMedicalService(String patientId, String serviceId) {
        currentPatient = find(patients, patient -> patient.getPatientId().equals(patientId));
        currentService = find(services, service -> service.getServiceId().equals(serviceId));
        if (currentPatient == null || currentService == null) {
            currentPatient = null;
            currentService = null;
            return null;
        }
        if (currentService.requiresReferral()) {
            return currentPatient.getActiveReferrals();
        }
        return new ArrayList<>();
    }

    public Appointment bookAppointment(String slotId, String referralId) {
        AvailabilitySlot slot = find(slots, s -> s.getSlotId().equals(slotId));
        if (currentPatient == null || currentService == null || slot == null) {
            return null;
        }
        Referral referral = referralId == null ? null : currentPatient.getReferral(referralId);
        if (currentService.requiresReferral() && (referral == null || !referral.isActive())) {
            return null;
        }
        Appointment appointment = slot.book(currentPatient, currentService, referral, staff);
        if (appointment != null) {
            currentPatient.addAppointment(appointment);
            staff.logAction("book appointment", currentPatient);
        }
        return appointment;
    }


    public List<AvailabilitySlot> searchAvailability(String professionalId, String specialtyName, String locationId,
                                                     LocalDate fromDate, LocalDate toDate) {
        HealthcareProfessional professional = find(professionals, p -> p.getEmployeeId().equals(professionalId));
        Specialty specialty = find(specialties, s -> s.getName().equalsIgnoreCase(specialtyName));
        ClinicLocation location = find(locations, l -> l.getLocationId().equals(locationId));
        if ((professionalId != null && professional == null) || (specialtyName != null && specialty == null)
                || (locationId != null && location == null)
                || (fromDate != null && toDate != null && fromDate.isAfter(toDate))) {
            return null;
        }
        List<AvailabilitySlot> found = new ArrayList<>();
        for (AvailabilitySlot slot : slots) {
            if (slot.matches(professional, specialty, location, fromDate, toDate)) {
                found.add(slot);
            }
        }
        found.sort(Comparator.comparing(AvailabilitySlot::getDate).thenComparing(AvailabilitySlot::getStartTime));
        return found;
    }


    public String makeAvailability(String professionalId, String locationId, LocalDate fromDate, LocalDate toDate,
                                   Set<DayOfWeek> daysOfWeek, Integer dayOfMonth, LocalTime startTime,
                                   LocalTime endTime, Duration slotLength) {
        HealthcareProfessional professional = find(professionals, p -> p.getEmployeeId().equals(professionalId));
        ClinicLocation location = find(locations, l -> l.getLocationId().equals(locationId));
        if (professional == null || location == null) {
            return "Unknown professional or location.";
        }
        if (!professional.practicesAt(location)) {
            return "The professional does not practise at this location.";
        }
        if (fromDate.isAfter(toDate) || !startTime.isBefore(endTime) || slotLength.isZero() || slotLength.isNegative()) {
            return "Invalid availability parameters.";
        }
        int created = 0;
        int skipped = 0;
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            if (!matchesPattern(date, daysOfWeek, dayOfMonth)) {
                continue;
            }
            // Stop when the next slot would end after endTime (or wrap past midnight).
            for (LocalTime start = startTime; !start.plus(slotLength).isAfter(endTime)
                    && start.plus(slotLength).isAfter(start); start = start.plus(slotLength)) {
                AvailabilitySlot slot = professional.addSlot(location, date, start, start.plus(slotLength));
                if (slot == null) {
                    skipped++;
                } else {
                    slots.add(slot);
                    created++;
                }
            }
        }
        staff.logAction("make availability");
        return "Created " + created + " slots, skipped " + skipped + " (clash with an existing slot).";
    }

    public String importAvailability(String filePath, String locationId) {
        ClinicLocation location = find(locations, l -> l.getLocationId().equals(locationId));
        if (location == null) {
            return "Unknown location.";
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "Could not read " + filePath + ". Nothing was imported.";
        }
        int imported = 0;
        int skipped = 0;
        for (String line : lines.subList(Math.min(1, lines.size()), lines.size())) {
            if (line.isBlank()) {
                continue;
            }
            String[] row = line.split(",", -1);
            LocalDate date;
            LocalTime start;
            LocalTime end;
            try {
                date = LocalDate.parse(row[4].trim());
                start = LocalTime.parse(row[5].trim());
                end = LocalTime.parse(row[6].trim());
            } catch (DateTimeParseException | ArrayIndexOutOfBoundsException e) {
                skipped++;
                continue;
            }
            String firstName = row[0].trim();
            String lastName = row[1].trim();
            HealthcareProfessional professional = find(professionals,
                    p -> p.getFirstName().equalsIgnoreCase(firstName) && p.getLastName().equalsIgnoreCase(lastName));
            if (professional == null) {
                professional = new HealthcareProfessional("P" + (professionals.size() + 1), firstName, lastName, row[2].trim());
                professionals.add(professional);
            }
            for (String name : row[3].split(";")) {
                String specialtyName = name.trim();
                Specialty specialty = find(specialties, s -> s.getName().equalsIgnoreCase(specialtyName));
                if (specialty == null) {
                    specialty = new Specialty(specialtyName);
                    specialties.add(specialty);
                }
                professional.addSpecialty(specialty);
            }
            professional.addLocation(location);
            AvailabilitySlot slot = professional.addSlot(location, date, start, end);
            if (slot == null) {
                skipped++;
            } else {
                slots.add(slot);
                imported++;
            }
        }
        staff.logAction("import availability");
        return "Imported " + imported + " slots, skipped " + skipped + " rows (malformed or duplicate).";
    }


    // Returns the number of slots written, 0 if there is nothing to export, or -1 if the file cannot be written.
    public int exportAvailability(String filePath) {
        if (slots.isEmpty()) {
            return 0;
        }
        List<String> lines = new ArrayList<>();
        lines.add("First Name,Last Name,Title,Specialty,Date,Start,End");
        for (AvailabilitySlot slot : slots) {
            HealthcareProfessional professional = slot.getProfessional();
            List<String> specialtyNames = new ArrayList<>();
            for (Specialty specialty : professional.getSpecialties()) {
                specialtyNames.add(specialty.getName());
            }
            lines.add(String.join(",", professional.getFirstName(), professional.getLastName(), professional.getTitle(),
                    String.join(";", specialtyNames), slot.getDate().toString(),
                    slot.getStartTime().toString(), slot.getEndTime().toString()));
        }
        try {
            Files.write(Path.of(filePath), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return -1;
        }
        return slots.size();
    }

    public MedicalService getCurrentService() {
        return currentService;
    }

    // Weekly (days of the week) or monthly (day of the month) repeat pattern.
    private boolean matchesPattern(LocalDate date, Set<DayOfWeek> daysOfWeek, Integer dayOfMonth) {
        return (daysOfWeek != null && daysOfWeek.contains(date.getDayOfWeek()))
                || (dayOfMonth != null && date.getDayOfMonth() == dayOfMonth);
    }

    private static <T> T find(List<T> list, Predicate<T> test) {
        for (T item : list) {
            if (test.test(item)) {
                return item;
            }
        }
        return null;
    }
}