package lib.actions;

import lib.Account;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;

public class CreateAlias implements Action {

    public Account sender;
    public String alias;
    public long fee;

    public CreateAlias() {
        this.fee = 0;
    }

    public CreateAlias from(Account sender) {
        this.sender = sender;
        return this;
    }

    public CreateAlias alias(String alias) {
        this.alias = alias;
        return this;
    }

    public CreateAlias fee(long fee) {
        this.fee = fee;
        return this;
    }

    @Override
    public long calcFee() {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = MIN_FEE;
            totalFee += sender.isSmart() ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

}
