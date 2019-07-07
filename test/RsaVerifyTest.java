import lib.Account;
import lib.Node;
import lib.Version;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.Arrays;

import static lib.Node.runDockerNode;
import static lib.actions.invoke.Arg.arg;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RsaVerifyTest {

    private Node node;
    private Account alice;

    @BeforeEach
    void before() {
        node = runDockerNode(Version.TESTNET);

        alice = new Account("alice", node, 100_00000000L);
    }

    @Test
    void paymentIsPartOfDAppBalance() throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        char[] v32chars = new char[16382];
        Arrays.fill(v32chars, 'ё');
        String v32 = "b" + new String(v32chars) + "bb";

        alice.writes(d -> d.binary("1", v32.getBytes()));

        byte[] bytes = alice.dataBin("1");
        assertEquals(32767, bytes.length);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(bytes);
        baos.write(bytes);
        byte[] source = baos.toByteArray();

        alice.setsScript(s -> s.script("rsa-verify"));

        RSA rsa = new RSA();
//        byte[] none = rsa.sign("NONE", source);
        byte[] md5 = rsa.sign("MD5", source);
        byte[] sha1 = rsa.sign("SHA1", source);
        byte[] sha224 = rsa.sign("SHA224", source);
        byte[] sha256 = rsa.sign("SHA256", source);
        byte[] sha384 = rsa.sign("SHA384", source);
        byte[] sha512 = rsa.sign("SHA512", source);
        byte[] sha3_224 = rsa.sign("SHA3-224", source);
        byte[] sha3_256 = rsa.sign("SHA3-256", source);
        byte[] sha3_384 = rsa.sign("SHA3-384", source);
        byte[] sha3_512 = rsa.sign("SHA3-512", source);

        alice.invokes(i -> i.function("rsa", arg(rsa.keys.getPublic().getEncoded()),
                /*arg(none), */arg(md5), arg(sha1), arg(sha224), arg(sha256), arg(sha384), arg(sha512),
                arg(sha3_224), arg(sha3_256), arg(sha3_384), arg(sha3_512)));

        assertTrue(alice.dataBool("result"));
    }

    @AfterEach
    void after() {
        node.stopDockerNode();
    }

    class RSA {
        BouncyCastleProvider bcp;
        KeyPairGenerator gen;
        KeyPair keys;

        RSA() throws NoSuchAlgorithmException {
            this.bcp = new BouncyCastleProvider();
            
            this.gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048, new SecureRandom());
            
            this.keys = gen.generateKeyPair();
        }

        byte[] sign(String alg, byte[] source) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
            Signature sig = Signature.getInstance(alg + "withRSA", bcp);
            sig.initSign(keys.getPrivate());
            sig.update(source);
            return sig.sign();
        }
    }

}
