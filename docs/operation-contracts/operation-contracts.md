# System Operations and Operation Contracts

Iteration 1 — Appointment Tracking System

The system operations below are taken from the system sequence diagrams (SSD1–SSD3). Each one is an event that crosses the system boundary from an actor to the System, treated as a black box. Together they form the public interface of the System.

All class, attribute and association names in the contracts are taken from the UML domain model.

## 1. System Operations

```
                         System
----------------------------------------------------------------
findPatient(criteria)
requestAvailableSlots(patientId, serviceId, professionalId, locationId, date)
bookAppointment(startTime, referralId)
startConsultation(appointmentId)
recordObservations(observations)
recordDiagnosis(diagnoses, treatmentRecommendations)
startPrescription()
addMedication(medicationId, dosage, frequency)
recordFollowUp(followUpRequirements)
endConsultation()
selectReferralTarget(patientId, specialtyName, professionalId)
submitReferral(clinicalReason, priority)
```

| Operation | Use case(s) | SSD | Type |
|---|---|---|---|
| findPatient | UC1 Book Appointment, UC3 Issue Referral | SSD1, SSD3 | Query |
| requestAvailableSlots | UC1 Book Appointment | SSD1 | Query |
| bookAppointment | UC1 Book Appointment | SSD1 | State change |
| startConsultation | UC2 Conduct Consultation | SSD2 | State change |
| recordObservations | UC2 Conduct Consultation | SSD2 | State change |
| recordDiagnosis | UC2 Conduct Consultation | SSD2 | State change |
| startPrescription | Issue Prescription (extends UC2) | SSD2 | State change |
| addMedication | Issue Prescription (extends UC2) | SSD2 | State change |
| recordFollowUp | UC2 Conduct Consultation | SSD2 | State change |
| endConsultation | UC2 Conduct Consultation | SSD2 | State change |
| selectReferralTarget | UC3 Issue Referral | SSD3 | Query |
| submitReferral | UC3 Issue Referral | SSD3 | State change |

`findPatient` is shared by two use cases, so it is listed and contracted once.

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

### CO2: requestAvailableSlots

**Operation:** requestAvailableSlots(patientId: String, serviceId: String, professionalId: String, locationId: String, date: Date)

**Cross References:** UC1 Book Appointment (steps 4–5, extensions 5a, 5b, 5c, 5d)

**Preconditions:**
- The user (AdministrativeStaff, or a Patient booking for themselves) is identified and authenticated.
- A Patient with `patientId` exists.
- A MedicalService, HealthcareProfessional and ClinicLocation exist for `serviceId`, `professionalId` and `locationId`.

**Postconditions:**
- None. This is a query operation. The System returns the free time slots on `date`, computed from the professional's Schedule held at the ClinicLocation (Works, Held-at) minus the existing Appointments the professional conducts on that date. If MedicalService.requiresReferral is true, it also returns the patient's active Referral instances.
- The System retains the patient, service, professional, location and date as the current booking request, for use by bookAppointment.

---

### CO3: bookAppointment

**Operation:** bookAppointment(startTime: Time, referralId: String [0..1])

**Cross References:** UC1 Book Appointment (steps 6–7, extensions 5b, 7a)

**Preconditions:**
- A booking request is underway: requestAvailableSlots has returned available slots for a patient, service, professional, location and date.
- `startTime` is one of the slots returned.
- If the MedicalService requires a referral, `referralId` identifies one of the patient's active Referrals.

**Postconditions:**
- An Appointment instance `a` was created. *(instance creation)*
- `a.date` was set to the requested date and `a.startTime` was set to `startTime`. *(attribute modification)*
- `a.status` was set to BOOKED if the booking user is AdministrativeStaff, or to CONFIRMED if the booking user is the Patient. *(attribute modification)*
- `a` was associated with the Patient. *(association formed — Attends)*
- `a` was associated with the HealthcareProfessional. *(association formed — Conducts)*
- `a` was associated with the MedicalService. *(association formed — Is-for)*
- `a` was associated with the ClinicLocation. *(association formed — Takes-place-at)*
- If `referralId` was given, `a` was associated with the Referral, based on `referralId` match. *(association formed — Results-in)*
- An ActivityLogEntry instance `e` was created. *(instance creation)*
- `e.timestamp` was set to the current date and time, and `e.action` was set to "book appointment". *(attribute modification)*
- If the booking user is AdministrativeStaff, `e` was associated with that staff member. *(association formed — Performs)*
- `e` was associated with the Patient. *(association formed — Concerns)*

**Note:** The booked time becomes unavailable because the new Appointment is now among the professional's appointments. The Schedule itself is not modified.

---

### CO4: startConsultation

**Operation:** startConsultation(appointmentId: String)

**Cross References:** UC2 Conduct Consultation (steps 1–2, extensions 1a, 1b, 1c)

**Preconditions:**
- The HealthcareProfessional is identified and authenticated with clinical access.
- An Appointment `a` with `appointmentId` exists, and `a.date` is today.
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

## 3. Assumptions

1. **Status values.** The status values used in the contracts are the ones named in the fully-dressed use cases and assumptions: AppointmentStatus BOOKED, CONFIRMED, ARRIVED, COMPLETED, CANCELLED, and ReferralStatus PENDING.
2. **Patient self-booking (CO3).** In the domain model, only ClinicStaff can perform an ActivityLogEntry. When a patient books for themselves, the log entry is linked to the Patient through Concerns only, and no staff member is recorded as performing it.
