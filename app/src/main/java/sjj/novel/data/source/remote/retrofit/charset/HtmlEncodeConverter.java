package sjj.novel.data.source.remote.retrofit.charset;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.BufferedSource;
import retrofit2.Converter;
import retrofit2.Retrofit;
import sjj.novel.data.source.remote.retrofit.ann.Html;

import static android.text.TextUtils.isEmpty;

public class HtmlEncodeConverter extends Converter.Factory {
    private Gson gson;

    private HtmlEncodeConverter(Gson gson) {
        this.gson = gson;
    }

    public static HtmlEncodeConverter create() {
        return create(new Gson());
    }

    public static HtmlEncodeConverter create(Gson gson) {
        return new HtmlEncodeConverter(gson);
    }

    @Override
    public Converter<ResponseBody, Object> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
//        Converter<ResponseBody, Object> converter = retrofit.nextResponseBodyConverter(this,type, annotations);
        for (Annotation annotation : annotations) {
            if (annotation instanceof Html) {
                return value -> {
                    try {
                        if (type.equals(String.class)) {
                            return stringConverter(value);
                        } else {
                            return gson.getAdapter(TypeToken.get(type)).fromJson(stringConverter(value));
                        }
                    } finally {
                        Util.closeQuietly(value);
                    }
                };
            }
        }

        return null;
    }

    private String stringConverter(ResponseBody value) throws IOException {
        String charsetStr;
        MediaType mediaType = value.contentType();
        BufferedSource source = value.source();
        source.request(Long.MAX_VALUE);
        byte[] responseBytes = source.buffer().readByteArray();

        //根据http头判断
        if (mediaType != null) {
            Charset charset = mediaType.charset();
            if (charset != null) {
                charsetStr = charset.displayName();
                if (!isEmpty(charsetStr)) {
                    return new String(responseBytes, Charset.forName(charsetStr));
                }
            }
        }
        //根据meta判断
        byte[] headerBytes = Arrays.copyOfRange(responseBytes, 0, Math.min(responseBytes.length,1024));
        Document doc = Jsoup.parse(new String(headerBytes, StandardCharsets.UTF_8));
        Elements metaTags = doc.getElementsByTag("meta");
        for (Element metaTag : metaTags) {
            String content = metaTag.attr("content");
            String http_equiv = metaTag.attr("http-equiv");
            charsetStr = metaTag.attr("charset");
            if (!charsetStr.isEmpty()) {
                if (!isEmpty(charsetStr)) {
                    return new String(responseBytes, Charset.forName(charsetStr));
                }
            }
            if (http_equiv.toLowerCase().equals("content-type")) {
                if (content.toLowerCase().contains("charset")) {
                    charsetStr = content.substring(content.toLowerCase().indexOf("charset") + "charset=".length());
                } else {
                    charsetStr = content.substring(content.toLowerCase().indexOf(";") + 1);
                }
                if (!isEmpty(charsetStr)) {
                    try {
                        return new String(responseBytes, Charset.forName(charsetStr));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //根据内容判断
        charsetStr = CharsetDetector.detectCharset(new ByteArrayInputStream(responseBytes));
        return new String(responseBytes, Charset.forName(charsetStr));
    }

}
