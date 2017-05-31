package connect.utils.okhttp;

import okhttp3.Response;

public abstract class Callback<T> {

    /**
     * parse ProtoBuff
     * @param response
     * @return
     * @throws Exception
     */
    public abstract Integer parseNetworkResponse(Response response) throws Exception;

    /**
     * Access to success
     * @param response
     */
    public abstract void onResponse(T response);

    /**
     * Access to fail
     * @param response
     */
    public abstract void onError(T response);

    /**
     * Network Not Works
     */
    public void onError() {

    }
}