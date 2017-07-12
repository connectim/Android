package connect.database.green.DaoHelper;

import android.text.TextUtils;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.Collections;
import java.util.List;

import connect.database.green.BaseDao;
import connect.database.green.bean.TransactionEntity;
import connect.database.green.dao.TransactionEntityDao;

/**
 * Created by pujin on 2017/1/13.
 */
public class TransactionHelper extends BaseDao {
    private static TransactionHelper transactionHelper;
    private TransactionEntityDao transactionEntityDao;

    public TransactionHelper() {
        super();
        transactionEntityDao = daoSession.getTransactionEntityDao();
    }

    public static TransactionHelper getInstance() {
        if (transactionHelper == null) {
            transactionHelper = new TransactionHelper();
        }
        return transactionHelper;
    }

    public static void closeHelper() {
        transactionHelper = null;
    }

    /**********************************************************************************************
     *                                      select
     *********************************************************************************************/
    public TransactionEntity loadTransEntity(String hashid) {
        QueryBuilder<TransactionEntity> queryBuilder = transactionEntityDao.queryBuilder();
        queryBuilder.where(TransactionEntityDao.Properties.Hashid.eq(hashid)).limit(1).build();
        List<TransactionEntity> transEntities = queryBuilder.listLazy();
        return transEntities.size() == 0 ? null : transEntities.get(0);
    }

    /**
     * The query in recent 10 transfer record
     * @return
     */
    public List<TransactionEntity> loadLatelyTrans() {
        QueryBuilder<TransactionEntity> queryBuilder = transactionEntityDao.queryBuilder();
        queryBuilder.limit(10);
        List<TransactionEntity> transEntities = queryBuilder.list();
        if(transEntities != null)
            Collections.reverse(transEntities);
        return transEntities;
    }

    /**********************************************************************************************
     *                                     add/update
     *********************************************************************************************/

    /**
     * Update the transfer transaction status
     *
     * @param hashid
     * @param messageid "":do not update messageid
     * @param state
     */
    public void updateTransEntity(String hashid, String messageid, int state) {
        TransactionEntity transactionEntity = TransactionHelper.getInstance().loadTransEntity(hashid);
        if (transactionEntity == null) {
            transactionEntity = new TransactionEntity();
            transactionEntity.setHashid(hashid);
        }
        transactionEntity.setStatus(state);

        if (TextUtils.isEmpty(transactionEntity.getMessage_id()) && !TextUtils.isEmpty(messageid)) {
            transactionEntity.setMessage_id(messageid);
        }
        transactionEntityDao.insertOrReplace(transactionEntity);
    }

    /**
     * update state
     * @param hashid
     * @param messageid "":do not update messageid
     * @param paycount -1:The current pay toll increased 1
     * @param crowdcount 0:Don't modify the current the raise
     */
    public void updateTransEntity(String hashid, String messageid, int paycount, int crowdcount) {
        TransactionEntity transactionEntity = TransactionHelper.getInstance().loadTransEntity(hashid);
        if (transactionEntity == null) {
            transactionEntity = new TransactionEntity();
            transactionEntity.setHashid(hashid);
        }
        transactionEntity.setStatus(paycount == crowdcount ? 2 : 1);

        int pay_count = transactionEntity.getPay_count() == null ? 0 : transactionEntity.getPay_count();
        transactionEntity.setPay_count(paycount == -1 ? 1 + pay_count : paycount);//-1: increase 1

        if (TextUtils.isEmpty(transactionEntity.getMessage_id()) && !TextUtils.isEmpty(messageid)) {
            transactionEntity.setMessage_id(messageid);
        }
        if (crowdcount != 0) {
            transactionEntity.setCrowd_count(crowdcount);
        }
        transactionEntityDao.insertOrReplace(transactionEntity);
    }
}