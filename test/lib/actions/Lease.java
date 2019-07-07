package lib.actions;

import com.wavesplatform.wavesj.Transaction;
import lib.Account;

import java.io.IOException;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;

public class Lease implements Action {

    public long amount;
    public Account sender;
    public Account recipient;
    public long fee;

    public Lease(long amount) {
        this.amount = amount;
        this.fee = 0;
    }

    public Lease from(Account sender) {
        this.sender = sender;
        if (this.recipient == null) this.recipient = this.sender; //TODO может сам себе?
        return this;
    }

    public Lease to(Account recipient) {
        this.recipient = recipient;
        if (this.sender == null) this.sender = this.recipient;
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

    public Transaction successfully() throws IOException {
        return sender.node.waitForTransaction(sender.node.wavesNode.lease(
                sender.wavesAccount, recipient.address(), amount, calcFee()));
    }

}
