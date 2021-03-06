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

import euklid.f.IArc.ArcType
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Represents an arc defined by a framing rectangle, start angle, angular extend, and closure type.
 */
@Suppress("DATA_CLASS_OVERRIDE_DEFAULT_VALUES_WARNING")
data class Arc(
        /** The x-coordinate of this arc's framing rectangle.  */
        override var x: Float = 0f,
        /** The y-coordinate of this arc's framing rectangle.  */
        override var y: Float = 0f,
        /** The width of this arc's framing rectangle.  */
        override var width: Float = 0f,
        /** The height of this arc's framing rectangle.  */
        override var height: Float = 0f,
        /** The starting angle of this arc.  */
        override var angleStart: Float = 0f,
        /** The angular extent of this arc.  */
        override var angleExtent: Float = 0f,
        /** The type of this arc: [IArc.ArcType.OPEN], etc.  */
        override var arcType: ArcType = ArcType.OPEN
) : AbstractArc() {

    /**
     * Creates an arc of the specified type with the supplied framing rectangle, starting angle and
     * angular extent.
     */
    constructor(bounds: IRectangle, start: Float, extent: Float, type: ArcType) :
            this(bounds.x, bounds.y, bounds.width, bounds.height, start, extent, type)

    /**
     * Sets the location, size, angular extents, and closure type of this arc to the specified
     * values.
     * @return a reference to this this, for chaining.
     */
    fun setArc(x: Float, y: Float, width: Float, height: Float,
               angleStart: Float, angleExtent: Float, arcType: ArcType): Arc {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.angleStart = angleStart
        this.angleExtent = angleExtent
        this.arcType = arcType
        return this
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc to the specified
     * values.
     */
    fun setArc(point: XY, size: IDimension, start: Float, extent: Float, type: ArcType) {
        setArc(point.x, point.y, size.width, size.height, start, extent, type)
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc to the specified
     * values.
     */
    fun setArc(rect: IRectangle, start: Float, extent: Float, type: ArcType) {
        setArc(rect.x, rect.y, rect.width, rect.height, start, extent, type)
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc to the same values as
     * the supplied arc.
     */
    fun setArc(arc: IArc) {
        setArc(arc.x, arc.y, arc.width, arc.height, arc.angleStart,
                arc.angleExtent, arc.arcType)
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc based on the
     * specified values.
     */
    fun setArcByCenter(x: Float, y: Float, radius: Float,
                       start: Float, extent: Float, type: ArcType) {
        setArc(x - radius, y - radius, radius * 2f, radius * 2f, start, extent, type)
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc based on the
     * specified values.
     */
    fun setArcByTangent(p1: XY, p2: XY, p3: XY, radius: Float) {
        // use simple geometric calculations of arc center, radius and angles by tangents
        var a1 = -atan2(p1.y - p2.y, p1.x - p2.x)
        var a2 = -atan2(p3.y - p2.y, p3.x - p2.x)
        val am = (a1 + a2) / 2f
        var ah = a1 - am
        val d = radius / abs(sin(ah))
        val x = p2.x + d * cos(am)
        val y = p2.y - d * sin(am)
        ah = if (ah >= 0f) MathUtil.PI * 1.5f - ah else MathUtil.PI * 0.5f - ah
        a1 = normAngle(MathUtil.toDegrees(am - ah))
        a2 = normAngle(MathUtil.toDegrees(am + ah))
        var delta = a2 - a1
        if (delta <= 0f) {
            delta += 360f
        }
        setArcByCenter(x, y, radius, a1, delta, arcType)
    }

    /**
     * Sets the starting angle of this arc to the angle defined by the supplied point relative to
     * the center of this arc.
     */
    fun setAngleStart(point: XY) {
        val angle = atan2(point.y - centerY, point.x - centerX)
        this.angleStart = normAngle(-MathUtil.toDegrees(angle))
    }

    /**
     * Sets the starting angle and angular extent of this arc using two sets of coordinates. The
     * first set of coordinates is used to determine the angle of the starting point relative to
     * the arc's center. The second set of coordinates is used to determine the angle of the end
     * point relative to the arc's center. The arc will always be non-empty and extend
     * counterclockwise from the first point around to the second point.
     */
    fun setAngles(x1: Float, y1: Float, x2: Float, y2: Float) {
        val cx = centerX
        val cy = centerY
        val a1 = normAngle(-MathUtil.toDegrees(atan2(y1 - cy, x1 - cx)))
        var a2 = normAngle(-MathUtil.toDegrees(atan2(y2 - cy, x2 - cx)))
        a2 -= a1
        if (a2 <= 0f) {
            a2 += 360f
        }
        this.angleStart = a1
        this.angleExtent = a2
    }

    /**
     * Sets the starting angle and angular extent of this arc using two sets of coordinates. The
     * first set of coordinates is used to determine the angle of the starting point relative to
     * the arc's center. The second set of coordinates is used to determine the angle of the end
     * point relative to the arc's center. The arc will always be non-empty and extend
     * counterclockwise from the first point around to the second point.
     */
    fun setAngles(p1: XY, p2: XY) {
        setAngles(p1.x, p1.y, p2.x, p2.y)
    }

    override fun setFrame(x: Float, y: Float, width: Float, height: Float) = setArc(x, y, width, height, angleStart, angleExtent, arcType)
}
