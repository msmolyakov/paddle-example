package util.actions;

import com.wavesplatform.wavesj.Transaction;
import util.Account;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import static util.Constants.ONE_WAVES;

public class SetAssetScript implements Action {

    private Account sender;
    private String scriptFile;
    private long fee;
    private String assetId;

    public SetAssetScript(String scriptFile) {
        this.scriptFile = scriptFile.isEmpty() ? null : scriptFile;
        this.assetId = null;
        this.fee = 0;
    }

    public SetAssetScript from(Account sender) {
        this.sender = sender;
        return this;
    }

    public SetAssetScript to(String assetId) {
        this.assetId = assetId;
        return this;
    }

    public SetAssetScript withFee(long fee) {
        this.fee = fee;
        return this;
    }

    @Override
    public long calcFee() {
        if (this.fee == 0) {
            return ONE_WAVES;
        } else {
            return this.fee;
        }
    }

    @Override
    public Transaction successfully() throws IOException, TimeoutException {
        String compiledScript = sender.node.wavesNode.compileScript(new String(Files.readAllBytes(Paths.get(scriptFile))));

        return sender.node.waitForTransaction(sender.node.wavesNode.setAssetScript(
                sender.wavesAccount, sender.node.wavesNode.getChainId(), assetId, compiledScript, calcFee()));
    }

    @Override
    public void butGotError() {

    }
}
