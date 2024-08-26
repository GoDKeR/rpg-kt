package org.godker.rpg.game.graphics
import org.godker.rpg.math.Vector3f
import org.godker.rpg.math.Vector4f

/**
 * This class represents a RGBA color.
 *
 * @author Heiko Brumme
 */
class Color {
    /** This value specifies the red component.  */
    private var red = 0f

    /** This value specifies the green component.  */
    private var green = 0f

    /** This value specifies the blue component.  */
    private var blue = 0f

    /** This value specifies the transparency.  */
    private var alpha = 0f

    /**
     * Creates a RGBA-Color.
     *
     * @param red   The red component. Range from 0f to 1f.
     * @param green The green component. Range from 0f to 1f.
     * @param blue  The blue component. Range from 0f to 1f.
     * @param alpha The transparency. Range from 0f to 1f.
     */
    /**
     * Creates a RGB-Color with an alpha value of 1.
     *
     * @param red   The red component. Range from 0f to 1f.
     * @param green The green component. Range from 0f to 1f.
     * @param blue  The blue component. Range from 0f to 1f.
     */
    /** The default color is black.  */
    @JvmOverloads
    constructor(red: Float = 0f, green: Float = 0f, blue: Float = 0f, alpha: Float = 1f) {
        setRed(red)
        setGreen(green)
        setBlue(blue)
        setAlpha(alpha)
    }

    /**
     * Creates a RGBA-Color.
     *
     * @param red   The red component. Range from 0 to 255.
     * @param green The green component. Range from 0 to 255.
     * @param blue  The blue component. Range from 0 to 255.
     * @param alpha The transparency. Range from 0 to 255.
     */
    /**
     * Creates a RGB-Color with an alpha value of 1.
     *
     * @param red   The red component. Range from 0 to 255.
     * @param green The green component. Range from 0 to 255.
     * @param blue  The blue component. Range from 0 to 255.
     */
    @JvmOverloads
    constructor(red: Int, green: Int, blue: Int, alpha: Int = 255) {
        setRed(red)
        setGreen(green)
        setBlue(blue)
        setAlpha(alpha)
    }

    /**
     * Returns the red component.
     *
     * @return The red component.
     */
    fun getRed(): Float {
        return red
    }

    /**
     * Sets the red component.
     *
     * @param red The red component. Range from 0f to 1f.
     */
    fun setRed(red: Float) {
        var red = red
        if (red < 0f) {
            red = 0f
        }
        if (red > 1f) {
            red = 1f
        }
        this.red = red
    }

    /**
     * Sets the red component.
     *
     * @param red The red component. Range from 0 to 255.
     */
    fun setRed(red: Int) {
        setRed(red / 255f)
    }

    /**
     * Returns the green component.
     *
     * @return The green component.
     */
    fun getGreen(): Float {
        return green
    }

    /**
     * Sets the green component.
     *
     * @param green The green component. Range from 0f to 1f.
     */
    fun setGreen(green: Float) {
        var green = green
        if (green < 0f) {
            green = 0f
        }
        if (green > 1f) {
            green = 1f
        }
        this.green = green
    }

    /**
     * Sets the green component.
     *
     * @param green The green component. Range from 0 to 255.
     */
    fun setGreen(green: Int) {
        setGreen(green / 255f)
    }

    /**
     * Returns the blue component.
     *
     * @return The blue component.
     */
    fun getBlue(): Float {
        return blue
    }

    /**
     * Sets the blue component.
     *
     * @param blue The blue component. Range from 0f to 1f.
     */
    fun setBlue(blue: Float) {
        var blue = blue
        if (blue < 0f) {
            blue = 0f
        }
        if (blue > 1f) {
            blue = 1f
        }
        this.blue = blue
    }

    /**
     * Sets the blue component.
     *
     * @param blue The blue component. Range from 0 to 255.
     */
    fun setBlue(blue: Int) {
        setBlue(blue / 255f)
    }

    /**
     * Returns the transparency.
     *
     * @return The transparency.
     */
    fun getAlpha(): Float {
        return alpha
    }

    /**
     * Sets the transparency.
     *
     * @param alpha The transparency. Range from 0f to 1f.
     */
    fun setAlpha(alpha: Float) {
        var alpha = alpha
        if (alpha < 0f) {
            alpha = 0f
        }
        if (alpha > 1f) {
            alpha = 1f
        }
        this.alpha = alpha
    }

    /**
     * Sets the transparency.
     *
     * @param alpha The transparency. Range from 0 to 255.
     */
    fun setAlpha(alpha: Int) {
        setAlpha(alpha / 255f)
    }

    /**
     * Returns the color as a (x,y,z)-Vector.
     *
     * @return The color as vec3.
     */
    fun toVector3f(): Vector3f {
        return Vector3f(red, green, blue)
    }

    /**
     * Returns the color as a (x,y,z,w)-Vector.
     *
     * @return The color as vec4.
     */
    fun toVector4f(): Vector4f {
        return Vector4f(red, green, blue, alpha)
    }

    companion object {
        val WHITE: Color = Color(1f, 1f, 1f)
        val BLACK: Color = Color(0f, 0f, 0f)
        val RED: Color = Color(1f, 0f, 0f)
        val GREEN: Color = Color(0f, 1f, 0f)
        val BLUE: Color = Color(0f, 0f, 1f)
    }
}
