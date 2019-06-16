package lib.actions;

import com.wavesplatform.wavesj.Transaction;
import lib.Account;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;

public class Transfer implements Action {

    private long amount;
    private String assetId;
    private Account sender;
    private Account recipient;
    private String attachment;
    private long fee;

    public Transfer(long amount, String assetId) {
        this.amount = amount;
        this.assetId = assetId;
        this.attachment = "";
        this.fee = 0;
    }

    public Transfer(long amount) {
        this(amount, null);
    }

    public Transfer from(Account sender) {
        this.sender = sender;
        if (this.recipient == null) this.recipient = this.sender;
        return this;
    }

    public Transfer to(Account recipient) {
        this.recipient = recipient;
        if (this.sender == null) this.sender = this.recipient;
        return this;
    }

    public Transfer withAttachment(String message) {
        this.attachment = message;
        return this;
    }

    public Transfer withFee(long fee) {
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
            totalFee += sender.node.isSmart(assetId) ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

    @Override
    public Transaction successfully() throws IOException, TimeoutException {
        return sender.node.waitForTransaction(sender.node.wavesNode.transfer(
                sender.wavesAccount, recipient.address(), amount, assetId, calcFee(), "WAVES", attachment));
    }

    @Override
    public void butGotError() {

    }
}
