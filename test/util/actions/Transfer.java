package util.actions;

import com.wavesplatform.wavesj.Transaction;
import util.Account;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static util.Constants.MIN_FEE;

public class Transfer implements Action {

    private long amount;
    private Account sender;
    private Account recipient;
    private long fee;

    public Transfer(long amount) {
        this.amount = amount;
        this.fee = MIN_FEE;
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

    @Override
    public long calcFee() {
        //TODO add extra fees
        return this.fee;
    }

    @Override
    public Transaction successfully() throws IOException, TimeoutException {
        return sender.node.waitForTransaction(sender.node.wavesNode.transfer(
                sender.wavesAccount, recipient.wavesAccount.getAddress(), amount, calcFee(), ""));
    }

    @Override
    public void butGotError() {

    }
}
