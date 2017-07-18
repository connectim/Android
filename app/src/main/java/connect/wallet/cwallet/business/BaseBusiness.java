package connect.wallet.cwallet.business;

import java.util.List;

import connect.activity.wallet.manager.TransferManager;
import wallet_gateway.WalletOuterClass;

/**
 * 具体业务层接口
 * Created by Administrator on 2017/7/18.
 */
public abstract class BaseBusiness {

    /**
     * 转账
     * @param txin
     * @param outPuts
     * @param onResultCall
     */
    void transfer(WalletOuterClass.Txin txin,
                  List<WalletOuterClass.Txout> outPuts, TransferManager.OnResultCall onResultCall){

    }

    /**
     * 红包
     * @param txin
     * @param reciverId
     * @param type
     * @param size
     * @param amount
     * @param category
     * @param onResultCall
     */
    void luckyPacket(WalletOuterClass.Txin txin, String reciverId, int type, int size,
                     long amount, int category, TransferManager.OnResultCall onResultCall){

    }

    /**
     * 外部转账
     *
     * @param fromlist
     * @param indexList
     * @param amount
     */
    void outerTransfer(List<String> fromlist, List<Integer> indexList, long amount,
                       TransferManager.OnResultCall onResultCall){

    }

    /**
     * 支付
     *
     * @param hashId
     * @param payType
     * @param onResultCall
     */
    void payment(String hashId, int payType, TransferManager.OnResultCall onResultCall){

    }
}
