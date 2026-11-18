# Interaction Diagrams: Design Notes (Iteration 2)

These diagrams cover the three critical use cases: Book Appointment, Search Availability and Make Availability. There is one communication diagram per system operation, built from the operation contracts in `docs/operation-contracts/operation-contracts.md`. Class names come from `docs/domain-model/domain-model-iter2.puml`.

## Controller

`AppointmentSystem` is a facade controller. It receives every system operation, and it holds:
- the in-memory collections (patients, medical services, availability slots, and so on)
- `staff`, the logged-in AdministrativeStaff
- the current booking request (`currentPatient`, `currentService`), which `selectMedicalService` keeps for `searchAvailability`, `joinWaitlist` and `bookAppointment`

## UC1 Book Appointment

### findPatient (CO1)
- **Information Expert:** Patient knows its firstName, lastName, dateOfBirth and phoneNumber, so `matches(criteria)` is on Patient. It searches its own Insurance objects (Covered-by) for an insurance number.
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
  | Attends | stores `p` |
  | Occupies | stores the slot |
  | Conducts | stores the slot's professional |
  | Is-for | stores `service` |
  | Results-in | stores `ref` |
  | Requests | stores `staff` |
  | Allocates | stores `rs`, and calls `r.allocate(a)` on each resource |

  `p.addAppointment(a)` forms Attends from the patient's side.
- **Creator:** ClinicStaff records its ActivityLogEntries (Performs), so `staff` creates the entry and adds it to its collection.
- **Failure:** if `book()` returns no Appointment, the controller reports "slot unavailable" (7a) or "required resources unavailable", and staff picks another slot.

## Search Availability

### searchAvailability (CO13)
- **Lookups:** the controller resolves each criterion that is given (professional, specialty, location) with `find` on its collections. If one is unknown, or `fromDate` is after `toDate`, it reports "invalid search criteria".
- **Information Expert:** each AvailabilitySlot decides whether it `matches(h, sp, l, fromDate, toDate)`:
  - It knows its status (`isAvailable()`), its date, its professional (Offers) and its location (Held-at).
  - For a specialty, it asks its professional `practises(sp)`. The professional knows its Specialties (Practices-in).
- **Results:** the controller returns the matching slots sorted by date and start time. An empty list means "no available slots". Book Appointment includes this same operation (`<<include>>`).

## Make Availability

### makeAvailability (CO14)
- **Information Expert:** HealthcareProfessional knows its ClinicLocations (Practices-at), so `practisesAt(l)` is on the professional. If it returns false, the controller reports "professional does not practise at this location".
- **Creator:** HealthcareProfessional creates its AvailabilitySlots. It records them (Offers, 1 to *) and has the initializing data (itself). Each new slot gets `status = AVAILABLE` and is linked to `h` (Offers) and `l` (Held-at).
- **RepeatPattern:** a design class for the `repeatPattern` parameter, which is days of the week or a day of the month. It is the Expert on whether a date `d` matches. Following domain model assumption 1, it is not stored.
- **Information Expert:** each existing AvailabilitySlot knows its date and times, so it answers `overlaps(d, start, end)`. Intervals that clash are skipped and reported back.
- **Controller collection:** the new slots are also added to the controller's slot collection. `searchAvailability` and `bookAppointment` look slots up there.
- **Activity log:** `staff` logs "make availability" once for the whole operation, with no patient (Concerns is 0..1).

## Visibility

| Sender → receiver | Kind |
|---|---|
| controller → its collections, `staff`, current booking request | attribute |
| controller → `slot`, `p`, `service` returned by `find` | local |
| `slot` → `service`, `p`, `ref`, `staff` (passed in `book`) | parameter |
| `service` → `r` (Requires) | attribute |
| `a` → `r` (`rs` returned by `getResources`) | local |
| `p` → its Referral, Insurance and Appointment collections | attribute |
| `staff` → its ActivityLogEntry collection | attribute |
| controller → `s` (each slot in `slots`), `h`, `l`, `sp` returned by `find` | local |
| `s` → its `professional` (Offers) | attribute |
| `professional` / `h` → its Specialty, ClinicLocation and AvailabilitySlot collections | attribute |
| `h` → `repeatPattern`, `l` (passed in `makeSlots`) | parameter |
| `h` → `x` (each slot in its collection), `s` (just created) | attribute / local |

## Methods per class (for the class diagram)

| Class | Methods |
|---|---|
| AppointmentSystem | findPatient, selectMedicalService, bookAppointment, searchAvailability, makeAvailability |
| Patient | matches, getActiveReferrals, getReferral, addAppointment |
| MedicalService | requiresReferral, resourcesFreeDuring, getResources |
| Referral | isActive |
| AvailabilitySlot | book, isAvailable, markBooked, matches, overlaps, constructor (d, start, end, h, l) |
| HealthcareProfessional | practises, practisesAt, makeSlots |
| RepeatPattern | matches |
| Resource | isFreeDuring, allocate |
| Appointment | constructor (p, slot, professional, service, ref, staff) |
| AdministrativeStaff | logAction |
| ActivityLogEntry | constructor (action, p [0..1]) |
