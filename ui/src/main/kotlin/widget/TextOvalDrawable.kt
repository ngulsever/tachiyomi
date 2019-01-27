/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.widget

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.ShapeDrawable
import androidx.annotation.ColorInt
import java.util.Arrays
import java.util.Random

class TextOvalDrawable(
  text: String,
  @ColorInt private val backgroundColor: Int = Color.GRAY,
  @ColorInt private val textColor: Int = Color.WHITE
) : ShapeDrawable() {

  private val ovalRect = RectF()
  private val padding = Rect()

  private val text = text.toUpperCase()
  private var centerX = 0f
  private var centerY = 0f
  private var centerFont = 0f

  private val textPaint = Paint().apply {
    color = textColor
    isAntiAlias = true
    style = Paint.Style.FILL
    textAlign = Paint.Align.CENTER
    strokeWidth = 0f
  }

  init {
    paint.color = backgroundColor
  }

  override fun draw(canvas: Canvas) {
    canvas.drawOval(ovalRect, paint)
    canvas.drawText(text, centerX, centerY - centerFont, textPaint)
  }

  override fun onBoundsChange(bounds: Rect) {
    super.onBoundsChange(bounds)
    getPadding(padding)

    ovalRect.set(
      bounds.left.toFloat() + padding.left,
      bounds.top.toFloat() + padding.top,
      bounds.right.toFloat() - padding.right,
      bounds.bottom.toFloat() - padding.bottom
    )

    centerX = ovalRect.width() / 2 + ovalRect.left
    centerY = ovalRect.height() / 2 + ovalRect.top
    textPaint.textSize = Math.min(centerX, centerY)
    centerFont = (textPaint.descent() + textPaint.ascent()) / 2
  }

  override fun setAlpha(alpha: Int) {
    textPaint.alpha = alpha
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
    textPaint.colorFilter = colorFilter
  }

  override fun getOpacity(): Int {
    return PixelFormat.TRANSLUCENT
  }

  override fun getIntrinsicWidth(): Int {
    return -1
  }

  override fun getIntrinsicHeight(): Int {
    return -1
  }

  override fun getConstantState(): ConstantState? {
    return null
  }

  object Colors {
    private val random by lazy { Random(System.currentTimeMillis()) }
    val colors = Arrays.asList(
      -0x1a8c8d,
      -0xf9d6e,
      -0x459738,
      -0x6a8a33,
      -0x867935,
      -0x9b4a0a,
      -0xb03c09,
      -0xb22f1f,
      -0xb24954,
      -0x7e387c,
      -0x512a7f,
      -0x759b,
      -0x2b1ea9,
      -0x2ab1,
      -0x48b3,
      -0x5e7781,
      -0x6f5b52
    )

    fun getRandomColor(): Int {
      return colors[random.nextInt(colors.size)]
    }

    fun getColor(key: Any): Int {
      return colors[Math.abs(key.hashCode()) % colors.size]
    }
  }

}
