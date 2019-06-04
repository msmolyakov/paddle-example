package lib.actions;

import com.wavesplatform.wavesj.Transaction;
import lib.Account;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static lib.Constants.ONE_WAVES;

public class Reissue implements Action {

    private Account issuer;
    private String assetId;
    private long quantity;
    private boolean isReissuable;
    private long fee;

    public Reissue(String assetId) {
        this.assetId = assetId;
        this.quantity = 10000_00000000L;
        this.isReissuable = false;
        this.fee = 0;
    }

    public Reissue from(Account issuer) {
        this.issuer = issuer;
        return this;
    }

    public Reissue quantity(long quantity) {
        this.quantity = quantity;
        return this;
    }

    public Reissue reissuable(boolean isReissuable) {
        this.isReissuable = isReissuable;
        return this;
    }

    public Reissue reissuable() {
        return reissuable(true);
    }

    public Reissue notReissuable() {
        return reissuable(false);
    }

    public Reissue withFee(long fee) {
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
        return issuer.node.waitForTransaction(issuer.node.wavesNode.reissueAsset(issuer.wavesAccount,
                issuer.node.wavesNode.getChainId(), assetId, quantity, isReissuable, calcFee()));
    }

    @Override
    public void butGotError() {

    }
}
