// A referral for a patient. A new referral is PENDING (CO12), and it is active while PENDING.
public class Referral {
    private String referralId;
    private ReferralStatus status;

    public Referral(String referralId) {
        this.referralId = referralId;
        this.status = ReferralStatus.PENDING;
    }

    public String getReferralId() {
        return referralId;
    }

    public ReferralStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == ReferralStatus.PENDING;
    }
}
