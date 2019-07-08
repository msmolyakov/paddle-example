package lib;

import com.wavesplatform.wavesj.ByteString;
import com.wavesplatform.wavesj.DataEntry;
import com.wavesplatform.wavesj.PrivateKeyAccount;
import com.wavesplatform.wavesj.matcher.Order;
import com.wavesplatform.wavesj.matcher.OrderV2;
import com.wavesplatform.wavesj.transactions.*;
import lib.actions.*;
import lib.exceptions.NodeError;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

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
            this.node.rich.transfers(t -> t.amount(initWavesBalance).to(this));
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

    public IssueTransaction issues(Consumer<Issue> i) {
        Issue is = new Issue(this);
        i.accept(is);

        try {
            return (IssueTransaction) node.waitForTransaction(node.wavesNode.issueAsset(is.issuer.wavesAccount,
                    node.getChainId(), is.name, is.description, is.quantity, is.decimals,
                    is.isReissuable,is.script, is.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public TransferTransaction transfers(Consumer<Transfer> t) {
        Transfer tr = new Transfer(this);
        t.accept(tr);

        try {
            return (TransferTransaction) node.waitForTransaction(node.wavesNode.transfer(tr.sender.wavesAccount,
                    tr.recipient, tr.amount, tr.assetId, tr.calcFee(), "WAVES", tr.attachment));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public ReissueTransaction reissues(Consumer<Reissue> r) {
        Reissue ri = new Reissue(this);
        r.accept(ri);

        try {
            return (ReissueTransaction) node.waitForTransaction(node.wavesNode.reissueAsset(ri.issuer.wavesAccount,
                    node.getChainId(), ri.assetId, ri.quantity, ri.isReissuable, ri.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public BurnTransaction burns(Consumer<Burn> b) {
        Burn bu = new Burn(this);
        b.accept(bu);

        try {
            return (BurnTransaction) node.waitForTransaction(node.wavesNode.burnAsset(
                    bu.issuer.wavesAccount, node.getChainId(), bu.assetId, bu.quantity, bu.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public ExchangeTransaction exchanges(Consumer<Exchange> e) {
        Exchange ex = new Exchange(this);
        e.accept(ex);

        long now = System.currentTimeMillis();
        long nowPlus29Days = now + 2505600000L;

        OrderV2 buyV2 = new OrderV2(ex.buy.sender.wavesAccount, ex.buy.matcher.wavesAccount,
                ex.buy.type == BUY ? Order.Type.BUY : Order.Type.SELL, ex.buy.pair, ex.buy.amount, ex.buy.price,
                now, nowPlus29Days, ex.buy.calcMatcherFee(), com.wavesplatform.wavesj.matcher.Order.V2);
        OrderV2 sellV2 = new OrderV2(ex.sell.sender.wavesAccount, ex.sell.matcher.wavesAccount,
                ex.sell.type == SELL ? Order.Type.SELL : Order.Type.BUY, ex.sell.pair, ex.sell.amount, ex.sell.price,
                now, nowPlus29Days, ex.buy.calcMatcherFee(), com.wavesplatform.wavesj.matcher.Order.V2);

        try {
            return (ExchangeTransaction) node.waitForTransaction(node.wavesNode.exchange(ex.sender.wavesAccount,
                    buyV2, sellV2, ex.calcAmount(), ex.calcPrice(),
                    ex.calcBuyMatcherFee(), ex.calcSellMatcherFee(), ex.calcFee()));
        } catch (IOException ioe) {
            throw new NodeError(ioe);
        }
    }

    public LeaseTransaction leases(Consumer<Lease> lease) {
        Lease l = new Lease(this);
        lease.accept(l);

        try {
            return (LeaseTransaction) node.waitForTransaction(node.wavesNode.lease(
                    l.sender.wavesAccount, l.recipient, l.amount, l.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public LeaseCancelTransaction cancelsLease(Consumer<LeaseCancel> l) {
        LeaseCancel lc = new LeaseCancel(this);
        l.accept(lc);

        try {
            return (LeaseCancelTransaction) node.waitForTransaction(node.wavesNode.cancelLease(
                    lc.sender.wavesAccount, node.wavesNode.getChainId(), lc.leaseId, lc.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public AliasTransaction createsAlias(Consumer<CreateAlias> a) {
        CreateAlias ca = new CreateAlias(this);
        a.accept(ca);

        try {
            return (AliasTransaction) node.waitForTransaction(node.wavesNode.alias(
                    ca.sender.wavesAccount, node.wavesNode.getChainId(), ca.alias, ca.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public MassTransferTransaction massTransfers(Consumer<MassTransfer> m) {
        MassTransfer mt = new MassTransfer(this);
        m.accept(mt);

        try {
            List<com.wavesplatform.wavesj.Transfer> transfers = new LinkedList<>();
            mt.transfers.forEach(t -> transfers.add(new com.wavesplatform.wavesj.Transfer(t.recipient, t.amount)));
            return (MassTransferTransaction) node.waitForTransaction(node.wavesNode.massTransfer(
                    mt.sender.wavesAccount, mt.assetId, transfers, mt.calcFee(), mt.attachment));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public DataTransaction writes(Consumer<WriteData> d) {
        WriteData wd = new WriteData(this);
        d.accept(wd);

        try {
            return (DataTransaction) node.waitForTransaction(node.wavesNode.data(
                    wd.sender.wavesAccount, wd.data, wd.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public SetScriptTransaction setsScript(Consumer<SetScript> s) {
        SetScript ss = new SetScript(this);
        s.accept(ss);

        try {
            String compiledScript;
            if (ss.scriptFile == null)
                compiledScript = null;
            else {
                //TODO for Issue
                String script = String.join("\n", readAllLines(Paths.get(ss.scriptFile)));
                compiledScript = node.compileScript(script);
            }

            return (SetScriptTransaction) node.waitForTransaction(node.wavesNode.setScript(
                    ss.sender.wavesAccount, compiledScript, node.getChainId(), ss.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public SponsorTransaction sponsors(Consumer<SponsorFee> s) {
        SponsorFee sf = new SponsorFee(this);
        s.accept(sf);

        try {
            return (SponsorTransaction) node.waitForTransaction(node.wavesNode.sponsorAsset(
                    sf.sender.wavesAccount, sf.assetId, sf.minSponsoredAssetFee, sf.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public SetAssetScriptTransaction setsAssetScript(Consumer<SetAssetScript> s) {
        SetAssetScript sa = new SetAssetScript(this);
        s.accept(sa);

        try {
            String compiledScript;
            if (sa.scriptFile == null)
                compiledScript = null;
            else {
                String script = String.join("\n", readAllLines(Paths.get(sa.scriptFile)));
                compiledScript = node.compileScript(script);
            }

            return (SetAssetScriptTransaction) node.waitForTransaction(node.wavesNode.setAssetScript(
                    sa.sender.wavesAccount, node.getChainId(), sa.assetId, compiledScript, sa.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public InvokeScriptTransaction invokes(Consumer<InvokeScript> i) {
        InvokeScript is = new InvokeScript(this);
        i.accept(is);

        String dApp = is.dApp == null || is.dApp.isEmpty() ? is.sender.address() : is.dApp;

        try {
            return (InvokeScriptTransaction) node.waitForTransaction(node.wavesNode.invokeScript(
                    is.sender.wavesAccount, is.sender.node.wavesNode.getChainId(),
                    dApp, is.call, is.payments, is.calcFee(), is.feeAssetId));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }
}
