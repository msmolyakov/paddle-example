package lib.actions;

import com.wavesplatform.wavesj.Transaction;
import lib.Account;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.ONE_WAVES;

public class SetAssetScript implements Action {

    public Account sender;
    public String scriptFile;
    public long fee;
    public String assetId;

    public SetAssetScript(String scriptFile, String assetId) {
        this.scriptFile = scriptFile.isEmpty() ? null : scriptFile;
        this.assetId = assetId;
        this.fee = 0;
    }

    public SetAssetScript from(Account sender) {
        this.sender = sender;
        return this;
    }

    public SetAssetScript withFee(long fee) {
        this.fee = fee;
        return this;
    }

    @Override
    public long calcFee() {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = ONE_WAVES;
            totalFee += sender.isSmart() ? EXTRA_FEE : 0;
            totalFee += sender.node.isSmart(assetId) ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

    public Transaction successfully() throws IOException {
        String compiledScript = sender.node.wavesNode.compileScript(new String(Files.readAllBytes(Paths.get(scriptFile))));

        return sender.node.waitForTransaction(sender.node.wavesNode.setAssetScript(
                sender.wavesAccount, sender.node.wavesNode.getChainId(), assetId, compiledScript, calcFee()));
    }

}
