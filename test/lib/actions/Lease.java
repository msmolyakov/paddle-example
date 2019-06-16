package lib.actions;

import com.wavesplatform.wavesj.Transaction;
import lib.Account;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static lib.Constants.*;

public class Lease implements Action {

    private long amount;
    private Account sender;
    private Account recipient;
    private long fee;

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

    public Lease withFee(long fee) {
        this.fee = fee;
        return this;
    }

    @Override
    public long calcFee() throws IOException {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = MIN_FEE;
            totalFee += sender.isSmart() ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

    @Override
    public Transaction successfully() throws IOException, TimeoutException {
        return sender.node.waitForTransaction(sender.node.wavesNode.lease(
                sender.wavesAccount, recipient.address(), amount, calcFee()));
    }

    @Override
    public void butGotError() {

    }
}
