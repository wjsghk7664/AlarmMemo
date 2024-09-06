package com.team5.alarmmemo.data.source.local

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.text.SpannableStringBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.naver.maps.geometry.LatLng
import com.team5.alarmmemo.presentation.memo.CheckItem
import com.team5.alarmmemo.util.BitmapTypeAdapter
import com.team5.alarmmemo.util.CheckItemTypeAdapter
import com.team5.alarmmemo.util.LatLngTypeAdapter
import com.team5.alarmmemo.util.PaintAdapter
import com.team5.alarmmemo.util.PairTypeAdapter
import com.team5.alarmmemo.util.PathAdapter
import com.team5.alarmmemo.util.RectFAdapter
import com.team5.alarmmemo.util.SpannableStringBuilderTypeAdapter
import com.team5.alarmmemo.util.TextBoxTypeAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object GsonProvider {

    @Provides
    fun provideGson(
        rect: RectFAdapter,
        paint: PaintAdapter,
        path: PathAdapter,
        bitmap: BitmapTypeAdapter,
        pair:PairTypeAdapter,
        textbox: TextBoxTypeAdapter,
        checkItem: CheckItemTypeAdapter,
        str: SpannableStringBuilderTypeAdapter,
        latLng: LatLngTypeAdapter
    ): Gson {
        return GsonBuilder()
            .registerTypeAdapter(RectF::class.java, rect)
            .registerTypeAdapter(Paint::class.java, paint)
            .registerTypeAdapter(Path::class.java, path)
            .registerTypeAdapter(Bitmap::class.java, bitmap)
            .registerTypeAdapter(Pair::class.java, pair)
            .registerTypeAdapter(Triple::class.java,textbox)
            .registerTypeAdapter(CheckItem::class.java, checkItem)
            .registerTypeAdapter(SpannableStringBuilder::class.java, str)
            .registerTypeAdapter(LatLng::class.java, latLng)
            .create()
    }
}