package util;

import com.wavesplatform.wavesj.PrivateKeyAccount;

public class Account {

    private PrivateKeyAccount acc;
    private PrivateKeyAccount wavesAccount;
    private String seedText;
    private String seed;
    private String privateKey;
    private String publicKey;
    private String address;

    public Account(String seedText, Node worksWith) {
        this.seedText = seedText;
        acc = PrivateKeyAccount.fromSeed(this.seedText, 0, worksWith.node.getChainId());
    }
}
