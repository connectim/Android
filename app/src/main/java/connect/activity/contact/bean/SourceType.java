package connect.activity.contact.bean;

import connect.ui.activity.R;
import connect.activity.base.BaseApplication;

import java.io.Serializable;

/**
 * friend from source
 * Created by Administrator on 2016/12/28.
 */
public enum SourceType implements Serializable {
    UNKOWN(0, R.string.Link_From_Unknow),
    CONTACT(1,R.string.Link_From_Contact_Match),
    QECODE(2,R.string.Link_From_QR_Code),
    TRANSACTION(3,R.string.Link_From_Transaction),
    GROUP(4,R.string.Link_From_Group),
    SEARCH(5,R.string.Link_From_Search),
    RECOMMEND(6,R.string.Link_From_Recommend),
    MAYKNOW(7,R.string.Link_From_May_Know),
    CARD(8,R.string.Link_From_friends_to_share),;

    private int type;
    private int resId;

    SourceType(int type,int resId) {
        this.type = type;
        this.resId = resId;
    }

    public String getString(){
        return BaseApplication.getInstance().getResources().getString(this.resId);
    }

    public int getType(){
        return this.type;
    }

    public static String getString(int type){
        for(SourceType sourceType : SourceType.values()){
            if(sourceType.type == type){
                return sourceType.getString();
            }
        }
        return "";
    }

    public static SourceType getSourceType(int type){
        for(SourceType sourceType : SourceType.values()){
            if(sourceType.type == type){
                return sourceType;
            }
        }
        return null;
    }

}
