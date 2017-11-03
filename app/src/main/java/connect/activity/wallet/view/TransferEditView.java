package connect.activity.wallet.view;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wallet.bean.CurrencyEnum;
import com.wallet.inter.WalletListener;

import java.io.IOException;
import java.lang.reflect.Type;

import connect.activity.base.BaseApplication;
import connect.activity.set.bean.PaySetBean;
import connect.activity.wallet.bean.RateBean;
import connect.activity.wallet.bean.WalletSetBean;
import connect.activity.wallet.manager.WalletManager;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.DialogUtil;
import connect.utils.RegularUtil;
import connect.utils.data.RateDataUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.filter.EditInputFilterPrice;
import connect.utils.okhttp.HttpRequest;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Transfer amount input
 */
public class TransferEditView extends LinearLayout implements View.OnClickListener{

    /** Top currency indicator */
    TextView editTitleTv;
    /** Input box currency symbol */
    TextView editSymbolTv;
    /** Currency input box */
    EditText amountInputEt;
    /** Handling fee (external) */
    TextView feeTv;
    /** note */
    TextView addNote;
    /** Converted currency */
    TextView transferTv;
    /** Account balance */
    TextView balanceTv;
    LinearLayout feeLin;

    private Context context;
    /** Edit default BTC */
    private RateBean btcBean;
    /** Currency external introduction of bitcoin corresponding transformation */
    private RateBean otherRate;
    private WalletSetBean walletSetBean;
    private InputFilter[] btcInputFilters = {new EditInputFilterPrice(Double.valueOf(999),8)};
    private InputFilter[] otherInputFilters = {new EditInputFilterPrice(Double.valueOf(999999999),2)};
    /** Default input box 10000 */
    private long editDefault = 10000;
    /** The current number of COINS */
    private OnEditListener onEditListener;
    private CurrencyEnum currencyEnum = CurrencyEnum.BTC;
    private String currentBtc = "";
    private String note = "";

    public TransferEditView(Context context) {
        this(context, null);
    }

    public TransferEditView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransferEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        View view = View.inflate(context, R.layout.item_transfer_edit, this);
        editTitleTv = (TextView)view.findViewById(R.id.edit_title_tv);
        editSymbolTv = (TextView)view.findViewById(R.id.edit_symbol_tv);
        amountInputEt = (EditText)view.findViewById(R.id.amoutinput_et);
        balanceTv = (TextView)view.findViewById(R.id.balance_tv);
        feeLin = (LinearLayout)view.findViewById(R.id.fee_lin);
        feeTv = (TextView)view.findViewById(R.id.fee_tv);
        feeTv.setOnClickListener(this);
        addNote = (TextView)view.findViewById(R.id.add_note);
        addNote.setOnClickListener(this);
        transferTv = (TextView)view.findViewById(R.id.transfer_tv);
        transferTv.setOnClickListener(this);
        initView();
    }

    public void initView(){
        btcBean = RateDataUtil.getInstance().getRateBTC();
        walletSetBean = ParamManager.getInstance().getWalletSet();

        editTitleTv.setText(context.getString(R.string.Wallet_Amount_BTC));
        editSymbolTv.setText(btcBean.getSymbol());
        amountInputEt.addTextChangedListener(textWatcher);
        amountInputEt.setText(RateFormatUtil.longToDoubleBtc(editDefault));
        amountInputEt.setFilters(btcInputFilters);
        // Shielding EditText paste function
        amountInputEt.setCustomSelectionActionModeCallback(amountInputCallback);
        amountInputEt.setTag(1);
        if (walletSetBean != null && walletSetBean.isAutoFee()) {
            feeTv.setText(R.string.Wallet_Auto_Calculate_Miner_Fee);
        } else if(walletSetBean != null) {
            feeTv.setText(context.getString(R.string.Wallet_Fee_BTC, RateFormatUtil.longToDouble(walletSetBean.getFee())));
        }

        showBalance();
        WalletManager.getInstance().syncWallet(new WalletListener<Integer>() {
            @Override
            public void success(Integer status) {
                if(status == 0){
                    showBalance();
                }
            }

            @Override
            public void fail(WalletError error) {}
        });
        requestRate();
    }

    private void showBalance(){
        CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(currencyEnum.getCode());
        if (currencyEntity != null) {
            balanceTv.setText(BaseApplication.getInstance().getString(R.string.Wallet_Balance_Credit,
                    RateFormatUtil.longToDoubleBtc(currencyEntity.getAmount())));
        } else {
            balanceTv.setText(BaseApplication.getInstance().getString(R.string.Wallet_Balance_Credit,
                    RateFormatUtil.longToDoubleBtc(0L)));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.transfer_tv:
                if(otherRate == null || walletSetBean == null){
                    return;
                }
                if ((Integer)amountInputEt.getTag() == 1) {
                    amountInputEt.setTag(2);
                    amountInputEt.setFilters(otherInputFilters);
                    amountInputEt.setText(transferTv.getText().toString().replaceAll("[^\\d.]*", ""));
                    editSymbolTv.setText(otherRate.getSymbol());
                    editTitleTv.setText(BaseApplication.getInstance().getString(R.string.Wallet_Amount_Symbol,otherRate.getCode()));
                } else {
                    amountInputEt.setTag(1);
                    amountInputEt.setFilters(btcInputFilters);
                    amountInputEt.setText(transferTv.getText().toString());
                    editSymbolTv.setText(R.string.Set_BTC_symbol);
                    editTitleTv.setText(R.string.Wallet_Amount_BTC);
                }
                /*isClickTransfer = true;
                if(isEditBTC){
                    isEditBTC = false;
                    editSymbolTv.setText(otherRate.getSymbol());
                    amoutInputEt.setFilters(otherInputFilters);

                    amoutInputEt.setText(transferTv.getText().toString().replaceAll("[^\\d.]*", ""));
                    transferTv.setText(context.getResources().getString(R.string.Set_BTC_symbol) + " " + currentBtc);
                    editTitleTv.setText(BaseApplication.getInstance().getString(R.string.Wallet_Amount_Symbol,otherRate.getCode()));
                }else{
                    isEditBTC = true;
                    editSymbolTv.setText(R.string.Set_BTC_symbol);
                    amoutInputEt.setFilters(btcInputFilters);

                    transferTv.setText(otherRate.getSymbol() + " " + amoutInputEt.getText().toString());
                    amoutInputEt.setText(currentBtc);
                    editTitleTv.setText(R.string.Wallet_Amount_BTC);
                }*/
                break;
            case R.id.add_note:
                DialogUtil.showEditView(context, context.getResources().getString(R.string.Wallet_Add_note),
                        "", "", "", "", "",false,15,new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        addNote.setText(value);
                        note = value;
                    }
                    @Override
                    public void cancel() {}
                });
                break;
            case R.id.fee_tv:
                if(onEditListener != null){
                    onEditListener.setFee();
                }
                break;
            default:
                break;
        }
    }

    ActionMode.Callback amountInputCallback = new ActionMode.Callback(){
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }
        @Override
        public void onDestroyActionMode(ActionMode mode) {}
    };

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if(!(RegularUtil.matches(s.toString(), RegularUtil.VERIFICATION_AMOUT)) ||
                    RegularUtil.matches(s.toString(), RegularUtil.ALL_NUMBER) ||
                    otherRate == null ||
                    otherRate.getRate() == null){
                return;
            }

            currentBtc = TextUtils.isEmpty(s.toString()) ? "0" : s.toString();
            if((Integer)amountInputEt.getTag() == 1){
                transferTv.setText(otherRate.getSymbol() + " " +
                        RateFormatUtil.foematNumber(RateFormatUtil.PATTERN_OTHER, Double.valueOf(currentBtc) * otherRate.getRate()));
            }else{
                currentBtc = RateFormatUtil.foematNumber(RateFormatUtil.PATTERN_BTC, Double.valueOf(currentBtc) / otherRate.getRate());
                transferTv.setText(context.getResources().getString(R.string.Set_BTC_symbol) + " " + currentBtc);
            }
            if(onEditListener != null){
                onEditListener.onEdit(s.toString());
            }

            /*if (isEditBTC) {
                currentBtc = s.toString();
            } else {
                if (TextUtils.isEmpty(s) || otherRate == null || otherRate.getRate() == null) {
                    currentBtc = "";
                } else {
                    currentBtc = RateFormatUtil.foematNumber(RateFormatUtil.PATTERN_BTC, Double.valueOf(s.toString()) / otherRate.getRate());
                }
            }

            //If it is caused by the change of Transfer direct return
            if(isClickTransfer){
                isClickTransfer = false;
                return;
            }

            //Data is not legal
            if (!(RegularUtil.matches(s.toString(), RegularUtil.VERIFICATION_AMOUT)
                    || RegularUtil.matches(s.toString(), RegularUtil.ALL_NUMBER))) {
                return;
            }

            //Determine the input box, enter the book currency or other currency
            //For currency conversion
            if (isEditBTC) {
                currentBtc = s.toString();
            } else {
                if (TextUtils.isEmpty(s) || otherRate == null || otherRate.getRate() == null) {
                    currentBtc = "";
                } else {
                    currentBtc = RateFormatUtil.foematNumber(RateFormatUtil.PATTERN_BTC, Double.valueOf(s.toString()) / otherRate.getRate());
                }
            }

            //Conversion to other currencies
            if (otherRate != null && otherRate.getRate() != null) {
                if (TextUtils.isEmpty(s)) {
                    if (isEditBTC) {
                        transferTv.setText(otherRate.getSymbol() + " ");
                    } else {
                        transferTv.setText(context.getResources().getString(R.string.Set_BTC_symbol) + " ");
                    }
                } else {
                    if (isEditBTC) {
                        transferTv.setText(otherRate.getSymbol() + " " +
                                RateFormatUtil.foematNumber(RateFormatUtil.PATTERN_OTHER, Double.valueOf(s.toString()) * otherRate.getRate()));
                    } else {
                        transferTv.setText(context.getResources().getString(R.string.Set_BTC_symbol) + " " + currentBtc);
                    }
                }
            } else {//Get no exchange rate
                transferTv.setText(R.string.Wallet_Transfer_Unable);
            }

            if(onEditListener != null){
                onEditListener.onEdit(s.toString());
            }*/
        }
    };

    /**
     * The exchange-rate
     */
    private void requestRate(){
        String code = ParamManager.getInstance().getWalletSet().getCurrency();
        RateBean rateBean = RateDataUtil.getInstance().getRate(code);
        if(rateBean == null || context == null){
            return;
        }
        HttpRequest.getInstance().get(rateBean.getUrl(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                amountInputEt.setText(RateFormatUtil.longToDoubleBtc(editDefault));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String tempResponse =  response.body().string();
                Type type = new TypeToken<RateBean>() {}.getType();
                otherRate = new Gson().fromJson(tempResponse, type);
                amountInputEt.setText(RateFormatUtil.longToDoubleBtc(editDefault));
            }
        });
    }

    public void setInputAmount(Double amount){
        amountInputEt.setText(amount + "");
    }

    public String getCurrentBtc(){
        return currentBtc;
    }

    public Long getCurrentBtcLong(){
        long amount = RateFormatUtil.stringToLongBtc(currentBtc);
        return amount;
    }

    /**
     * Whether the available balance is visible
     * @param visibility
     */
    public void setVisibilityBalance(int visibility){
        balanceTv.setVisibility(visibility);
    }

    /**
     * Input callback (mainly used to change the contents of the input box, the external supply port)
     * @param onEditListener
     */
    public void setEditListener(OnEditListener onEditListener){
        this.onEditListener = onEditListener;
    }

    /**
     * Set the message
     * @param str
     */
    public void setNote(String str){
        addNote.setText(str);
        note = str;
    }
    /**
     * get the message
     * @return
     */
    public String getNote(){
        return note;
    }

    /**
     * Set fee display
     * @param visibility
     */
    public void setFeeVisibility(int visibility){
        feeLin.setVisibility(visibility);
    }

    public CurrencyEnum getCurrencyType() {
        return currencyEnum;
    }

    public interface OnEditListener {

        void onEdit(String value);

        void setFee();
    }

}
