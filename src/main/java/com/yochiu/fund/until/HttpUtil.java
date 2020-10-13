package com.yochiu.fund.until;

/**
 * @Author: yochiu
 * @Description:
 * @Date: 2020/4/27
 */


import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.util.List;
import java.util.Map.Entry;

/**
 * @Author: yochiu
 * @Description:
 * @Date: 2020/4/27
 */

public class HttpUtil {

    private static final String WEBSOCKET_PATH = "/websocket";

    private static final String MQTT_PATH = "/mqtt";

    //private static final Logger LOG = LoggerFactory.getLogger(HttpUtil.class);

    public static String get(String url) {
        return get(url, 1000, 1000);
    }

    public static String get(String url, int connectTimeout, int socketTimeout) {
        return get(url, connectTimeout, socketTimeout, "UTF-8", null);
    }

    public static String get(String url, String accessToken, int connectTimeout, int socketTimeout) {
        return get(url, accessToken, connectTimeout, socketTimeout, null);
    }

    public static String get(String url, Map<String, String> headers) {
        return get(url, 3000, 3000, "UTF-8", null, headers);
    }

    public static String get(String url, String headerName, String headerValue, int connectTimeout, int socketTimeout) {
        Map<String, String> headers = new HashMap<>();
        headers.put(headerName, headerValue);
        return get(url, socketTimeout, connectTimeout, "UTF-8", null, headers);
    }

    public static String get(String url, int connectTimeout, int socketTimeout, String charset, Map<String, String> params) {
        return get(url, null, connectTimeout, socketTimeout, charset, params);
    }

    public static String get(String url, String accessToken, int connectTimeout, int socketTimeout, Map<String, String> params) {
        return get(url, accessToken, connectTimeout, socketTimeout, "UTF-8", params);
    }

    private static String get(String url, String accessToken, int connectTimeout, int socketTimeout, String charset, Map<String, String> params) {
        Map<String, String> headers = new HashMap<>();
        if (StringUtils.isNotBlank(accessToken)) {
            headers.put("Authorization", "Bearer " + accessToken);
        }
        return get(url, connectTimeout, socketTimeout, charset, params, headers);
    }

    public static String get(String url, int connectTimeout, int socketTimeout, String charset, Map<String, String> params, Map<String, String> headers) {
        try {
            StringBuilder urlSB = new StringBuilder(url);
            //拼接参数
            if (params != null) {
                int i = 0;
                for (Map.Entry<String, String> param : params.entrySet()) {
                    if (i == 0) {
                        urlSB.append(url.indexOf("?") >= 0 ? '&' : '?').append(param.getKey()).append('=').append(param.getValue());
                    } else {
                        urlSB.append('&').append(param.getKey()).append('=').append(param.getValue());
                    }
                    ++i;
                }
            }
            //设置超时
            Request request = Request.Get(urlSB.toString())
                    .connectTimeout(connectTimeout)
                    .socketTimeout(socketTimeout);
            //添加header
            if (headers != null) {
                for (Entry<String, String> entry : headers.entrySet()) {
                    request.setHeader(entry.getKey(), entry.getValue());
                }
            }
            //请求内容，并转码
            Content content = request.execute().returnContent();
            return IOUtils.toString(content.asStream(), charset);
        } catch (Exception e) {
            //LOG.error("http get {}", url, e);
        }
        return null;
    }

    public static String get(Executor executor, String url) {
        try {
            Content content = executor.execute(Request.Get(url).connectTimeout(1000)
                    .socketTimeout(1000)).returnContent();
            return IOUtils.toString(content.asStream(), "UTF-8");
        } catch (Exception e) {
            //LOG.error("get url", e);
        }
        return null;
    }

    public static <T extends JSON> String post(String url, String accessToken, T json) throws IOException {
        if (json == null) {
            return get(url);
        }

        Request request = Request.Post(url);

        if (StringUtils.isNotBlank(accessToken)) {
            request.setHeader("Authorization", "Bearer " + accessToken);
        }

        Content content = request.bodyString(json.toString(), ContentType.APPLICATION_JSON).connectTimeout(10000)
                .socketTimeout(10000).execute().returnContent();

        return IOUtils.toString(content.asStream(), "UTF-8");
    }

    public static String postJson(String url, String body) throws IOException {
        Content content = Request.Post(url)
                .bodyString(body, ContentType.create("application/json"))
                .connectTimeout(10000).socketTimeout(10000)
                .execute().returnContent();
        return IOUtils.toString(content.asStream(), content.getType().getCharset());
    }

    public static String post(String url, Map<String, String> params) {
        if (params == null) {
            return get(url);
        }

        return postForms(url, 10000, 10000, "UTF-8", params);
    }

    public static String post(String url, int connectTimeout, int socketTimeout) {
        return postForms(url, connectTimeout, socketTimeout, "UTF-8", null);
    }

    public static String post(String url, String bodyJson, Map<String, String> params) {
        return postFormsAndJson(url, bodyJson, params);
    }

    private static String postForms(String url, int connectTimeout, int socketTimeout, String charset, Map<String, String> forms) {
        try {
            Request request = Request.Post(url);

            //form
            if (forms != null) {
                List<NameValuePair> pairs;
                Form form = Form.form();
                for (Entry<String, String> entry : forms.entrySet()) {
                    form.add(entry.getKey(), entry.getValue());
                }
                pairs = form.build();
                request.bodyForm(pairs, Charset.forName(charset));
            }

            Content content = request.connectTimeout(connectTimeout).socketTimeout(socketTimeout).execute().returnContent();
            return IOUtils.toString(content.asStream(), charset);
        } catch (Exception e) {
            //LOG.error("http post {}", url, e);
        }
        return null;
    }

    private static String postFormsAndJson(String url, String bodyJson, Map<String, String> forms) {
        try {
            Request request = Request.Post(url);

            //form
            if (forms != null) {
                List<NameValuePair> pairs;
                Form form = Form.form();
                for (Entry<String, String> entry : forms.entrySet()) {
                    form.add(entry.getKey(), entry.getValue());
                }
                pairs = form.build();
                request.bodyForm(pairs, Charset.forName("UTF-8"));
            }

            //json body
            if (StringUtils.isNotEmpty(bodyJson)) {
                request.bodyString(bodyJson, ContentType.create("application/json"));
            }

            Content content = request.connectTimeout(10000).socketTimeout(10000)
                    .execute().returnContent();
            return IOUtils.toString(content.asStream(), "UTF-8");
        } catch (Exception e) {
            //LOG.error("http post {}", url, e);
        }
        return null;
    }


    public static String getByProxy(String url, Map<String, String> requestProperties) {
        try {
            Request request = Request.Get(url);
            if (requestProperties != null) {
                for (Map.Entry<String, String> entry : requestProperties.entrySet()) {
                    request.addHeader(entry.getKey(), entry.getValue());
                }
            }
            HttpHost host = new HttpHost("us.stunnel.net", 110, "https");
            Content content = request.viaProxy(host).connectTimeout(5000).
                    socketTimeout(5000).execute().returnContent();
            return IOUtils.toString(content.asStream(), "UTF-8");
        } catch (Exception e) {
            //LOG.error("get", e);
        }
        return null;
    }


    public static String post(Executor executor, String url,
                              Map<String, String> params) throws IOException {
        if (params == null) {
            return get(url);
        }

        List<NameValuePair> pairs;
        Form form = Form.form();

        for (Entry<String, String> entry : params.entrySet()) {
            form.add(entry.getKey(), entry.getValue());
        }
        pairs = form.build();

        Content content = executor.execute(Request.Post(url).bodyForm(
                pairs, Charset.forName("UTF-8")).connectTimeout(5000)
                .socketTimeout(5000)).returnContent();

        return IOUtils.toString(content.asStream(), "UTF-8");
    }

    /**
     * return scheme according to protocol
     *
     * @param uri
     * @param ssl
     * @return
     */
    public static String getScheme(String uri, boolean ssl) {
        if (WEBSOCKET_PATH.equals(uri)) {
            if (ssl) {
                return "wss://";
            } else {
                return "ws://";
            }
        } else if (MQTT_PATH.equals(uri)) {
            if (ssl) {
                return "mqtts://";
            } else {
                return "mqtt://";
            }
        }

        return null;
    }
}
