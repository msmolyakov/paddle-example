package lib.actions;

import com.wavesplatform.wavesj.Transaction;

import java.io.IOException;

public interface Action {

    //TODO static methods-constructors in each implementation: setScript(), invoke(), ...

    //TODO timestamp
    //TODO feeAssetId
    long calcFee();
    Transaction successfully() throws IOException;
}
