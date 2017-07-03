package connect.widget.prompt;

import android.content.Context;
import android.view.View;

/**
 * Created by chenpengfei on 2016/11/2.
 */
public class ChatPromptViewManager extends PromptViewHelper.PromptViewManager {

    public ChatPromptViewManager(Context activity, String[] dataArray) {
        super(activity, dataArray);
    }

    @Override
    public View inflateView() {
        return new PromptView(activity);
    }

    @Override
    public void bindData(View view, final String[] dataArray) {
        if (view instanceof PromptView) {
            PromptView promptView = (PromptView) view;
            promptView.setContentArray(dataArray);
            promptView.setOnItemClickListener(new PromptView.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    if (onItemClickListener != null)
                        onItemClickListener.onPromptClick(dataArray[position]);
                }
            });
        }
    }
}
