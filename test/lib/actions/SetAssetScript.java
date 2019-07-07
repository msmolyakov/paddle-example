package lib.actions;

import lib.Account;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.ONE_WAVES;

public class SetAssetScript implements Action {

    public Account sender;
    public String assetId;
    public String scriptFile;
    public long fee;

    public SetAssetScript() {
        this.fee = 0;
    }

    public SetAssetScript from(Account sender) {
        this.sender = sender;
        return this;
    }

    public SetAssetScript asset(String assetId) {
        this.assetId = assetId;
        return this;
    }

    public SetAssetScript script(String scriptFile) {
        this.scriptFile = scriptFile == null ? null :
                scriptFile.endsWith(".ride") ? scriptFile : scriptFile + ".ride";
        return this;
    }

    public SetAssetScript fee(long fee) {
        this.fee = fee;
        return this;
    }

    @Override
    public long calcFee() {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = ONE_WAVES;
            totalFee += sender.isSmart() ? EXTRA_FEE : 0;
            totalFee += sender.node.isSmart(assetId) ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

}
