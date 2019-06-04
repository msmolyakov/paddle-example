package lib;

import com.wavesplatform.wavesj.ByteString;
import com.wavesplatform.wavesj.DataEntry;
import com.wavesplatform.wavesj.PrivateKeyAccount;
import lib.actions.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Account {

    public PrivateKeyAccount wavesAccount;
    private String seedText;
    public Node node;

    public Account(String seedText, Node worksWith) {
        this.seedText = seedText;
        this.node = worksWith;
        wavesAccount = PrivateKeyAccount.fromSeed(this.seedText, 0, node.wavesNode.getChainId());
    }

    public Account(String seedText, Node worksWith, long initWavesBalance) throws IOException, TimeoutException {
        this.seedText = seedText;
        this.node = worksWith;
        wavesAccount = PrivateKeyAccount.fromSeed(this.seedText, 0, node.wavesNode.getChainId());
        this.node.rich.transfers(initWavesBalance).to(this).successfully();
    }

    public List<DataEntry> data() throws IOException {
        return node.wavesNode.getData(wavesAccount.getAddress());
    }

    public DataEntry data(String key) throws IOException {
        return node.wavesNode.getDataByKey(wavesAccount.getAddress(), key);
    }

    public String dataStr(String key) throws IOException {
        return (String) node.wavesNode.getDataByKey(wavesAccount.getAddress(), key).getValue();
    }

    public long dataInt(String key) throws IOException {
        return (long) node.wavesNode.getDataByKey(wavesAccount.getAddress(), key).getValue();
    }

    public boolean dataBool(String key) throws IOException {
        return (boolean) node.wavesNode.getDataByKey(wavesAccount.getAddress(), key).getValue();
    }

    public byte[] dataBin(String key) throws IOException {
        return ((ByteString) node.wavesNode.getDataByKey(wavesAccount.getAddress(), key).getValue()).getBytes();
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
        return new SetScript(scriptFile).from(this);
    }

    public SponsorFee sponsors(String assetId) {
        return new SponsorFee(assetId).from(this);
    }

    public SetScript setsAssetScript(String scriptFile) {
        return new SetScript(scriptFile).from(this);
    }

    public InvokeScript invokes() {
        return new InvokeScript().from(this);
    }
}
