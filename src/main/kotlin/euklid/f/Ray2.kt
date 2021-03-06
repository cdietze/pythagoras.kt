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

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sqrt

/**
 * A ray consisting of an origin point and a unit direction vector.
 */
data class Ray2(
        /** The ray's point of origin.  */
        override val origin: Vector = Vector(),
        /** The ray's unit direction vector.  */
        override val direction: Vector = Vector()
) : IRay2 {

    /**
     * Copies the parameters of another ray.
     * @return a reference to this ray, for chaining.
     */
    fun set(other: IRay2): Ray2 = set(other.origin, other.direction)

    /**
     * Sets the ray parameters to the values contained in the supplied vectors.
     * @return a reference to this ray, for chaining.
     */
    fun set(origin: IVector, direction: IVector): Ray2 {
        this.origin.set(origin)
        this.direction.set(direction)
        return this
    }

    /**
     * Transforms this ray in-place.
     * @return a reference to this ray, for chaining.
     */
    fun transformLocal(transform: Transform): Ray2 = transform(transform, this)

    override fun transform(transform: Transform): Ray2 = transform(transform, Ray2())

    override fun transform(transform: Transform, result: Ray2): Ray2 {
        transform.transformPoint(origin, result.origin)
        transform.transform(direction, result.direction).normalizeLocal()
        return result
    }

    override fun intersects(pt: IVector): Boolean {
        if (abs(direction.x) > abs(direction.y)) {
            val t = (pt.x - origin.x) / direction.x
            return t >= 0f && origin.y + t * direction.y == pt.y
        } else {
            val t = (pt.y - origin.y) / direction.y
            return t >= 0f && origin.x + t * direction.x == pt.x
        }
    }

    override fun getIntersection(start: IVector, end: IVector, result: Vector): Boolean {
        // ray is a + t*b, segment is c + s*d
        val ax = origin.x
        val ay = origin.y
        val bx = direction.x
        val by = direction.y
        val cx = start.x
        val cy = start.y
        val dx = end.x - start.x
        val dy = end.y - start.y

        val divisor = bx * dy - by * dx
        if (abs(divisor) < MathUtil.EPSILON) {
            // the lines are parallel (or the segment is zero-length)
            val t = min(getIntersection(start), getIntersection(end))
            val isect = t != Float.MAX_VALUE
            if (isect) {
                origin.addScaled(direction, t, result)
            }
            return isect
        }
        val cxax = cx - ax
        val cyay = cy - ay
        val s = (by * cxax - bx * cyay) / divisor
        if (s < 0f || s > 1f) {
            return false
        }
        val t = (dy * cxax - dx * cyay) / divisor
        val isect = t >= 0f
        if (isect) {
            origin.addScaled(direction, t, result)
        }
        return isect
    }

    override fun getIntersection(start: IVector, end: IVector, radius: Float, result: Vector): Boolean {
        val startx = start.x
        val starty = start.y
        // compute the segment's line parameters
        var a = starty - end.y
        var b = end.x - startx
        val len = hypot(a, b)
        if (len < MathUtil.EPSILON) { // start equals end; check as circle
            return getIntersection(start, radius, result)
        }
        val rlen = 1f / len
        a *= rlen
        b *= rlen
        var c = -a * startx - b * starty

        // find out where the origin lies with respect to the top and bottom
        var dist = a * origin.x + b * origin.y + c
        val above = dist > +radius
        val below = dist < -radius
        val x: Float
        val y: Float
        if (above || below) { // check the intersection with the top/bottom boundary
            val divisor = a * direction.x + b * direction.y
            if (abs(divisor) < MathUtil.EPSILON) { // lines are parallel
                return false
            }
            c += if (above) -radius else +radius
            val t = (-a * origin.x - b * origin.y - c) / divisor
            if (t < 0f) { // wrong direction
                return false
            }
            x = origin.x + t * direction.x
            y = origin.y + t * direction.y

        } else { // middle; check the origin
            x = origin.x
            y = origin.y
        }
        // see where the test point lies with respect to the start and end boundaries
        val tmp = a
        a = b
        b = -tmp
        c = -a * startx - b * starty
        dist = a * x + b * y + c
        if (dist < 0f) { // before start
            return getIntersection(start, radius, result)
        } else if (dist > len) { // after end
            return getIntersection(end, radius, result)
        } else { // middle
            result.set(x, y)
            return true
        }
    }

    override fun getIntersection(center: IVector, radius: Float, result: Vector): Boolean {
        // see if we start inside the circle
        if (origin.distanceSq(center) <= radius * radius) {
            result.set(origin)
            return true
        }
        // then if we intersect the circle
        val ax = origin.x - center.x
        val ay = origin.y - center.y
        val b = 2f * (direction.x * ax + direction.y * ay)
        val c = ax * ax + ay * ay - radius * radius
        val radicand = b * b - 4f * c
        if (radicand < 0f) {
            return false
        }
        val t = (-b - sqrt(radicand)) * 0.5f
        val isect = t >= 0f
        if (isect) {
            origin.addScaled(direction, t, result)
        }
        return isect
    }

    override fun getNearestPoint(point: IVector, result: Vector): Vector {
        val r = point.subtract(origin).dot(direction)
        result.set(origin.add(direction.scale(r)))
        return result
    }

    /**
     * Returns the parameter of the ray when it intersects the supplied point, or
     * [Float.MAX_VALUE] if there is no such intersection.
     */
    private fun getIntersection(pt: IVector): Float {
        if (abs(direction.x) > abs(direction.y)) {
            val t = (pt.x - origin.x) / direction.x
            return if (t >= 0f && origin.y + t * direction.y == pt.y) t else Float.MAX_VALUE
        } else {
            val t = (pt.y - origin.y) / direction.y
            return if (t >= 0f && origin.x + t * direction.x == pt.x) t else Float.MAX_VALUE
        }
    }
}
