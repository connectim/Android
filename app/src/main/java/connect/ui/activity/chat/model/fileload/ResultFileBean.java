package connect.ui.activity.chat.model.fileload;

import java.io.Serializable;

/**
 * Created by gtq on 2016/12/6.
 */
public class ResultFileBean implements Serializable{

    /**
     * sign : 3044022071442eaba1519e25f9f25dbb83d0a25710aab9c84716522d3495ac493762f89302202b7797cb4f827423fbbe50c601a505d39cd15efe79b769bbf5da16d8fdf5b2e8
     * cipher_data : {"iv":"ea2d0c1c170a30eaa7d6e727ded78893","aad":"72ffc5b55ac47b6e9b63dbbb007e45ab","tag":"10afbb89c08228634db55e4e0e5457ed","ciphertext":"955212846aa59bda48d86a061440d7bf1a9d6877a903be82c9a085d1088fd55810f3bf1cd0e78ade3fa8ef8aea13b0ec4d3275393beb41b765f0c3a3f16cc11c94f2bf1f4e42b905f362e64d43e757fc922c5db056fdfee106231559f3ac51915908d9fa22037227da65164f22d4c20a2f99686f3f3e5c45eba05d9926e43174fec6af23f4db53dea33d2010f9682c714c09c411b11ce590a58e76aa4d08056785f541e2e2d7a41755722a048a8130bfb12336bafb42"}
     */

    private String sign;
    private CipherDataBean cipher_data;

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public CipherDataBean getCipher_data() {
        return cipher_data;
    }

    public void setCipher_data(CipherDataBean cipher_data) {
        this.cipher_data = cipher_data;
    }

    public static class CipherDataBean {
        /**
         * iv : ea2d0c1c170a30eaa7d6e727ded78893
         * aad : 72ffc5b55ac47b6e9b63dbbb007e45ab
         * tag : 10afbb89c08228634db55e4e0e5457ed
         * ciphertext : 955212846aa59bda48d86a061440d7bf1a9d6877a903be82c9a085d1088fd55810f3bf1cd0e78ade3fa8ef8aea13b0ec4d3275393beb41b765f0c3a3f16cc11c94f2bf1f4e42b905f362e64d43e757fc922c5db056fdfee106231559f3ac51915908d9fa22037227da65164f22d4c20a2f99686f3f3e5c45eba05d9926e43174fec6af23f4db53dea33d2010f9682c714c09c411b11ce590a58e76aa4d08056785f541e2e2d7a41755722a048a8130bfb12336bafb42
         */

        private String iv;
        private String aad;
        private String tag;
        private String ciphertext;

        public String getIv() {
            return iv;
        }

        public void setIv(String iv) {
            this.iv = iv;
        }

        public String getAad() {
            return aad;
        }

        public void setAad(String aad) {
            this.aad = aad;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getCiphertext() {
            return ciphertext;
        }

        public void setCiphertext(String ciphertext) {
            this.ciphertext = ciphertext;
        }
    }
}
