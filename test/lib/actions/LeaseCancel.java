package lib.actions;

import com.wavesplatform.wavesj.Transaction;
import lib.Account;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static lib.Constants.*;

public class LeaseCancel implements Action {

    private String leaseId;
    private Account sender;
    private long fee;

    public LeaseCancel(String leaseId) {
        this.leaseId = leaseId;
        this.fee = 0;
    }

    public LeaseCancel from(Account sender) {
        this.sender = sender;
        return this;
    }

    public LeaseCancel withFee(long fee) {
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
        return sender.node.waitForTransaction(sender.node.wavesNode.cancelLease(
                sender.wavesAccount, sender.node.wavesNode.getChainId(), leaseId, calcFee()));
    }

    @Override
    public void butGotError() {

    }
}
