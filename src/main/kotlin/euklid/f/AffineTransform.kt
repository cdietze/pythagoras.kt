/*
 * Copyright 2017 The Euklid Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package euklid.f

import euklid.util.NoninvertibleTransformException
import kotlin.math.*

/**
 * Implements an affine (3x2 matrix) transform. The transformation matrix has the form:
 * ```
 * [ m00, m10, tx ]
 * [ m01, m11, ty ]
 * [   0,   0,  1 ]
 * ```
 */
data class AffineTransform(
        /** The scale, rotation and shear components of this transform.  */
        var m00: Float = 1f,
        var m01: Float = 0f,
        var m10: Float = 0f,
        var m11: Float = 1f,
        /** The translation components of this transform.  */
        override var tx: Float = 0f,
        override var ty: Float = 0f
) : Transform {

    /** Sets this affine transform matrix to `other`.
     * @return this instance, for chaining.
     */
    fun set(other: AffineTransform): AffineTransform =
            setTransform(other.m00, other.m01, other.m10, other.m11, other.tx, other.ty)

    override val uniformScale: Float
        get() {
            // the square root of the signed area of the parallelogram spanned by the axis vectors
            val cp = m00 * m11 - m01 * m10
            return if (cp < 0f) -sqrt(-cp) else sqrt(cp)
        }

    override val scaleX: Float
        get() {
            return sqrt(m00 * m00 + m01 * m01)
        }

    override val scaleY: Float
        get() {
            return sqrt(m10 * m10 + m11 * m11)
        }

    override val rotation: Float
        get() {
            // use the iterative polar decomposition algorithm described by Ken Shoemake:
            // http://www.cs.wisc.edu/graphics/Courses/838-s2002/Papers/polar-decomp.pdf

            // start with the contents of the upper 2x2 portion of the matrix
            var n00 = m00
            var n10 = m10
            var n01 = m01
            var n11 = m11
            for (ii in 0..9) {
                // store the results of the previous iteration
                val o00 = n00
                val o10 = n10
                val o01 = n01
                val o11 = n11

                // compute average of the matrix with its inverse transpose
                val det = o00 * o11 - o10 * o01
                if (abs(det) == 0f) {
                    // determinant is zero; matrix is not invertible
                    throw NoninvertibleTransformException(this.toString())
                }
                val hrdet = 0.5f / det
                n00 = +o11 * hrdet + o00 * 0.5f
                n10 = -o01 * hrdet + o10 * 0.5f

                n01 = -o10 * hrdet + o01 * 0.5f
                n11 = +o00 * hrdet + o11 * 0.5f

                // compute the difference; if it's small enough, we're done
                val d00 = n00 - o00
                val d10 = n10 - o10
                val d01 = n01 - o01
                val d11 = n11 - o11
                if (d00 * d00 + d10 * d10 + d01 * d01 + d11 * d11 < MathUtil.EPSILON) {
                    break
                }
            }
            // now that we have a nice orthogonal matrix, we can extract the rotation
            return atan2(n01, n00)
        }

    override fun get(matrix: FloatArray) {
        matrix[0] = m00
        matrix[1] = m01
        matrix[2] = m10
        matrix[3] = m11
        matrix[4] = tx
        matrix[5] = ty
    }

    override fun setUniformScale(scale: Float): AffineTransform {
        return setScale(scale, scale) as AffineTransform
    }

    override fun setScaleX(scaleX: Float): AffineTransform {
        // normalize the scale to 1, then re-apply
        val mult = scaleX / this.scaleX
        m00 *= mult
        m01 *= mult
        return this
    }

    override fun setScaleY(scaleY: Float): AffineTransform {
        // normalize the scale to 1, then re-apply
        val mult = scaleY / this.scaleY
        m10 *= mult
        m11 *= mult
        return this
    }

    override fun setRotation(angle: Float): AffineTransform {
        // extract the scale, then reapply rotation and scale together
        val sx = scaleX
        val sy = scaleY
        val sina = sin(angle)
        val cosa = cos(angle)
        m00 = cosa * sx
        m01 = sina * sx
        m10 = -sina * sy
        m11 = cosa * sy
        return this
    }

    override fun setTranslation(tx: Float, ty: Float): AffineTransform {
        this.tx = tx
        this.ty = ty
        return this
    }

    override fun setTx(tx: Float): AffineTransform {
        this.tx = tx
        return this
    }

    override fun setTy(ty: Float): AffineTransform {
        this.ty = ty
        return this
    }

    override fun setTransform(m00: Float, m01: Float, m10: Float, m11: Float,
                              tx: Float, ty: Float): AffineTransform {
        this.m00 = m00
        this.m01 = m01
        this.m10 = m10
        this.m11 = m11
        this.tx = tx
        this.ty = ty
        return this
    }

    override fun uniformScale(scale: Float): AffineTransform {
        return scale(scale, scale)
    }

    override fun scale(scaleX: Float, scaleY: Float): AffineTransform {
        m00 *= scaleX
        m01 *= scaleX
        m10 *= scaleY
        m11 *= scaleY
        return this
    }

    override fun scaleX(scaleX: Float): AffineTransform {
        return Transforms.multiply(this, scaleX, 0f, 0f, 1f, 0f, 0f, this)
    }

    override fun scaleY(scaleY: Float): AffineTransform {
        return Transforms.multiply(this, 1f, 0f, 0f, scaleY, 0f, 0f, this)
    }

    override fun rotate(angle: Float): AffineTransform {
        val sina = sin(angle)
        val cosa = cos(angle)
        return Transforms.multiply(this, cosa, sina, -sina, cosa, 0f, 0f, this)
    }

    override fun translate(tx: Float, ty: Float): AffineTransform {
        this.tx += m00 * tx + m10 * ty
        this.ty += m11 * ty + m01 * tx
        return this
    }

    override fun translateX(tx: Float): AffineTransform {
        return Transforms.multiply(this, 1f, 0f, 0f, 1f, tx, 0f, this)
    }

    override fun translateY(ty: Float): AffineTransform {
        return Transforms.multiply(this, 1f, 0f, 0f, 1f, 0f, ty, this)
    }

    override fun shear(sx: Float, sy: Float): AffineTransform {
        return Transforms.multiply(this, 1f, sy, sx, 1f, 0f, 0f, this)
    }

    override fun shearX(sx: Float): AffineTransform {
        return Transforms.multiply(this, 1f, 0f, sx, 1f, 0f, 0f, this)
    }

    override fun shearY(sy: Float): AffineTransform {
        return Transforms.multiply(this, 1f, sy, 0f, 1f, 0f, 0f, this)
    }

    override fun invert(): AffineTransform {
        // compute the determinant, storing the subdeterminants for later use
        val det = m00 * m11 - m10 * m01
        if (abs(det) == 0f) {
            // determinant is zero; matrix is not invertible
            throw NoninvertibleTransformException(this.toString())
        }
        val rdet = 1f / det
        return AffineTransform(
                +m11 * rdet, -m10 * rdet,
                -m01 * rdet, +m00 * rdet,
                (m10 * ty - m11 * tx) * rdet, (m01 * tx - m00 * ty) * rdet)
    }

    override fun concatenate(other: Transform): Transform {
        if (generality() < other.generality()) {
            return other.preConcatenate(this)
        }
        if (other is AffineTransform) {
            return Transforms.multiply(this, other, AffineTransform())
        } else {
            val oaff = AffineTransform(other)
            return Transforms.multiply(this, oaff, oaff)
        }
    }

    override fun preConcatenate(other: Transform): Transform {
        if (generality() < other.generality()) {
            return other.concatenate(this)
        }
        if (other is AffineTransform) {
            return Transforms.multiply(other, this, AffineTransform())
        } else {
            val oaff = AffineTransform(other)
            return Transforms.multiply(oaff, this, oaff)
        }
    }

    override fun lerp(other: Transform, t: Float): Transform {
        if (generality() < other.generality()) {
            return other.lerp(this, -t) // TODO: is this correct?
        }

        val ot = other as? AffineTransform ?: AffineTransform(other)
        return AffineTransform(
                m00 + t * (ot.m00 - m00), m01 + t * (ot.m01 - m01),
                m10 + t * (ot.m10 - m10), m11 + t * (ot.m11 - m11),
                tx + t * (ot.tx - tx), ty + t * (ot.ty - ty))
    }

    override fun transform(p: IPoint, into: Point): Point {
        val x = p.x
        val y = p.y
        return into.set(m00 * x + m10 * y + tx, m01 * x + m11 * y + ty)
    }

    override fun transform(src: Array<IPoint>, srcOff: Int, dst: Array<Point>, dstOff: Int, count: Int) {
        var srcOff2 = srcOff
        var dstOff2 = dstOff
        for (ii in 0..count - 1) {
            transform(src[srcOff2++], dst[dstOff2++])
        }
    }

    override fun transform(src: FloatArray, srcOff: Int, dst: FloatArray, dstOff: Int, count: Int) {
        var srcOff2 = srcOff
        var dstOff2 = dstOff
        for (ii in 0..count - 1) {
            val x = src[srcOff2++]
            val y = src[srcOff2++]
            dst[dstOff2++] = m00 * x + m10 * y + tx
            dst[dstOff2++] = m01 * x + m11 * y + ty
        }
    }

    override fun inverseTransform(p: IPoint, into: Point): Point {
        val x = p.x - tx
        val y = p.y - ty
        val det = m00 * m11 - m01 * m10
        if (abs(det) == 0f) {
            // determinant is zero; matrix is not invertible
            throw NoninvertibleTransformException(this.toString())
        }
        val rdet = 1 / det
        return into.set((x * m11 - y * m10) * rdet,
                (y * m00 - x * m01) * rdet)
    }

    override fun transformPoint(v: IVector, into: Vector): Vector {
        val x = v.x
        val y = v.y
        return into.set(m00 * x + m10 * y + tx, m01 * x + m11 * y + ty)
    }

    override fun transform(v: IVector, into: Vector): Vector {
        val x = v.x
        val y = v.y
        return into.set(m00 * x + m10 * y, m01 * x + m11 * y)
    }

    override fun inverseTransform(v: IVector, into: Vector): Vector {
        val x = v.x
        val y = v.y
        val det = m00 * m11 - m01 * m10
        if (abs(det) == 0f) {
            // determinant is zero; matrix is not invertible
            throw NoninvertibleTransformException(this.toString())
        }
        val rdet = 1 / det
        return into.set((x * m11 - y * m10) * rdet,
                (y * m00 - x * m01) * rdet)
    }

    override fun copy(): AffineTransform =
            copy(m00 = m00, m01 = m01, m10 = m10, m11 = m11, tx = tx, ty = ty)

    override fun generality(): Int = GENERALITY

    override fun toString(): String = when {
        m00 != 1f || m01 != 0f || m10 != 0f || m11 != 1f ->
            "affine [" +
                    MathUtil.toString(m00) + " " + MathUtil.toString(m01) + " " +
                    MathUtil.toString(m10) + " " + MathUtil.toString(m11) + " " + translation + "]"
        tx != 0f || ty != 0f ->
            "trans " + translation
        else ->
            "ident"
    }

    // we don't publicize this because it might encourage someone to do something stupid like
    // create a new AffineTransform from another AffineTransform using this instead of copy()
    private constructor(other: Transform) : this(other.scaleX, other.scaleY, other.rotation,
            other.tx, other.ty)

    companion object {
        /** Identifies the affine transform in [generality].  */
        val GENERALITY = 4
    }
}

/** Creates an affine transform from the supplied scale, rotation and translation.  */
fun AffineTransform(scaleX: Float, scaleY: Float, angle: Float, tx: Float, ty: Float): AffineTransform {
    val sina = sin(angle)
    val cosa = cos(angle)
    return AffineTransform(
            cosa * scaleX,
            sina * scaleY,
            -sina * scaleX,
            cosa * scaleY,
            tx,
            ty)
}
