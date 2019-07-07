package lib.actions;

import lib.Account;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;

public class Transfer implements Action {

    public Account sender;
    public String recipient;
    public long amount;
    public String assetId;
    public String attachment;
    public long fee;

    public Transfer() {
        this.attachment = "";
        this.fee = 0;
    }

    public Transfer from(Account sender) {
        this.sender = sender;
        if (this.recipient == null) this.recipient = this.sender.address(); //TODO а стоит ли?
        return this;
    }

    public Transfer to(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public Transfer to(Account account) {
        this.recipient = account.address();
        return this;
    }

    public Transfer amount(long amount) {
        this.amount = amount;
        return this;
    }

    public Transfer asset(String assetId) {
        this.assetId = assetId;
        return this;
    }

    public Transfer attachment(String message) {
        this.attachment = message;
        return this;
    }

    public Transfer fee(long fee) {
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

}
