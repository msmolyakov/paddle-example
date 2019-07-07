package lib.actions.exchange;

import com.wavesplatform.wavesj.AssetPair;
import lib.Account;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;
import static lib.actions.exchange.OrderType.BUY;
import static lib.actions.exchange.OrderType.SELL;

public class Order {

    public Account sender;
    public Account matcher;
    public OrderType type;
    public AssetPair pair;
    public long amount = 0;
    public long price = 0;
    public long matcherFee = 0;

    public static Order buy() {
        Order buy = new Order().type(BUY);
        return buy;
    }

    public static Order sell() {
        Order sell = new Order().type(SELL);
        return sell;
    }

    public Order from(Account sender) {
        this.sender = sender;
        return this;
    }

    public Order matcher(Account matcher) {
        this.matcher = matcher;
        return this;
    }

    public Order type(OrderType type) {
        this.type = type;
        return this;
    }

    public Order pair(String amountAsset, String priceAsset) {
        this.pair = new AssetPair(amountAsset, priceAsset);
        return this;
    }

    public Order amount(long value) {
        this.amount = value;
        return this;
    }

    public Order price(long value) {
        this.price = value;
        return this;
    }

    public Order matcherFee(long fee) {
        this.matcherFee = fee;
        return this;
    }

    public long calcMatcherFee() {
        if (matcherFee > 0)
            return matcherFee;
        else {
            long fee = MIN_FEE * 3;
            //extra fee isn't required for sender script
            fee += matcher.isSmart() ? EXTRA_FEE : 0; //TODO проверять, требует ли матчер за это extra fee
            fee += sender.node.isSmart(pair.getAmountAsset()) ? EXTRA_FEE : 0;
            fee += sender.node.isSmart(pair.getPriceAsset()) ? EXTRA_FEE : 0;
            return fee;
        }
    }

}
