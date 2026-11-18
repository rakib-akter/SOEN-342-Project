# Interaction Diagrams: Design Notes (Iteration 2)

These diagrams cover the three critical use cases: Book Appointment, Search Availability and Make Availability. There is one communication diagram per system operation, built from the operation contracts in `docs/operation-contracts/operation-contracts.md`. Class names come from `docs/domain-model/domain-model-iter2.puml`.

## Controller

`AppointmentSystem` is a facade controller. It receives every system operation, and it holds:
- the in-memory collections (patients, medical services, availability slots, and so on)
- `staff`, the logged-in AdministrativeStaff
- the current booking request (`currentPatient`, `currentService`), which `selectMedicalService` keeps for `joinWaitlist` and `bookAppointment`

## UC1 Book Appointment

### findPatient (CO1)
- **Information Expert:** Patient knows its firstName, lastName, dateOfBirth, phoneNumber and insuranceNumbers, so `matches(insuranceNumber, dateOfBirth, phoneNumber, firstName, lastName)` is on Patient. Insurance is kept inside Patient as `insuranceNumbers`, since the search only needs the number.
- **Results:** the controller returns every patient that matches. An empty list is extension 3a, and several matches is 3b.

### selectMedicalService (CO2)
- **Information Expert:** MedicalService knows `requiresReferral`. Patient knows its Referrals (Refers). Referral knows its status (`isActive()`).
- **Referral required:** if a referral is required and the patient has no active referral, the controller reports "referral required" (extension 5a).

### bookAppointment (CO3, with Allocate Resources)
- **Creator:** `AvailabilitySlot` creates the `Appointment`. It has the initializing data: date, time and location, plus the professional who offers it (Offers). It also enforces "one active appointment per slot", so the availability check and `markBooked()` stay in one object.
- **Information Expert:**
  - The slot knows its own status (`isAvailable()`, `markBooked()`).
  - MedicalService knows the resources it requires (Requires), so it checks `resourcesFreeDuring(slot)`.
  - A Resource knows the appointments it is allocated to (Allocates).
- **Resource availability:** `isFreeDuring(slot)` is always true for a role. For a room or equipment, it is true when none of the resource's non-cancelled appointments occupies an overlapping slot.
- **Associations in the Appointment constructor:**

  | Association | Formed by |
  |---|---|
  | Attends | stores `patient` |
  | Occupies | stores the slot |
  | Conducts | stores the slot's professional |
  | Is-for | stores `service` |
  | Results-in | stores `referral` |
  | Requests | stores `staff` |
  | Allocates | stores `resources`, and calls `resource.allocate(appointment)` on each resource |

  `patient.addAppointment(appointment)` forms Attends from the patient's side.
- **Creator:** ClinicStaff records its ActivityLogEntries (Performs), so `staff` creates the entry and adds it to its collection.
- **Failure:** if `book()` returns no Appointment, the controller reports "slot unavailable" (7a) or "required resources unavailable", and staff picks another slot.

## Search Availability

### searchAvailability (CO13)
- **Lookups:** the controller resolves each criterion that is given (professional, specialty, location) with `find` on its collections. If one is unknown, or `fromDate` is after `toDate`, it reports "invalid search criteria".
- **Information Expert:** each AvailabilitySlot decides whether it `matches(professional, specialty, location, fromDate, toDate)`:
  - It knows its status (`isAvailable()`), its date, its professional (Offers) and its location (Held-at).
  - For a specialty, it asks its professional `practicesIn(specialty)`. The professional knows its Specialties (Practices-in).
- **Results:** the controller returns the matching slots sorted by date and start time. An empty list means "no available slots". Book Appointment includes this same operation (`<<include>>`).

## Make Availability

### makeAvailability (CO14)
- **Information Expert:** HealthcareProfessional knows its ClinicLocations (Practices-at), so `practicesAt(location)` is on the professional. If it returns false, the controller reports "professional does not practise at this location".
- **Creator:** HealthcareProfessional creates its AvailabilitySlots through `addSlot(location, date, startTime, endTime)`. It records them (Offers, 1 to *) and has the initializing data (itself). Each new slot gets `status = AVAILABLE` and is linked to `professional` (Offers) and `location` (Held-at).
- **Repeat pattern:** given as the parameters `daysOfWeek` and `dayOfMonth`. The controller checks each date with its private `matchesPattern(date, daysOfWeek, dayOfMonth)`. Following domain model assumption 1, the pattern is not stored.
- **Information Expert:** each existing AvailabilitySlot knows its date and times, so it answers `overlaps(date, startTime, endTime)`. `addSlot` returns nothing when an interval clashes, and the controller reports that interval as skipped in its summary.
- **Controller collection:** the new slots are also added to the controller's slot collection. `searchAvailability` and `bookAppointment` look slots up there.
- **Activity log:** `staff` logs "make availability" once for the whole operation, with no patient (Concerns is 0..1).

## Visibility

| Sender → receiver | Kind |
|---|---|
| controller → its collections, `staff`, current booking request | attribute |
| controller → `slot`, `patient`, `service` returned by `find` | local |
| `slot` → `service`, `patient`, `referral`, `staff` (passed in `book`) | parameter |
| `service` → `resource` (Requires) | attribute |
| `appointment` → `resource` (`resources` returned by `getResources`) | local |
| `patient` → its Referral and Appointment collections | attribute |
| `staff` → its ActivityLogEntry collection | attribute |
| controller → `slot` (each slot in `slots`), `professional`, `location`, `specialty` returned by `find` | local |
| `slot` → its `slotProfessional` (Offers) | attribute |
| `professional` / `slotProfessional` → its Specialty, ClinicLocation and AvailabilitySlot collections | attribute |
| `professional` → `location` (passed in `addSlot`) | parameter |
| `professional` → `existingSlot` (each slot in its collection), `newSlot` (just created) | attribute / local |

## Methods per class (for the class diagram)

| Class | Methods |
|---|---|
| AppointmentSystem | findPatient, selectMedicalService, bookAppointment, searchAvailability, makeAvailability, importAvailability, exportAvailability, matchesPattern (private) |
| Patient | matches, getActiveReferrals, getReferral, addAppointment |
| MedicalService | requiresReferral, resourcesFreeDuring, getResources |
| Referral | isActive |
| AvailabilitySlot | book, isAvailable, markBooked, matches, overlaps, constructor (date, startTime, endTime, professional, location) |
| HealthcareProfessional | practicesIn, practicesAt, addSpecialty, addLocation, addSlot |
| Resource | isFreeDuring, allocate |
| Appointment | constructor (patient, slot, professional, service, referral, staff) |
| AdministrativeStaff | logAction |
| ActivityLogEntry | constructor (actionName, patient) |
