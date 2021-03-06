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

import euklid.platform.arrayCopy
import kotlin.math.max

/**
 * Represents a path constructed from lines and curves and which can contain subpaths.
 */
class Path : IShape {

    constructor(rule: Int = WIND_NON_ZERO, initialCapacity: Int = BUFFER_SIZE) {
        setWindingRule(rule)
        types = ByteArray(initialCapacity)
        points = FloatArray(initialCapacity * 2)
    }

    constructor(shape: IShape) : this(WIND_NON_ZERO, BUFFER_SIZE) {
        val p = shape.pathIterator()
        setWindingRule(p.windingRule())
        append(p, false)
    }

    fun setWindingRule(rule: Int) {
        if (rule != WIND_EVEN_ODD && rule != WIND_NON_ZERO) {
            throw IllegalArgumentException("Invalid winding rule value")
        }
        this.rule = rule
    }

    fun windingRule(): Int {
        return rule
    }

    fun moveTo(x: Float, y: Float): Path {
        if (typeSize > 0 && types[typeSize - 1].toInt() == PathIterator.SEG_MOVETO) {
            points[pointSize - 2] = x
            points[pointSize - 1] = y
        } else {
            checkBuf(2, false)
            types[typeSize++] = PathIterator.SEG_MOVETO.toByte()
            points[pointSize++] = x
            points[pointSize++] = y
        }
        return this
    }

    fun lineTo(x: Float, y: Float): Path {
        checkBuf(2, true)
        types[typeSize++] = PathIterator.SEG_LINETO.toByte()
        points[pointSize++] = x
        points[pointSize++] = y
        return this
    }

    fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float): Path {
        checkBuf(4, true)
        types[typeSize++] = PathIterator.SEG_QUADTO.toByte()
        points[pointSize++] = x1
        points[pointSize++] = y1
        points[pointSize++] = x2
        points[pointSize++] = y2
        return this
    }

    fun curveTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Path {
        checkBuf(6, true)
        types[typeSize++] = PathIterator.SEG_CUBICTO.toByte()
        points[pointSize++] = x1
        points[pointSize++] = y1
        points[pointSize++] = x2
        points[pointSize++] = y2
        points[pointSize++] = x3
        points[pointSize++] = y3
        return this
    }

    fun closePath(): Path {
        if (typeSize == 0 || types[typeSize - 1].toInt() != PathIterator.SEG_CLOSE) {
            checkBuf(0, true)
            types[typeSize++] = PathIterator.SEG_CLOSE.toByte()
        }
        return this
    }

    fun append(shape: IShape, connect: Boolean): Path {
        val p = shape.pathIterator()
        return append(p, connect)
    }

    fun append(path: PathIterator, connect: Boolean): Path {
        var connect = connect
        while (!path.isDone) {
            val coords = FloatArray(6)
            when (path.currentSegment(coords)) {
                PathIterator.SEG_MOVETO -> if (!connect || typeSize == 0) {
                    moveTo(coords[0], coords[1])
                } else if (types[typeSize - 1].toInt() != PathIterator.SEG_CLOSE &&
                        points[pointSize - 2] == coords[0] &&
                        points[pointSize - 1] == coords[1]) {
                    // we're already here
                } else {
                    lineTo(coords[0], coords[1])
                }
                PathIterator.SEG_LINETO -> lineTo(coords[0], coords[1])
                PathIterator.SEG_QUADTO -> quadTo(coords[0], coords[1], coords[2], coords[3])
                PathIterator.SEG_CUBICTO -> curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5])
                PathIterator.SEG_CLOSE -> closePath()
            }
            path.next()
            connect = false
        }
        return this
    }

    fun currentPoint(): Point? {
        if (typeSize == 0) {
            return null
        }
        var j = pointSize - 2
        if (types[typeSize - 1].toInt() == PathIterator.SEG_CLOSE) {
            for (i in typeSize - 2 downTo 1) {
                val type = types[i].toInt()
                if (type == PathIterator.SEG_MOVETO) {
                    break
                }
                j -= pointShift[type]
            }
        }
        return Point(points[j], points[j + 1])
    }

    fun reset(): Path {
        typeSize = 0
        pointSize = 0
        return this
    }

    fun transform(t: Transform): Path {
        t.transform(points, 0, points, 0, pointSize / 2)
        return this
    }

    fun createTransformedShape(t: Transform?): IShape {
        val p = copy()
        if (t != null) {
            p.transform(t)
        }
        return p
    }

    override fun bounds(target: Rectangle): Rectangle {
        var rx1: Float
        var ry1: Float
        var rx2: Float
        var ry2: Float
        if (pointSize == 0) {
            ry2 = 0f
            rx2 = ry2
            ry1 = rx2
            rx1 = ry1
        } else {
            var i = pointSize - 1
            ry2 = points[i--]
            ry1 = ry2
            rx2 = points[i--]
            rx1 = rx2
            while (i > 0) {
                val y = points[i--]
                val x = points[i--]
                if (x < rx1) {
                    rx1 = x
                } else if (x > rx2) {
                    rx2 = x
                }
                if (y < ry1) {
                    ry1 = y
                } else if (y > ry2) {
                    ry2 = y
                }
            }
        }
        target.setBounds(rx1, ry1, rx2 - rx1, ry2 - ry1)
        return target
    }

    override            // TODO: will this be insanely difficult to do correctly?
    val isEmpty: Boolean
        get() = bounds().isEmpty

    override fun contains(x: Float, y: Float): Boolean {
        return isInside(Crossing.crossShape(this, x, y))
    }

    override fun contains(x: Float, y: Float, width: Float, height: Float): Boolean {
        val cross = Crossing.intersectShape(this, x, y, width, height)
        return cross != Crossing.CROSSING && isInside(cross)
    }

    override fun intersects(x: Float, y: Float, width: Float, height: Float): Boolean {
        val cross = Crossing.intersectShape(this, x, y, width, height)
        return cross == Crossing.CROSSING || isInside(cross)
    }

    override fun pathIterator(transform: Transform?): PathIterator {
        return Iterator(this, transform)
    }

    override fun pathIterator(transform: Transform?, flatness: Float): PathIterator {
        return FlatteningPathIterator(pathIterator(transform), flatness)
    }

    /** Returns a copy of this path. */
    fun copy(): Path =
            Path(rule, types.copyOf(), points.copyOf(), typeSize, pointSize)

    /**
     * Checks points and types buffer size to add pointCount points. If necessary realloc buffers
     * to enlarge size.
     * @param pointCount the point count to be added in buffer
     */
    protected fun checkBuf(pointCount: Int, checkMove: Boolean) {
        if (checkMove && typeSize == 0) {
            throw IllegalPathStateException("First segment must be a SEG_MOVETO")
        }
        if (typeSize == types.size) {
            val tmp = ByteArray(typeSize + BUFFER_CAPACITY)
            arrayCopy(types, 0, tmp, 0, typeSize)
            types = tmp
        }
        if (pointSize + pointCount > points.size) {
            val tmp = FloatArray(pointSize + max(BUFFER_CAPACITY * 2, pointCount))
            arrayCopy(points, 0, tmp, 0, pointSize)
            points = tmp
        }
    }

    /**
     * Checks cross count according to path rule to define is it point inside shape or not.
     * @param cross the point cross count.
     * *
     * @return true if point is inside path, or false otherwise.
     */
    protected fun isInside(cross: Int): Boolean {
        return if (rule == WIND_NON_ZERO)
            Crossing.isInsideNonZero(cross)
        else
            Crossing.isInsideEvenOdd(cross)
    }

    private constructor(rule: Int, types: ByteArray, points: FloatArray, typeSize: Int, pointSize: Int) {
        this.rule = rule
        this.types = types
        this.points = points
        this.typeSize = typeSize
        this.pointSize = pointSize
    }

    /** An iterator over a [Path].  */
    protected class Iterator internal constructor(
            /** The source Path object.  */
            private val p: Path,
            /** The path iterator transformation.  */
            private val t: Transform? = null) : PathIterator {
        /** The current cursor position in types buffer.  */
        private var typeIndex: Int = 0

        /** The current cursor position in points buffer.  */
        private var pointIndex: Int = 0

        override fun windingRule(): Int {
            return p.windingRule()
        }

        override val isDone: Boolean
            get() = typeIndex >= p.typeSize

        override fun next() {
            typeIndex++
        }

        override fun currentSegment(coords: FloatArray): Int {
            if (isDone) {
                throw NoSuchElementException("Iterator out of bounds")
            }
            val type = p.types[typeIndex].toInt()
            val count = Path.pointShift[type]
            arrayCopy(p.points, pointIndex, coords, 0, count)
            t?.transform(coords, 0, coords, 0, count / 2)
            pointIndex += count
            return type
        }
    }

    /** The point's types buffer.  */
    protected var types: ByteArray

    /** The points buffer.  */
    protected var points: FloatArray

    /** The point's type buffer size.  */
    protected var typeSize: Int = 0

    /** The points buffer size.  */
    protected var pointSize: Int = 0

    /* The path rule. */
    protected var rule: Int = 0

    companion object {
        /** Specifies the even/odd rule for determining the interior of a path.  */
        val WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD

        /** Specifies the non-zero rule for determining the interior of a path.  */
        val WIND_NON_ZERO = PathIterator.WIND_NON_ZERO

        /** The space required in points buffer for different segment types.  */
        protected var pointShift = intArrayOf(2, // MOVETO
                2, // LINETO
                4, // QUADTO
                6, // CUBICTO
                0) // CLOSE

        /** The default initial buffer size.  */
        protected val BUFFER_SIZE = 10

        /** The amount by which to expand the buffer capacity.  */
        protected val BUFFER_CAPACITY = 10
    }
}
