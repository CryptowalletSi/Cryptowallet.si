package com.coinomi.wallet.ui;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coinomi.core.coins.CoinType;
import com.coinomi.core.wallet.AbstractAddress;
import com.coinomi.core.wallet.WalletAccount;
import com.coinomi.wallet.Constants;
import com.coinomi.wallet.ExchangeHistoryProvider;
import com.coinomi.wallet.ExchangeHistoryProvider.ExchangeEntry;
import com.coinomi.wallet.R;
import com.coinomi.wallet.WalletApplication;
import com.coinomi.wallet.ui.common.BaseFragment;
import com.coinomi.wallet.util.Fonts;
import com.coinomi.wallet.util.WeakHandler;

import java.util.List;
import java.util.Timer;

import static com.coinomi.core.Preconditions.checkNotNull;
import static com.coinomi.wallet.util.UiUtils.setGone;
import static com.coinomi.wallet.util.UiUtils.setInvisible;
import static com.coinomi.wallet.util.UiUtils.setVisible;

/**
 * @author John L. Jegutanis
 */
public class TradeStatusFragment extends BaseFragment {

    private static final int UPDATE_STATUS = 1;
    private static final int ERROR_MESSAGE = 2;

    private static final int ID_STATUS_LOADER = 0;

    private static final String ARG_SHOW_EXIT_BUTTON = "show_exit_button";

    private Listener listener;
    private ContentResolver contentResolver;
    private LoaderManager loaderManager;
    private TextView exchangeInfo;
    private TextView depositIcon;
    private ProgressBar depositProgress;
    private TextView depositText;
    private TextView exchangeIcon;
    private ProgressBar exchangeProgress;
    private TextView exchangeText;
    private TextView errorIcon;
    private TextView errorText;
    private Button viewTransaction;
    private Button emailReceipt;
    private MenuItem actionDeleteMenu;
    private boolean showExitButton;

    private Uri statusUri;
    private ExchangeEntry exchangeStatus;
    private final Handler handler = new MyHandler(this);
    private Timer timer;
    private WalletApplication application;

    private static class MyHandler extends WeakHandler<TradeStatusFragment> {
        public MyHandler(TradeStatusFragment ref) { super(ref); }

        @Override
        protected void weakHandleMessage(TradeStatusFragment ref, Message msg) {
            switch (msg.what) {
                case UPDATE_STATUS:
                    ref.updateStatus((ExchangeEntry) msg.obj);
                    break;
                case ERROR_MESSAGE:
                    ref.errorIcon.setVisibility(View.VISIBLE);
                    ref.errorText.setVisibility(View.VISIBLE);
                    ref.errorText.setText(ref.getString(R.string.trade_status_failed_detail, msg.obj));
                    break;
            }
        }
    }

    private void updateStatus(ExchangeEntry newStatus) {
        exchangeStatus = checkNotNull(newStatus);
        updateView();
    }

    public static TradeStatusFragment newInstance(ExchangeEntry exchangeEntry) {
        return newInstance(exchangeEntry, false);
    }

    public static TradeStatusFragment newInstance(ExchangeEntry exchangeEntry, boolean showExitButton) {
        TradeStatusFragment fragment = new TradeStatusFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.ARG_EXCHANGE_ENTRY, exchangeEntry);
        args.putBoolean(ARG_SHOW_EXIT_BUTTON, showExitButton);
        fragment.setArguments(args);
        return fragment;
    }

    public TradeStatusFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        showExitButton = args.getBoolean(ARG_SHOW_EXIT_BUTTON, false);
        exchangeStatus = (ExchangeEntry) args.getSerializable(Constants.ARG_EXCHANGE_ENTRY);
        AbstractAddress deposit = exchangeStatus.depositAddress;
        statusUri = ExchangeHistoryProvider.contentUri(application.getPackageName(), deposit);
        loaderManager.initLoader(ID_STATUS_LOADER, null, statusLoaderCallbacks);

        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        loaderManager.destroyLoader(ID_STATUS_LOADER);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trade_status, container, false);

        exchangeInfo = view.findViewById(R.id.exchange_status_info);
        depositIcon = view.findViewById(R.id.trade_deposit_status_icon);
        depositProgress = view.findViewById(R.id.trade_deposit_status_progress);
        depositText = view.findViewById(R.id.trade_deposit_status_text);
        exchangeIcon = view.findViewById(R.id.trade_exchange_status_icon);
        exchangeProgress = view.findViewById(R.id.trade_exchange_status_progress);
        exchangeText = view.findViewById(R.id.trade_exchange_status_text);
        errorIcon = view.findViewById(R.id.trade_error_status_icon);
        errorText = view.findViewById(R.id.trade_error_status_text);
        Fonts.setTypeface(depositIcon, Fonts.Font.COINOMI_FONT_ICONS);
        Fonts.setTypeface(exchangeIcon, Fonts.Font.COINOMI_FONT_ICONS);
        Fonts.setTypeface(errorIcon, Fonts.Font.COINOMI_FONT_ICONS);

        viewTransaction = view.findViewById(R.id.trade_view_transaction);
        emailReceipt = view.findViewById(R.id.trade_email_receipt);

        if (showExitButton) {
            view.findViewById(R.id.button_exit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onExitPressed();
                }
            });
        } else {
            view.findViewById(R.id.button_exit).setVisibility(View.GONE);
        }

        updateView();

        return view;
    }

    public void onExitPressed() {
        if (listener != null) {
            listener.onFinish();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.exchange_status, menu);
        actionDeleteMenu = menu.findItem(R.id.action_delete);
        updateView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.trade_status_delete_message)
                        .setNegativeButton(R.string.button_cancel, null)
                        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                contentResolver.delete(statusUri, null, null);
                                onExitPressed();
                            }
                        })
                        .create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateView() {
        switch (exchangeStatus.status) {
            case ExchangeEntry.STATUS_INITIAL:
                if (actionDeleteMenu != null) actionDeleteMenu.setVisible(false);
                exchangeInfo.setText(R.string.trade_status_message);
                setGone(depositIcon);
                setVisible(depositProgress);
                setVisible(depositText);
                depositText.setText(R.string.trade_status_waiting_deposit);
                setInvisible(exchangeIcon);
                setGone(exchangeProgress);
                setInvisible(exchangeText);
                setGone(errorIcon);
                setGone(errorText);
                setInvisible(viewTransaction);
                setInvisible(emailReceipt);
                break;
            case ExchangeEntry.STATUS_PROCESSING:
                if (actionDeleteMenu != null) actionDeleteMenu.setVisible(false);
                exchangeInfo.setText(R.string.trade_status_message);
                setVisible(depositIcon);
                setGone(depositProgress);
                setVisible(depositText);
                depositText.setText(getString(R.string.trade_status_received_deposit,
                        exchangeStatus.depositAmount));
                setGone(exchangeIcon);
                setVisible(exchangeProgress);
                setVisible(exchangeText);
                exchangeText.setText(R.string.trade_status_waiting_trade);
                setGone(errorIcon);
                setGone(errorText);
                setInvisible(viewTransaction);
                setInvisible(emailReceipt);
                break;
            case ExchangeEntry.STATUS_COMPLETE:
                if (actionDeleteMenu != null) actionDeleteMenu.setVisible(true);
                exchangeInfo.setText(R.string.trade_status_complete_message);
                setVisible(depositIcon);
                setGone(depositProgress);
                setVisible(depositText);
                depositText.setText(getString(R.string.trade_status_received_deposit,
                        exchangeStatus.depositAmount));
                setVisible(exchangeIcon);
                setGone(exchangeProgress);
                setVisible(exchangeText);
                exchangeText.setText(getString(R.string.trade_status_complete_detail,
                        exchangeStatus.withdrawAmount));
                setGone(errorIcon);
                setGone(errorText);
                updateViewTransaction();
                updateEmailReceipt();
                break;
            case ExchangeEntry.STATUS_FAILED:
                if (actionDeleteMenu != null) actionDeleteMenu.setVisible(true);
                setVisible(errorIcon);
                setVisible(errorText);
                errorText.setText(R.string.trade_status_failed);
        }
    }

    private void updateViewTransaction() {
        final String txId = exchangeStatus.withdrawTransactionId;
        final AbstractAddress withdrawAddress = exchangeStatus.withdrawAddress;
        final CoinType withdrawType = withdrawAddress.getType();
        final List<WalletAccount> accounts = application.getAccounts(withdrawAddress);

        if (accounts.size() > 0 || Constants.COINS_BLOCK_EXPLORERS.containsKey(withdrawType)) {
            setVisible(viewTransaction);
        } else {
            setGone(viewTransaction);
        }

        viewTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accountId = null;
                for (WalletAccount account : accounts) {
                    if (account.getTransaction(txId) != null) {
                        accountId = account.getId();
                        break;
                    }
                }
                if (accountId != null && txId != null) {
                    Intent intent = new Intent(getActivity(), TransactionDetailsActivity.class);
                    intent.putExtra(Constants.ARG_ACCOUNT_ID, accountId);
                    intent.putExtra(Constants.ARG_TRANSACTION_ID, txId);
                    startActivity(intent);
                } else {
                    // Take to an external blockchain explorer
                    if (Constants.COINS_BLOCK_EXPLORERS.containsKey(withdrawType)) {
                        String url = String.format(Constants.COINS_BLOCK_EXPLORERS.get(withdrawType), txId);
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    } else {
                        // Should not happen
                        Toast.makeText(getActivity(), R.string.error_generic,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void updateEmailReceipt() {
        // TODO enable
        setInvisible(emailReceipt);
//        setVisible(emailReceipt);
//        emailReceipt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                emailReceiptDialog.show(getFragmentManager(), null);
//            }
//        });
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        try {
            listener = (Listener) context;
            contentResolver = context.getContentResolver();
            application = (WalletApplication) context.getApplicationContext();
            loaderManager = getLoaderManager();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement " + TradeStatusFragment.Listener.class);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface Listener {
        void onFinish();
    }

    private final LoaderCallbacks<Cursor> statusLoaderCallbacks = new LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
            return new CursorLoader(application.getApplicationContext(), statusUri,
                    null, null, null, null);
        }

        @Override
        public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
            if (data != null && data.getCount() > 0) {
                data.moveToFirst();
                ExchangeEntry newStatus = ExchangeHistoryProvider.getExchangeEntry(data);
                handler.sendMessage(handler.obtainMessage(UPDATE_STATUS, newStatus));
            }
        }

        @Override public void onLoaderReset(final Loader<Cursor> loader) { }
    };

//    TODO implement
//    private DialogFragment emailReceiptDialog = new DialogFragment() {
//        @Override @NonNull
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            final LayoutInflater inflater = LayoutInflater.from(getActivity());
//            final View view = inflater.inflate(R.layout.email_receipt_dialog, null);
//            final TextView emailView = (TextView) view.findViewById(R.id.email);
//
//            return new DialogBuilder(getActivity())
//                    .setTitle(R.string.email_receipt_title)
//                    .setView(view)
//                    .setNegativeButton(R.string.button_cancel, null)
//                    .setPositiveButton(R.string.button_add, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            // TODO implement async task
//                            String email = emailView.getText().toString();
////                            ShapeShiftEmail reply = application.getShapeShift().requestEmailReceipt(email, exchangeStatus.getShapeShiftTxStatus());
//                        }
//                    }).create();
//        }
//    };
}
