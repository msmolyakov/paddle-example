package lib.actions;

import com.wavesplatform.wavesj.Transaction;
import lib.Account;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;

public class SetScript implements Action {

    public Account sender;
    public String scriptFile;
    public long fee;

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
    public long calcFee() {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = MIN_FEE * 10;
            totalFee += sender.isSmart() ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

    @Override
    public Transaction successfully() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(scriptFile)); //TODO Issue, SetAssetScript
        String script = String.join("\n", lines);
        String compiledScript = sender.node.wavesNode.compileScript(script);

        return sender.node.waitForTransaction(sender.node.wavesNode.setScript(
                sender.wavesAccount, compiledScript, sender.node.wavesNode.getChainId(), calcFee()));
    }

}
