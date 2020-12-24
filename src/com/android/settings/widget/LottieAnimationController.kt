/*
 * Copyright (C) 2020 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.view.TextureView
import android.view.View
import android.widget.ImageView

import androidx.core.content.ContextCompat

import com.airbnb.lottie.*
import com.airbnb.lottie.model.KeyPath

import com.android.settings.R
import com.android.settings.widget.VideoPreference

import com.android.settings.custom.helpers.LottieUtil

import kotlin.math.roundToInt

class LottieAnimationController(private val context: Context, resId: Int) : VideoPreference.AnimationController {
    private val drawable = LottieDrawable()

    init {
        drawable.repeatCount = ValueAnimator.INFINITE
        drawable.enableMergePathsForKitKatAndAbove(true)

        drawable.composition = LottieCompositionFactory.fromRawResSync(context, resId).value
    }

    override fun attachView(video: TextureView?, preview: View?, playButton: View?) {
        video?.visibility = View.GONE
        updateViewStates(playButton)
        (preview as ImageView?)?.also {
            drawable.callback = it

            it.setImageDrawable(drawable)
            it.scaleType = ImageView.ScaleType.CENTER_INSIDE
            it.setOnClickListener { updateViewStates(playButton) }
        }
    }

    override fun getVideoWidth(): Int = drawable.composition.bounds.width()
    override fun getVideoHeight(): Int = drawable.composition.bounds.height()
    override fun getDuration(): Int = drawable.maxFrame.roundToInt()

    override fun isPlaying(): Boolean = drawable.isAnimating
    override fun pause() = drawable.pauseAnimation()
    override fun start() = drawable.resumeAnimation()
    override fun release() {
        drawable.stop()
        drawable.removeAllAnimatorListeners()
        drawable.removeAllUpdateListeners()
    }

    private fun updateViewStates(playButton: View?) {
        if (isPlaying) {
            release()
            playButton?.visibility = View.VISIBLE
        } else {
            playButton?.visibility = View.GONE
            attachValueCallbacks(drawable)
            start()
        }
    }

    private fun attachValueCallbacks(drawable: LottieDrawable) {
        val a = context.obtainStyledAttributes(android.R.style.Theme_DeviceDefault_Settings, intArrayOf(
            android.R.attr.colorAccent,
            android.R.attr.colorBackground
        ))
        val accent = a.getColor(0, Color.BLACK)
        val background = a.getColor(1, Color.BLACK)
        a.recycle()

        val accentLight1 = LottieUtil.createAccentLight1(accent)
        val accentLight2 = LottieUtil.createAccentLight2(accent)
        val bezel = ContextCompat.getColor(context, R.color.illustration_color_bezel)
        val frame = ContextCompat.getColor(context, R.color.illustration_color_frame)
        val screen = ContextCompat.getColor(context, R.color.illustration_color_screen)
        val screenOff = ContextCompat.getColor(context, R.color.illustration_color_screen_off)
        val highlight = ContextCompat.getColor(context, R.color.illustration_color_highlight)

        drawable.apply {
            addValueCallback(KeyPath("**", "Accent"), LottieProperty.COLOR) { accent }
            addValueCallback(KeyPath("**", "AccentLight1"), LottieProperty.COLOR) { accentLight1 }
            addValueCallback(KeyPath("**", "AccentLight2"), LottieProperty.COLOR) { accentLight2 }
            addValueCallback(KeyPath("**", "Background"), LottieProperty.COLOR) { background }
            addValueCallback(KeyPath("**", "Bezel"), LottieProperty.COLOR) { bezel }
            addValueCallback(KeyPath("**", "Frame"), LottieProperty.COLOR) { frame }
            addValueCallback(KeyPath("**", "Highlight"), LottieProperty.COLOR) { highlight }
            addValueCallback(KeyPath("**", "HighlightFaint"), LottieProperty.COLOR) { highlight }
            addValueCallback(KeyPath("**", "Screen"), LottieProperty.COLOR) { screen }
            addValueCallback(KeyPath("**", "ScreenOff"), LottieProperty.COLOR) { screenOff }

            addValueCallback(KeyPath("**", "Accent"), LottieProperty.STROKE_COLOR) { accent }
            addValueCallback(KeyPath("**", "AccentLight1"), LottieProperty.STROKE_COLOR) { accentLight1 }
            addValueCallback(KeyPath("**", "AccentLight2"), LottieProperty.STROKE_COLOR) { accentLight2 }
            addValueCallback(KeyPath("**", "Background"), LottieProperty.STROKE_COLOR) { background }
            addValueCallback(KeyPath("**", "Bezel"), LottieProperty.STROKE_COLOR) { bezel }
            addValueCallback(KeyPath("**", "Frame"), LottieProperty.STROKE_COLOR) { frame }
            addValueCallback(KeyPath("**", "Highlight"), LottieProperty.STROKE_COLOR) { highlight }
            addValueCallback(KeyPath("**", "HighlightFaint"), LottieProperty.STROKE_COLOR) { highlight }
            addValueCallback(KeyPath("**", "Screen"), LottieProperty.STROKE_COLOR) { screen }
            addValueCallback(KeyPath("**", "ScreenOff"), LottieProperty.STROKE_COLOR) { screenOff }

            addValueCallback(KeyPath("**", "Highlight"), LottieProperty.OPACITY) { 54 }
            addValueCallback(KeyPath("**", "HighlightFaint"), LottieProperty.OPACITY) { 12 }
        }
    }
}
