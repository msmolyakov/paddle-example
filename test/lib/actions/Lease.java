package lib.actions;

import lib.Account;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;

public class Lease implements Action {

    public Account sender;
    public String recipient;
    public long amount;
    public long fee;

    public Lease() {
        this.fee = 0;
    }

    public Lease from(Account sender) {
        this.sender = sender;
        return this;
    }

    public Lease to(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public Lease to(Account account) {
        this.recipient = account.address();
        return this;
    }

    public Lease amount(long amount) {
        this.amount = amount;
        return this;
    }

    public Lease fee(long fee) {
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
