package cn.linksign.utils;


import java.io.*;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.*;

public class HttpUtils {


    /**
     * 下载文件 POST请求
     *
     * @param url
     * @param param
     * @return
     */
    public static File httpPostDownload(String url, Map<String, String> param) {
        HttpClient httpclient = wrapClient(url);
        try {
            System.out.println("httpPostDownload:"+url);
            HttpPost httppost = new HttpPost(url);
//
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            if (param != null) {
                for (Map.Entry<String, String> entry : param.entrySet()) {
                    builder.addTextBody(entry.getKey(), entry.getValue());
                }
            }
            httppost.setEntity(builder.build());
            HttpResponse response = httpclient.execute(httppost);

                HttpEntity entity = response.getEntity();
                java.io.InputStream inputStream = entity.getContent();
                String suffix = ".temp";
                if (url.contains(".")) {
                    suffix = url.substring(url.lastIndexOf("."));
                }

                File file = File.createTempFile("temp_", suffix);
                IOUtils.copy(inputStream, new FileOutputStream(file));
                return file;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    /**
     * 下载文件 GET请求
     *
     * @param url
     * @param param
     * @return
     */
    public static File httpGetDownload(String url, Map<String, String> param) {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        for (int i = 0; i < 3; i++) {
            System.out.println("httpGetDownload:"+url);

            HttpClient httpclient = wrapClient(url);
            try {
                if (param != null) {
                    String p = "?";
                    for (Map.Entry<String, String> entry : param.entrySet()) {
                        p += entry.getKey() + "=" + entry.getValue();
                    }
                    url = url + p;
                }



                HttpGet httpGet = new HttpGet(url);

                HttpResponse response = httpclient.execute(httpGet);
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();
                String suffix = ".temp";
                if (url.contains(".") && url.indexOf("?")!=-1) {
                    suffix = url.substring(url.lastIndexOf("."), url.indexOf("?"));
                }else if (url.contains("."))
                {
                    suffix = url.substring(url.lastIndexOf("."));
                }
                File file = File.createTempFile("temp_", suffix);
                fileOutputStream = new FileOutputStream(file);
                IOUtils.copy(inputStream, fileOutputStream);
                return file;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(fileOutputStream);

            }
        }
        return null;
    }

    private static HttpClient wrapClient(String host) {
        HttpClient httpClient = new DefaultHttpClient();
        if (host.startsWith("https://")) {
            sslClient(httpClient);
        }

        return httpClient;
    }

    private static void sslClient(HttpClient httpClient) {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] xcs, String str) {

                }
                public void checkServerTrusted(X509Certificate[] xcs, String str) {

                }
            };
            ctx.init(null, new TrustManager[] { tm }, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = httpClient.getConnectionManager();
            SchemeRegistry registry = ccm.getSchemeRegistry();
            registry.register(new Scheme("https", 443, ssf));
        } catch (KeyManagementException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Http POST
     *
     * @param url  地址
     * @param file 文件
     * @return
     */
    public static String HttpPost(String url, File file) {
        HttpClient httpclient = wrapClient(url);
        try {

            HttpPost httppost = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            if (file != null) {
                builder.addBinaryBody("file", file);
            }
            httppost.setEntity(builder.build());
            HttpResponse response = httpclient.execute(httppost);
            try {
                return EntityUtils.toString(response.getEntity());
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        File file = httpGetDownload("https://uatstatic.oss-cn-shenzhen.aliyuncs.com/20171012/al8ee081a9fda946658afa3d648fc938ec.jpg", null);
        System.out.println(file.getAbsolutePath());
    }

}
