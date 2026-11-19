import java.time.*;
import java.util.*;

public class Main {
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        List<ClinicLocation> locations = List.of(
                new ClinicLocation("L1", "Downtown Clinic", "1000 Sainte-Catherine St W, Montreal"),
                new ClinicLocation("L2", "Laval Clinic", "500 Saint-Martin Blvd, Laval"));
        List<Patient> patients = List.of(
                new Patient("PT1", "Émilie", "Lavoie", LocalDate.of(1999, 1, 1), "514-555-0101",
                        List.of("LAVE99010101"), List.of()),
                new Patient("PT2", "Olivier", "Côté", LocalDate.of(1985, 6, 15), "514-555-0102",
                        List.of("COTO85061502"), List.of(new Referral("R1"))),
                new Patient("PT3", "Fatima", "Benali", LocalDate.of(1990, 3, 22), "438-555-0103",
                        List.of(), List.of()),
                new Patient("PT4", "Thomas", "Girard", LocalDate.of(1992, 8, 24), "514-555-0104",
                        List.of("GIRT92082403"), List.of()));
        List<MedicalService> services = List.of(
                new MedicalService("MS1", "General consultation", false),
                new MedicalService("MS2", "Cardiology consultation", true),
                new MedicalService("MS3", "Dermatology consultation", true));
        AdministrativeStaff staff = new AdministrativeStaff("E1", "Julie", "Martin");
        AppointmentSystem system = new AppointmentSystem(staff, locations, patients, services);

        System.out.println("Clinic Appointment System (PoC). Logged in as " + staff + ".");
        System.out.println("Clinic locations: L1 Downtown Clinic, L2 Laval Clinic");
        while (true) {
            System.out.println();
            System.out.println("1. Import availability from CSV");
            System.out.println("2. Make availability");
            System.out.println("3. Search availability");
            System.out.println("4. Book appointment");
            System.out.println("5. Export availability to CSV");
            System.out.println("0. Exit");
            String choice = ask("Choice");
            try {
                switch (choice == null ? "" : choice) {
                    case "1" -> System.out.println(system.importAvailability(ask("CSV file"), ask("Location id")));
                    case "2" -> System.out.println(system.makeAvailability(ask("Professional id"), ask("Location id"),
                            date(ask("From date (yyyy-mm-dd)")), date(ask("To date (yyyy-mm-dd)")),
                            days(ask("Days of week, e.g. MONDAY,WEDNESDAY (blank if monthly)")),
                            number(ask("Day of month (blank if weekly)")),
                            time(ask("Start time (hh:mm)")), time(ask("End time (hh:mm)")),
                            Duration.ofMinutes(Long.parseLong(ask("Slot length in minutes")))));
                    case "3" -> search(system);
                    case "4" -> book(system, services);
                    case "5" -> {
                        int written = system.exportAvailability(ask("Output file"));
                        System.out.println(written < 0 ? "Could not write the file." : "Exported " + written + " slots.");
                    }
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Unknown choice.");
                }
            } catch (RuntimeException e) {
                System.out.println("Invalid input, please try again.");
            }
        }
    }

    // Book Appointment: findPatient, selectMedicalService, searchAvailability (included), bookAppointment.
    private static void book(AppointmentSystem system, List<MedicalService> services) {
        System.out.println("Find the patient (leave a field blank to skip it):");
        List<Patient> found = system.findPatient(ask("Insurance number"), date(ask("Date of birth (yyyy-mm-dd)")),
                ask("Phone number"), ask("First name"), ask("Last name"));
        if (found.isEmpty()) {
            System.out.println("No patient found.");
            return;
        }
        found.forEach(System.out::println);

        for (MedicalService service : services) {
            System.out.println(service.getServiceId() + "  " + service.getName()
                    + (service.requiresReferral() ? " (referral required)" : ""));
        }
        List<Referral> referrals = system.selectMedicalService(ask("Patient id"), ask("Service id"));
        if (referrals == null) {
            System.out.println("Unknown patient or service.");
            return;
        }
        if (system.getCurrentService().requiresReferral()) {
            if (referrals.isEmpty()) {
                System.out.println("A referral is required and the patient has no active referral.");
                return;
            }
            referrals.forEach(referral -> System.out.println("Active referral: " + referral.getReferralId()));
        }

        search(system);
        Appointment appointment = system.bookAppointment(ask("Slot id"), ask("Referral id (blank if none)"));
        System.out.println(appointment == null
                ? "Booking failed: the slot is not available or the referral is not valid."
                : "Booked: " + appointment);
    }

    private static void search(AppointmentSystem system) {
        System.out.println("Search availability (leave a field blank to skip it):");
        List<AvailabilitySlot> found = system.searchAvailability(ask("Professional id"), ask("Specialty"),
                ask("Location id"), date(ask("From date (yyyy-mm-dd)")), date(ask("To date (yyyy-mm-dd)")));
        if (found == null) {
            System.out.println("Invalid search criteria.");
        } else if (found.isEmpty()) {
            System.out.println("No available slots.");
        } else {
            found.forEach(System.out::println);
        }
    }

    // Returns the trimmed input, or null if it is blank.
    private static String ask(String prompt) {
        System.out.print(prompt + ": ");
        if (!in.hasNextLine()) {
            System.exit(0);
        }
        String line = in.nextLine().trim();
        return line.isEmpty() ? null : line;
    }

    private static LocalDate date(String text) {
        return text == null ? null : LocalDate.parse(text);
    }

    private static LocalTime time(String text) {
        return text == null ? null : LocalTime.parse(text);
    }

    private static Integer number(String text) {
        return text == null ? null : Integer.valueOf(text);
    }

    private static Set<DayOfWeek> days(String text) {
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        if (text != null) {
            for (String day : text.split(",")) {
                days.add(DayOfWeek.valueOf(day.trim().toUpperCase()));
            }
        }
        return days;
    }
}
