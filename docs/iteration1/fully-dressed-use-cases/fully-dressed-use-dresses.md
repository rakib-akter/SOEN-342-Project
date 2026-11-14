# Fully Dressed Use Cases

## UC1: Book Appointment

**Scope:** Appointment Tracking System
**Level:** User goal
**Primary Actor:** Administrative Staff

**Stakeholders and Interests:**
- Patient: Wants a timely appointment with the right professional, for the right service, at a convenient location.
- Administrative Staff: Wants fast, accurate booking with no double booking.
- Healthcare Professional: Wants their schedule respected. Appointments that require a referral must have one.
- Clinic: Wants accurate records. Every booking must be traceable.

**Preconditions:**
- The user (administrative staff or patient) is identified and authenticated.
- The patient is registered in the system.

**Success Guarantee (Postconditions):**
- An appointment is recorded with status "booked". It is linked to the patient, the healthcare professional, the medical service, the clinic location, and the referral if the service requires one.
- If the patient booked the appointment, its status is "confirmed".
- The booked time is no longer available in the professional's schedule.
- The booking activity is logged.

**Main Success Scenario:**
1. The patient requests an appointment for a medical service.
2. Staff searches for the patient by insurance number, date of birth, phone number, or partial first and/or last name.
3. The system presents the matching patient.
4. Staff enters the requested medical service, healthcare professional, clinic location, and preferred date.
5. The system verifies that the service does not require a referral, checks the professional's schedule and scheduling constraints, and presents the available time slots.
6. Staff selects a time slot.
7. The system records the appointment, logs the activity, and presents a confirmation with the appointment details.

**Extensions:**
- 3a. No patient matches the search criteria.
  1. The system indicates that no patient was found.
  2. Staff refines the search and the flow resumes at step 3.
  3. If no match is found, the use case ends without booking an appointment.
- 3b. Several patients match.
  1. The system presents all matches.
  2. Staff narrows the search, for example by date of birth, and selects the correct patient.
- 5a. Failure: required referral missing. The service requires a referral and the patient has no active referral for this specialty or professional.
  1. The system indicates that a referral is required.
  2. The use case ends without booking an appointment.
- 5b. The service requires a referral and the patient has an active one.
  1. The system presents the patient's active referrals.
  2. Staff selects the referral. The system links it to the appointment.
- 5c. Failure: no availability. The professional has no available slot on the requested date.
  1. The system indicates that no slots are available.
  2. Staff requests another date or another professional in the same specialty. The flow resumes at step 5.
  3. If no option suits the patient, the use case ends without booking an appointment.
- 5d. The professional does not practise at the requested location on that date.
  1. The system indicates this and presents the locations where the professional is scheduled.
  2. Staff selects a location and the flow resumes at step 5.
- 7a. The selected slot was taken before the booking was completed.
  1. The system indicates that the slot is no longer available and presents the updated slots.
  2. The flow resumes at step 6.
- \*a. At any time, staff cancels the booking.
  1. No appointment is created.

**Special Requirements:**
- The system is accessed through a command line interface.
- Only authenticated users with the administrative staff or patient role may book.
- Every booking is logged.

**Technology and Data Variations List:**
- The patient can be identified by RAMQ number, private insurance number, date of birth, phone number, or partial name.
- A patient may perform this use case themselves, following the same flow.

**Frequency of Occurrence:** Many times per day.

**Open Issues:**
- What scheduling constraints apply? Minimum notice, appointment length per service, and maximum booking window are not specified.
- Can an appointment be booked by specialty without naming a professional?

## UC2: Conduct Consultation

**Scope:** Appointment Tracking System
**Level:** User goal
**Primary Actor:** Healthcare Professional

**Stakeholders and Interests:**
- Patient: Wants an accurate clinical record, appropriate treatment, and confidentiality.
- Healthcare Professional: Wants fast, complete recording and access to the patient's clinical history.
- Administrative Staff: Wants the appointment marked completed so billing and follow ups can proceed. Staff must not see clinical details.
- Clinic: Wants a complete clinical record, with every access logged.

**Preconditions:**
- The healthcare professional is identified and authenticated with clinical access.
- An appointment for the patient with this professional exists for today.
- The patient's arrival has been registered (Register Patient Arrival).

**Success Guarantee (Postconditions):**
- A consultation is recorded, linked to the appointment, and added to the patient's clinical record.
- Observations, diagnoses, treatment recommendations, and follow up requirements are recorded.
- The appointment status is set to "completed".
- Access to the clinical record and the recording activity are logged.

**Main Success Scenario:**
1. The healthcare professional selects the patient's appointment from today's appointments.
2. The system presents the patient's information and clinical record history.
3. The professional records observations.
4. The professional records diagnoses and treatment recommendations.
5. The professional records any follow up requirements.
6. The professional indicates that the consultation is complete.
7. The system saves the consultation to the patient's clinical record, marks the appointment "completed", logs the activity, and confirms.

**Extension Points:** Prescription, at step 4 (Issue Prescription, extend).

**Extensions:**
- 1a. Failure: patient has not arrived. The patient's arrival has not been registered.
  1. The system indicates that the patient has not arrived.
  2. The use case ends without starting a consultation.
- 1b. Failure: not the professional's appointment. The appointment is assigned to a different professional.
  1. The system denies access to the clinical record and logs the attempt.
  2. The use case ends.
- 1c. The appointment has been cancelled.
  1. The system indicates that the appointment is no longer valid.
  2. The use case ends.
- 4a. The professional decides a prescription is needed.
  1. Issue Prescription is performed. The professional specifies one or more medications with dosage and frequency. The system records the prescription linked to the consultation.
  2. The flow resumes at step 5.
- 5a. No follow up is needed.
  1. The professional skips this step and the flow resumes at step 6.
- 6a. Required clinical information is missing, such as observations or a diagnosis.
  1. The system indicates the missing information.
  2. The professional completes it and the flow resumes at step 6.
- \*a. Failure: session interrupted. The session ends before the consultation is completed.
  1. The consultation is not recorded.
  2. The appointment keeps its "arrived" status.

**Special Requirements:**
- The system is accessed through a command line interface.
- Only authorized healthcare professionals may view or record clinical information. Administrative staff have no access.
- Every access to a clinical record is logged.

**Technology and Data Variations List:**
- Diagnoses may be recorded as free text.

**Frequency of Occurrence:** Once per completed appointment.

**Open Issues:**
- Can a consultation be edited after completion? If so, are amendments versioned?
- Should diagnoses use a standard coding system?
- Is a consultation possible without a prior appointment, such as a walk in?

## UC3: Issue Referral

**Scope:** Appointment Tracking System
**Level:** User goal
**Primary Actor:** Healthcare Professional (referring professional)

**Stakeholders and Interests:**
- Patient: Wants timely access to the right specialist.
- Referring Healthcare Professional: Wants the clinical reason and urgency conveyed clearly.
- Receiving Professional: Wants enough clinical context and a priority to plan the consultation.
- Administrative Staff: Wants a valid referral available so appointments requiring one can be booked.
- Clinic: Wants every referral traceable to its issuer.

**Preconditions:**
- The healthcare professional is identified and authenticated.
- The patient is registered in the system.

**Success Guarantee (Postconditions):**
- A referral is recorded with status "pending". It records the patient, the referring professional, the requested specialty or professional, the clinical reason, and the priority.
- The referral is available for booking appointments (Book Appointment).
- The activity is logged.

**Main Success Scenario:**
1. The healthcare professional searches for the patient to be referred.
2. The system presents the patient and any existing referrals.
3. The professional specifies the requested specialty or a specific professional.
4. The system verifies that the requested specialty or professional is offered at the clinic.
5. The professional enters the clinical reason and priority.
6. The professional submits the referral.
7. The system records the referral with status "pending", logs the activity, and confirms with the referral details.

**Extensions:**
- 2a. Failure: patient not found.
  1. The system indicates that no matching patient exists.
  2. The professional refines the search and the flow resumes at step 2.
  3. If the patient is not registered, the use case ends without a referral.
- 2b. The patient already has an active referral for the same specialty.
  1. The system warns the professional and shows the existing referral.
  2. The professional proceeds and the flow resumes at step 3, or cancels and the use case ends.
- 4a. Failure: specialty or professional not offered.
  1. The system indicates that the requested specialty or professional is not offered at the clinic.
  2. The professional selects another and the flow resumes at step 4.
  3. If no alternative applies, the use case ends without a referral.
- 6a. The clinical reason is missing or the priority is invalid.
  1. The system indicates the problem.
  2. The professional corrects it and the flow resumes at step 6.
- \*a. At any time, the professional cancels.
  1. No referral is created.

**Special Requirements:**
- The system is accessed through a command line interface.
- The clinical reason is confidential and visible only to authorized healthcare professionals.

**Technology and Data Variations List:**
- The patient can be identified by insurance number, date of birth, phone number, or partial name.
- Referrals from external physicians are handled by Record External Referral, not this use case.

**Frequency of Occurrence:** Whenever specialist care is needed.

**Open Issues:**
- What priority levels exist, and do they impose booking deadlines?
- Do referrals expire?
- How is a referral's status updated after it results in an appointment?