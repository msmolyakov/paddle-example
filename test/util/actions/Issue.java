package util.actions;

import com.wavesplatform.wavesj.Transaction;
import util.Account;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static util.Constants.ONE_WAVES;

public class Issue implements Action {

    private Account issuer;
    private String name;
    private String description;
    private long quantity;
    private byte decimals;
    private boolean isReissuable;
    private String script;
    private long fee;

    public Issue(String name) {
        this.name = name;
        this.description = "";
        this.quantity = 1000_00000000L;
        this.decimals = 8;
        this.isReissuable = false;
        this.script = null;
        this.fee = 0;
    }

    public Issue from(Account issuer) {
        this.issuer = issuer;
        return this;
    }

    public Issue withDescription(String description) {
        this.description = description;
        return this;
    }

    public Issue withQuantity(long quantity) {
        this.quantity = quantity;
        return this;
    }

    public Issue withDecimals(int value) {
        this.decimals = (byte) value;
        return this;
    }

    public Issue reissuable(boolean isReissuable) {
        this.isReissuable = isReissuable;
        return this;
    }

    public Issue reissuable() {
        return reissuable(true);
    }

    public Issue notReissuable() {
        return reissuable(false);
    }

    public Issue withScript(String compiledBase64) {
        this.script = compiledBase64;
        return this;
    }

    public Issue withFee(long fee) {
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
        return issuer.node.waitForTransaction(issuer.node.wavesNode.issueAsset(issuer.wavesAccount,
                issuer.node.wavesNode.getChainId(), name, description, quantity, decimals, isReissuable, script, calcFee()));
    }

    @Override
    public void butGotError() {

    }
}
