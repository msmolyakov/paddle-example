package lib.actions;

import lib.Account;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;

public class SetScript implements Action {

    public Account sender;
    public String scriptFile;
    public long fee;

    public SetScript(Account from) {
        this.sender = from;
        this.fee = 0;
    }

    public static SetScript setScript(Account from) {
        return new SetScript(from);
    }

    public SetScript script(String scriptFile) {
        this.scriptFile = scriptFile == null ? null :
                scriptFile.endsWith(".ride") ? scriptFile : scriptFile + ".ride";
        return this;
    }

    public SetScript fee(long fee) {
        this.fee = fee;
        return this;
    }

    @Override
    public long calcFee() {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = MIN_FEE * 10;
            totalFee += sender.isSmart() ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

}
