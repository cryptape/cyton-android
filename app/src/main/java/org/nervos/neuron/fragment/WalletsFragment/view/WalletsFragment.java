package org.nervos.neuron.fragment.WalletsFragment.view;

import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.view.View;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.AddWalletActivity;
import org.nervos.neuron.custom.WalletToolbar;
import org.nervos.neuron.custom.WalletTopView;
import org.nervos.neuron.fragment.NBaseFragment;
import org.nervos.neuron.fragment.SettingsFragment;
import org.nervos.neuron.fragment.TokenListFragment.view.TokenListFragment;
import org.nervos.neuron.fragment.WalletsFragment.presenter.WalletFragmentPresenter;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.db.DBWalletUtil;

/**
 * Created by BaojunCZ on 2018/8/1.
 */
public class WalletsFragment extends NBaseFragment {

    public static final String TAG = WalletsFragment.class.getName();

    private NestedScrollView mNestedScrollView;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private AppBarLayout appBarLayout;
    private WalletToolbar toolbar;
    private WalletTopView walletView;
    private WalletItem walletItem;
    private TokenListFragment tokenListFragment;
    private WalletFragmentPresenter presenter;

    private String[] mTitles = {"代币", "藏品"};

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_wallets;
    }

    @Override
    protected void initView() {
        super.initView();
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        toolbar = (WalletToolbar) findViewById(R.id.toolbar);
        walletView = (WalletTopView) findViewById(R.id.wallet_view);

        mNestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);
        mNestedScrollView.setFillViewport(true);

        mTabLayout.setupWithViewPager(mViewPager);
        MyAdapter adapter = new MyAdapter(getActivity().getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
    }

    @Override
    protected void initData() {
        super.initData();
        mTabLayout.post(() -> {
            presenter.setIndicator(mTabLayout, 70, 70);
        });
        tokenListFragment = new TokenListFragment();
        presenter = new WalletFragmentPresenter(getActivity());
        initWalletData();
    }

    @Override
    protected void initAction() {
        super.initAction();
        appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int scrollRangle = appBarLayout.getTotalScrollRange();
            if (verticalOffset == 0) {
                toolbar.setAlpha(0.0f);
                toolbar.setVisibility(View.GONE);
                walletView.setAlpha(1.0f);
            } else {
                float alfha = Math.round(100.0f * Math.abs(verticalOffset) / scrollRangle) / 100.0f;
                toolbar.setAlpha(alfha);
                toolbar.setVisibility(View.VISIBLE);
                walletView.setAlpha(1.0f - alfha);
            }
        });
    }

    private void initWalletData() {
        if ((walletItem = DBWalletUtil.getCurrentWallet(getContext())) != null) {
            walletView.setWalletItem(walletItem);
        } else {
            startActivity(new Intent(getActivity(), AddWalletActivity.class));
        }
    }


    private class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return tokenListFragment;
                case 1:
                    return new SettingsFragment();
                default:
                    return new TokenListFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }

}