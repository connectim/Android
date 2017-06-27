package connect.utils.data;

import connect.activity.login.bean.CountryBean;
import connect.utils.system.SystemDataUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guotengqian on 2016/3/31.
 */
public class PhoneDataUtil {
    private static PhoneDataUtil someUtil = null;

    public static PhoneDataUtil getInstance() {
        if (null == someUtil) {
            someUtil = new PhoneDataUtil();
        }
        return someUtil;
    }

    public static final String country_code =
            "[\n" +
                    "    {\n" +
                    "        \"countryName\": \"Afghanistan\",\n" +
                    "        \"countryCode\": \"AF\",\n" +
                    "        \"phoneCode\": \"93\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Albania\",\n" +
                    "        \"countryCode\": \"AL\",\n" +
                    "        \"phoneCode\": \"355\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Algeria\",\n" +
                    "        \"countryCode\": \"DZ\",\n" +
                    "        \"phoneCode\": \"213\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"American Samoa\",\n" +
                    "        \"countryCode\": \"AS\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Andorra\",\n" +
                    "        \"countryCode\": \"AD\",\n" +
                    "        \"phoneCode\": \"376\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Angola\",\n" +
                    "        \"countryCode\": \"AO\",\n" +
                    "        \"phoneCode\": \"244\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Anguilla\",\n" +
                    "        \"countryCode\": \"AI\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Antarctica\",\n" +
                    "        \"countryCode\": \"AQ\",\n" +
                    "        \"phoneCode\": \"672\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Antigua and Barbuda\",\n" +
                    "        \"countryCode\": \"AG\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Argentina\",\n" +
                    "        \"countryCode\": \"AR\",\n" +
                    "        \"phoneCode\": \"54\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Armenia\",\n" +
                    "        \"countryCode\": \"AM\",\n" +
                    "        \"phoneCode\": \"374\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Aruba\",\n" +
                    "        \"countryCode\": \"AW\",\n" +
                    "        \"phoneCode\": \"297\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Australia\",\n" +
                    "        \"countryCode\": \"AU\",\n" +
                    "        \"phoneCode\": \"61\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Austria\",\n" +
                    "        \"countryCode\": \"AT\",\n" +
                    "        \"phoneCode\": \"43\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Azerbaijan\",\n" +
                    "        \"countryCode\": \"AZ\",\n" +
                    "        \"phoneCode\": \"994\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Bahamas\",\n" +
                    "        \"countryCode\": \"BS\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Bahrain\",\n" +
                    "        \"countryCode\": \"BH\",\n" +
                    "        \"phoneCode\": \"973\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Bangladesh\",\n" +
                    "        \"countryCode\": \"BD\",\n" +
                    "        \"phoneCode\": \"880\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Barbados\",\n" +
                    "        \"countryCode\": \"BB\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Belarus\",\n" +
                    "        \"countryCode\": \"BY\",\n" +
                    "        \"phoneCode\": \"375\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Belgium\",\n" +
                    "        \"countryCode\": \"BE\",\n" +
                    "        \"phoneCode\": \"32\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Belize\",\n" +
                    "        \"countryCode\": \"BZ\",\n" +
                    "        \"phoneCode\": \"501\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Benin\",\n" +
                    "        \"countryCode\": \"BJ\",\n" +
                    "        \"phoneCode\": \"229\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Bermuda\",\n" +
                    "        \"countryCode\": \"BM\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Bhutan\",\n" +
                    "        \"countryCode\": \"BT\",\n" +
                    "        \"phoneCode\": \"975\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Bolivia\",\n" +
                    "        \"countryCode\": \"BO\",\n" +
                    "        \"phoneCode\": \"591\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Bosnia and Herzegovina\",\n" +
                    "        \"countryCode\": \"BA\",\n" +
                    "        \"phoneCode\": \"387\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Botswana\",\n" +
                    "        \"countryCode\": \"BW\",\n" +
                    "        \"phoneCode\": \"267\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Brazil\",\n" +
                    "        \"countryCode\": \"BR\",\n" +
                    "        \"phoneCode\": \"55\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"British Indian Ocean Territory\",\n" +
                    "        \"countryCode\": \"IO\",\n" +
                    "        \"phoneCode\": \"246\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"British Virgin Islands\",\n" +
                    "        \"countryCode\": \"VG\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Brunei\",\n" +
                    "        \"countryCode\": \"BN\",\n" +
                    "        \"phoneCode\": \"673\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Bulgaria\",\n" +
                    "        \"countryCode\": \"BG\",\n" +
                    "        \"phoneCode\": \"359\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Burkina Faso\",\n" +
                    "        \"countryCode\": \"BF\",\n" +
                    "        \"phoneCode\": \"226\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Burundi\",\n" +
                    "        \"countryCode\": \"BI\",\n" +
                    "        \"phoneCode\": \"257\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Cambodia\",\n" +
                    "        \"countryCode\": \"KH\",\n" +
                    "        \"phoneCode\": \"855\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Cameroon\",\n" +
                    "        \"countryCode\": \"CM\",\n" +
                    "        \"phoneCode\": \"237\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Canada\",\n" +
                    "        \"countryCode\": \"CA\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Cape Verde\",\n" +
                    "        \"countryCode\": \"CV\",\n" +
                    "        \"phoneCode\": \"238\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Cayman Islands\",\n" +
                    "        \"countryCode\": \"KY\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Central African Republic\",\n" +
                    "        \"countryCode\": \"CF\",\n" +
                    "        \"phoneCode\": \"236\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Chad\",\n" +
                    "        \"countryCode\": \"TD\",\n" +
                    "        \"phoneCode\": \"235\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Chile\",\n" +
                    "        \"countryCode\": \"CL\",\n" +
                    "        \"phoneCode\": \"56\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"China\",\n" +
                    "        \"countryCode\": \"CN\",\n" +
                    "        \"phoneCode\": \"86\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Christmas Island\",\n" +
                    "        \"countryCode\": \"CX\",\n" +
                    "        \"phoneCode\": \"61\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Cocos Islands\",\n" +
                    "        \"countryCode\": \"CC\",\n" +
                    "        \"phoneCode\": \"61\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Colombia\",\n" +
                    "        \"countryCode\": \"CO\",\n" +
                    "        \"phoneCode\": \"57\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Comoros\",\n" +
                    "        \"countryCode\": \"KM\",\n" +
                    "        \"phoneCode\": \"269\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Cook Islands\",\n" +
                    "        \"countryCode\": \"CK\",\n" +
                    "        \"phoneCode\": \"682\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Costa Rica\",\n" +
                    "        \"countryCode\": \"CR\",\n" +
                    "        \"phoneCode\": \"506\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Croatia\",\n" +
                    "        \"countryCode\": \"HR\",\n" +
                    "        \"phoneCode\": \"385\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Cuba\",\n" +
                    "        \"countryCode\": \"CU\",\n" +
                    "        \"phoneCode\": \"53\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Curacao\",\n" +
                    "        \"countryCode\": \"CW\",\n" +
                    "        \"phoneCode\": \"599\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Cyprus\",\n" +
                    "        \"countryCode\": \"CY\",\n" +
                    "        \"phoneCode\": \"357\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Czech Republic\",\n" +
                    "        \"countryCode\": \"CZ\",\n" +
                    "        \"phoneCode\": \"420\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Democratic Republic of the Congo\",\n" +
                    "        \"countryCode\": \"CD\",\n" +
                    "        \"phoneCode\": \"243\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Denmark\",\n" +
                    "        \"countryCode\": \"DK\",\n" +
                    "        \"phoneCode\": \"45\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Djibouti\",\n" +
                    "        \"countryCode\": \"DJ\",\n" +
                    "        \"phoneCode\": \"253\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Dominica\",\n" +
                    "        \"countryCode\": \"DM\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Dominican Republic\",\n" +
                    "        \"countryCode\": \"DO\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"East Timor\",\n" +
                    "        \"countryCode\": \"TL\",\n" +
                    "        \"phoneCode\": \"670\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Ecuador\",\n" +
                    "        \"countryCode\": \"EC\",\n" +
                    "        \"phoneCode\": \"593\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Egypt\",\n" +
                    "        \"countryCode\": \"EG\",\n" +
                    "        \"phoneCode\": \"20\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"El Salvador\",\n" +
                    "        \"countryCode\": \"SV\",\n" +
                    "        \"phoneCode\": \"503\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Equatorial Guinea\",\n" +
                    "        \"countryCode\": \"GQ\",\n" +
                    "        \"phoneCode\": \"240\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Eritrea\",\n" +
                    "        \"countryCode\": \"ER\",\n" +
                    "        \"phoneCode\": \"291\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Estonia\",\n" +
                    "        \"countryCode\": \"EE\",\n" +
                    "        \"phoneCode\": \"372\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Ethiopia\",\n" +
                    "        \"countryCode\": \"ET\",\n" +
                    "        \"phoneCode\": \"251\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Falkland Islands\",\n" +
                    "        \"countryCode\": \"FK\",\n" +
                    "        \"phoneCode\": \"500\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Faroe Islands\",\n" +
                    "        \"countryCode\": \"FO\",\n" +
                    "        \"phoneCode\": \"298\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Fiji\",\n" +
                    "        \"countryCode\": \"FJ\",\n" +
                    "        \"phoneCode\": \"679\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Finland\",\n" +
                    "        \"countryCode\": \"FI\",\n" +
                    "        \"phoneCode\": \"358\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"France\",\n" +
                    "        \"countryCode\": \"FR\",\n" +
                    "        \"phoneCode\": \"33\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"French Polynesia\",\n" +
                    "        \"countryCode\": \"PF\",\n" +
                    "        \"phoneCode\": \"689\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Gabon\",\n" +
                    "        \"countryCode\": \"GA\",\n" +
                    "        \"phoneCode\": \"241\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Gambia\",\n" +
                    "        \"countryCode\": \"GM\",\n" +
                    "        \"phoneCode\": \"220\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Georgia\",\n" +
                    "        \"countryCode\": \"GE\",\n" +
                    "        \"phoneCode\": \"995\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Germany\",\n" +
                    "        \"countryCode\": \"DE\",\n" +
                    "        \"phoneCode\": \"49\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Ghana\",\n" +
                    "        \"countryCode\": \"GH\",\n" +
                    "        \"phoneCode\": \"233\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Gibraltar\",\n" +
                    "        \"countryCode\": \"GI\",\n" +
                    "        \"phoneCode\": \"350\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Greece\",\n" +
                    "        \"countryCode\": \"GR\",\n" +
                    "        \"phoneCode\": \"30\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Greenland\",\n" +
                    "        \"countryCode\": \"GL\",\n" +
                    "        \"phoneCode\": \"299\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Grenada\",\n" +
                    "        \"countryCode\": \"GD\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Guam\",\n" +
                    "        \"countryCode\": \"GU\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Guatemala\",\n" +
                    "        \"countryCode\": \"GT\",\n" +
                    "        \"phoneCode\": \"502\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Guernsey\",\n" +
                    "        \"countryCode\": \"GG\",\n" +
                    "        \"phoneCode\": \"44\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Guinea\",\n" +
                    "        \"countryCode\": \"GN\",\n" +
                    "        \"phoneCode\": \"224\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Guinea-Bissau\",\n" +
                    "        \"countryCode\": \"GW\",\n" +
                    "        \"phoneCode\": \"245\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Guyana\",\n" +
                    "        \"countryCode\": \"GY\",\n" +
                    "        \"phoneCode\": \"592\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Haiti\",\n" +
                    "        \"countryCode\": \"HT\",\n" +
                    "        \"phoneCode\": \"509\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Honduras\",\n" +
                    "        \"countryCode\": \"HN\",\n" +
                    "        \"phoneCode\": \"504\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Hong Kong\",\n" +
                    "        \"countryCode\": \"HK\",\n" +
                    "        \"phoneCode\": \"852\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Hungary\",\n" +
                    "        \"countryCode\": \"HU\",\n" +
                    "        \"phoneCode\": \"36\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Iceland\",\n" +
                    "        \"countryCode\": \"IS\",\n" +
                    "        \"phoneCode\": \"354\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"India\",\n" +
                    "        \"countryCode\": \"IN\",\n" +
                    "        \"phoneCode\": \"91\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Indonesia\",\n" +
                    "        \"countryCode\": \"ID\",\n" +
                    "        \"phoneCode\": \"62\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Iran\",\n" +
                    "        \"countryCode\": \"IR\",\n" +
                    "        \"phoneCode\": \"98\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Iraq\",\n" +
                    "        \"countryCode\": \"IQ\",\n" +
                    "        \"phoneCode\": \"964\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Ireland\",\n" +
                    "        \"countryCode\": \"IE\",\n" +
                    "        \"phoneCode\": \"353\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Isle of Man\",\n" +
                    "        \"countryCode\": \"IM\",\n" +
                    "        \"phoneCode\": \"44\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Israel\",\n" +
                    "        \"countryCode\": \"IL\",\n" +
                    "        \"phoneCode\": \"972\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Italy\",\n" +
                    "        \"countryCode\": \"IT\",\n" +
                    "        \"phoneCode\": \"39\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Ivory Coast\",\n" +
                    "        \"countryCode\": \"CI\",\n" +
                    "        \"phoneCode\": \"225\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Jamaica\",\n" +
                    "        \"countryCode\": \"JM\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Japan\",\n" +
                    "        \"countryCode\": \"JP\",\n" +
                    "        \"phoneCode\": \"81\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Jersey\",\n" +
                    "        \"countryCode\": \"JE\",\n" +
                    "        \"phoneCode\": \"44\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Jordan\",\n" +
                    "        \"countryCode\": \"JO\",\n" +
                    "        \"phoneCode\": \"962\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Kazakhstan\",\n" +
                    "        \"countryCode\": \"KZ\",\n" +
                    "        \"phoneCode\": \"7\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Kenya\",\n" +
                    "        \"countryCode\": \"KE\",\n" +
                    "        \"phoneCode\": \"254\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Kiribati\",\n" +
                    "        \"countryCode\": \"KI\",\n" +
                    "        \"phoneCode\": \"686\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Kosovo\",\n" +
                    "        \"countryCode\": \"XK\",\n" +
                    "        \"phoneCode\": \"383\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Kuwait\",\n" +
                    "        \"countryCode\": \"KW\",\n" +
                    "        \"phoneCode\": \"965\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Kyrgyzstan\",\n" +
                    "        \"countryCode\": \"KG\",\n" +
                    "        \"phoneCode\": \"996\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Laos\",\n" +
                    "        \"countryCode\": \"LA\",\n" +
                    "        \"phoneCode\": \"856\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Latvia\",\n" +
                    "        \"countryCode\": \"LV\",\n" +
                    "        \"phoneCode\": \"371\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Lebanon\",\n" +
                    "        \"countryCode\": \"LB\",\n" +
                    "        \"phoneCode\": \"961\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Lesotho\",\n" +
                    "        \"countryCode\": \"LS\",\n" +
                    "        \"phoneCode\": \"266\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Liberia\",\n" +
                    "        \"countryCode\": \"LR\",\n" +
                    "        \"phoneCode\": \"231\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Libya\",\n" +
                    "        \"countryCode\": \"LY\",\n" +
                    "        \"phoneCode\": \"218\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Liechtenstein\",\n" +
                    "        \"countryCode\": \"LI\",\n" +
                    "        \"phoneCode\": \"423\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Lithuania\",\n" +
                    "        \"countryCode\": \"LT\",\n" +
                    "        \"phoneCode\": \"370\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Luxembourg\",\n" +
                    "        \"countryCode\": \"LU\",\n" +
                    "        \"phoneCode\": \"352\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Macao\",\n" +
                    "        \"countryCode\": \"MO\",\n" +
                    "        \"phoneCode\": \"853\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Macedonia\",\n" +
                    "        \"countryCode\": \"MK\",\n" +
                    "        \"phoneCode\": \"389\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Madagascar\",\n" +
                    "        \"countryCode\": \"MG\",\n" +
                    "        \"phoneCode\": \"261\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Malawi\",\n" +
                    "        \"countryCode\": \"MW\",\n" +
                    "        \"phoneCode\": \"265\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Malaysia\",\n" +
                    "        \"countryCode\": \"MY\",\n" +
                    "        \"phoneCode\": \"60\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Maldives\",\n" +
                    "        \"countryCode\": \"MV\",\n" +
                    "        \"phoneCode\": \"960\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Mali\",\n" +
                    "        \"countryCode\": \"ML\",\n" +
                    "        \"phoneCode\": \"223\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Malta\",\n" +
                    "        \"countryCode\": \"MT\",\n" +
                    "        \"phoneCode\": \"356\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Marshall Islands\",\n" +
                    "        \"countryCode\": \"MH\",\n" +
                    "        \"phoneCode\": \"692\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Mauritania\",\n" +
                    "        \"countryCode\": \"MR\",\n" +
                    "        \"phoneCode\": \"222\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Mauritius\",\n" +
                    "        \"countryCode\": \"MU\",\n" +
                    "        \"phoneCode\": \"230\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Mayotte\",\n" +
                    "        \"countryCode\": \"YT\",\n" +
                    "        \"phoneCode\": \"262\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Mexico\",\n" +
                    "        \"countryCode\": \"MX\",\n" +
                    "        \"phoneCode\": \"52\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Micronesia\",\n" +
                    "        \"countryCode\": \"FM\",\n" +
                    "        \"phoneCode\": \"691\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Moldova\",\n" +
                    "        \"countryCode\": \"MD\",\n" +
                    "        \"phoneCode\": \"373\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Monaco\",\n" +
                    "        \"countryCode\": \"MC\",\n" +
                    "        \"phoneCode\": \"377\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Mongolia\",\n" +
                    "        \"countryCode\": \"MN\",\n" +
                    "        \"phoneCode\": \"976\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Montenegro\",\n" +
                    "        \"countryCode\": \"ME\",\n" +
                    "        \"phoneCode\": \"382\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Montserrat\",\n" +
                    "        \"countryCode\": \"MS\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Morocco\",\n" +
                    "        \"countryCode\": \"MA\",\n" +
                    "        \"phoneCode\": \"212\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Mozambique\",\n" +
                    "        \"countryCode\": \"MZ\",\n" +
                    "        \"phoneCode\": \"258\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Myanmar\",\n" +
                    "        \"countryCode\": \"MM\",\n" +
                    "        \"phoneCode\": \"95\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Namibia\",\n" +
                    "        \"countryCode\": \"NA\",\n" +
                    "        \"phoneCode\": \"264\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Nauru\",\n" +
                    "        \"countryCode\": \"NR\",\n" +
                    "        \"phoneCode\": \"674\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Nepal\",\n" +
                    "        \"countryCode\": \"NP\",\n" +
                    "        \"phoneCode\": \"977\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Netherlands\",\n" +
                    "        \"countryCode\": \"NL\",\n" +
                    "        \"phoneCode\": \"31\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Netherlands Antilles\",\n" +
                    "        \"countryCode\": \"AN\",\n" +
                    "        \"phoneCode\": \"599\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"New Caledonia\",\n" +
                    "        \"countryCode\": \"NC\",\n" +
                    "        \"phoneCode\": \"687\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"New Zealand\",\n" +
                    "        \"countryCode\": \"NZ\",\n" +
                    "        \"phoneCode\": \"64\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Nicaragua\",\n" +
                    "        \"countryCode\": \"NI\",\n" +
                    "        \"phoneCode\": \"505\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Niger\",\n" +
                    "        \"countryCode\": \"NE\",\n" +
                    "        \"phoneCode\": \"227\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Nigeria\",\n" +
                    "        \"countryCode\": \"NG\",\n" +
                    "        \"phoneCode\": \"234\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Niue\",\n" +
                    "        \"countryCode\": \"NU\",\n" +
                    "        \"phoneCode\": \"683\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"North Korea\",\n" +
                    "        \"countryCode\": \"KP\",\n" +
                    "        \"phoneCode\": \"850\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Northern Mariana Islands\",\n" +
                    "        \"countryCode\": \"MP\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Norway\",\n" +
                    "        \"countryCode\": \"NO\",\n" +
                    "        \"phoneCode\": \"47\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Oman\",\n" +
                    "        \"countryCode\": \"OM\",\n" +
                    "        \"phoneCode\": \"968\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Pakistan\",\n" +
                    "        \"countryCode\": \"PK\",\n" +
                    "        \"phoneCode\": \"92\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Palau\",\n" +
                    "        \"countryCode\": \"PW\",\n" +
                    "        \"phoneCode\": \"680\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Palestine\",\n" +
                    "        \"countryCode\": \"PS\",\n" +
                    "        \"phoneCode\": \"970\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Panama\",\n" +
                    "        \"countryCode\": \"PA\",\n" +
                    "        \"phoneCode\": \"507\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Papua New Guinea\",\n" +
                    "        \"countryCode\": \"PG\",\n" +
                    "        \"phoneCode\": \"675\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Paraguay\",\n" +
                    "        \"countryCode\": \"PY\",\n" +
                    "        \"phoneCode\": \"595\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Peru\",\n" +
                    "        \"countryCode\": \"PE\",\n" +
                    "        \"phoneCode\": \"51\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Philippines\",\n" +
                    "        \"countryCode\": \"PH\",\n" +
                    "        \"phoneCode\": \"63\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Pitcairn\",\n" +
                    "        \"countryCode\": \"PN\",\n" +
                    "        \"phoneCode\": \"64\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Poland\",\n" +
                    "        \"countryCode\": \"PL\",\n" +
                    "        \"phoneCode\": \"48\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Portugal\",\n" +
                    "        \"countryCode\": \"PT\",\n" +
                    "        \"phoneCode\": \"351\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Puerto Rico\",\n" +
                    "        \"countryCode\": \"PR\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Qatar\",\n" +
                    "        \"countryCode\": \"QA\",\n" +
                    "        \"phoneCode\": \"974\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Republic of the Congo\",\n" +
                    "        \"countryCode\": \"CG\",\n" +
                    "        \"phoneCode\": \"242\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Reunion\",\n" +
                    "        \"countryCode\": \"RE\",\n" +
                    "        \"phoneCode\": \"262\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Romania\",\n" +
                    "        \"countryCode\": \"RO\",\n" +
                    "        \"phoneCode\": \"40\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Russia\",\n" +
                    "        \"countryCode\": \"RU\",\n" +
                    "        \"phoneCode\": \"7\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Rwanda\",\n" +
                    "        \"countryCode\": \"RW\",\n" +
                    "        \"phoneCode\": \"250\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Saint Barthelemy\",\n" +
                    "        \"countryCode\": \"BL\",\n" +
                    "        \"phoneCode\": \"590\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Saint Helena\",\n" +
                    "        \"countryCode\": \"SH\",\n" +
                    "        \"phoneCode\": \"290\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Saint Kitts and Nevis\",\n" +
                    "        \"countryCode\": \"KN\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Saint Lucia\",\n" +
                    "        \"countryCode\": \"LC\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Saint Martin\",\n" +
                    "        \"countryCode\": \"MF\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Saint Pierre and Miquelon\",\n" +
                    "        \"countryCode\": \"PM\",\n" +
                    "        \"phoneCode\": \"508\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Saint Vincent and the Grenadines\",\n" +
                    "        \"countryCode\": \"VC\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Samoa\",\n" +
                    "        \"countryCode\": \"WS\",\n" +
                    "        \"phoneCode\": \"685\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"San Marino\",\n" +
                    "        \"countryCode\": \"SM\",\n" +
                    "        \"phoneCode\": \"378\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Sao Tome and Principe\",\n" +
                    "        \"countryCode\": \"ST\",\n" +
                    "        \"phoneCode\": \"239\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Saudi Arabia\",\n" +
                    "        \"countryCode\": \"SA\",\n" +
                    "        \"phoneCode\": \"966\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Senegal\",\n" +
                    "        \"countryCode\": \"SN\",\n" +
                    "        \"phoneCode\": \"221\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Serbia\",\n" +
                    "        \"countryCode\": \"RS\",\n" +
                    "        \"phoneCode\": \"381\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Seychelles\",\n" +
                    "        \"countryCode\": \"SC\",\n" +
                    "        \"phoneCode\": \"248\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Sierra Leone\",\n" +
                    "        \"countryCode\": \"SL\",\n" +
                    "        \"phoneCode\": \"232\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Singapore\",\n" +
                    "        \"countryCode\": \"SG\",\n" +
                    "        \"phoneCode\": \"65\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Sint Maarten\",\n" +
                    "        \"countryCode\": \"SX\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Slovakia\",\n" +
                    "        \"countryCode\": \"SK\",\n" +
                    "        \"phoneCode\": \"421\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Slovenia\",\n" +
                    "        \"countryCode\": \"SI\",\n" +
                    "        \"phoneCode\": \"386\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Solomon Islands\",\n" +
                    "        \"countryCode\": \"SB\",\n" +
                    "        \"phoneCode\": \"677\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Somalia\",\n" +
                    "        \"countryCode\": \"SO\",\n" +
                    "        \"phoneCode\": \"252\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"South Africa\",\n" +
                    "        \"countryCode\": \"ZA\",\n" +
                    "        \"phoneCode\": \"27\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"South Korea\",\n" +
                    "        \"countryCode\": \"KR\",\n" +
                    "        \"phoneCode\": \"82\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"South Sudan\",\n" +
                    "        \"countryCode\": \"SS\",\n" +
                    "        \"phoneCode\": \"211\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Spain\",\n" +
                    "        \"countryCode\": \"ES\",\n" +
                    "        \"phoneCode\": \"34\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Sri Lanka\",\n" +
                    "        \"countryCode\": \"LK\",\n" +
                    "        \"phoneCode\": \"94\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Sudan\",\n" +
                    "        \"countryCode\": \"SD\",\n" +
                    "        \"phoneCode\": \"249\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Suriname\",\n" +
                    "        \"countryCode\": \"SR\",\n" +
                    "        \"phoneCode\": \"597\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Svalbard and Jan Mayen\",\n" +
                    "        \"countryCode\": \"SJ\",\n" +
                    "        \"phoneCode\": \"47\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Swaziland\",\n" +
                    "        \"countryCode\": \"SZ\",\n" +
                    "        \"phoneCode\": \"268\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Sweden\",\n" +
                    "        \"countryCode\": \"SE\",\n" +
                    "        \"phoneCode\": \"46\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Switzerland\",\n" +
                    "        \"countryCode\": \"CH\",\n" +
                    "        \"phoneCode\": \"41\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Syria\",\n" +
                    "        \"countryCode\": \"SY\",\n" +
                    "        \"phoneCode\": \"963\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Taiwan\",\n" +
                    "        \"countryCode\": \"TW\",\n" +
                    "        \"phoneCode\": \"886\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Tajikistan\",\n" +
                    "        \"countryCode\": \"TJ\",\n" +
                    "        \"phoneCode\": \"992\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Tanzania\",\n" +
                    "        \"countryCode\": \"TZ\",\n" +
                    "        \"phoneCode\": \"255\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Thailand\",\n" +
                    "        \"countryCode\": \"TH\",\n" +
                    "        \"phoneCode\": \"66\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Togo\",\n" +
                    "        \"countryCode\": \"TG\",\n" +
                    "        \"phoneCode\": \"228\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Tokelau\",\n" +
                    "        \"countryCode\": \"TK\",\n" +
                    "        \"phoneCode\": \"690\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Tonga\",\n" +
                    "        \"countryCode\": \"TO\",\n" +
                    "        \"phoneCode\": \"676\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Trinidad and Tobago\",\n" +
                    "        \"countryCode\": \"TT\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Tunisia\",\n" +
                    "        \"countryCode\": \"TN\",\n" +
                    "        \"phoneCode\": \"216\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Turkey\",\n" +
                    "        \"countryCode\": \"TR\",\n" +
                    "        \"phoneCode\": \"90\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Turkmenistan\",\n" +
                    "        \"countryCode\": \"TM\",\n" +
                    "        \"phoneCode\": \"993\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Turks and Caicos Islands\",\n" +
                    "        \"countryCode\": \"TC\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Tuvalu\",\n" +
                    "        \"countryCode\": \"TV\",\n" +
                    "        \"phoneCode\": \"688\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"U.S. Virgin Islands\",\n" +
                    "        \"countryCode\": \"VI\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Uganda\",\n" +
                    "        \"countryCode\": \"UG\",\n" +
                    "        \"phoneCode\": \"256\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Ukraine\",\n" +
                    "        \"countryCode\": \"UA\",\n" +
                    "        \"phoneCode\": \"380\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"United Arab Emirates\",\n" +
                    "        \"countryCode\": \"AE\",\n" +
                    "        \"phoneCode\": \"971\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"United Kingdom\",\n" +
                    "        \"countryCode\": \"GB\",\n" +
                    "        \"phoneCode\": \"44\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"United States\",\n" +
                    "        \"countryCode\": \"US\",\n" +
                    "        \"phoneCode\": \"1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Uruguay\",\n" +
                    "        \"countryCode\": \"UY\",\n" +
                    "        \"phoneCode\": \"598\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Uzbekistan\",\n" +
                    "        \"countryCode\": \"UZ\",\n" +
                    "        \"phoneCode\": \"998\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Vanuatu\",\n" +
                    "        \"countryCode\": \"VU\",\n" +
                    "        \"phoneCode\": \"678\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Vatican\",\n" +
                    "        \"countryCode\": \"VA\",\n" +
                    "        \"phoneCode\": \"39\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Venezuela\",\n" +
                    "        \"countryCode\": \"VE\",\n" +
                    "        \"phoneCode\": \"58\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Vietnam\",\n" +
                    "        \"countryCode\": \"VN\",\n" +
                    "        \"phoneCode\": \"84\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Wallis and Futuna\",\n" +
                    "        \"countryCode\": \"WF\",\n" +
                    "        \"phoneCode\": \"681\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Western Sahara\",\n" +
                    "        \"countryCode\": \"EH\",\n" +
                    "        \"phoneCode\": \"212\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Yemen\",\n" +
                    "        \"countryCode\": \"YE\",\n" +
                    "        \"phoneCode\": \"967\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Zambia\",\n" +
                    "        \"countryCode\": \"ZM\",\n" +
                    "        \"phoneCode\": \"260\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"countryName\": \"Zimbabwe\",\n" +
                    "        \"countryCode\": \"ZW\",\n" +
                    "        \"phoneCode\": \"263\"\n" +
                    "    }\n" +
                    "]";

    /**
     * mobile phone number ,country code information
     *
     * @return
     */
    public List<CountryBean> getCountryData() {
        List<CountryBean> list = new ArrayList<>();
        try {
            JSONArray data = new JSONArray(country_code);
            for (int i = 0; i < data.length(); i++) {
                CountryBean countryBean = new CountryBean();
                countryBean.setCode(data.getJSONObject(i).getString("phoneCode"));
                countryBean.setName(data.getJSONObject(i).getString("countryName"));
                countryBean.setCountryCode(data.getJSONObject(i).getString("countryCode"));
                list.add(countryBean);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * mobile phone number ,country code information
     *
     * @return
     */
    public CountryBean getCurrentCountryCode() {
        String localCode = SystemDataUtil.getCountry();
        try {
            JSONArray data = new JSONArray(country_code);
            for (int i = 0; i < data.length(); i++) {
                String code = data.getJSONObject(i).getString("countryCode");
                if(code.equals(localCode)){
                    CountryBean countryBean = new CountryBean();
                    countryBean.setCode(data.getJSONObject(i).getString("phoneCode"));
                    countryBean.setName(data.getJSONObject(i).getString("countryName"));
                    countryBean.setCountryCode(data.getJSONObject(i).getString("countryCode"));
                    return countryBean;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
