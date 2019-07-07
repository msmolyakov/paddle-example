package lib.actions;

import com.wavesplatform.wavesj.Transfer;
import lib.Account;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;

public class MassTransfer implements Action {

    public Account sender;
    public String assetId;
    public List<Transfer> transfers;
    public String attachment;
    public long fee;

    //TODO может быть без получателей?
    public MassTransfer() {
        this.transfers = new LinkedList<>();
        this.attachment = "";
        this.fee = 0;
    }

    public MassTransfer from(Account sender) {
        this.sender = sender;
        return this;
    }

    public MassTransfer asset(String assetId) {
        this.assetId = assetId;
        return this;
    }

    public MassTransfer recipients(Transfer... transfers) {
        this.transfers.addAll(Arrays.asList(transfers));
        return this;
    }

    public MassTransfer attachment(String message) {
        this.attachment = message;
        return this;
    }

    public MassTransfer fee(long fee) {
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
            totalFee += (transfers.size() + 1) / 2;
            return totalFee;
        }
    }

}
