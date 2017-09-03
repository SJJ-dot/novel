package sjj.fiction.data.source.remote

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

import java.io.IOException
import java.io.OutputStreamWriter
import java.io.StringReader
import java.io.Writer
import java.lang.reflect.Type
import java.nio.charset.Charset

import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import retrofit2.Converter
import retrofit2.Retrofit
import sjj.alog.Log

/**
 * Created by SJJ on 2017/9/3.
 */

class GsonConverterFactory(private val gson: Gson) : Converter.Factory() {

    override fun responseBodyConverter(type: Type?, annotations: Array<Annotation>?,
                                       retrofit: Retrofit?): Converter<ResponseBody, *> {
        val adapter = gson.getAdapter(TypeToken.get(type!!))
        return Converter<ResponseBody, Any> { value ->
            var string = value.string()
            if (!isJson(string)) {
                string = JSONArray().put(string).toString()
            }
            val jsonReader = gson.newJsonReader(StringReader(string))
            try {
                return@Converter adapter.read(jsonReader)
            } finally {
                value.close()
            }
        }
    }

    private fun isJson(s: String): Boolean {
        try {
            JSONObject(s)
            return true
        } catch (e: JSONException) {
            try {
                JSONArray(s)
                return true
            } catch (e1: JSONException) {
                return false
            }

        }

    }

    override fun requestBodyConverter(type: Type?, parameterAnnotations: Array<Annotation>?, methodAnnotations: Array<Annotation>?, retrofit: Retrofit?): Converter<*, RequestBody> {
        val adapter = gson.getAdapter(TypeToken.get(type!!))
        return GsonRequestBodyConverter(gson, adapter)
    }
}

internal class GsonRequestBodyConverter<T>(private val gson: Gson, private val adapter: TypeAdapter<T>) : Converter<T, RequestBody> {

    @Throws(IOException::class)
    override fun convert(value: T): RequestBody {
        val buffer = Buffer()
        val writer = OutputStreamWriter(buffer.outputStream(), UTF_8)
        val jsonWriter = gson.newJsonWriter(writer)
        adapter.write(jsonWriter, value)
        jsonWriter.close()
        return RequestBody.create(MEDIA_TYPE, buffer.readByteString())
    }

    companion object {
        private val MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8")
        private val UTF_8 = Charset.forName("UTF-8")
    }
}
