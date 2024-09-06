package com.team5.alarmmemo.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.naver.maps.geometry.LatLng
import com.team5.alarmmemo.presentation.memo.CheckItem
import com.team5.alarmmemo.presentation.memo.MemoType
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import java.util.Base64
import javax.inject.Inject

class RectFAdapter @Inject constructor() : JsonSerializer<RectF>, JsonDeserializer<RectF> {
    override fun serialize(src: RectF?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("left", src?.left)
        jsonObject.addProperty("top", src?.top)
        jsonObject.addProperty("right", src?.right)
        jsonObject.addProperty("bottom", src?.bottom)
        return jsonObject
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): RectF {
        val jsonObject = json?.asJsonObject
        val left = jsonObject?.get("left")?.asFloat ?: 0f
        val top = jsonObject?.get("top")?.asFloat ?: 0f
        val right = jsonObject?.get("right")?.asFloat ?: 0f
        val bottom = jsonObject?.get("bottom")?.asFloat ?: 0f
        return RectF(left, top, right, bottom)
    }
}

class PaintAdapter @Inject constructor(): JsonSerializer<Paint>, JsonDeserializer<Paint> {
    override fun serialize(src: Paint?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("color", src?.color)
        jsonObject.addProperty("style", src?.style?.toString())
        return jsonObject
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Paint {
        val jsonObject = json?.asJsonObject
        val paint = Paint()
        paint.color = jsonObject?.get("color")?.asInt ?: 0
        paint.style = when (jsonObject?.get("style")?.asString) {
            Paint.Style.FILL.toString() -> Paint.Style.FILL
            Paint.Style.STROKE.toString() -> Paint.Style.STROKE
            Paint.Style.FILL_AND_STROKE.toString() -> Paint.Style.FILL_AND_STROKE
            else -> Paint.Style.FILL
        }
        return paint
    }
}

class PathAdapter @Inject constructor(): JsonSerializer<Path>, JsonDeserializer<Path> {


    override fun serialize(src: Path?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonArray = JsonArray()

        if (src != null) {

            val pathMeasure = android.graphics.PathMeasure(src, false)
            val coordinates = FloatArray(2)
            var distance = 0f


            while (distance < pathMeasure.length) {
                pathMeasure.getPosTan(distance, coordinates, null)
                val commandArray = JsonArray()

                if (distance == 0f) {

                    commandArray.add("moveTo")
                } else {

                    commandArray.add("lineTo")
                }

                commandArray.add(coordinates[0])
                commandArray.add(coordinates[1])
                jsonArray.add(commandArray)

                distance += 2
            }
        }

        return jsonArray
    }


    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Path {
        val path = Path()
        val jsonArray = json?.asJsonArray

        jsonArray?.forEach { element ->
            val commandArray = element.asJsonArray
            val command = commandArray[0].asString
            val x = commandArray[1].asFloat
            val y = commandArray[2].asFloat

            when (command) {
                "moveTo" -> path.moveTo(x, y)
                "lineTo" -> path.lineTo(x, y)
            }
        }

        return path
    }
}

class BitmapTypeAdapter @Inject constructor(): JsonSerializer<Bitmap>, JsonDeserializer<Bitmap> {

    override fun serialize(src: Bitmap?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        if (src == null) {
            return JsonNull.INSTANCE
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        src.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        val base64String = Base64.getEncoder().encodeToString(byteArray)
        return JsonPrimitive(base64String)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Bitmap {
        if (json == null || json.isJsonNull) {
            throw JsonParseException("JSON element is null")
        }

        val base64String = json.asString

        val byteArray = Base64.getDecoder().decode(base64String)

        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}

class TextBoxTypeAdapter @Inject constructor(): JsonSerializer<Triple<String, RectF, Paint>>, JsonDeserializer<Triple<String, RectF, Paint>> {

    override fun serialize(src: Triple<String, RectF, Paint>?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("first", src?.first)
        jsonObject.add("second", context?.serialize(src?.second, RectF::class.java))
        jsonObject.add("third", context?.serialize(src?.third, Paint::class.java))
        return jsonObject
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Triple<String, RectF, Paint> {
        val jsonObject = json?.asJsonObject ?: return Triple("", RectF(), Paint())
        val first = jsonObject.get("first")?.asString ?: ""
        val second = context?.deserialize<RectF>(jsonObject.get("second"), RectF::class.java) ?: RectF()
        val third = context?.deserialize<Paint>(jsonObject.get("third"), Paint::class.java) ?: Paint()
        return Triple(first, second, third)
    }
}

class PairTypeAdapter @Inject constructor(): JsonSerializer<Pair<Any,Any>>, JsonDeserializer<Pair<Any,Any>>{
    override fun serialize(
        src: Pair<Any, Any>?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        src?.let {
            val first = it.first
            val second = it.second

            when {
                first is Path && second is Paint -> jsonObject.addProperty("type", "draw")
                first is Bitmap && second is RectF -> jsonObject.addProperty("type", "bitmap_rect")
                else -> jsonObject.addProperty("type", "unknown")
            }

            jsonObject.add("first", context?.serialize(first))
            jsonObject.add("second", context?.serialize(second))
        }
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Pair<Any, Any> {
        val jsonObject = json?.asJsonObject ?: return Pair(Any(), Any())
        val type = jsonObject.get("type")?.asString

        return when (type) {
            "draw" -> {
                val first = context?.deserialize<Path>(jsonObject.get("first"), Path::class.java) ?: Path()
                val second = context?.deserialize<Paint>(jsonObject.get("second"), Paint::class.java) ?: Paint()
                Pair(first, second)
            }
            "bitmap_rect" -> {
                val first = context?.deserialize<Bitmap>(jsonObject.get("first"), Bitmap::class.java) ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                val second = context?.deserialize<RectF>(jsonObject.get("second"), RectF::class.java) ?: RectF()
                Pair(first, second)
            }
            else -> Pair(Any(), Any())
        }
    }

}



class CheckItemTypeAdapter @Inject constructor(): JsonSerializer<CheckItem>, JsonDeserializer<CheckItem> {

    override fun serialize(src: CheckItem?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", src?.type?.name)
        jsonObject.add("data", context?.serialize(src?.data, src?.data?.javaClass))
        jsonObject.addProperty("zidx", src?.zidx)
        return jsonObject
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): CheckItem {
        val jsonObject = json?.asJsonObject ?: return CheckItem(MemoType.Default, null, 0)
        val typeString = jsonObject.get("type")?.asString ?: MemoType.Default.name
        val type = MemoType.valueOf(typeString)
        val dataElement = jsonObject.get("data")
        val data = when (type) {
            MemoType.TextBox -> context?.deserialize<Triple<String,RectF,Paint>>(dataElement, Triple::class.java)
            MemoType.Draw -> context?.deserialize<Pair<Path,Paint>>(dataElement, Pair::class.java)
            MemoType.Bitmap -> context?.deserialize<Pair<Bitmap,RectF>>(dataElement, Pair::class.java)
            MemoType.Text -> null
            MemoType.Default -> null
        }
        val zidx = jsonObject.get("zidx")?.asInt ?: 0
        return CheckItem(type, data, zidx)
    }
}

class SpannableStringBuilderTypeAdapter @Inject constructor() : JsonSerializer<SpannableStringBuilder>, JsonDeserializer<SpannableStringBuilder> {

    override fun serialize(src: SpannableStringBuilder?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        val text = src?.toString() ?: ""
        jsonObject.addProperty("text", text)

        val spanArray = JsonArray()
        src?.let {
            val spans = it.getSpans(0, it.length, Any::class.java)
            spans.forEach { span ->
                val spanObject = JsonObject()
                spanObject.addProperty("start", it.getSpanStart(span))
                spanObject.addProperty("end", it.getSpanEnd(span))
                spanObject.addProperty("type", span::class.java.name) // Use class name for type identification
                spanObject.add("data", context?.serialize(span, span::class.java))
                spanArray.add(spanObject)
            }
        }

        jsonObject.add("spans", spanArray)
        return jsonObject
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SpannableStringBuilder {
        val jsonObject = json?.asJsonObject ?: return SpannableStringBuilder()
        val text = jsonObject.get("text")?.asString ?: ""
        val spannableStringBuilder = SpannableStringBuilder(text)

        val spans = jsonObject.get("spans")?.asJsonArray
        spans?.forEach { element ->
            val spanObject = element.asJsonObject
            val start = spanObject.get("start")?.asInt ?: 0
            val end = spanObject.get("end")?.asInt ?: 0
            val type = spanObject.get("type")?.asString ?: ""

            try {
                val spanClass = Class.forName(type)
                val span = context?.deserialize<Any>(spanObject.get("data"), spanClass)
                if (span != null) {
                    spannableStringBuilder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return spannableStringBuilder
    }
}

class LatLngTypeAdapter @Inject constructor(): JsonSerializer<LatLng>, JsonDeserializer<LatLng> {

    override fun serialize(src: LatLng?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("latitude", src?.latitude ?: 0.0)
        jsonObject.addProperty("longitude", src?.longitude ?: 0.0)
        return jsonObject
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LatLng {
        val jsonObject = json?.asJsonObject ?: return LatLng(0.0, 0.0)
        val latitude = jsonObject.get("latitude")?.asDouble ?: 0.0
        val longitude = jsonObject.get("longitude")?.asDouble ?: 0.0
        return LatLng(latitude, longitude)
    }
}