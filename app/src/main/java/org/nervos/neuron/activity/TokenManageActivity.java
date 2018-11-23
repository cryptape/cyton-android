package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.greenrobot.eventbus.EventBus;
import org.nervos.neuron.R;
import org.nervos.neuron.event.AddTokenRefreshEvent;
import org.nervos.neuron.item.TokenEntity;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.FileUtil;
import org.nervos.neuron.util.TokenLogoUtil;
import org.nervos.neuron.util.db.DBTokenUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.view.TitleBar;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by duanyytop on 2018/5/17
 */
public class TokenManageActivity extends BaseActivity {

    private static final int REQUEST_CODE = 0x01;
    public static final int RESULT_CODE = 0x01;

    private TitleBar titleBar;
    private RecyclerView recyclerView;
    private List<TokenEntity> tokenList = new ArrayList<>();
    private TokenAdapter adapter = new TokenAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_manage);

        initView();
        initData();
    }

    private void initData() {
        String tokens = FileUtil.loadRawFile(mActivity, R.raw.tokens_eth);
        Type type = new TypeToken<List<TokenEntity>>() {
        }.getType();
        tokenList = new Gson().fromJson(tokens, type);
        for (TokenEntity entity : tokenList) {
            entity.chainId = ConstantUtil.ETHEREUM_MAIN_ID;
        }
        addCustomToken();
        adapter.notifyDataSetChanged();
    }

    private void addCustomToken() {
        List<TokenItem> customList = DBTokenUtil.getAllTokens(mActivity);
        if (customList != null && customList.size() > 0) {
            for (int i = 0; i < customList.size(); i++) {
                tokenList.add(i, new TokenEntity(customList.get(i)));       // add front of list
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void initView() {
        titleBar = findViewById(R.id.title);
        titleBar.setOnRightClickListener(new TitleBar.OnRightClickListener() {
            @Override
            public void onRightClick() {
                startActivityForResult(new Intent(mActivity, AddTokenActivity.class), REQUEST_CODE);
            }
        });
        titleBar.setOnLeftClickListener(new TitleBar.OnLeftClickListener() {
            @Override
            public void onLeftClick() {
                postTokenRefreshEvent();
                finish();
            }
        });
        recyclerView = findViewById(R.id.token_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setAdapter(adapter);
    }

    private void postTokenRefreshEvent() {
        EventBus.getDefault().post(new AddTokenRefreshEvent());
    }

    class TokenAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TokenViewHolder holder = new TokenViewHolder(LayoutInflater.from(
                    mActivity).inflate(R.layout.item_token_info, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TokenViewHolder) {
                TokenViewHolder viewHolder = (TokenViewHolder) holder;
                TokenLogoUtil.Companion.setLogo(new TokenItem(tokenList.get(position)), mActivity, viewHolder.tokenImage);
                viewHolder.tokenName.setText(tokenList.get(position).name);
                viewHolder.tokenSymbol.setText(tokenList.get(position).symbol);
                viewHolder.tokenContractAddress.setText(tokenList.get(position).address);
                tokenList.get(position).isSelected =
                        DBWalletUtil.checkTokenInCurrentWallet(mActivity, tokenList.get(position).symbol);

                viewHolder.tokenSelectSwitch.setChecked(tokenList.get(position).isSelected);
                viewHolder.tokenSelectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        tokenList.get(position).isSelected = !tokenList.get(position).isSelected;
                        viewHolder.tokenSelectSwitch.setChecked(tokenList.get(position).isSelected);

                        if (tokenList.get(position).isSelected) {
                            DBWalletUtil.addTokenToCurrentWallet(mActivity, new TokenItem(tokenList.get(position)));
                        } else {
                            DBWalletUtil.deleteTokenFromCurrentWallet(mActivity, new TokenItem(tokenList.get(position)));
                        }
                    }
                });
                viewHolder.itemView.setTag(position);
            }
        }

        @Override
        public int getItemCount() {
            return tokenList.size();
        }


        class TokenViewHolder extends RecyclerView.ViewHolder {
            ImageView tokenImage;
            TextView tokenName;
            TextView tokenSymbol;
            TextView tokenContractAddress;
            Switch tokenSelectSwitch;

            public TokenViewHolder(View view) {
                super(view);
                tokenImage = view.findViewById(R.id.token_image);
                tokenName = view.findViewById(R.id.token_name_text);
                tokenSymbol = view.findViewById(R.id.token_symbol_text);
                tokenContractAddress = view.findViewById(R.id.token_contract_address);
                tokenSelectSwitch = view.findViewById(R.id.switch_token_select);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
            postTokenRefreshEvent();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_CODE) {
            initData();
        }
    }
}
