package connect.utils.http;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;

import connect.utils.okhttp.HttpRequest;
import io.reactivex.Observable;
import protos.Connect;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import wallet_gateway.WalletOuterClass;

/**
 * Created by Administrator on 2017/9/27 0027.
 */

public interface BaseApi {
    /**======================================================================================
     *                                Login
     * ====================================================================================== */

    @GET("/launch_images/v1/{hash}/images")
    Observable<Object> getLaunchImages(@Path("hash") String hash);

    @POST("/connect/v1/sms/send")
    Observable<Connect.HttpNotSignResponse> smsSend(@Body Connect.SendMobileCode sendMobileCode);

    @POST("/connect/v1/sign_in")
    Observable<Connect.HttpNotSignResponse> signIn(@Body Connect.MobileVerify sendMobileCode);

    @Multipart
    @POST("/avatar/v1/up")
    Observable<Connect.HttpNotSignResponse> avatarUp(@Part byte[] headByte);

    @Multipart
    @POST("/connect/v1/sign_up")
    Observable<Connect.HttpNotSignResponse> signUp(@Body byte[] headByte);

    @POST("/fs/v1/up")
    Observable<Connect.HttpNotSignResponse> uploadFile(@Body Connect.MediaFile mediaFile);

    @POST("/connect/v1/private/sign_in")
    Observable<Connect.HttpNotSignResponse> privateSignIn(@Body Connect.UserPrivateSign userPrivateSign);

    /**======================================================================================
     *                                Login successfully initialized
     * ====================================================================================== */

    @POST("/connect/v1/users/expire/salt")
    Observable<Connect.HttpResponse> expireSalt(@Body ByteString byteString);

    @POST("/connect/v1/users/salt")
    Observable<Connect.HttpResponse> usersSalt(@Body Connect.GenerateToken generateToken);

    @POST("/connect/v1/version")
    Observable<Connect.HttpResponse> getVersion(@Body Connect.VersionRequest versionRequest);

    /**======================================================================================
     *                                setting
     * ====================================================================================== */

    @POST("/connect/v1/users/search")
    Observable<Connect.HttpResponse> getVersion(@Body Connect.SearchUser searchUser);

    @POST("/connect/v1/setting/userinfo")
    Observable<Connect.HttpResponse> setUserInfo(@Body Connect.IMRequest imRequest);

    @POST("/connect/v1/setting/connectId")
    Observable<Connect.HttpResponse> setConnectId(@Body Connect.ConnectId connectId);

    @POST("/connect/v1/setting/avatar")
    Observable<Connect.HttpResponse> setAvatar(@Body Connect.Avatar avatar);

    @POST("/connect/v1/setting/bind/mobile")
    Observable<Connect.HttpResponse> setBindMobile(@Body Connect.MobileVerify mobileVerify);

    @POST("/connect/v1/setting/unbind/mobile")
    Observable<Connect.HttpResponse> setUnbindMobile(@Body Connect.MobileVerify mobileVerify);

    @POST("/connect/v1/setting/backup/key")
    Observable<Connect.HttpResponse> setBackupKey(@Body Connect.ChangeLoginPassword changeLoginPassword);

    @POST("/connect/v1/setting/privacy/info")
    Observable<Connect.HttpResponse> setPrivacyInfo(@Body ByteString byteString);

    @POST("/connect/v1/setting/phonebook/sync")
    Observable<Connect.HttpResponse> setPhoneBookSync(@Body Connect.PhoneBook phoneBook);

    @POST("/connect/v1/setting/privacy")
    Observable<Connect.HttpResponse> setPrivacy(@Body Connect.Privacy privacy);

    @POST("/connect/v1/setting/pay/setting/sync")
    Observable<Connect.HttpResponse> setPaySettingSync(@Body ByteString byteString);

    @POST("/connect/v1/setting/pay/setting")
    Observable<Connect.HttpResponse> setPaySetting(@Body Connect.PaymentSetting paymentSetting);

    @POST("/connect/v1/setting/pay/pin/version")
    Observable<Connect.HttpResponse> setPayPinVersion(@Body Connect.PayPinVersion payPinVersion);

    /**======================================================================================
     *                                wallet
     * ====================================================================================== */

    @GET("/connect/v1/estimatefee")
    Observable<String> getEstimatefee();

    @POST("/wallet/v1/red_package/history")
    Observable<Connect.HttpResponse> redPackageHistory(@Body Connect.History history);

    @POST("/wallet/v1/red_package/info/{token}")
    Observable<Connect.HttpResponse> redPackageInfo(@Path("token") String token,@Body ByteString byteString);

    @POST("/wallet/v1/billing/external/cancel")
    Observable<Connect.HttpResponse> billingExternalCancel(@Body Connect.BillHashId billHashId);

    @POST("/wallet/v1/billing/external/history")
    Observable<Connect.HttpResponse> billingExternalHistory(@Body Connect.History history);

    @POST("/wallet/v1/address_book/list")
    Observable<Connect.HttpResponse> addressBookList(@Body ByteString byteString);

    @POST("/wallet/v1/address_book/add")
    Observable<Connect.HttpResponse> addressBookAdd(@Body Connect.AddressBook.AddressInfo addressInfo);

    @POST("/wallet/v1/address_book/tag")
    Observable<Connect.HttpResponse> addressBookTag(@Body Connect.AddressBook.AddressInfo addressInfo);

    @POST("/wallet/v1/address_book/remove")
    Observable<Connect.HttpResponse> addressBookRemove(@Body Connect.AddressBook.AddressInfo addressInfo);

    @POST("/wallet/v2/create")
    Observable<Connect.HttpResponse> walletCreate(@Body WalletOuterClass.RequestWalletInfo requestWalletInfo);

    @POST("/wallet/v2/sync")
    Observable<Connect.HttpResponse> walletSync(@Body ByteString byteString);

    @POST("/wallet/v2/coins/create")
    Observable<Connect.HttpResponse> walletCoinsCreate(@Body WalletOuterClass.CreateCoinRequest createCoinRequest);

    @POST("/wallet/v2/update")
    Observable<Connect.HttpResponse> walletUpdate(@Body WalletOuterClass.RequestWalletInfo requestWalletInfo);

    @POST("/wallet/v2/coins/update")
    Observable<Connect.HttpResponse> walletCoinsUpdate(@Body WalletOuterClass.Coin coin);

    @POST("/wallet/v2/coins/addresses/get_default")
    Observable<Connect.HttpResponse> walletCoinsAddressGetDefault(@Body ByteString byteString);

    @POST("/wallet/v2/service/transfer")
    Observable<Connect.HttpResponse> walletServiceTransfer(@Body WalletOuterClass.ConnectTransferRequest connectTransferRequest);

    @POST("/wallet/v2/coins/addresses/list")
    Observable<Connect.HttpResponse> walletCoinsAddressList(@Body WalletOuterClass.Coin coin);

    @POST("/wallet/v2/service/user_status")
    Observable<Connect.HttpResponse> walletServiceUserStatus(@Body WalletOuterClass.RequestUserInfo requestUserInfo);

    @POST("/wallet/v2/service/receive")
    Observable<Connect.HttpResponse> walletServiceReceive(@Body WalletOuterClass.ReceiveRequest receiveRequest);

    @POST("/wallet/v2/service/payment")
    Observable<Connect.HttpResponse> walletServicePayment(@Body WalletOuterClass.Payment payment);

    @POST("/wallet/v2/service/luckpackage")
    Observable<Connect.HttpResponse> walletServiceLuckPackage(@Body WalletOuterClass.LuckyPackageRequest luckyPackageRequest);

    @POST("/wallet/v2/service/external")
    Observable<Connect.HttpResponse> walletServiceExternal(@Body WalletOuterClass.OutTransfer outTransfer);

    @POST("/wallet/v2/service/crowdfuning")
    Observable<Connect.HttpResponse> walletServiceCrowdFuning(@Body WalletOuterClass.CrowdfundingRequest crowdfundingRequest);

    @POST("/wallet/v2/service/publish")
    Observable<Connect.HttpResponse> walletServicePublish(@Body WalletOuterClass.PublishTransaction publishTransaction);

    @POST("/wallet/v2/service/transfer_to_address")
    Observable<Connect.HttpResponse> walletServiceTransferToAddress(@Body WalletOuterClass.TransferRequest transferRequest);

    @POST("/wallet/v2/coins/addresses/tx")
    Observable<Connect.HttpResponse> walletCoinsAddressTx(@Body WalletOuterClass.GetTx getTx);

    @POST("/wallet/v2/coins/info")
    Observable<Connect.HttpResponse> walletCoinsInfo(@Body WalletOuterClass.Coin coin);

    /**======================================================================================
     *                                contact
     * ====================================================================================== */

    @POST("/connect/v1/blacklist/")
    Observable<Connect.HttpResponse> blackList(@Body Connect.UserIdentifier userIdentifier);

    @POST("/connect/v1/blacklist/remove")
    Observable<Connect.HttpResponse> blackListRemove(@Body Connect.UserIdentifier userIdentifier);

    @POST("/connect/v1/blacklist/list")
    Observable<Connect.HttpResponse> blackListList(@Body ByteString byteString);

    @POST("/connect/v1/users/phonebook")
    Observable<Connect.HttpResponse> usersPhoneBook(@Body Connect.RequestNotEncrypt notEncrypt);

    @POST("/connect/v1/users/recommend")
    Observable<Connect.HttpResponse> usersRecommend(@Body ByteString byteString);

    @POST("/connect/v1/users/friends/records")
    Observable<Connect.HttpResponse> usersFriendsRecords(@Body Connect.FriendRecords friendRecords);

    @POST("/connect/v1/users/searchByPubKey")
    Observable<Connect.HttpResponse> usersSearchByPubKey(@Body Connect.SearchUser searchUser);

    /**======================================================================================
     *                                      setting group
     * ======================================================================================*/

    @POST("/connect/v1/group")
    Observable<Connect.HttpResponse> group(@Body Connect.CreateGroup createGroup);

    @POST("/connect/v1/group/deluser")
    Observable<Connect.HttpResponse> groupDelUser(@Body Connect.DelOrQuitGroupMember delMember);

    @POST("/connect/v1/group/quit")
    Observable<Connect.HttpResponse> groupQuit(@Body Connect.GroupId groupId);

    @POST("/connect/v1/group/set_common")
    Observable<Connect.HttpResponse> groupSetCommon(@Body Connect.GroupId groupId);

    @POST("/connect/v1/group/remove_common")
    Observable<Connect.HttpResponse> groupRemoveCommon(@Body Connect.GroupId groupId);

    @POST("/connect/v1/group/member_update")
    Observable<Connect.HttpResponse> groupMemberUpdate(@Body Connect.UpdateGroupMemberInfo memberInfo);

    @POST("/connect/v1/group/update")
    Observable<Connect.HttpResponse> groupUpdate(@Body Connect.UpdateGroupInfo groupInfo);

    @POST("/connect/v1/group/info")
    Observable<Connect.HttpResponse> groupInfo(@Body Connect.GroupId groupId);

    @POST("/connect/v1/group/setting_info")
    Observable<Connect.HttpResponse> groupSettingInfo(@Body Connect.GroupId groupId);

    @POST("/connect/v1/group/hash")
    Observable<Connect.HttpResponse> groupHash(@Body Connect.GroupId groupId);

    @POST("/connect/v1/group/refresh/hash")
    Observable<Connect.HttpResponse> groupRefreshHash(@Body Connect.GroupId groupId);
    @POST("/connect/v1/group/attorn")
    Observable<Connect.HttpResponse> groupAttorn(@Body Connect.GroupAttorn attorn);

    @POST("/connect/v1/group/setting")
    Observable<Connect.HttpResponse> groupSetting(@Body Connect.GroupSetting setting);

    @POST("/connect/v1/group/public_info")
    Observable<Connect.HttpResponse> groupPublicInfo(@Body Connect.GroupId groupId);

    @POST("/connect/v1/group/invite")
    Observable<Connect.HttpResponse> groupInvite(@Body Connect.GroupInvite invite);

    @POST("/connect/v1/group/apply")
    Observable<Connect.HttpResponse> groupApply(@Body Connect.GroupApply apply);

    @POST("/connect/v1/group/reviewed")
    Observable<Connect.HttpResponse> groupReviewed(@Body Connect.GroupReviewed reviewed);

    @POST("/connect/v1/group/reject")
    Observable<Connect.HttpResponse> groupReject(@Body Connect.GroupReviewed reviewed);

    @POST("/wallet/v1/crowdfuning/records/users")
    Observable<Connect.HttpResponse> crowdFuningRecordsUsers(@Body Connect.UserCrowdfundingInfo userCrowdfundingInfo);

    @POST("/wallet/v1/billing/info")
    Observable<Connect.HttpResponse> walletBillingInfo(@Body Connect.BillHashId hashId);

    @POST("/wallet/v1/billing/external/info")
    Observable<Connect.HttpResponse> walletBillingExternalInfo(@Body Connect.BillHashId hashId);

    @POST("/wallet/v1/crowdfuning/info")
    Observable<Connect.HttpResponse> walletCrowFundingInfo(@Body Connect.BillHashId hashId);

    @POST("/wallet/v1/red_package/grab")
    Observable<Connect.HttpResponse> walletRedPackageGrab(@Body Connect.RedPackageHash packageHash);

    @POST("/connect/v1/group/upload_key")
    Observable<Connect.HttpResponse> groupUploadKey(@Body Connect.GroupCollaborative collaborative);

    @POST("/connect/v1/group/download_key")
    Observable<Connect.HttpResponse> groupDownloadKey(@Body Connect.GroupId groupId);

    @POST("/connect/v1/group/backup")
    Observable<Connect.HttpResponse> groupBackup(@Body Connect.GroupId groupId);

    @POST("/connect/v1/group/share")
    Observable<Connect.HttpResponse> groupShare(@Body Connect.GroupId groupId);

    @POST("/connect/v1/group/info/token")
    Observable<Connect.HttpResponse> groupInfoToken(@Body Connect.GroupToken groupToken);

    @POST("/connect/v1/group/invite/token")
    Observable<Connect.HttpResponse> groupInviteToken(@Body Connect.GroupInviteUser groupInviteUser);

    @POST("/connect/v1/group/mute")
    Observable<Connect.HttpResponse> groupMute(@Body Connect.UpdateGroupMute updateGroupMute);

    @POST("/wallet/v1/red_package/grabSystem")
    Observable<Connect.HttpResponse> walletRedPackageGrabSystem(@Body Connect.RedPackageHash packageHash);

    @POST("/wallet/v1/red_package/system/info")
    Observable<Connect.HttpResponse> walletRedPackageSystemInfo(@Body Connect.RedPackageHash redPackageHash);

}
