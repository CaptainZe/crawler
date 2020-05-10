package com.ze.crawler.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ze.crawler.core.constants.Constant;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

/**
 * 网络请求工具类
 */
public class HttpClientUtils {
    /**
     * 网络请求GET
     */
    public static <T> T get(String url, Class<T> clazz, String authorization) {
        // 1.生成httpclient，相当于该打开一个浏览器
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        // 2.创建get请求，相当于在浏览器地址栏输入 网址
        HttpGet request = new HttpGet(url);
        // 设置请求头，将爬虫伪装成浏览器
        request.setHeader(Constant.REQUEST_HEADER_USER_AGENT, Constant.CHROME_BROWSER_USER_AGENT);
        // 认证token
        if (!StringUtils.isEmpty(authorization)) {
            request.setHeader(Constant.REQUEST_HEADER_AUTHORIZATION, authorization);
        }
        // 设置超时时间
        RequestConfig config = RequestConfig.custom().setSocketTimeout(Constant.SOCKET_TIMEOUT).build();
        request.setConfig(config);
//        // 设置代理
//        HttpHost proxy = new HttpHost("112.85.168.223", 9999);
//        RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
//        request.setConfig(config);

        try {
            // 3.执行get请求，相当于在输入地址栏后敲回车键
            response = httpClient.execute(request);

            // 4.判断响应状态为200，进行处理
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // 5.获取响应内容
                HttpEntity httpEntity = response.getEntity();
                String html = EntityUtils.toString(httpEntity, "utf-8");
                return JSON.parseObject(html, clazz);
            } else {
                //如果返回状态不是200，比如404（页面不存在）等，根据情况做处理，这里略
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            // 6.关闭
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpClient);
        }
    }

    /**
     * 网络请求GET
     */
    public static <T> T get(String url, Class<T> clazz) {
        return get(url, clazz, null);
    }

    /**
     * 网络请求POST
     */
    public static <T> T post(String url, JSONObject jsonObject, Class<T> clazz, String authorization) {
        // 1.生成httpclient，相当于该打开一个浏览器
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            // 2.创建post请求，相当于在浏览器地址栏输入 网址
            HttpPost request = new HttpPost(url);
            // 设置请求body
            StringEntity se = new StringEntity(jsonObject.toString(), "UTF-8");
            se.setContentEncoding("UTF-8");
            se.setContentType(Constant.REQUEST_HEADER_CONTENT_TYPE_APPLICATION_JSON);
            request.setEntity(se);
            // 设置请求头，将爬虫伪装成浏览器
            request.setHeader(Constant.REQUEST_HEADER_USER_AGENT, Constant.CHROME_BROWSER_USER_AGENT);
            // 认证token
            if (!StringUtils.isEmpty(authorization)) {
                request.setHeader(Constant.REQUEST_HEADER_AUTHORIZATION, authorization);
            }

            // 设置代理 & 设置超时时间
//            HttpHost proxy = new HttpHost(Constant.PROXY_HOST, Constant.PROXY_PORT);
//            RequestConfig config = RequestConfig.custom().setSocketTimeout(Constant.SOCKET_TIMEOUT).setProxy(proxy).build();
//            request.setConfig(config);
            RequestConfig config = RequestConfig.custom().setSocketTimeout(Constant.SOCKET_TIMEOUT).build();
            request.setConfig(config);

            // 3.执行get请求，相当于在输入地址栏后敲回车键
            response = httpClient.execute(request);

            // 4.判断响应状态为200，进行处理
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // 5.获取响应内容
                HttpEntity httpEntity = response.getEntity();
                String html = EntityUtils.toString(httpEntity, "utf-8");
                return JSON.parseObject(html, clazz);
            } else {
                //如果返回状态不是200，比如404（页面不存在）等，根据情况做处理，这里略
                return null;
            }
        } catch (Exception e) {
            // Connection reset
            return null;
        } finally {
            // 6.关闭
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpClient);
        }
    }

    /**
     * 网络请求POST
     */
    public static <T> T post(String url, JSONObject jsonObject, Class<T> clazz) {
        return post(url, jsonObject, clazz, null);
    }
}
