package lib.actions;

import com.wavesplatform.wavesj.Transaction;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface Action {

    //TODO static methods-constructors in each implementation: setScript(), invoke(), ...

    //TODO timestamp
    //TODO feeAssetId
    long calcFee(); //TODO add extra fees
    Transaction successfully() throws IOException, TimeoutException;
    void butGotError();
}
