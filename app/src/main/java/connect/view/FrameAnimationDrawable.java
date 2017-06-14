package connect.view;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.widget.ImageView;

import connect.utils.BitmapUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Frame animation
 * Created by gtq on 2016/12/28.
 */
public class FrameAnimationDrawable {

    /**
     * Drawable
     */
    public static class FrameDrawable {
        byte[] bytes;
        int duration;
        Drawable drawable;
        boolean isReady = false;
    }

    /**
     * Frame animation initialization
     * @param resourceId animation-list
     * @param imageView The picture of the display
     */
    public static void animateDrawableLoad(int resourceId, final ImageView imageView,final OnDrawablesListener listener) {
        loadRaw(resourceId, imageView.getContext(), new OnDrawableLoadedListener() {
            @Override
            public void onDrawableLoaded(List<FrameDrawable> drawables) {
                if (listener != null) {
                    listener.onDrawsStart();
                }
                animateRawManually(drawables, imageView, listener);
            }
        });
    }

    /**
     * xml Parse is complete interface
     */
    public interface OnDrawableLoadedListener {
        void onDrawableLoaded(List<FrameDrawable> myFrames);
    }

    private OnDrawablesListener onDrawablesListener;

    /**
     * The flash status callback
     */
    public interface OnDrawablesListener {
        void onDrawsStart();

        void onDrawsStop();
    }

    /**
     * parse animation-list
     * @param resourceId
     * @param context
     * @param onDrawableLoadedListener
     */
    private static void loadFromXml(final int resourceId, final Context context, final OnDrawableLoadedListener onDrawableLoadedListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<FrameDrawable> myFrames = new ArrayList<>();
                XmlResourceParser parser = context.getResources().getXml(resourceId);

                try {
                    int eventType = parser.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_DOCUMENT) {
                        } else if (eventType == XmlPullParser.START_TAG) {
                            if (parser.getName().equals("item")) {
                                byte[] bytes = null;
                                int duration = 1000;
                                for (int i = 0; i < parser.getAttributeCount(); i++) {
                                    if (parser.getAttributeName(i).equals("drawable")) {
                                        int resId = Integer.parseInt(parser.getAttributeValue(i).substring(1));
                                        bytes = InputStreamToByte(context.getResources().openRawResource(resId));
                                    } else if (parser.getAttributeName(i).equals("duration")) {
                                        duration = parser.getAttributeIntValue(i, 1000);
                                    }
                                }

                                FrameDrawable frameDrawable = new FrameDrawable();
                                frameDrawable.bytes = bytes;
                                frameDrawable.duration = duration;
                                myFrames.add(frameDrawable);
                            }
                        } else if (eventType == XmlPullParser.END_TAG) {
                        } else if (eventType == XmlPullParser.TEXT) {
                        }
                        eventType = parser.next();
                    }
                } catch (IOException | XmlPullParserException e) {
                    e.printStackTrace();
                }

                // Run on UI Thread
                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (onDrawableLoadedListener != null) {
                            onDrawableLoadedListener.onDrawableLoaded(myFrames);
                        }
                    }
                });
            }
        }).run();
    }

    public static byte[] InputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();
        return imgdata;
    }

    public static void loadRaw(final int resourceId, final Context context, final OnDrawableLoadedListener onDrawableLoadedListener) {
        loadFromXml(resourceId, context, onDrawableLoadedListener);
    }


    public static void animateRawManually(List<FrameDrawable> myFrames, ImageView imageView,final OnDrawablesListener listener) {
        animateRawManually(myFrames, imageView, listener, 0);
    }

    private static void animateRawManually(final List<FrameDrawable> myFrames, final ImageView imageView, final OnDrawablesListener listener, final int frameNumber) {
        final FrameDrawable thisFrame = myFrames.get(frameNumber);

        if (frameNumber == 0) {
            thisFrame.drawable = new BitmapDrawable(imageView.getContext().getResources(), BitmapFactory.decodeByteArray(thisFrame.bytes, 0, thisFrame.bytes.length));
        } else {
            FrameDrawable previousFrame = myFrames.get(frameNumber - 1);
            ((BitmapDrawable) previousFrame.drawable).getBitmap().recycle();
            previousFrame.drawable = null;
            previousFrame.isReady = false;
        }

        imageView.setImageDrawable(thisFrame.drawable);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Make sure ImageView hasn't been changed to a different Image in this time
                if (imageView.getDrawable() == thisFrame.drawable) {
                    if (frameNumber + 1 < myFrames.size()) {
                        FrameDrawable nextFrame = myFrames.get(frameNumber + 1);

                        if (nextFrame.isReady) {
                            // Animate next frame
                            animateRawManually(myFrames, imageView, listener, frameNumber + 1);
                        } else {
                            nextFrame.isReady = true;
                        }
                    } else {
                        if (listener != null) {
                            listener.onDrawsStop();
                        }
                    }
                }
            }
        }, thisFrame.duration);

        // Load next frame
        if (frameNumber + 1 < myFrames.size()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FrameDrawable nextFrame = myFrames.get(frameNumber + 1);
                    nextFrame.drawable = new BitmapDrawable(imageView.getContext().getResources(), BitmapFactory.decodeByteArray(nextFrame.bytes, 0, nextFrame.bytes.length));
                    if (nextFrame.isReady) {
                        // Animate next frame
                        animateRawManually(myFrames, imageView, listener, frameNumber + 1);
                    } else {
                        nextFrame.isReady = true;
                    }

                }
            }).run();
        }
    }
}