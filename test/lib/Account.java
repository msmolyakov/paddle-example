package lib;

import com.wavesplatform.wavesj.ByteString;
import com.wavesplatform.wavesj.DataEntry;
import com.wavesplatform.wavesj.PrivateKeyAccount;
import lib.actions.*;
import lib.actions.exchange.Order;
import lib.exceptions.NodeError;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

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

    public SetScript setsScript(String scriptFile) {
        return new SetScript(scriptFile.endsWith(".ride") ? scriptFile : scriptFile + ".ride").from(this);
    }

    public SponsorFee sponsors(String assetId) {
        return new SponsorFee(assetId).from(this);
    }

    public SetAssetScript setsAssetScript(String scriptFile, String assetId) {
        return new SetAssetScript(scriptFile.endsWith(".ride") ? scriptFile : scriptFile + ".ride", assetId).from(this);
    }

    public InvokeScript invokes(String addressOrAlias) {
        return new InvokeScript(addressOrAlias).from(this);
    }

    public InvokeScript invokes(Account dApp) {
        return new InvokeScript(dApp.address()).from(this);
    }

    public InvokeScript invokes() {
        return new InvokeScript().from(this);
    }
}
