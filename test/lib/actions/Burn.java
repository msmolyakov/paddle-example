package lib.actions;

import com.wavesplatform.wavesj.Transaction;
import lib.Account;

import java.io.IOException;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;

public class Burn implements Action {

    public Account issuer;
    public String assetId;
    public long quantity;
    public long fee;

    public Burn(String assetId) {
        this.assetId = assetId;
        this.quantity = 0;
        this.fee = 0;
    }

    public Burn from(Account issuer) {
        this.issuer = issuer;
        return this;
    }

    public Burn quantity(long quantity) {
        this.quantity = quantity;
        return this;
    }

    public Burn withFee(long fee) {
        this.fee = fee;
        return this;
    }

    @Override
    public long calcFee() {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = MIN_FEE;
            totalFee += issuer.isSmart() ? EXTRA_FEE : 0;
            totalFee += issuer.node.isSmart(assetId) ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

    public Transaction successfully() throws IOException {
        return issuer.node.waitForTransaction(issuer.node.wavesNode.burnAsset(issuer.wavesAccount,
                issuer.node.wavesNode.getChainId(), assetId, quantity, calcFee()));
    }

}
