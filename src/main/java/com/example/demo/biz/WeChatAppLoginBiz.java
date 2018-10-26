package com.example.demo.biz;

import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.example.demo.comm.HmacUtil;
import com.example.demo.comm.ResponseMsg;
import com.example.demo.comm.SystemException;
import com.example.demo.login.WeChatAppLoginReq;
import com.example.demo.po.UserInfoPo;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.base64url.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;



@Component
public class WeChatAppLoginBiz
{
    private static final Logger logger = LoggerFactory.getLogger(WeChatAppLoginBiz.class);

    @Autowired
    private UserInfoBiz userInfoBiz;

    public static boolean initialized = false;

    private static final String APPID = "wx9e8c80fd6539a9c7";
    private static final String SECRET = "";

    public Map<String,Object> login(WeChatAppLoginReq req)
    {
        //获取 session_key 和 openId
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid="+APPID+"&secret="+SECRET+"&js_code="+req.getCode()+"&grant_type=authorization_code";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String>  responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        if(responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK)
        {
            String sessionData = responseEntity.getBody();
            logger.info("sessionData = "+ sessionData);
            JSONObject jsonObj = JSON.parseObject(sessionData);
            String openId = jsonObj.getString("openid");
            String sessionKey = jsonObj.getString("session_key");

            String signature = HmacUtil.SHA1(req.getRawData()+sessionKey);
            if(!signature.equals(req.getSignature()))
            {
                logger.info(" req signature="+req.getSignature());
                logger.info(" java signature="+req.getSignature());
                throw new SystemException(ResponseMsg.WECHAT_LOGIN_USER_ERROR);
            }
            byte[] resultByte = null;
            try {
                resultByte = decrypt(Base64.decode(req.getEncryptedData()), Base64.decode(sessionKey), Base64.decode(req.getIv()));
            } catch (Exception e) {
                throw new SystemException(ResponseMsg.WECHAT_LOGIN_USER_ERROR);
            }
            if(null != resultByte && resultByte.length > 0)
            {
                String userInfoStr = "";
                try {
                    userInfoStr = new String(resultByte, "UTF-8");
                } catch (UnsupportedEncodingException e)
                {
                    logger.error(e.getMessage());
                }
                logger.info("userInfo = "+ userInfoStr);
                JSONObject userInfoObj = JSON.parseObject(userInfoStr);
                UserInfoPo userPo = new UserInfoPo();
//                userPo.setName(userInfoObj.getString("nickName"));
//                userPo.setCreatedTime(new Date());
//                userPo.setGender(userInfoObj.getString("gender"));
//                userPo.setIcon(userInfoObj.getString("avatarUrl"));
//                userPo.setLoginId(userInfoObj.getString("unionId"));
//                userPo.setType((short)UserType.WeiXin);
//                userPo.setLoginType(UserType.WeChatApp);
                //userPo.setNation(userInfoObj.getString("city"));

                //userInfoObj.getString("city");
                //userInfoObj.getString("province");
                //userInfoObj.getString("country");
                Map<String,Object> data = userInfoBiz.insertOrUpdate(userPo);

                return data;
            }else
            {
                throw new SystemException(ResponseMsg.WECHAT_LOGIN_USER_ERROR);
            }


        }else
        {
            throw new SystemException(ResponseMsg.WECHAT_LOGIN_CODE_ERROR);
        }
    }



    private byte[] decrypt(byte[] content, byte[] keyByte, byte[] ivByte) throws InvalidAlgorithmParameterException {
        initialize();
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            Key sKeySpec = new SecretKeySpec(keyByte, "AES");

            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, generateIV(ivByte));// 初始化
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static void initialize(){
        if (initialized) return;
        Security.addProvider(new BouncyCastleProvider());
        initialized = true;
    }
    //生成iv
    public static AlgorithmParameters generateIV(byte[] iv) throws Exception{
        AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
        params.init(new IvParameterSpec(iv));
        return params;
    }
}


