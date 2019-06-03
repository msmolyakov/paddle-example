package util.actions;

import com.wavesplatform.wavesj.Transaction;
import com.wavesplatform.wavesj.Transfer;
import util.Account;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static util.Constants.MIN_FEE;

public class MassTransfer implements Action {

    private Account sender;
    private String assetId;
    private List<Transfer> transfers;
    private String attachment;
    private long fee;

    //TODO может быть без получателей?
    public MassTransfer(String assetId) {
        this.assetId = assetId;
        this.transfers = new LinkedList<>();
        this.attachment = "";
        this.fee = 0;
    }

    public MassTransfer() {
        this(null);
    }

    public MassTransfer from(Account sender) {
        this.sender = sender;
        return this;
    }

    public MassTransfer recipients(Transfer... transfers) {
        this.transfers.addAll(Arrays.asList(transfers));
        return this;
    }

    public MassTransfer withAttachment(String message) {
        this.attachment = message;
        return this;
    }

    public MassTransfer withFee(long fee) {
        this.fee = fee;
        return this;
    }

    @Override
    public long calcFee() {
        if (this.fee == 0) {
            return MIN_FEE + (transfers.size() + 1) / 2;
        } else {
            return this.fee;
        }
    }

    @Override
    public Transaction successfully() throws IOException, TimeoutException {
        return sender.node.waitForTransaction(sender.node.wavesNode.massTransfer(sender.wavesAccount, assetId, transfers, calcFee(), attachment));
    }

    @Override
    public void butGotError() {

    }
}
