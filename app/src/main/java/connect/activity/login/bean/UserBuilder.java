package connect.activity.login.bean;

/**
 * Created by Administrator on 2016/12/2.
 */
public class UserBuilder {

    public String talkKey;
    public String name;
    public String avatar;
    public String passHint;
    public String priKey;
    public String pubKey;
    public String address;
    public String phone;

    public UserBuilder talkKey(String name){
        this.name=name;
        return this;
    }

    public UserBuilder name(String name){
        this.name=name;
        return this;
    }

    public UserBuilder avatar(String name){
        this.name=name;
        return this;
    }

    public UserBuilder passHint(String name){
        this.name=name;
        return this;
    }

    public UserBuilder priKey(String name){
        this.name=name;
        return this;
    }

    public UserBuilder pubKey(String name){
        this.name=name;
        return this;
    }

    public UserBuilder address(String name){
        this.name=name;
        return this;
    }

    public UserBuilder phone(String name){
        this.name=name;
        return this;
    }

    /*public UserBean build(){
        return new UserBean(this);
    }*/

}
