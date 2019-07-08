package lib.actions;

import lib.Account;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.ONE_WAVES;

public class Reissue implements Action {

    public Account issuer;
    public String assetId;
    public long quantity;
    public boolean isReissuable;
    public long fee;

    public Reissue(Account from) {
        this.issuer = from;

        this.quantity = 10000_00000000L; //TODO а надо ли?
        this.isReissuable = true; //TODO а надо ли?
        this.fee = 0;
    }

    public static Reissue reissue(Account from) {
        return new Reissue(from);
    }

    public Reissue asset(String assetId) {
        this.assetId = assetId;
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

    public Reissue fee(long fee) {
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
            totalFee += issuer.node.isSmart(assetId) ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

}
