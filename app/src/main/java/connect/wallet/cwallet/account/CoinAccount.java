package connect.wallet.cwallet.account;

import connect.wallet.cwallet.inter.WalletListener;

public interface CoinAccount {

    /**
     * Balance
     */
    public void balance();

    /**
     * Hide the address
     */
    public void hideAddress(String address);

    /**
     * Get the address list
     */
    public void requestAddressList(WalletListener listener);
}
