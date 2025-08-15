package com.coinomi.wallet.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.view.ActionMode;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.coinomi.core.wallet.Wallet;
import com.coinomi.core.wallet.WalletAccount;
import com.coinomi.wallet.Configuration;
import com.coinomi.wallet.R;
import com.coinomi.wallet.WalletApplication;
import com.coinomi.wallet.ui.adaptors.AccountListAdapter;
import com.coinomi.wallet.ui.common.BaseFragment;
import com.coinomi.wallet.ui.common.BasePartnersDataFragment;
import com.coinomi.wallet.ui.widget.Amount;
import com.coinomi.wallet.ui.widget.SwipeRefreshLayout;
import com.coinomi.wallet.util.ThrottlingWalletChangeListener;
import com.coinomi.wallet.util.UiUtils;
import com.coinomi.wallet.util.WeakHandler;

import org.bitcoinj.utils.Threading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;

/**
 * @author vbcs
 * @author John L. Jegutanis
 */
public class OverviewFragment extends BasePartnersDataFragment {
    private static final Logger log = LoggerFactory.getLogger(OverviewFragment.class);

    private static final int WALLET_CHANGED = 0;
    private static final int UPDATE_VIEW = 1;

    private final Handler handler = new MyHandler(this);

    private static class MyHandler extends WeakHandler<OverviewFragment> {
        public MyHandler(OverviewFragment ref) { super(ref); }

        @Override
        @SuppressWarnings("unchecked")
        protected void weakHandleMessage(OverviewFragment ref, Message msg) {
            switch (msg.what) {
                case WALLET_CHANGED:
                    ref.updateWallet();
                    break;
                case UPDATE_VIEW:
                    ref.updateView();
                    break;
            }
        }
    }

    private Wallet wallet;

    private WalletApplication application;

    private AccountListAdapter adapter;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    @BindView(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;
    @BindView(R.id.account_rows) ListView accountRows;

    private Listener listener;

    public static OverviewFragment getInstance() {
        return new OverviewFragment();
    }

    public OverviewFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        wallet = application.getWallet();
        if (wallet == null) {
            return;
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        accountRows = ButterKnife.findById(view, R.id.account_rows);

        View spaceView = new View(getContext());
        int height = getResources().getDimensionPixelSize(R.dimen.row_padding_horizontal) * 2;
        spaceView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));

        accountRows.addHeaderView(spaceView, null, false);

        View partnersDataView = inflater.inflate(R.layout.partners_images_container, null);
        accountRows.addFooterView(partnersDataView);

        setBinder(ButterKnife.bind(this, view));

        if (wallet == null) {
            return view;
        }

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(() -> {
            if (listener != null) {
                listener.onRefresh();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                R.color.progress_bar_color_1,
                R.color.progress_bar_color_2,
                R.color.progress_bar_color_3,
                R.color.progress_bar_color_4);

        // Set a space in the end of the list
        View listFooter = new View(getActivity());
        listFooter.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin));
        accountRows.addFooterView(listFooter);

        // Init list adapter
        adapter = new AccountListAdapter(inflater.getContext(), wallet);
        accountRows.setAdapter(adapter);

        return view;
    }

    private final ThrottlingWalletChangeListener walletChangeListener = new ThrottlingWalletChangeListener() {

        @Override
        public void onThrottledWalletChanged() {
            handler.sendMessage(handler.obtainMessage(WALLET_CHANGED));
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mNavigationDrawerFragment != null && !mNavigationDrawerFragment.isDrawerOpen()) {
            inflater.inflate(R.menu.overview, menu);
        }
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        try {
            listener = (Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + Listener.class);
        }
        application = (WalletApplication) context.getApplicationContext();
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();

        // TODO add an event listener to the Wallet class
        for (WalletAccount account : wallet.getAllAccounts()) {
            account.addEventListener(walletChangeListener, Threading.SAME_THREAD);
        }

        updateWallet();
        updateView();
    }

    @Override
    public void onPause() {
        // TODO add an event listener to the Wallet class
        for (WalletAccount account : wallet.getAllAccounts()) {
            account.removeEventListener(walletChangeListener);
        }
        walletChangeListener.removeCallbacks();

        super.onPause();
    }

    @OnItemClick(R.id.account_rows)
    public void onAmountClick(int position) {
        if (position >= accountRows.getHeaderViewsCount()) {
            // Note the usage of getItemAtPosition() instead of adapter's getItem() because
            // the latter does not take into account the header (which has position 0).
            Object obj = accountRows.getItemAtPosition(position);

            if (listener != null && obj instanceof WalletAccount) {
                listener.onAccountSelected(((WalletAccount) obj).getId());
            } else {
                showGenericError();
            }
        }
    }

    @OnItemLongClick(R.id.account_rows)
    public boolean onAmountLongClick(int position) {
        if (position >= accountRows.getHeaderViewsCount()) {
            // Note the usage of getItemAtPosition() instead of adapter's getItem() because
            // the latter does not take into account the header (which has position 0).
            Object obj = accountRows.getItemAtPosition(position);
            Activity activity = getActivity();

            if (obj instanceof WalletAccount && activity != null) {
                ActionMode actionMode = UiUtils.startAccountActionMode(
                        (WalletAccount) obj, activity, getFragmentManager());
                // Hack to dismiss this action mode when back is pressed
                if (activity instanceof WalletActivity) {
                    ((WalletActivity) activity).registerActionMode(actionMode);
                }

                return true;
            } else {
                showGenericError();
            }
        }
        return false;
    }

    private void showGenericError() {
        Toast.makeText(getActivity(), getString(R.string.error_generic), Toast.LENGTH_LONG).show();
    }

    public void updateWallet() {
        if (wallet != null) {
            adapter.replace(wallet);
            updateView();
        }
    }


    public void updateView() {
        swipeContainer.setRefreshing(wallet.isLoading());
    }

    @Override
    protected boolean enableLoadingPartnersData() {
        return true;
    }

    public interface Listener extends EditAccountFragment.Listener {
        void onLocalAmountClick();
        void onAccountSelected(String accountId);
        void onRefresh();
    }
}
