package lib.actions.mass;

import lib.Account;

public class Transfer {

    public static com.wavesplatform.wavesj.Transfer to(long amount, Account recipient) {
        return new com.wavesplatform.wavesj.Transfer(recipient.address(), amount);
    }

    public static com.wavesplatform.wavesj.Transfer to(long amount, String recipient) {
        return new com.wavesplatform.wavesj.Transfer(recipient, amount);
    }

}
