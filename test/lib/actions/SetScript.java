package lib.actions;

import com.wavesplatform.wavesj.Transaction;
import lib.Account;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import static lib.Constants.*;

public class SetScript implements Action {

    private Account sender;
    private String scriptFile;
    private long fee;

    public SetScript(String scriptFile) {
        this.scriptFile = scriptFile.isEmpty() ? null : scriptFile;
        this.fee = 0;
    }

    public SetScript from(Account sender) {
        this.sender = sender;
        return this;
    }

    public SetScript withFee(long fee) {
        this.fee = fee;
        return this;
    }

    @Override
    public long calcFee() throws IOException {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = MIN_FEE * 10;
            totalFee += sender.isSmart() ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

    @Override
    public Transaction successfully() throws IOException, TimeoutException {
        String compiledScript = sender.node.wavesNode.compileScript(new String(Files.readAllBytes(Paths.get(scriptFile))));

        return sender.node.waitForTransaction(sender.node.wavesNode.setScript(
                sender.wavesAccount, compiledScript, sender.node.wavesNode.getChainId(), calcFee()));
    }

    @Override
    public void butGotError() {

    }
}
