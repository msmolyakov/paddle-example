package util.actions;

import com.wavesplatform.wavesj.Transaction;
import util.Account;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static util.Constants.ONE_WAVES;

public class Issue implements Action {

    private Account issuer;
    private String name;
    private String description;
    private long quantity;
    private byte decimals;
    private boolean isReissuable;
    private String script;
    private long fee;

    public Issue(String name) {
        this.name = name;
        this.description = "";
        this.quantity = 100000000;
        this.decimals = 2;
        this.isReissuable = false;
        this.script = null;
        this.fee = ONE_WAVES;
    }

    public Issue from(Account issuer) {
        this.issuer = issuer;
        return this;
    }

    public Issue withDecimals(int value) {
        this.decimals = (byte) value;
        return this;
    }

    public Issue reissuable() {
        this.isReissuable = true;
        return this;
    }

    @Override
    public long calcFee() {
        //TODO add extra fees
        return this.fee;
    }

    @Override
    public Transaction successfully() throws IOException, TimeoutException {
        return issuer.node.waitForTransaction(issuer.node.wavesNode.issueAsset(issuer.wavesAccount,
                issuer.node.wavesNode.getChainId(), name, description, quantity, decimals, isReissuable, script, calcFee()));
    }

    @Override
    public void butGotError() {

    }
}
