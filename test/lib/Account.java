package lib;

import com.wavesplatform.wavesj.ByteString;
import com.wavesplatform.wavesj.DataEntry;
import com.wavesplatform.wavesj.PrivateKeyAccount;
import com.wavesplatform.wavesj.Transaction;
import com.wavesplatform.wavesj.matcher.Order;
import com.wavesplatform.wavesj.matcher.OrderV2;
import com.wavesplatform.wavesj.transactions.*;
import lib.actions.*;
import lib.exceptions.NodeError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.readAllLines;
import static lib.actions.exchange.OrderType.BUY;
import static lib.actions.exchange.OrderType.SELL;

public class Account {

    public PrivateKeyAccount wavesAccount;
    public final String seedText;
    public Node node;

    public Account(String seedText, Node worksWith, long initWavesBalance) {
        this.seedText = seedText;
        this.node = worksWith;
        wavesAccount = PrivateKeyAccount.fromSeed(this.seedText, 0, node.wavesNode.getChainId());

        if (initWavesBalance > 0) {
            try {
                this.node.rich.transfers(initWavesBalance).to(this).successfully();
            } catch (IOException e) {
                throw new NodeError(e);
            }
        }
    }

    public Account(String seedText, Node worksWith) {
        this(seedText, worksWith, 0);
    }

    public Account(Node worksWith, long initWavesBalance) {
        this(UUID.randomUUID().toString(), worksWith, initWavesBalance);
    }

    public Account(Node worksWith) {
        this(worksWith, 0);
    }

    public byte[] privateKey() {
        return this.wavesAccount.getPrivateKey();
    }

    public byte[] publicKey() {
        return this.wavesAccount.getPublicKey();
    }

    public String address() {
        return wavesAccount.getAddress();
    }

    public boolean isSmart() {
        return node.isSmart(this);
    }

    public long balance() {
        try {
            return node.wavesNode.getBalance(address());
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public long assetBalance(String assetId) {
        try {
            return node.wavesNode.getBalance(address(), assetId);
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public List<DataEntry> data() {
        try {
            return node.wavesNode.getData(address());
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public DataEntry data(String key) {
        try {
            return node.wavesNode.getDataByKey(address(), key);
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public String dataStr(String key) {
        return (String) data(key).getValue();
    }

    public long dataInt(String key) {
        return (long) data(key).getValue();
    }

    public boolean dataBool(String key) {
        return (boolean) data(key).getValue();
    }

    public byte[] dataBin(String key) {
        return ((ByteString) data(key).getValue()).getBytes();
    }

    public Issue issues(String name) {
        return new Issue(name).from(this);
    }

    public Transfer transfers(long amount) {
        return new Transfer(amount).from(this);
    }

    public Transfer transfers(long amount, String assetId) {
        return new Transfer(amount, assetId).from(this);
    }

    public Reissue reissues(String name) {
        return new Reissue(name).from(this);
    }

    public BurnTransaction burns(Consumer<Burn> burn) {
        Burn b = new Burn().from(this);
        burn.accept(b);

        try {
            return (BurnTransaction) node.waitForTransaction(node.wavesNode.burnAsset(
                    b.issuer.wavesAccount, node.wavesNode.getChainId(), b.assetId, b.quantity, b.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public ExchangeTransaction exchanges(Consumer<Exchange> exchange) {
        Exchange x = new Exchange().from(this);
        exchange.accept(x);

        long now = System.currentTimeMillis();
        long nowPlus29Days = now + 2505600000L;

        OrderV2 buyV2 = new OrderV2(x.buy.sender.wavesAccount, x.buy.matcher.wavesAccount,
                x.buy.type == BUY ? Order.Type.BUY : Order.Type.SELL, x.buy.pair, x.buy.amount, x.buy.price,
                now, nowPlus29Days, x.buy.calcMatcherFee(), com.wavesplatform.wavesj.matcher.Order.V2);
        OrderV2 sellV2 = new OrderV2(x.sell.sender.wavesAccount, x.sell.matcher.wavesAccount,
                x.sell.type == SELL ? Order.Type.SELL : Order.Type.BUY, x.sell.pair, x.sell.amount, x.sell.price,
                now, nowPlus29Days, x.buy.calcMatcherFee(), com.wavesplatform.wavesj.matcher.Order.V2);

        try {
            return (ExchangeTransaction) node.waitForTransaction(node.wavesNode.exchange(x.sender.wavesAccount,
                    buyV2, sellV2, x.calcAmount(), x.calcPrice(),
                    x.calcBuyMatcherFee(), x.calcSellMatcherFee(), x.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public LeaseTransaction leases(Consumer<Lease> lease) {
        Lease l = new Lease().from(this);
        lease.accept(l);

        try {
            return (LeaseTransaction) node.waitForTransaction(node.wavesNode.lease(
                    l.sender.wavesAccount, l.recipient, l.amount, l.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public LeaseCancelTransaction cancelsLease(Consumer<LeaseCancel> leaseCancel) {
        LeaseCancel l = new LeaseCancel().from(this);
        leaseCancel.accept(l);

        try {
            return (LeaseCancelTransaction) node.waitForTransaction(node.wavesNode.cancelLease(
                    l.sender.wavesAccount, node.wavesNode.getChainId(), l.leaseId, l.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public AliasTransaction createsAlias(Consumer<CreateAlias> createAlias) {
        CreateAlias a = new CreateAlias().from(this);
        createAlias.accept(a);

        try {
            return (AliasTransaction) node.waitForTransaction(node.wavesNode.alias(
                    a.sender.wavesAccount, node.wavesNode.getChainId(), a.alias, a.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public MassTransferTransaction massTransfers(Consumer<MassTransfer> massTransfer) {
        MassTransfer m = new MassTransfer().from(this);
        massTransfer.accept(m);

        try {
            return (MassTransferTransaction) node.waitForTransaction(node.wavesNode.massTransfer(
                    m.sender.wavesAccount, m.assetId, m.transfers, m.calcFee(), m.attachment));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public DataTransaction writes(Consumer<WriteData> writeData) {
        WriteData w = new WriteData().from(this);
        writeData.accept(w);

        try {
            return (DataTransaction) node.waitForTransaction(node.wavesNode.data(
                    w.sender.wavesAccount, w.data, w.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public SetScriptTransaction setsScript(Consumer<SetScript> setScript) {
        SetScript s = new SetScript().from(this);
        setScript.accept(s);

        try {
            String compiledScript;
            if (s.scriptFile == null)
                compiledScript = null;
            else {
                //TODO for Issue
                String script = String.join("\n", readAllLines(Paths.get(s.scriptFile)));
                compiledScript = node.compileScript(script);
            }

            return (SetScriptTransaction) node.waitForTransaction(node.wavesNode.setScript(
                    s.sender.wavesAccount, compiledScript, node.getChainId(), s.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public SponsorTransaction sponsors(Consumer<SponsorFee> sponsorFee) {
        SponsorFee s = new SponsorFee().from(this);
        sponsorFee.accept(s);

        try {
            return (SponsorTransaction) node.waitForTransaction(node.wavesNode.sponsorAsset(
                    s.sender.wavesAccount, s.assetId, s.minSponsoredAssetFee, s.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public SetAssetScriptTransaction setsAssetScript(Consumer<SetAssetScript> setAssetScript) {
        SetAssetScript s = new SetAssetScript().from(this);
        setAssetScript.accept(s);

        try {
            String compiledScript;
            if (s.scriptFile == null)
                compiledScript = null;
            else {
                String script = String.join("\n", readAllLines(Paths.get(s.scriptFile)));
                compiledScript = node.compileScript(script);
            }

            return (SetAssetScriptTransaction) node.waitForTransaction(node.wavesNode.setAssetScript(
                    s.sender.wavesAccount, node.getChainId(), s.assetId, compiledScript, s.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public InvokeScriptTransaction invokes(Consumer<InvokeScript> invokeScript) {
        InvokeScript i = new InvokeScript().from(this);
        invokeScript.accept(i);

        String dApp = i.dApp == null || i.dApp.isEmpty() ? i.sender.address() : i.dApp;

        try {
            return (InvokeScriptTransaction) node.waitForTransaction(node.wavesNode.invokeScript(
                    i.sender.wavesAccount, i.sender.node.wavesNode.getChainId(),
                    dApp, i.call, i.payments, i.calcFee(), i.feeAssetId));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }
}
