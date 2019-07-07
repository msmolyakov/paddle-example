package lib.actions;

import com.wavesplatform.wavesj.Transaction;
import lib.Account;

import java.io.IOException;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;

public class Transfer implements Action {

    public long amount;
    public String assetId;
    public Account sender;
    public Account recipient;
    public String attachment;
    public long fee;

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
    public long calcFee() {
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
    public Transaction successfully() throws IOException {
        return sender.node.waitForTransaction(sender.node.wavesNode.transfer(
                sender.wavesAccount, recipient.address(), amount, assetId, calcFee(), "WAVES", attachment));
    }

}
