package lib.actions;

import lib.Account;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.ONE_WAVES;

//TODO отмена спонсорства
public class SponsorFee implements Action {

    public Account sender;
    public String assetId;
    public long minSponsoredAssetFee;
    public long fee;

    public SponsorFee() {
        this.minSponsoredAssetFee = 1;
        this.fee = 0;
    }

    public SponsorFee from(Account sender) {
        this.sender = sender;
        return this;
    }

    public SponsorFee asset(String assetId) {
        this.assetId = assetId;
        return this;
    }

    public SponsorFee amountForMinFee(long assetAmount) {
        this.minSponsoredAssetFee = assetAmount;
        return this;
    }

    public SponsorFee withFee(long fee) {
        this.fee = fee;
        return this;
    }

    @Override
    public long calcFee() {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = ONE_WAVES;
            totalFee += sender.isSmart() ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

}
