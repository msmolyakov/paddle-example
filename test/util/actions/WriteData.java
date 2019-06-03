package util.actions;

import com.wavesplatform.wavesj.DataEntry;
import com.wavesplatform.wavesj.Transaction;
import com.wavesplatform.wavesj.transactions.DataTransaction;
import util.Account;
import util.actions.data.Entry;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static util.Constants.MIN_FEE;

public class WriteData implements Action {

    private List<DataEntry<?>> data;
    private Account sender;
    private long fee;

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
    public long calcFee() {
        if (this.fee == 0) {
            byte[] bytes = new DataTransaction(sender.wavesAccount, data, calcFee(), System.currentTimeMillis()).getBodyBytes();
            return MIN_FEE + ((bytes.length - 1) / 1024) * MIN_FEE;
        } else {
            return this.fee;
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
