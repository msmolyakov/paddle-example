package lib.actions;

import com.wavesplatform.wavesj.Transaction;
import lib.Account;

import java.io.IOException;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;

public class LeaseCancel implements Action {

    public String leaseId;
    public Account sender;
    public long fee;

    public LeaseCancel(String leaseId) {
        this.leaseId = leaseId;
        this.fee = 0;
    }

    public LeaseCancel from(Account sender) {
        this.sender = sender;
        return this;
    }

    public LeaseCancel fee(long fee) {
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
        return sender.node.waitForTransaction(sender.node.wavesNode.cancelLease(
                sender.wavesAccount, sender.node.wavesNode.getChainId(), leaseId, calcFee()));
    }

}
