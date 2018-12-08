package sjj.novel.util

import com.google.gson.*
import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

val gson = GsonBuilder()
        .apply {
            ScalarsJsonDeserializer.types.forEach { registerTypeAdapter(it, ScalarsJsonDeserializer) }
        }.addSerializationExclusionStrategy(object : ExclusionStrategy {
            override fun shouldSkipClass(clazz: Class<*>?): Boolean = false

            override fun shouldSkipField(f: FieldAttributes): Boolean {
                val expose = f.getAnnotation(Expose::class.java)
                return expose != null && !expose.serialize
            }
        }).create()

inline fun <reified T> Gson.fromJson(json: String): T {
    return fromJson(json, object : TypeToken<T>() {}.type)
}

private object ScalarsJsonDeserializer : JsonDeserializer<Any> {
    val types = arrayOf<Type>(String::class.java,
            Boolean::class.java,
            Byte::class.java,
            Char::class.java,
            Double::class.java,
            Float::class.java,
            Int::class.java,
            Long::class.java,
            Short::class.java
    )

    override fun deserialize(json: JsonElement?, typeOfT: Type, context: JsonDeserializationContext?): Any? {
        return try {
            when (TypeToken.get(typeOfT).rawType) {
                String::class.java -> json?.asString
                Boolean::class.java -> json?.asBoolean
                Byte::class.java -> json?.asByte
                Char::class.java -> json?.asCharacter
                Double::class.java -> json?.asDouble
                Float::class.java -> json?.asFloat
                Int::class.java -> json?.asInt
                Long::class.java -> json?.asLong
                Short::class.java -> json?.asShort
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

}