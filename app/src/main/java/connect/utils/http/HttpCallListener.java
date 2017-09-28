package connect.utils.http;

public abstract class HttpCallListener<T> {

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

}