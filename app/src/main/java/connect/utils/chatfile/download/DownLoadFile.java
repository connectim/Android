package connect.utils.chatfile.download;

import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupEntity;
import connect.utils.StringUtil;
import connect.utils.chatfile.inter.InterFileDown;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.okhttp.HttpRequest;
import instant.bean.Session;
import instant.bean.UserCookie;
import instant.utils.cryption.SupportKeyUril;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import protos.Connect;

/**
 * File download tool
 */
public class DownLoadFile {

    private Handler mDelivery = new Handler(Looper.getMainLooper());

    private Connect.ChatType chatType;
    private String identify;
    private String url;
    private InterFileDown fileDownLoad;

    public DownLoadFile(Connect.ChatType chatType, String identify, String url, final InterFileDown fileDownLoad) {
        this.chatType = chatType;
        this.identify = identify;
        this.url = url;
        this.fileDownLoad = fileDownLoad;
    }

    public void downFile() {
        Request request = new Request.Builder()
                .url(url)
                .build();
        HttpRequest.getInstance().mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                resultFail();
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if (response.code() == 200) {
                    InputStream inputStream = null;
                    ByteArrayOutputStream outputStream = null;
                    try {
                        long current = 0;
                        int length = 0;
                        long total = response.body().contentLength();
                        byte[] buffer = new byte[1024];

                        outputStream = new ByteArrayOutputStream();
                        inputStream = response.body().byteStream();
                        while ((length = inputStream.read(buffer)) != -1) {
                            current += length;
                            outputStream.write(buffer, 0, length);
                            resultProgress(current, total);
                        }
                        outputStream.flush();
                        resultSuccess(outputStream.toByteArray());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                            inputStream = null;
                        }
                        if (outputStream != null) {
                            outputStream.close();
                            outputStream = null;
                        }
                    }
                } else {
                    resultFail();
                }
            }
        });
    }

    public void resultFail(){
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                fileDownLoad.failDown();
            }
        });
    }

    public void resultSuccess(final byte[] bytes) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (chatType == Connect.ChatType.CONNECT_SYSTEM) {
                        fileDownLoad.successDown(bytes);
                    } else {
                        Connect.GcmData gcmData = Connect.GcmData.parseFrom(bytes);
                        Connect.StructData structData = null;
                        byte[] dataFile = null;
                        if (chatType == Connect.ChatType.PRIVATE) {//private chat
                            UserCookie userCookie = Session.getInstance().getChatCookie();
                            String myPrivateKey = userCookie.getPriKey();

                            UserCookie friendCookie = Session.getInstance().getFriendCookie(identify);
                            String friendPublicKey = friendCookie.getPubKey();

                            EncryptionUtil.ExtendedECDH ecdhExts = EncryptionUtil.ExtendedECDH.OTHER;
                            ecdhExts.setBytes(SupportKeyUril.xor(userCookie.getSalt(), friendCookie.getSalt()));
                            dataFile = DecryptionUtil.decodeAESGCM(ecdhExts, myPrivateKey, friendPublicKey, gcmData);
                        } else if (chatType == Connect.ChatType.GROUPCHAT) {//group chat
                            GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(identify);
                            structData = DecryptionUtil.decodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY, StringUtil.hexStringToBytes(groupEntity.getEcdh_key()), gcmData);
                            dataFile = structData.getPlainData().toByteArray();
                        }
                        fileDownLoad.successDown(dataFile);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void resultProgress(final long current, final long total){
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                fileDownLoad.onProgress(current,total);
            }
        });
    }
}
