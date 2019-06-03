package util.actions;

import com.wavesplatform.wavesj.Transaction;
import util.Account;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static util.Constants.MIN_FEE;

public class CreateAlias implements Action {

    private String alias;
    private Account sender;
    private long fee;

    public CreateAlias(String alias) {
        this.alias = alias;
        this.fee = 0;
    }

    public CreateAlias from(Account sender) {
        this.sender = sender;
        return this;
    }

    public CreateAlias withFee(long fee) {
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
        return sender.node.waitForTransaction(sender.node.wavesNode.alias(
                sender.wavesAccount, sender.node.wavesNode.getChainId(), alias, calcFee()));
    }

    @Override
    public void butGotError() {

    }
}
