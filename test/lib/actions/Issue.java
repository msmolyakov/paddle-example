package lib.actions;

import com.wavesplatform.wavesj.Transaction;
import lib.Account;

import java.io.IOException;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.ONE_WAVES;

public class Issue implements Action {

    public Account issuer;
    public String name;
    public String description;
    public long quantity;
    public byte decimals;
    public boolean isReissuable;
    public String script;
    public long fee;

    public Issue(String name) {
        this.name = name;
        this.description = "";
        this.quantity = 1000_00000000L;
        this.decimals = 8;
        this.isReissuable = true;
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
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = ONE_WAVES;
            totalFee += issuer.isSmart() ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

    @Override
    public Transaction successfully() throws IOException {
        return issuer.node.waitForTransaction(issuer.node.wavesNode.issueAsset(issuer.wavesAccount,
                issuer.node.wavesNode.getChainId(), name, description, quantity, decimals, isReissuable, script, calcFee()));
    }

}
