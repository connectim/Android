package connect.utils.okhttp;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import okhttp3.Response;

/**
 * Parsing ProtoBuff and back to the mediation analysis results
 */
public abstract class ResultCall<T> extends Callback<T> {

    private T bean = null;

    @Override
    public Integer parseNetworkResponse(Response response) throws Exception {
        byte[] data = response.body().bytes();
        Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

        Method method = entityClass.getMethod("parseFrom", byte[].class);
        bean = (T) method.invoke(null, data);

        int code = (Integer) entityClass.getMethod("getCode").invoke(bean);
        return code;
    }

    public T getData(){
        return bean;
    }

}
