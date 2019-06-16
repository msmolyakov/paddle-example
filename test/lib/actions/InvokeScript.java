package lib.actions;

import com.wavesplatform.wavesj.Transaction;
import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction;
import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction.Payment;
import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction.FunctionCall;
import lib.Account;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;

public class InvokeScript implements Action {

    private Account sender;
    private String dApp;
    private FunctionCall call;
    private List<Payment> payments;
    private long fee;
    private String feeAssetId;

    public InvokeScript(String addressOrAlias) {
        this.dApp = addressOrAlias.isEmpty() ? null : addressOrAlias;
        this.call = null;
        this.payments = new ArrayList<>();
        this.fee = 0;
        this.feeAssetId = "WAVES";
    }

    public InvokeScript(Account dApp) {
        this(dApp.address());
    }

    public InvokeScript() {
        this("");
    }

    public InvokeScript from(Account sender) {
        this.sender = sender;
        if (this.dApp == null) this.dApp = this.sender.address();
        return this;
    }

    public InvokeScript function(String name, InvokeScriptTransaction.FunctionalArg... args) {
        if (this.call == null) {
            this.call = new FunctionCall(name);
        } else {
            this.call.setName(name);
            //TODO clear args
        }
        Arrays.stream(args).forEach(arg -> this.call.addArg(arg));
        return this;
    }

    public InvokeScript defaultFunction() {
        this.call = null;
        return this;
    }

    public InvokeScript withPayment(long amount, String assetId) {
        this.payments.add(new Payment(amount, assetId));
        return this;
    }

    public InvokeScript withWavesPayment(long amount) {
        return withPayment(amount, null);
    }

    public InvokeScript withFee(long fee) {
        this.fee = fee;
        return this;
    }

    /**
     * Важно! Не учитывает переводы смарт ассетов через TransferSet.
     * В таком случае комиссию можно указывать самостоятельно: `invoke.withFee(invoke.calcFee() + EXTRA_FEE)`
     * @return
     * @throws IOException
     */
    @Override
    public long calcFee() throws IOException {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = MIN_FEE + EXTRA_FEE;
            totalFee += sender.isSmart() ? EXTRA_FEE : 0;
            for (Payment pmt : payments)
                totalFee += sender.node.isSmart(pmt.getAssetId()) ? EXTRA_FEE : 0;
            return totalFee;
        }
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
