package com.example.smartagro.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import com.google.android.material.card.MaterialCardView

/**
 * Custom glassmorphism card view with WHITE glossy frosted glass effect.
 * 
 * Implementation:
 * - White glossy glass background: Bright, frosted, slightly opaque
 * - Glossy gradient: Top-left brighter, bottom-right slightly less bright
 * - Minimal dark scrim: Only for subtle contrast if needed (<= 0.04)
 * - Content layer: Icons, text, values stay perfectly sharp (100% opacity, no blur)
 * - Proper layering: Background glass -> Content (sharp on top)
 * 
 * Usage:
 * Replace MaterialCardView with GlassCardView in XML layouts.
 * The card will automatically apply white glossy glassmorphism effect
 * while keeping all content (text, icons, values) completely sharp and readable.
 */
class GlassCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    // White glossy frosted glass parameters (premium tweaks + stronger blur effect)
    private var baseWhiteAlpha: Float = 0.26f // 26% opacity base white fill (increased for stronger blur effect)
    private var whiteFrostAlpha: Float = 0.30f // 30% opacity white frosted overlay (increased for stronger blur/glass effect)
    private var darkOverlayAlpha: Float = 0.03f // 3% opacity dark overlay (minimal, only for subtle contrast)
    
    // Glossy gradient: top-left brighter (34%), bottom-right softer (18%) - premium highlight with blur
    private var glossyTopAlpha: Float = 0.34f // Top-left highlight (increased for more premium look + blur)
    private var glossyBottomAlpha: Float = 0.18f // Bottom-right (increased for smoother gradient + blur)
    
    private val backgroundGlassDrawable: GradientDrawable by lazy {
        GradientDrawable().apply {
            val alpha = (baseWhiteAlpha * 255).toInt()
            setColor(ColorUtils.setAlphaComponent(0xFFFFFF, alpha))
        }
    }
    
    private val whiteFrostDrawable: GradientDrawable by lazy {
        GradientDrawable().apply {
            val alpha = (whiteFrostAlpha * 255).toInt()
            setColor(ColorUtils.setAlphaComponent(0xFFFFFF, alpha))
        }
    }
    
    private val darkOverlayDrawable: GradientDrawable by lazy {
        GradientDrawable().apply {
            val alpha = (darkOverlayAlpha * 255).toInt()
            setColor(ColorUtils.setAlphaComponent(0x000000, alpha))
        }
    }
    
    // Glossy gradient drawable (top-left to bottom-right)
    private val glossyGradientDrawable: GradientDrawable by lazy {
        GradientDrawable(
            GradientDrawable.Orientation.TL_BR, // Top-left to bottom-right
            intArrayOf(
                ColorUtils.setAlphaComponent(0xFFFFFF, (glossyTopAlpha * 255).toInt()),
                ColorUtils.setAlphaComponent(0xFFFFFF, (glossyBottomAlpha * 255).toInt())
            )
        )
    }
    
    // Inner highlight line paint (top edge)
    private val innerHighlightPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ColorUtils.setAlphaComponent(0xFFFFFF, (0.18f * 255).toInt())
            strokeWidth = 1f * resources.displayMetrics.density
            style = Paint.Style.STROKE
        }
    }
    
    init {
        // Set base card properties for white glossy glassmorphism (premium tweaks + stronger blur)
        cardElevation = 6f // Soft shadow for glossy effect
        strokeWidth = 1
        strokeColor = 0x52FFFFFF.toInt() // White stroke with 32% alpha (increased for more visible glossy border)
        
        // Set transparent background - we'll draw glass layer manually
        setCardBackgroundColor(0x00000000) // Fully transparent
        
        // Initialize overlays (increased opacities simulate stronger blur effect)
        updateOverlays()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        darkOverlayDrawable.setBounds(0, 0, w, h)
        whiteFrostDrawable.setBounds(0, 0, w, h)
        backgroundGlassDrawable.setBounds(0, 0, w, h)
        glossyGradientDrawable.setBounds(0, 0, w, h)
        // Update corner radius to match card
        updateCornerRadius()
    }

    override fun onDraw(canvas: Canvas) {
        // Draw white glossy glass background layer FIRST (before content)
        // This creates the bright, frosted, glossy glass effect with STRONGER BLUR on the background only
        // Increased overlay opacities simulate stronger blur effect
        
        // Get corner radius from card shape
        val cornerRadius = 24f * resources.displayMetrics.density
        val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val path = Path().apply {
            addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW)
        }
        
        // Save canvas state and clip to rounded corners
        canvas.save()
        canvas.clipPath(path)
        
        // Draw background layers with increased opacity (simulates stronger blur effect)
        // Layer 1: Base white fill (increased opacity for stronger blur)
        backgroundGlassDrawable.draw(canvas)
        
        // Layer 2: Minimal dark overlay (only for subtle contrast, <= 0.04 max)
        if (darkOverlayAlpha > 0f) {
            darkOverlayDrawable.draw(canvas)
        }
        
        // Layer 3: Glossy gradient (increased opacity for stronger blur effect)
        glossyGradientDrawable.draw(canvas)
        
        // Layer 4: White frosted overlay (increased opacity for stronger blur/glass effect)
        whiteFrostDrawable.draw(canvas)
        
        // Restore canvas
        canvas.restore()
        
        // Draw card stroke/border (glossy border) - NOT blurred, stays sharp
        super.onDraw(canvas)
        
        // Draw inner highlight line at top edge (optional glossy shine) - NOT blurred, stays sharp
        canvas.save()
        val highlightPath = Path().apply {
            moveTo(cornerRadius, 0f)
            lineTo(width.toFloat() - cornerRadius, 0f)
        }
        canvas.drawPath(highlightPath, innerHighlightPaint)
        canvas.restore()
    }

    override fun dispatchDraw(canvas: Canvas) {
        // Draw children (content) on top of glass background
        // Content is drawn AFTER background, so it stays perfectly sharp
        super.dispatchDraw(canvas)
        
        // Ensure all child views have full opacity (no alpha reduction)
        ensureContentSharpness()
    }

    /**
     * Update the overlay drawables with current settings.
     */
    private fun updateOverlays() {
        // Update base white fill (bright frosted glass)
        val baseAlpha = (baseWhiteAlpha * 255).toInt()
        backgroundGlassDrawable.setColor(ColorUtils.setAlphaComponent(0xFFFFFF, baseAlpha))
        
        // Update minimal dark overlay (only for subtle contrast, <= 0.04 max)
        val darkAlpha = (darkOverlayAlpha.coerceAtMost(0.04f) * 255).toInt()
        darkOverlayDrawable.setColor(ColorUtils.setAlphaComponent(0x000000, darkAlpha))
        
        // Update white frosted overlay (stronger glass effect)
        val whiteAlpha = (whiteFrostAlpha * 255).toInt()
        whiteFrostDrawable.setColor(ColorUtils.setAlphaComponent(0xFFFFFF, whiteAlpha))
        
        // Update glossy gradient (top-left brighter, bottom-right less bright)
        glossyGradientDrawable.setColors(
            intArrayOf(
                ColorUtils.setAlphaComponent(0xFFFFFF, (glossyTopAlpha * 255).toInt()),
                ColorUtils.setAlphaComponent(0xFFFFFF, (glossyBottomAlpha * 255).toInt())
            )
        )
        
        // Update corner radius to match card
        updateCornerRadius()
        invalidate()
    }
    
    private fun updateCornerRadius() {
        // Use a fixed corner radius that matches typical card corner radius (24dp)
        val cornerRadius = 24f * resources.displayMetrics.density
        darkOverlayDrawable.cornerRadius = cornerRadius
        whiteFrostDrawable.cornerRadius = cornerRadius
        backgroundGlassDrawable.cornerRadius = cornerRadius
        glossyGradientDrawable.cornerRadius = cornerRadius
    }
    
    /**
     * Ensure all content views have full opacity and are not affected by blur.
     * This prevents any alpha reduction on child views, keeping content sharp.
     */
    private fun ensureContentSharpness() {
        // Ensure all child views maintain full opacity
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.alpha = 1.0f // Full opacity for content
            
            // Recursively ensure nested views are sharp
            if (child is ViewGroup) {
                ensureChildViewsSharpness(child)
            }
        }
    }
    
    private fun ensureChildViewsSharpness(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            child.alpha = 1.0f // Full opacity
            
            // Recursively check nested views
            if (child is ViewGroup) {
                ensureChildViewsSharpness(child)
            }
        }
    }

    /**
     * Set dark overlay alpha (0.0 to 1.0).
     * Default is 0.25 (25% opacity) - light for premium glass effect.
     */
    fun setDarkOverlayAlpha(alpha: Float) {
        darkOverlayAlpha = alpha.coerceIn(0f, 1f)
        updateOverlays()
    }

    /**
     * Set white frosted overlay alpha (0.0 to 1.0).
     * Default is 0.09 (9% opacity) - subtle frosted effect.
     */
    fun setWhiteFrostAlpha(alpha: Float) {
        whiteFrostAlpha = alpha.coerceIn(0f, 1f)
        updateOverlays()
    }
}
