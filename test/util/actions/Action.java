package util.actions;

import com.wavesplatform.wavesj.Transaction;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface Action {

    long calcFee();
    Transaction successfully() throws IOException, TimeoutException;
    void butGotError();

}
