package connect.utils.transfer;

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

import java.io.IOException;
import java.lang.reflect.Type;

import connect.db.MemoryDataManager;
import connect.db.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.ui.activity.set.bean.PaySetBean;
import connect.ui.activity.set.manager.EditInputFilterPrice;
import connect.ui.activity.wallet.bean.RateBean;
import connect.ui.activity.wallet.bean.WalletAccountBean;
import connect.ui.base.BaseApplication;
import connect.utils.DialogUtil;
import connect.utils.RegularUtil;
import connect.utils.UriUtil;
import connect.utils.data.RateDataUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import protos.Connect;

/**
 * Transfer amount input
 */
public class TransferEditView extends LinearLayout implements View.OnClickListener{

    /** Top currency indicator */
    TextView editTitleTv;
    /** Input box currency symbol */
    TextView editSymbolTv;
    /** Currency input box */
    EditText amoutinputEt;
    /** Handling fee (external) */
    TextView feeTv;
    /** note */
    TextView addNote;
    /** Converted currency */
    TextView transferTv;
    /** Account balance */
    TextView amountTv;

    private Context context;
    /** Edit default BTC */
    private RateBean btcBean;
    /** Currency external introduction of bitcoin corresponding transformation */
    private RateBean otherRate;
    /** Default input box 10000 */
    private long editDefault = 10000;
    private InputFilter[] btcInputFilters = {new EditInputFilterPrice(Double.valueOf(999),8)};
    private InputFilter[] otherInputFilters = {new EditInputFilterPrice(Double.valueOf(999999999),2)};
    /** EditView is currently BTC*/
    private boolean isEditBTC = true;
    /** The current number of COINS */
    private String currentBtc = "";
    private OnEditListener onEditListener;
    /**
     * Click on the swap button
     * increase the judge to change the shield TextWatcher
     * to solve the problem of different exchange rate calculation precision caused by the click exchange
     */
    private boolean isClickTransfer;
    private String note = "";
    private PaySetBean paySetBean;
    private WalletAccountBean accountBean;

    public TransferEditView(Context context) {
        this(context, null);
    }

    public TransferEditView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransferEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFind(context);
    }

    private void initFind(Context context) {
        this.context = context;
        View view = View.inflate(context, R.layout.item_transfer_edit, this);
        editTitleTv = (TextView)view.findViewById(R.id.edit_title_tv);
        editSymbolTv = (TextView)view.findViewById(R.id.edit_symbol_tv);
        amoutinputEt = (EditText)view.findViewById(R.id.amoutinput_et);
        amountTv = (TextView)view.findViewById(R.id.amount_tv);
        feeTv = (TextView)view.findViewById(R.id.fee_tv);
        feeTv.setOnClickListener(this);
        addNote = (TextView)view.findViewById(R.id.add_note);
        addNote.setOnClickListener(this);
        transferTv = (TextView)view.findViewById(R.id.transfer_tv);
        transferTv.setOnClickListener(this);

        // Shielding EditText paste function
        amoutinputEt.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
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
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
    }

    /**
     * Initialize the View must be called again
     */
    public void initView(){
        initView(null);
    }

    public void initView(Double amount){
        if(amount != null){
            editDefault = RateFormatUtil.doubleToLongBtc(amount);
        }
        btcBean = RateDataUtil.getInstance().getRateBTC();
        otherRate = ParamManager.getInstance().getCountryRate();
        paySetBean = ParamManager.getInstance().getPaySet();

        if(paySetBean == null){
            return;
        }
        editTitleTv.setText(context.getString(R.string.Wallet_Amount_BTC));
        editSymbolTv.setText(btcBean.getSymbol());
        amountTv.setText(BaseApplication.getInstance().getString(R.string.Wallet_Balance_Credit, RateFormatUtil.longToDoubleBtc(0)));
        amoutinputEt.addTextChangedListener(textWatcher);
        amoutinputEt.setText(RateFormatUtil.longToDoubleBtc(editDefault));
        amoutinputEt.setFilters(btcInputFilters);
        if(paySetBean.isAutoFee()){
            feeTv.setText(R.string.Wallet_Auto_Calculate_Miner_Fee);
        }else{
            feeTv.setText(context.getString(R.string.Wallet_Fee_BTC, RateFormatUtil.longToDouble(paySetBean.getFee())));
        }
        requestRate();
        requestWallet();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.transfer_tv:
                if(otherRate == null || paySetBean == null){
                    return;
                }
                isClickTransfer = true;
                if(isEditBTC){
                    isEditBTC = false;
                    editSymbolTv.setText(otherRate.getSymbol());
                    amoutinputEt.setFilters(otherInputFilters);

                    amoutinputEt.setText(transferTv.getText().toString().replaceAll("[^\\d.]*", ""));
                    transferTv.setText(context.getResources().getString(R.string.Set_BTC_symbol) + " " + currentBtc);
                    editTitleTv.setText(BaseApplication.getInstance().getString(R.string.Wallet_Amount_Symbol,otherRate.getCode()));
                }else{
                    isEditBTC = true;
                    editSymbolTv.setText(R.string.Set_BTC_symbol);
                    amoutinputEt.setFilters(btcInputFilters);

                    transferTv.setText(otherRate.getSymbol() + " " + amoutinputEt.getText().toString());
                    amoutinputEt.setText(currentBtc);
                    editTitleTv.setText(R.string.Wallet_Amount_BTC);
                }
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
                    public void cancel() {

                    }
                });
                break;
            case R.id.fee_tv:
                onEditListener.setFee();
                break;
            default:
                break;
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
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
            }
        }
    };

    /**
     * The exchange-rate
     */
    private void requestRate(){
        if(otherRate == null || context == null){
            return;
        }
        HttpRequest.getInstance().get(otherRate.getUrl(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                amoutinputEt.setText(RateFormatUtil.longToDoubleBtc(editDefault));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String tempResponse =  response.body().string();
                Type type = new TypeToken<RateBean>() {}.getType();
                RateBean rateBean = new Gson().fromJson(tempResponse, type);
                amoutinputEt.setText(RateFormatUtil.longToDoubleBtc(editDefault));
                otherRate.setRate(rateBean.getRate());
                ParamManager.getInstance().putCountryRate(otherRate);
            }
        });
    }

    /**
     * Get the wallet balance
     */
    private void requestWallet(){
        String url = String.format(UriUtil.BLOCKCHAIN_UNSPENT_INFO, MemoryDataManager.getInstance().getAddress());
        accountBean = new WalletAccountBean(0L,0L);
        OkHttpUtil.getInstance().get(url, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    if (response.getCode() == 2000) {
                        Connect.UnspentAmount unspentAmount = Connect.UnspentAmount.parseFrom(response.getBody());
                        accountBean = new WalletAccountBean(unspentAmount.getAmount(),unspentAmount.getAvaliableAmount());
                        amountTv.setText(BaseApplication.getInstance().getString(R.string.Wallet_Balance_Credit,
                                RateFormatUtil.longToDoubleBtc(accountBean.getAvaAmount())));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                amountTv.setText(BaseApplication.getInstance().getString(R.string.Wallet_Balance_Credit,
                        RateFormatUtil.longToDoubleBtc(0)));
            }
        });
    }

    /**
     * Gets the current input bitcoin
     * @return
     */
    public String getCurrentBtc(){
        return currentBtc;
    }

    /**
     * available balance
     * @return
     */
    public Long getAvaAmount(){
        return null == accountBean.getAvaAmount() ? 0 : accountBean.getAvaAmount();
    }

    /**
     * Hide it when you don't need to display the available amount
     */
    public void setAmountTvGone(){
        amountTv.setVisibility(View.GONE);
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
     * Whether the available balance is visible
     * @param visibility
     */
    public void setVisibilityAmount(int visibility){
        amountTv.setVisibility(visibility);
    }

    /**
     * Set fee display
     * @param visibility
     */
    public void setFeeVisibility(int visibility){
        findViewById(R.id.linearlayout).setVisibility(visibility);
    }

    public interface OnEditListener {

        void onEdit(String value);

        void setFee();

    }

}
