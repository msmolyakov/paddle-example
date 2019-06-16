package lib.actions;

import com.wavesplatform.wavesj.Transaction;
import com.wavesplatform.wavesj.matcher.Order.Type;
import com.wavesplatform.wavesj.matcher.OrderV2;
import lib.Account;
import lib.actions.exchange.Order;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static lib.Constants.EXTRA_FEE;
import static lib.Constants.MIN_FEE;
import static lib.actions.exchange.OrderType.BUY;
import static lib.actions.exchange.OrderType.SELL;

public class Exchange implements Action {

    private Account sender; //TODO make all public in all actions
    private Order buy;
    private Order sell;
    private long amount;
    private long price;
    private long buyMatcherFee;
    private long sellMatcherFee;
    private long fee;

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

    public long calcBuyMatcherFee() {
        return buyMatcherFee > 0 ? buyMatcherFee : MIN_FEE * 3; //TODO proportionally from amount/price and order fee
    }

    public long calcSellMatcherFee() {
        return sellMatcherFee > 0 ? sellMatcherFee : MIN_FEE * 3;
    }

    @Override
    public long calcFee() throws IOException {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = buy.calcMatcherFee();
            totalFee += sell.sender.isSmart() ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

    @Override
    public Transaction successfully() throws IOException, TimeoutException {
        //TODO calc amount and price if 0
        long now = System.currentTimeMillis();
        long nowPlus29Days = now + 2505600000L;

        OrderV2 buyV2 = new OrderV2(buy.sender.wavesAccount, buy.matcher.wavesAccount,
                buy.type == BUY ? Type.BUY : Type.SELL, buy.pair, buy.amount, buy.price, 
                now, nowPlus29Days, buy.calcMatcherFee(), com.wavesplatform.wavesj.matcher.Order.V2);
        OrderV2 sellV2 = new OrderV2(sell.sender.wavesAccount, sell.matcher.wavesAccount,
                sell.type == SELL ? Type.SELL : Type.SELL, sell.pair, sell.amount, sell.price,
                now, nowPlus29Days, buy.calcMatcherFee(), com.wavesplatform.wavesj.matcher.Order.V2);

        return sender.node.waitForTransaction(sender.node.wavesNode.exchange(sender.wavesAccount,
                buyV2, sellV2, amount, price, calcBuyMatcherFee(), calcSellMatcherFee(), calcFee()));
    }

    @Override
    public void butGotError() {

    }
}
