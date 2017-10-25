package connect.utils.exception.inter;

import connect.utils.exception.BaseException;

/**
 * Created by Administrator on 2017/10/24.
 */

public interface ICrash {

    BaseException dispath();

    BaseException upload();
}
