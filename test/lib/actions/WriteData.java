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

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;

public class WriteData implements Action {

    public Account sender;
    public List<DataEntry<?>> data;
    public long fee;

    public WriteData() {
        this.data = new LinkedList<>();
        this.fee = 0;
    }

    public WriteData from(Account sender) {
        this.sender = sender;
        return this;
    }

    public WriteData data(DataEntry<?>... data) {
        this.data = new LinkedList<>(Arrays.asList(data));
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

}
