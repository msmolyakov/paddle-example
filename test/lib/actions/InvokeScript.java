package lib.actions;

import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction;
import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction.FunctionCall;
import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction.Payment;
import lib.Account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;

public class InvokeScript implements Action {

    public Account sender;
    public String dApp;
    public FunctionCall call;
    public List<Payment> payments;
    public long fee;
    public String feeAssetId;

    public InvokeScript(String addressOrAlias) {
        this.dApp = addressOrAlias;
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

    public InvokeScript dApp(String addressOrAlias) {
        this.dApp = addressOrAlias;
        return this;
    }

    public InvokeScript dApp(Account account) {
        this.dApp = account.address();
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

    public InvokeScript payment(long amount, String assetId) {
        this.payments.add(new Payment(amount, assetId));
        return this;
    }

    public InvokeScript wavesPayment(long amount) {
        return payment(amount, null);
    }

    public InvokeScript fee(long fee) {
        this.fee = fee;
        return this;
    }

    /**
     * Важно! Не учитывает переводы смарт ассетов через TransferSet.
     * В таком случае комиссию можно указывать самостоятельно: `invoke.fee(invoke.calcFee() + EXTRA_FEE)`
     */
    @Override
    public long calcFee() {
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

}
