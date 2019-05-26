package util.actions;

import com.wavesplatform.wavesj.Transaction;
import util.Account;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import static util.Constants.MIN_FEE;

public class SetScript implements Action {

    private Account sender;
    private String scriptFile;
    private long fee;

    public SetScript(String scriptFile) {
        this.scriptFile = scriptFile;
        this.fee = MIN_FEE * 10;
    }

    public SetScript from(Account sender) {
        this.sender = sender;
        return this;
    }

    @Override
    public long calcFee() {
        //TODO add extra fees
        return this.fee;
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
