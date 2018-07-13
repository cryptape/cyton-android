package org.nervos.neuron.item;

import android.text.TextUtils;

import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class TransactionRequest {

    private static final BigInteger TOKENDecimal = new BigInteger("1000000000000000000");
    private static final BigInteger BIG_10000 = BigInteger.valueOf(10000);

    public String from;
    public String to;
    public long nonce;
    private long quota = -1;
    public String data;
    private String value;
    public long chainId;
    public int version;
    private String gasLimit;
    private String gasPrice;

    public double getValue() {
        if (Numeric.containsHexPrefix(value)) {
            return Numeric.toBigInt(value).multiply(BIG_10000)
                    .divide(TOKENDecimal).doubleValue()/10000.0;
        } else {
            return new BigInteger(value).multiply(BIG_10000)
                    .divide(TOKENDecimal).doubleValue()/10000.0;
        }
    }

    public double getQuota() {
        return BigInteger.valueOf(quota).multiply(BIG_10000)
                .divide(TOKENDecimal).doubleValue()/10000.0;
    }

    public double getGas() {
        BigInteger limit = Numeric.containsHexPrefix(gasLimit)?
                Numeric.toBigInt(gasLimit):new BigInteger(gasLimit);
        BigInteger price = Numeric.containsHexPrefix(gasPrice)?
                Numeric.toBigInt(gasPrice):new BigInteger(gasPrice);
        return limit.multiply(price).multiply(BIG_10000).divide(TOKENDecimal).doubleValue()/10000.0;
    }

    public String getGasLimit() {
        return gasLimit;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public boolean isEthereum() {
        return !TextUtils.isEmpty(gasPrice);
    }
}
