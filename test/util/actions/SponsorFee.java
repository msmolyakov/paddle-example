package util.actions;

import com.wavesplatform.wavesj.Transaction;
import util.Account;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static util.Constants.ONE_WAVES;

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
    public long calcFee() {
        if (this.fee == 0) {
            return ONE_WAVES;
        } else {
            return this.fee;
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
