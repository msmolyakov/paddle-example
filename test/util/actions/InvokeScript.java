package util.actions;

import com.wavesplatform.wavesj.Transaction;
import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction;
import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction.Payment;
import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction.FunctionCall;
import util.Account;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static util.Constants.EXTRA_FEE;
import static util.Constants.MIN_FEE;

public class InvokeScript implements Action {

    private Account sender;
    private String dApp;
    private FunctionCall call;
    private List<Payment> payments;
    private long fee;
    private String feeAssetId;

    public InvokeScript() {
        this.call = null;
        this.payments = new ArrayList<>();
        this.fee = MIN_FEE + EXTRA_FEE;
        this.feeAssetId = "WAVES";
    }

    public InvokeScript from(Account sender) {
        this.sender = sender;
        if (this.dApp == null) this.dApp = this.sender.wavesAccount.getAddress();
        return this;
    }

    public InvokeScript dApp(String addressOrAlias) {
        this.dApp = addressOrAlias;
        return this;
    }

    public InvokeScript dApp(Account dApp) {
        this.dApp(dApp.wavesAccount.getAddress());
        return this;
    }

    public InvokeScript function(String name, InvokeScriptTransaction.FunctionalArg... args) {
        if (this.call == null) {
            this.call = new FunctionCall(name);
        } else {
            this.call.setName(name);
        }
        Arrays.stream(args).forEach(arg -> this.call.addArg(arg));
        return this;
    }

    public InvokeScript defaultFunction() {
        this.call = null;
        return this;
    }

    @Override
    public long calcFee() {
        //TODO add extra fees
        return this.fee;
    }

    @Override
    public Transaction successfully() throws IOException, TimeoutException {
        return sender.node.waitForTransaction(sender.node.wavesNode.invokeScript(
                sender.wavesAccount, sender.node.wavesNode.getChainId(), this.dApp,
                this.call, this.payments, calcFee(), this.feeAssetId));
    }

    @Override
    public void butGotError() {

    }
}
