package instant.ui.service;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import java.nio.ByteBuffer;

import instant.sender.inter.LocalServiceListener;
import instant.sender.SenderManager;
import instant.utils.log.LogManager;

/**
 * Created by puin on 17-10-8.
 */

public class SenderService extends ReceiverService implements LocalServiceListener {

    private String Tag = "_LocalSendService";

    @Override
    public void onCreate() {
        super.onCreate();
        SenderManager.getInstance().registerListener(this);
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, SenderService.class);
        context.startService(intent);
    }

    @Override
    public void messageSend(byte[] ack, ByteBuffer byteBuffer) {
        try {
            pushBinder.connectMessage(ack, byteBuffer.array());
        } catch (RemoteException e) {
            e.printStackTrace();
            LogManager.getLogger().d(Tag,e.getMessage());
        }
    }

    @Override
    public void exitAccount() {
        try {
            pushBinder.connectExit();
        } catch (RemoteException e) {
            e.printStackTrace();
            LogManager.getLogger().d(Tag, e.getMessage());
        }
    }
}
