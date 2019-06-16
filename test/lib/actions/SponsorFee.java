package lib.actions;

import com.wavesplatform.wavesj.Transaction;
import lib.Account;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.ONE_WAVES;

//TODO отмена спонсорства
public class SponsorFee implements Action {

    private Account sender;
    private String assetId;
    private long minSponsoredAssetFee;
    private long fee;

    public SponsorFee(String assetId) {
        this.assetId = assetId;
        this.minSponsoredAssetFee = 1;
        this.fee = 0;
    }

    public SponsorFee from(Account sender) {
        this.sender = sender;
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
    public long calcFee() throws IOException {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = ONE_WAVES;
            totalFee += sender.isSmart() ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

    @Override
    public Transaction successfully() throws IOException, TimeoutException {
        return sender.node.waitForTransaction(sender.node.wavesNode.sponsorAsset(
                sender.wavesAccount, assetId, minSponsoredAssetFee, calcFee()));
    }

    @Override
    public void butGotError() {

    }
}
