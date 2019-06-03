package util.actions;

import com.wavesplatform.wavesj.Transaction;
import util.Account;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static util.Constants.MIN_FEE;

public class Burn implements Action {

    private Account issuer;
    private String assetId;
    private long quantity;
    private long fee;

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
        if (this.fee == 0) {
            return MIN_FEE;
        } else {
            return this.fee;
        }
    }

    @Override
    public Transaction successfully() throws IOException, TimeoutException {
        return issuer.node.waitForTransaction(issuer.node.wavesNode.burnAsset(issuer.wavesAccount,
                issuer.node.wavesNode.getChainId(), assetId, quantity, calcFee()));
    }

    @Override
    public void butGotError() {

    }
}
