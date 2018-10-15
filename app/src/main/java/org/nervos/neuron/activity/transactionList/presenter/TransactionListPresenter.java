package org.nervos.neuron.activity.transactionList.presenter;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.transactionList.model.TokenDescribeModel;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.EthErc20TokenInfoItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.service.CITATransactionService;
import org.nervos.neuron.service.HttpUrls;
import org.nervos.neuron.service.HttpService;
import org.nervos.neuron.service.TokenService;
import org.nervos.neuron.util.CurrencyUtil;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.db.DBChainUtil;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by BaojunCZ on 2018/10/9.
 */
public class TransactionListPresenter {

    private Activity activity;
    private TransactionListPresenterImpl listener;
    private TokenDescribeModel tokenDescribeModel;
    private TokenItem tokenItem;

    public TransactionListPresenter(Activity activity, TokenItem tokenItem, TransactionListPresenterImpl listener) {
        this.activity = activity;
        this.listener = listener;
        this.tokenItem = tokenItem;
    }

    public void setTokenLogo(ImageView tokenLogoImage) {
        if (isEthereum(tokenItem)) {
            if (!isNativeToken(tokenItem)) {
                if (TextUtils.isEmpty(tokenItem.avatar))
                    Glide.with(activity)
                            .load(Uri.parse(HttpUrls.TOKEN_LOGO.replace("@address", tokenItem.contractAddress)))
                            .into(tokenLogoImage);
                else
                    Glide.with(activity)
                            .load(Uri.parse(tokenItem.avatar))
                            .into(tokenLogoImage);
            }
        }
    }

    public void getTokenDescribe() {
        tokenDescribeModel = new TokenDescribeModel(new TokenDescribeModel.TokenDescribeModelImpl() {
            @Override
            public void success(EthErc20TokenInfoItem item) {
                listener.getTokenDescribe(item);
                listener.showTokenDescribe(true);
            }

            @Override
            public void error() {
            }
        });
        tokenDescribeModel.get(tokenItem.contractAddress);
    }

    public void getBalance() {
        if (tokenItem.balance != 0.0 && isEthereum(tokenItem)) {
            TokenService.getCurrency(tokenItem.symbol, CurrencyUtil.getCurrencyItem(activity).getName())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(String s) {
                            if (!TextUtils.isEmpty(s)) {
                                double price = Double.parseDouble(s.trim());
                                DecimalFormat df = new DecimalFormat("######0.00");
                                DecimalFormat formater = new DecimalFormat("0.####");
                                formater.setRoundingMode(RoundingMode.FLOOR);
                                listener.getCurrency(formater.format(tokenItem.balance)
                                        + tokenItem.symbol + "~"
                                        + CurrencyUtil.getCurrencyItem(activity).getSymbol()
                                        + Double.parseDouble(df.format(price * tokenItem.balance)));
                            } else
                                listener.getCurrency("0" + tokenItem.symbol);
                        }
                    });
        }
    }

    public void getTransactionList(String from) {
        Observable<List<TransactionItem>> observable;
        if (isNativeToken(tokenItem)) {
            observable = isEthereum(tokenItem) ?
                    HttpService.getETHTransactionList(activity) :
                    HttpService.getAppChainTransactionList(activity);
        } else {
            observable = isEthereum(tokenItem)?
                    HttpService.getETHERC20TransactionList(activity, tokenItem) :
                    HttpService.getAppChainERC20TransactionList(activity, tokenItem);
        }
        observable.subscribe(new Subscriber<List<TransactionItem>>() {
            @Override
            public void onCompleted() {
                listener.hideProgressBar();
                listener.setRefreshing(false);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                listener.hideProgressBar();
                listener.setRefreshing(false);
            }

            @Override
            public void onNext(List<TransactionItem> list) {
                if (list == null) {
                    Toast.makeText(activity, R.string.network_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                Collections.sort(list, (o1, o2) -> {
                    int ret = 0;
                    Long x = Long.valueOf(o1.timestamp);
                    Long y = Long.valueOf(o2.timestamp);
                    ret = -1 * x.compareTo(y);
                    return ret;
                });
                if (isEthereum(tokenItem)) {

                } else {
                    for (TransactionItem item : list) {
                        item.status = TextUtils.isEmpty(item.errorMessage) ? 1 : 0;
                    }
                    ChainItem chain = DBChainUtil.getChain(activity, tokenItem.chainId);
                    List<TransactionItem> allList = CITATransactionService.getTransactionList(activity, isNativeToken(tokenItem), chain.httpProvider, tokenItem.contractAddress, list, from);
                    listener.refreshList(allList);
                }
            }
        });
    }

    public boolean isNativeToken(TokenItem tokenItem) {
        return TextUtils.isEmpty(tokenItem.contractAddress);
    }

    public boolean isEthereum(TokenItem tokenItem) {
        return tokenItem.chainId < 0;
    }

    public interface TransactionListPresenterImpl {
        void hideProgressBar();

        void setRefreshing(boolean refreshing);

        void refreshList(List<TransactionItem> list);

        void getTokenDescribe(EthErc20TokenInfoItem item);

        void showTokenDescribe(boolean show);

        void getCurrency(String currency);
    }

}