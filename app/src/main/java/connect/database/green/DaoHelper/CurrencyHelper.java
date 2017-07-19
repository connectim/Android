package connect.database.green.DaoHelper;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import connect.database.green.BaseDao;
import connect.database.green.bean.CurrencyAddressEntity;
import connect.database.green.bean.CurrencyEntity;
import connect.database.green.dao.CurrencyAddressEntityDao;
import connect.database.green.dao.CurrencyEntityDao;
import wallet_gateway.WalletOuterClass;

/**
 * Currency database management
 */
public class CurrencyHelper extends BaseDao{

    private static CurrencyHelper currencyHelper;
    private final CurrencyEntityDao currencyEntityDao;
    private final CurrencyAddressEntityDao currencyAddressEntityDao;

    public CurrencyHelper() {
        super();
        currencyEntityDao = daoSession.getCurrencyEntityDao();
        currencyAddressEntityDao = daoSession.getCurrencyAddressEntityDao();
    }

    public static CurrencyHelper getInstance() {
        if (currencyHelper == null) {
            synchronized (ContactHelper.class) {
                if (currencyHelper == null) {
                    currencyHelper = new CurrencyHelper();
                }
            }
        }
        return currencyHelper;
    }

    /*********************************  load ***********************************/
    /**
     * Query all currencies
     * @return
     */
    public List<CurrencyEntity> loadCurrencyList(){
         return currencyEntityDao.loadAll();
    }

    /**
     * Depending on the type of currency query currency
     * @param currency
     * @return
     */
    public CurrencyEntity loadCurrency(int currency){
        QueryBuilder<CurrencyEntity> queryBuilder = currencyEntityDao.queryBuilder();
        queryBuilder.where(CurrencyEntityDao.Properties.Currency.eq(currency)).limit(1).build();
        List<CurrencyEntity> currencyEntities = queryBuilder.list();
        if (null == currencyEntities || currencyEntities.size() == 0) {
            return null;
        }
        return currencyEntities.get(0);
    }

    /**
     * Query all currency address
     * @return
     */
    private List<CurrencyAddressEntity> loadCurrencyAddress(){
        return currencyAddressEntityDao.loadAll();
    }

    public CurrencyAddressEntity loadCurrencyAddressFromAddress(String address) {
        QueryBuilder<CurrencyAddressEntity> queryBuilder = currencyAddressEntityDao.queryBuilder();
        queryBuilder.where(CurrencyAddressEntityDao.Properties.Address.eq(address)).limit(1).build();
        List<CurrencyAddressEntity> currencyAddressEntities = queryBuilder.list();
        if (null == currencyAddressEntities || currencyAddressEntities.size() == 0) {
            return null;
        }
        return currencyAddressEntities.get(0);
    }

    public List<CurrencyAddressEntity> loadCurrencyAddress(int currencyCode) {
        QueryBuilder<CurrencyAddressEntity> queryBuilder = currencyAddressEntityDao.queryBuilder();
        queryBuilder.where(CurrencyAddressEntityDao.Properties.Currency.eq(currencyCode)).limit(1).build();
        List<CurrencyAddressEntity> currencyAddressEntities = queryBuilder.list();
        if (null == currencyAddressEntities || currencyAddressEntities.size() == 0) {
            return null;
        }
        return currencyAddressEntities;
    }

    /*********************************  insert ***********************************/
    /**
     * insert currencies
     * @param list
     */
    public void insertCurrencyListCoin(List<WalletOuterClass.Coin> list){
        for(WalletOuterClass.Coin coin : list){
            CurrencyEntity currencyEntity = loadCurrency(coin.getCurrency());
            if(currencyEntity == null){
                currencyEntity = new CurrencyEntity();
            }
            currencyEntity.setSalt(coin.getSalt());
            currencyEntity.setBalance(coin.getBalance());
            currencyEntity.setAmount(coin.getAmount());
            currencyEntity.setCurrency(coin.getCurrency());
            currencyEntity.setCategory(coin.getCategory());
            currencyEntity.setPayload(coin.getPayload());
            currencyEntity.setStatus(coin.getStatus());
            insertCurrency(currencyEntity);
        }
    }

    /**
     * insert currencies
     * @param list
     */
    public void insertCurrencyList(List<CurrencyEntity> list){
        currencyEntityDao.insertOrReplaceInTx(list);
    }

    /**
     * insert single currency
     * @param currencyEntity
     */
    public void insertCurrency(CurrencyEntity currencyEntity){
        QueryBuilder<CurrencyEntity> qb = currencyEntityDao.queryBuilder();
        qb.where(CurrencyEntityDao.Properties.Currency.eq(currencyEntity.getCurrency()));
        List<CurrencyEntity> list = qb.list();
        if (list.size() > 0) {
            deleteCurrencyEntity(currencyEntity.getCurrency());
        }
        currencyEntityDao.insertOrReplace(currencyEntity);
    }

    /**
     * insert currencies address
     * @param list
     */
    public void insertCurrencyAddressListCoinInfo(List<WalletOuterClass.CoinInfo> list,int currencyCode){

        for(WalletOuterClass.CoinInfo coinInfo : list){
            CurrencyAddressEntity addressEntity = loadCurrencyAddressFromAddress(coinInfo.getAddress());
            if(addressEntity == null){
                addressEntity = new CurrencyAddressEntity();
            }
            addressEntity.setIndex(coinInfo.getIndex());
            addressEntity.setAddress(coinInfo.getAddress());
            addressEntity.setLabel(coinInfo.getLabel());
            addressEntity.setStatus(coinInfo.getStatus());
            addressEntity.setBalance(coinInfo.getBalance());
            addressEntity.setAmount(coinInfo.getAmount());
            addressEntity.setCurrency(currencyCode);
            insertCurrencyAddress(addressEntity);
        }
    }

    public void insertCurrencyAddressList(List<CurrencyAddressEntity> list){
        currencyAddressEntityDao.insertOrReplaceInTx(list);
    }

    /**
     * insert single currency address
     * @param currencyAddressEntity
     */
    public void insertCurrencyAddress(CurrencyAddressEntity currencyAddressEntity){
        QueryBuilder<CurrencyAddressEntity> qb = currencyAddressEntityDao.queryBuilder();
        qb.where(CurrencyAddressEntityDao.Properties.Currency.eq(currencyAddressEntity.getCurrency()));
        List<CurrencyAddressEntity> list = qb.list();
        if (list.size() > 0) {
            deleteCurrencyAddressEntity(currencyAddressEntity.getAddress());
        }
        currencyAddressEntityDao.insertOrReplace(currencyAddressEntity);
    }
    /*********************************  update ***********************************/
    /**
     * update single currency
     * @param currencyEntity
     */
    public void updateCurrency(CurrencyEntity currencyEntity){
        currencyEntityDao.update(currencyEntity);
    }

    /**
     * update single currency
     * @param currencyAddressEntity
     */
    public void updateCurrency(CurrencyAddressEntity currencyAddressEntity){
        currencyAddressEntityDao.update(currencyAddressEntity);
    }

    /*********************************  delete ***********************************/
    /**
     * Removing a single currency
     * @param currency
     */
    public void deleteCurrencyEntity(int currency) {
        QueryBuilder<CurrencyEntity> qb = currencyEntityDao.queryBuilder();
        DeleteQuery<CurrencyEntity> bd = qb.where(CurrencyEntityDao.Properties.Currency.eq(currency))
                .buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

    /**
     * Removing a single currency address
     * @param address
     */
    public void deleteCurrencyAddressEntity(String address) {
        QueryBuilder<CurrencyAddressEntity> qb = currencyAddressEntityDao.queryBuilder();
        DeleteQuery<CurrencyAddressEntity> bd = qb.where(CurrencyAddressEntityDao.Properties.Address.eq(address))
                .buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }
}
