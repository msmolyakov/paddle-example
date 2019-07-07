package lib.actions;

import com.wavesplatform.wavesj.Transaction;
import com.wavesplatform.wavesj.matcher.Order.Type;
import com.wavesplatform.wavesj.matcher.OrderV2;
import lib.Account;
import lib.actions.exchange.Order;

import java.io.IOException;

import static lib.Constants.MIN_FEE;
import static lib.actions.exchange.OrderType.BUY;
import static lib.actions.exchange.OrderType.SELL;

public class Exchange implements Action {

    public Account sender;
    public Order buy;
    public Order sell;
    public long amount;
    public long price;
    public long buyMatcherFee;
    public long sellMatcherFee;
    public long fee;

    public Exchange(Order buy, Order sell) {
        this.buy = buy;
        this.sell = sell;
        this.amount = 0;
        this.price = 0;
        this.buyMatcherFee = 0;
        this.sellMatcherFee = 0;
        this.fee = 0;
    }

    public Exchange from(Account sender) {
        this.sender = sender;
        return this;
    }

    public Exchange amount(long amount) {
        this.amount = amount;
        return this;
    }

    public Exchange price(long price) {
        this.price = price;
        return this;
    }

    public Exchange buyMatcherFee(long value) {
        this.buyMatcherFee = value;
        return this;
    }

    public Exchange sellMatcherFee(long value) {
        this.sellMatcherFee = value;
        return this;
    }

    public Exchange withFee(long fee) {
        this.fee = fee;
        return this;
    }

    public long calcAmount() {
        return amount > 0 ? amount : Math.min(buy.amount, sell.amount);
    }

    public long calcPrice() {
        return price > 0 ? price : buy.price;
    }

    public long calcBuyMatcherFee() {
        return buyMatcherFee > 0 ? buyMatcherFee : MIN_FEE * 3; //TODO proportionally from amount/price and order fee
    }

    public long calcSellMatcherFee() {
        return sellMatcherFee > 0 ? sellMatcherFee : MIN_FEE * 3;
    }

    @Override
    public long calcFee() {
        if (this.fee > 0) {
            return this.fee;
        } else {
            return buy.calcMatcherFee();
        }
    }

    public Transaction successfully() throws IOException {
        long now = System.currentTimeMillis();
        long nowPlus29Days = now + 2505600000L;

        OrderV2 buyV2 = new OrderV2(buy.sender.wavesAccount, buy.matcher.wavesAccount,
                buy.type == BUY ? Type.BUY : Type.SELL, buy.pair, buy.amount, buy.price, 
                now, nowPlus29Days, buy.calcMatcherFee(), com.wavesplatform.wavesj.matcher.Order.V2);
        OrderV2 sellV2 = new OrderV2(sell.sender.wavesAccount, sell.matcher.wavesAccount,
                sell.type == SELL ? Type.SELL : Type.SELL, sell.pair, sell.amount, sell.price,
                now, nowPlus29Days, buy.calcMatcherFee(), com.wavesplatform.wavesj.matcher.Order.V2);

        return sender.node.waitForTransaction(sender.node.wavesNode.exchange(sender.wavesAccount,
                buyV2, sellV2, calcAmount(), calcPrice(), calcBuyMatcherFee(), calcSellMatcherFee(), calcFee()));
    }

}
