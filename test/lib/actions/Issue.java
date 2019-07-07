package lib.actions;

import lib.Account;

import java.util.Random;

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

    public Issue() {
        this.name = "Asset " + new Random().nextInt(10000);
        this.description = "";
        this.decimals = 8;
        this.isReissuable = true;
        this.script = null;
        this.fee = 0;
    }

    public Issue from(Account issuer) {
        this.issuer = issuer;
        return this;
    }

    public Issue name(String name) {
        this.name = name;
        return this;
    }

    public Issue description(String description) {
        this.description = description;
        return this;
    }

    public Issue quantity(long quantity) {
        this.quantity = quantity;
        return this;
    }

    public Issue decimals(int value) {
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

    public Issue script(String compiledBase64) {
        this.script = compiledBase64;
        return this;
    }

    public Issue fee(long fee) {
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

}
