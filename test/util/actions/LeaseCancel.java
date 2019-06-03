package util.actions;

import com.wavesplatform.wavesj.Transaction;
import util.Account;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static util.Constants.MIN_FEE;

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
    public long calcFee() {
        if (this.fee == 0) {
            return MIN_FEE;
        } else {
            return this.fee;
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
