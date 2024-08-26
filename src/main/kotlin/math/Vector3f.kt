/*
 * The MIT License (MIT)
 *
 * Copyright © 2015-2017, Heiko Brumme
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.godker.rpg.math

import java.nio.FloatBuffer
import kotlin.math.sqrt


/**
 * This class represents a (x,y,z)-Vector. GLSL equivalent to vec3.
 *
 * @author Heiko Brumme
 */
class Vector3f {
    var x: Float
    var y: Float
    var z: Float

    /**
     * Creates a default 3-tuple vector with all values set to 0.
     */
    constructor() {
        this.x = 0f
        this.y = 0f
        this.z = 0f
    }

    /**
     * Creates a 3-tuple vector with specified values.
     *
     * @param x x value
     * @param y y value
     * @param z z value
     */
    constructor(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    /**
     * Calculates the squared length of the vector.
     *
     * @return Squared length of this vector
     */
    fun lengthSquared(): Float {
        return x * x + y * y + z * z
    }

    /**
     * Calculates the length of the vector.
     *
     * @return Length of this vector
     */
    fun length(): Float {
        return sqrt(lengthSquared().toDouble()) as Float
    }

    /**
     * Normalizes the vector.
     *
     * @return Normalized vector
     */
    fun normalize(): Vector3f {
        val length = length()
        return divide(length)
    }

    /**
     * Adds this vector to another vector.
     *
     * @param other The other vector
     *
     * @return Sum of this + other
     */
    fun add(other: Vector3f): Vector3f {
        val x = this.x + other.x
        val y = this.y + other.y
        val z = this.z + other.z
        return Vector3f(x, y, z)
    }

    /**
     * Negates this vector.
     *
     * @return Negated vector
     */
    fun negate(): Vector3f {
        return scale(-1f)
    }

    /**
     * Subtracts this vector from another vector.
     *
     * @param other The other vector
     *
     * @return Difference of this - other
     */
    fun subtract(other: Vector3f): Vector3f {
        return this.add(other.negate())
    }

    /**
     * Multiplies a vector by a scalar.
     *
     * @param scalar Scalar to multiply
     *
     * @return Scalar product of this * scalar
     */
    fun scale(scalar: Float): Vector3f {
        val x = this.x * scalar
        val y = this.y * scalar
        val z = this.z * scalar
        return Vector3f(x, y, z)
    }

    /**
     * Divides a vector by a scalar.
     *
     * @param scalar Scalar to multiply
     *
     * @return Scalar quotient of this / scalar
     */
    fun divide(scalar: Float): Vector3f {
        return scale(1f / scalar)
    }

    /**
     * Calculates the dot product of this vector with another vector.
     *
     * @param other The other vector
     *
     * @return Dot product of this * other
     */
    fun dot(other: Vector3f): Float {
        return this.x * other.x + this.y * other.y + this.z * other.z
    }

    /**
     * Calculates the dot product of this vector with another vector.
     *
     * @param other The other vector
     *
     * @return Cross product of this x other
     */
    fun cross(other: Vector3f): Vector3f {
        val x = this.y * other.z - this.z * other.y
        val y = this.z * other.x - this.x * other.z
        val z = this.x * other.y - this.y * other.x
        return Vector3f(x, y, z)
    }

    /**
     * Calculates a linear interpolation between this vector with another
     * vector.
     *
     * @param other The other vector
     * @param alpha The alpha value, must be between 0.0 and 1.0
     *
     * @return Linear interpolated vector
     */
    fun lerp(other: Vector3f, alpha: Float): Vector3f {
        return scale(1f - alpha).add(other.scale(alpha))
    }

    /**
     * Stores the vector in a given Buffer.
     *
     * @param buffer The buffer to store the vector data
     */
    fun toBuffer(buffer: FloatBuffer) {
        buffer.put(x).put(y).put(z)
        buffer.flip()
    }
}