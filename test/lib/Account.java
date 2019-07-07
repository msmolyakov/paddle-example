package lib;

import com.wavesplatform.wavesj.ByteString;
import com.wavesplatform.wavesj.DataEntry;
import com.wavesplatform.wavesj.PrivateKeyAccount;
import com.wavesplatform.wavesj.Transaction;
import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction;
import com.wavesplatform.wavesj.transactions.SetAssetScriptTransaction;
import com.wavesplatform.wavesj.transactions.SetScriptTransaction;
import com.wavesplatform.wavesj.transactions.SponsorTransaction;
import lib.actions.*;
import lib.actions.exchange.Order;
import lib.exceptions.NodeError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.readAllLines;

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

    public Burn burns(String assetId) {
        return new Burn(assetId).from(this);
    }

    public Exchange exchanges(Order buy, Order sell) {
        return new Exchange(buy, sell).from(this);
    }

    public Lease leases(long amount) {
        return new Lease(amount).from(this);
    }

    public LeaseCancel cancelsLease(String leaseId) {
        return new LeaseCancel(leaseId).from(this);
    }

    public CreateAlias createsAlias(String alias) {
        return new CreateAlias(alias).from(this);
    }

    public MassTransfer massTransfers() {
        return new MassTransfer().from(this);
    }

    public WriteData writes(DataEntry<?>... data) {
        return new WriteData(data).from(this);
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
