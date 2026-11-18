# System Operations and Operation Contracts

Iteration 2 — Appointment Tracking System

The system operations below are taken from the system sequence diagrams (SSD1–SSD9). Each one is an event that crosses the system boundary from an actor to the System, treated as a black box. Together they form the public interface of the System.

All class, attribute and association names in the contracts are taken from the Iteration 2 UML domain model (`domain-model-iter2.puml`).

**Changes since Iteration 1:**
- SSD1 Book Appointment now includes Search Availability and books a dated AvailabilitySlot. `requestAvailableSlots` is replaced by `selectMedicalService` (CO2) and `searchAvailability` (CO13), and CO3 `bookAppointment` now takes a slot.
- CO4 now reads the appointment's date from its slot, since Appointment no longer has a date.
- New SSDs and contracts: Search Availability, Make Availability, Import Availability from CSV, Export Availability to CSV, and Join Waitlist (an extension of Book Appointment).
- New SSDs without contracts yet: Cancel Appointment with Offer Appointment to a Waitlisted Patient (SSD8), and Manage Waitlist (SSD9).

## 1. System Operations

```
                         System
----------------------------------------------------------------
findPatient(criteria)
selectMedicalService(patientId, serviceId)
bookAppointment(slotId, referralId)
startConsultation(appointmentId)
recordObservations(observations)
recordDiagnosis(diagnoses, treatmentRecommendations)
startPrescription()
addMedication(medicationId, dosage, frequency)
recordFollowUp(followUpRequirements)
endConsultation()
selectReferralTarget(patientId, specialtyName, professionalId)
submitReferral(clinicalReason, priority)
searchAvailability(professionalId, specialtyName, locationId, fromDate, toDate)
makeAvailability(professionalId, locationId, fromDate, toDate, repeatPattern, startTime, endTime, slotLength)
importAvailability(filePath, locationId)
exportAvailability(filePath)
joinWaitlist(professionalId, specialtyName, fromDate, toDate, priority)
cancelAppointment(appointmentId)
offerAppointment(entryId)
acceptOffer(entryId)
changePriority(entryId, priority)
removeFromWaitlist(entryId)
```

| Contract | Operation | Use case(s) | SSD | Type |
|---|---|---|---|---|
| CO1 | findPatient | Book Appointment, Issue Referral | SSD1, SSD3 | Query |
| CO2 | selectMedicalService | Book Appointment | SSD1 | Query |
| CO3 | bookAppointment | Book Appointment (+ Allocate Resources) | SSD1 | State change |
| CO4 | startConsultation | Conduct Consultation | SSD2 | State change |
| CO5 | recordObservations | Conduct Consultation | SSD2 | State change |
| CO6 | recordDiagnosis | Conduct Consultation | SSD2 | State change |
| CO7 | startPrescription | Issue Prescription (extends Conduct Consultation) | SSD2 | State change |
| CO8 | addMedication | Issue Prescription (extends Conduct Consultation) | SSD2 | State change |
| CO9 | recordFollowUp | Conduct Consultation | SSD2 | State change |
| CO10 | endConsultation | Conduct Consultation | SSD2 | State change |
| CO11 | selectReferralTarget | Issue Referral | SSD3 | Query |
| CO12 | submitReferral | Issue Referral | SSD3 | State change |
| CO13 | searchAvailability | Search Availability (included by Book Appointment) | SSD4, SSD1 | Query |
| CO14 | makeAvailability | Make Availability | SSD5 | State change |
| CO15 | importAvailability | Import Availability from CSV | SSD6 | State change |
| CO16 | exportAvailability | Export Availability to CSV | SSD7 | Query |
| CO17 | joinWaitlist | Join Waitlist (extends Book Appointment) | SSD1 | State change |
| — | cancelAppointment | Cancel Appointment | SSD8 | State change |
| — | offerAppointment | Offer Appointment to a Waitlisted Patient (extends Cancel Appointment) | SSD8 | State change |
| — | acceptOffer | Offer Appointment to a Waitlisted Patient (extends Cancel Appointment) | SSD8 | State change |
| — | changePriority | Manage Waitlist | SSD9 | State change |
| — | removeFromWaitlist | Manage Waitlist | SSD9 | State change |

`findPatient` and `searchAvailability` each appear in two SSDs, so each is listed and contracted once.

## 2. Operation Contracts

Preconditions are the obligations of the client (the caller). Postconditions are the obligations of the supplier (the System). They describe the state of the domain objects after the operation completes, using three kinds of change: instance creation/deletion, attribute modification, and association formed/broken.

---

### CO1: findPatient

**Operation:** findPatient(criteria: SearchCriteria)

**Cross References:** UC1 Book Appointment (steps 2–3, extensions 3a, 3b); UC3 Issue Referral (steps 1–2, extensions 2a, 2b)

**Preconditions:**
- The user is identified and authenticated as AdministrativeStaff, HealthcareProfessional, or (in UC1) a Patient booking for themselves.
- `criteria` contains at least one of: insurance number, date of birth, phone number, partial first name and/or last name.

**Postconditions:**
- None. This is a query operation. It returns the Patient instances matching `criteria` (by Insurance.insuranceNumber, Patient.dateOfBirth, Patient.phoneNumber, or partial Patient.firstName/lastName). In UC3 it also returns the patient's Referral instances. No objects are created, modified, or associated.

---

### CO2: selectMedicalService

**Operation:** selectMedicalService(patientId: String, serviceId: String)

**Cross References:** UC1 Book Appointment (steps 4–5, extensions 5a, 5b)

**Preconditions:**
- The user (AdministrativeStaff, or a Patient booking for themselves) is identified and authenticated.
- A Patient with `patientId` exists.
- A MedicalService with `serviceId` exists.

**Postconditions:**
- None. This is a query operation. If MedicalService.requiresReferral is true, the System returns the patient's active Referral instances (Refers). If there are none, it reports that a referral is required (extension 5a).
- The System retains the patient and the medical service as the current booking request, for use by joinWaitlist and bookAppointment.

---

### CO3: bookAppointment

**Operation:** bookAppointment(slotId: String, referralId: String [0..1])

**Cross References:** UC1 Book Appointment (steps 6–7, extensions 5b, 7a); Allocate Resources (extends Book Appointment)

**Preconditions:**
- A booking request is underway: selectMedicalService succeeded for a patient and a medical service.
- An AvailabilitySlot `s` with `slotId` exists, and `s.status` is AVAILABLE.
- If the MedicalService requires a referral, `referralId` identifies one of the patient's active Referrals.
- Every Resource of type room or equipment that the MedicalService requires (Requires) is not allocated to another non-cancelled Appointment whose slot overlaps `s`.

**Postconditions:**
- An Appointment instance `a` was created. *(instance creation)*
- `a.status` was set to BOOKED if the booking user is AdministrativeStaff, or to CONFIRMED if the booking user is the Patient. *(attribute modification)*
- `a` was associated with `s`. *(association formed — Occupies)*
- `s.status` was set to BOOKED. *(attribute modification)*
- `a` was associated with the Patient. *(association formed — Attends)*
- `a` was associated with the HealthcareProfessional who offers `s`. *(association formed — Conducts)*
- `a` was associated with the MedicalService. *(association formed — Is-for)*
- `a` was associated with the booking user, either the Patient or the ClinicStaff member. *(association formed — Requests)*
- If `referralId` was given, `a` was associated with the Referral, based on `referralId` match. *(association formed — Results-in)*
- For each Resource `r` that the MedicalService requires, `a` was associated with `r`. *(association formed — Allocates; this is the Allocate Resources extension)*
- An ActivityLogEntry instance `e` was created. *(instance creation)*
- `e.timestamp` was set to the current date and time, and `e.action` was set to "book appointment". *(attribute modification)*
- If the booking user is AdministrativeStaff, `e` was associated with that staff member. *(association formed — Performs)*
- `e` was associated with the Patient. *(association formed — Concerns)*

**Note:** The appointment's date, time and clinic location are those of `s` (Occupies, Held-at), so they are not set on the Appointment itself.

---

### CO4: startConsultation

**Operation:** startConsultation(appointmentId: String)

**Cross References:** UC2 Conduct Consultation (steps 1–2, extensions 1a, 1b, 1c)

**Preconditions:**
- The HealthcareProfessional is identified and authenticated with clinical access.
- An Appointment `a` with `appointmentId` exists, and the AvailabilitySlot that `a` occupies has today's date (Occupies).
- `a.status` is ARRIVED.
- `a` is associated with this HealthcareProfessional (Conducts).

**Postconditions:**
- A Consultation instance `c` was created. *(instance creation)*
- `c` was associated with `a`. *(association formed — Produces)*
- An ActivityLogEntry instance `e` was created, recording access to the clinical record. *(instance creation)*
- `e.timestamp` was set to the current date and time, and `e.action` was set to "access clinical record". *(attribute modification)*
- `e` was associated with the HealthcareProfessional *(association formed — Performs)* and with the Patient who attends `a` *(association formed — Concerns)*.

**Failure note (1b):** If `a` is conducted by another professional, no Consultation is created. Only the ActivityLogEntry is created, with `e.action` set to "denied access attempt".

---

### CO5: recordObservations

**Operation:** recordObservations(observations: String)

**Cross References:** UC2 Conduct Consultation (step 3, extension 6a)

**Preconditions:**
- A Consultation `c` is underway (startConsultation succeeded and endConsultation has not yet been called).

**Postconditions:**
- `c.observations` was set to `observations`. *(attribute modification)*

---

### CO6: recordDiagnosis

**Operation:** recordDiagnosis(diagnoses: String, treatmentRecommendations: String)

**Cross References:** UC2 Conduct Consultation (step 4, extension 6a)

**Preconditions:**
- A Consultation `c` is underway.

**Postconditions:**
- `c.diagnoses` was set to `diagnoses`. *(attribute modification)*
- `c.treatmentRecommendations` was set to `treatmentRecommendations`. *(attribute modification)*

---

### CO7: startPrescription

**Operation:** startPrescription()

**Cross References:** Issue Prescription (extends UC2 Conduct Consultation at extension point "Prescription", extension 4a)

**Preconditions:**
- A Consultation `c` is underway.

**Postconditions:**
- A Prescription instance `p` was created. *(instance creation)*
- `p.dateIssued` was set to today's date. *(attribute modification)*
- `p` was associated with `c`. *(association formed — Issues)*

---

### CO8: addMedication

**Operation:** addMedication(medicationId: String, dosage: String, frequency: String)

**Cross References:** Issue Prescription (extends UC2 Conduct Consultation, extension 4a)

**Preconditions:**
- A Prescription `p` is underway (startPrescription succeeded).
- A Medication with `medicationId` exists.

**Postconditions:**
- A PrescriptionLineItem instance `pli` was created. *(instance creation)*
- `pli.dosage` was set to `dosage`, and `pli.frequency` was set to `frequency`. *(attribute modification)*
- `pli` was associated with `p`. *(association formed — Contains)*
- `pli` was associated with a Medication, based on `medicationId` match. *(association formed — Prescribes)*

---

### CO9: recordFollowUp

**Operation:** recordFollowUp(followUpRequirements: String)

**Cross References:** UC2 Conduct Consultation (step 5; not called when extension 5a skips the follow-up)

**Preconditions:**
- A Consultation `c` is underway.

**Postconditions:**
- `c.followUpRequirements` was set to `followUpRequirements`. *(attribute modification)*

---

### CO10: endConsultation

**Operation:** endConsultation()

**Cross References:** UC2 Conduct Consultation (steps 6–7, extensions 6a, *a)

**Preconditions:**
- A Consultation `c` is underway for Appointment `a`.
- `c.observations` and `c.diagnoses` have been recorded.

**Postconditions:**
- `c` was associated with the ClinicalRecord of the Patient who attends `a`. *(association formed — Contains)*
- `a.status` was set to COMPLETED. *(attribute modification)*
- An ActivityLogEntry instance `e` was created. *(instance creation)*
- `e.timestamp` was set to the current date and time, and `e.action` was set to "record consultation". *(attribute modification)*
- `e` was associated with the HealthcareProfessional *(association formed — Performs)* and with the Patient *(association formed — Concerns)*.

**Failure note (*a):** If the session ends before endConsultation, `c` is never associated with the ClinicalRecord and is discarded (instance deletion), and `a.status` remains ARRIVED.

---

### CO11: selectReferralTarget

**Operation:** selectReferralTarget(patientId: String, specialtyName: String [0..1], professionalId: String [0..1])

**Cross References:** UC3 Issue Referral (steps 3–4, extension 4a)

**Preconditions:**
- The HealthcareProfessional is identified and authenticated.
- A Patient with `patientId` exists.
- Exactly one of `specialtyName` or `professionalId` is given.

**Postconditions:**
- None. This is a query operation. The System verifies that the Specialty is practised by at least one HealthcareProfessional at the clinic (Practices-in), or that the HealthcareProfessional exists.
- The System retains the patient and the target as the current referral request, for use by submitReferral.

---

### CO12: submitReferral

**Operation:** submitReferral(clinicalReason: String, priority: Priority)

**Cross References:** UC3 Issue Referral (steps 5–7, extension 6a)

**Preconditions:**
- A referral request is underway: selectReferralTarget succeeded for a patient and a target.
- `clinicalReason` is not empty, and `priority` is a valid Priority value.

**Postconditions:**
- A Referral instance `r` was created. *(instance creation)*
- `r.clinicalReason` was set to `clinicalReason`, `r.priority` was set to `priority`, and `r.status` was set to PENDING. *(attribute modification)*
- `r` was associated with the Patient. *(association formed — Refers)*
- `r` was associated with the referring HealthcareProfessional. *(association formed — Issues)*
- If a specialty was selected, `r` was associated with the Specialty, based on `specialtyName` match. Otherwise `r` was associated with the requested HealthcareProfessional, based on `professionalId` match. *(association formed — Requests)*
- An ActivityLogEntry instance `e` was created. *(instance creation)*
- `e.timestamp` was set to the current date and time, and `e.action` was set to "issue referral". *(attribute modification)*
- `e` was associated with the HealthcareProfessional *(association formed — Performs)* and with the Patient *(association formed — Concerns)*.

---

### CO13: searchAvailability

**Operation:** searchAvailability(professionalId: String [0..1], specialtyName: String [0..1], locationId: String [0..1], fromDate: Date [0..1], toDate: Date [0..1])

**Cross References:** Search Availability (SSD4); UC1 Book Appointment, which includes it (step 5, extension 5c)

**Preconditions:**
- The user (AdministrativeStaff, or a Patient booking for themselves) is identified and authenticated.
- Any HealthcareProfessional, Specialty or ClinicLocation given exists.
- If both dates are given, `fromDate` is not after `toDate`.

**Postconditions:**
- None. This is a query operation. The System returns the AvailabilitySlot instances whose status is AVAILABLE and that match every criterion given:
  - offered by the HealthcareProfessional with `professionalId` (Offers);
  - offered by a HealthcareProfessional who practises the Specialty named `specialtyName` (Offers, Practices-in);
  - held at the ClinicLocation with `locationId` (Held-at);
  - with a date between `fromDate` and `toDate`.
- The slots are returned in order of date and start time. No objects are created, modified, or associated.

---

### CO14: makeAvailability

**Operation:** makeAvailability(professionalId: String, locationId: String, fromDate: Date, toDate: Date, repeatPattern: RepeatPattern, startTime: Time, endTime: Time, slotLength: Duration)

**Cross References:** Make Availability (SSD5)

**Preconditions:**
- The user is identified and authenticated as AdministrativeStaff, or as the HealthcareProfessional with `professionalId` making their own availability.
- A HealthcareProfessional `h` with `professionalId` and a ClinicLocation `l` with `locationId` exist, and `h` practises at `l` (Practices-at).
- `fromDate` is not after `toDate`, `startTime` is before `endTime`, and `slotLength` is greater than zero and no longer than the time between `startTime` and `endTime`.

**Postconditions:**
For each date `d` from `fromDate` to `toDate` that matches `repeatPattern`, and each consecutive interval of length `slotLength` from `startTime` that ends no later than `endTime` and does not overlap an existing AvailabilitySlot of `h` on `d`:
- An AvailabilitySlot instance `s` was created. *(instance creation)*
- `s.date` was set to `d`, `s.startTime` and `s.endTime` were set to the interval's start and end, and `s.status` was set to AVAILABLE. *(attribute modification)*
- `s` was associated with `h`. *(association formed — Offers)*
- `s` was associated with `l`. *(association formed — Held-at)*

Then, once for the whole operation:
- An ActivityLogEntry instance `e` was created. *(instance creation)*
- `e.timestamp` was set to the current date and time, and `e.action` was set to "make availability". *(attribute modification)*
- `e` was associated with the ClinicStaff member who made the availability. *(association formed — Performs)*

**Notes:**
- `repeatPattern` gives either day(s) of the week or a day of the month. It is only used to generate the slots and is not stored (domain model assumption 1).
- Example: Dr. X, every Monday from 2026-11-01 to 2026-11-30, 09:00–12:00, 30-minute slots, creates 6 slots on each of the 5 Mondays, so 30 AvailabilitySlots.

---

### CO15: importAvailability

**Operation:** importAvailability(filePath: String, locationId: String)

**Cross References:** Import Availability from CSV (SSD6)

**Preconditions:**
- The AdministrativeStaff member is identified and authenticated.
- A ClinicLocation `l` with `locationId` exists.
- The file at `filePath` can be read and has a header row with the columns First Name, Last Name, Title, Specialty, Date, Start, End.

**Postconditions:**
For each well-formed row of the file that does not duplicate an existing AvailabilitySlot (same professional, date and start time):
- If no HealthcareProfessional matches the row's first and last name, a HealthcareProfessional `h` was created, and `h.firstName`, `h.lastName` and `h.title` were set from the row. *(instance creation, attribute modification)*
- For each specialty name in the row's Specialty field (names separated by ";", as in CO16), if no Specialty has that name, a Specialty `sp` was created and `sp.name` was set. *(instance creation, attribute modification)*
- `h` was associated with each of those Specialty instances, if not already. *(association formed — Practices-in)*
- `h` was associated with `l`, if not already. *(association formed — Practices-at)*
- An AvailabilitySlot instance `s` was created. *(instance creation)*
- `s.date`, `s.startTime` and `s.endTime` were set from the row, and `s.status` was set to AVAILABLE. *(attribute modification)*
- `s` was associated with `h` *(association formed — Offers)* and with `l` *(association formed — Held-at)*.

Then, once for the whole operation:
- An ActivityLogEntry instance `e` was created. *(instance creation)*
- `e.timestamp` was set to the current date and time, and `e.action` was set to "import availability". *(attribute modification)*
- `e` was associated with the AdministrativeStaff member. *(association formed — Performs)*

**Notes:**
- Creating professionals and specialties during import is the one exception to Iteration 1 assumption 6, since the system starts empty (domain model assumption 6). Their other details, such as employeeId and licenseNumber, are completed later through Manage Professional Information.
- Malformed rows and duplicate rows are skipped and reported in the import summary. If the file cannot be read, nothing is imported.

---

### CO16: exportAvailability

**Operation:** exportAvailability(filePath: String)

**Cross References:** Export Availability to CSV (SSD7)

**Preconditions:**
- The AdministrativeStaff member is identified and authenticated.
- At least one AvailabilitySlot exists.
- The file at `filePath` can be written.

**Postconditions:**
- None on the domain model. This operation creates, modifies and associates no domain objects.
- The file at `filePath` was written, replacing any existing file. It contains a header row and one row for every AvailabilitySlot in the system, whatever its status, with the columns First Name, Last Name, Title, Specialty, Date, Start, End. The professional's fields come from the HealthcareProfessional who offers the slot (Offers), and the Specialty field lists the professional's specialties (Practices-in), separated by ";".

**Note:** The columns are the same as the import format, so an exported file can be imported again.

---

### CO17: joinWaitlist

**Operation:** joinWaitlist(professionalId: String [0..1], specialtyName: String [0..1], fromDate: Date [0..1], toDate: Date [0..1], priority: Integer)

**Cross References:** Join Waitlist (extends UC1 Book Appointment at extension 5c)

**Preconditions:**
- The AdministrativeStaff member is identified and authenticated (the patient asks staff to add them).
- A booking request is underway: selectMedicalService succeeded for a patient.
- At least one of `professionalId`, `specialtyName`, or a date range is given, and any HealthcareProfessional or Specialty given exists.
- If both dates are given, `fromDate` is not after `toDate`.

**Postconditions:**
- A WaitlistEntry instance `w` was created. *(instance creation)*
- `w.priority` was set to `priority`, `w.dateRangeStart` to `fromDate`, `w.dateRangeEnd` to `toDate`, and `w.status` to WAITING. *(attribute modification)*
- `w` was associated with the Patient. *(association formed — Waits-in)*
- If `professionalId` was given, `w` was associated with that HealthcareProfessional. *(association formed — For)*
- If `specialtyName` was given, `w` was associated with that Specialty. *(association formed — For)*
- An ActivityLogEntry instance `e` was created. *(instance creation)*
- `e.timestamp` was set to the current date and time, and `e.action` was set to "join waitlist". *(attribute modification)*
- `e` was associated with the AdministrativeStaff member *(association formed — Performs)* and with the Patient *(association formed — Concerns)*.

## 3. Assumptions

1. **Status values.** AppointmentStatus: BOOKED, CONFIRMED, ARRIVED, COMPLETED, CANCELLED. ReferralStatus: PENDING (a Referral is "active" while its status is PENDING). SlotStatus: AVAILABLE, BOOKED. WaitlistStatus: WAITING, OFFERED, BOOKED, REMOVED (domain model assumption 4).
2. **Patient self-booking (CO3).** In the domain model, only ClinicStaff can perform an ActivityLogEntry. When a patient books for themselves, the log entry is linked to the Patient through Concerns only, and no staff member is recorded as performing it.
3. **Activity logging.** Every operation that changes the domain creates an ActivityLogEntry, except the steps inside a consultation (CO5–CO9), which are logged once by endConsultation. Query operations are not logged. Log entries about availability concern no patient, which the 0..1 multiplicity of Concerns allows.
4. **Resource conflicts (CO3).** Only rooms and equipment are checked for conflicts with overlapping appointments. Human resources are roles, not specific people, so the system does not check who fills them (domain model assumption 5).
5. **Matching imported professionals (CO15).** A row's professional is matched to an existing HealthcareProfessional by first and last name.
6. **Booked appointments CSV.** The PoC imports and exports availability only. The sample booked-appointments and waiting-list CSV files are not imported in this iteration.
