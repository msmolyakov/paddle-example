package lib.actions;

import com.wavesplatform.wavesj.DataEntry;
import com.wavesplatform.wavesj.Transaction;
import com.wavesplatform.wavesj.transactions.DataTransaction;
import lib.Account;
import lib.actions.data.Entry;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;

public class WriteData implements Action {

    public List<DataEntry<?>> data;
    public Account sender;
    public long fee;

    public WriteData(DataEntry<?>... data) {
        this.data = new LinkedList<>(Arrays.asList(data));
        this.fee = 0;
    }

    public WriteData from(Account sender) {
        this.sender = sender;
        return this;
    }

    public WriteData binary(String key, byte[] value) {
        data.add(Entry.binary(key, value));
        return this;
    }

    public WriteData bool(String key, boolean value) {
        data.add(Entry.bool(key, value));
        return this;
    }

    public WriteData integer(String key, long value) {
        data.add(Entry.integer(key, value));
        return this;
    }

    public WriteData string(String key, String value) {
        data.add(Entry.string(key, value));
        return this;
    }

    public WriteData withFee(long fee) {
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

            byte[] bytes = new DataTransaction(sender.wavesAccount, data, 1, System.currentTimeMillis()).getBodyBytes();
            totalFee += ((bytes.length - 1) / 1024) * MIN_FEE;

            return totalFee;
        }
    }

    @Override
    public Transaction successfully() throws IOException, TimeoutException {
        return sender.node.waitForTransaction(sender.node.wavesNode.data(sender.wavesAccount, data, calcFee()));
    }

    @Override
    public void butGotError() {

    }
}
