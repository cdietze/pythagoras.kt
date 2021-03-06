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

import euklid.assertEqualsWithDelta
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests parts of the [MathUtil] class.
 */
class MathUtilTest {

    @Test
    fun testLerpa() {
        assertEqualsWithDelta(MathUtil.lerpa(PI4, -PI4, 0.25f), PI8, MathUtil.EPSILON)
        assertEqualsWithDelta(MathUtil.lerpa(PI4, -PI4, 0.75f), -PI8, MathUtil.EPSILON)
        assertEqualsWithDelta(MathUtil.lerpa(-PI4, PI4, 0.25f), -PI8, MathUtil.EPSILON)
        assertEqualsWithDelta(MathUtil.lerpa(-PI4, PI4, 0.75f), PI8, MathUtil.EPSILON)
        // make sure we lerp the shortest route around the circle
        assertEqualsWithDelta(MathUtil.lerpa(3 * PI4, PI4, 0.5f), PI2, MathUtil.EPSILON)
        assertEqualsWithDelta(MathUtil.lerpa(PI4, 3 * PI4, 0.5f), PI2, MathUtil.EPSILON)
        assertEqualsWithDelta(MathUtil.lerpa(-3 * PI4, -PI4, 0.5f), -PI2, MathUtil.EPSILON)
        assertEqualsWithDelta(MathUtil.lerpa(-PI4, -3 * PI4, 0.5f), -PI2, MathUtil.EPSILON)

        assertEqualsWithDelta(MathUtil.lerpa(3 * PI4, -3 * PI4, 0.5f), -PI, MathUtil.EPSILON)
    }

    @Test
    fun testToString() {
        assertEquals("+1.0", MathUtil.toString(1f))
        assertEquals("-1.0", MathUtil.toString(-1f))
        assertEquals("+1.1", MathUtil.toString(1.1f))
        assertEquals("-1.1", MathUtil.toString(-1.1f))
        assertEquals("+3.141", MathUtil.toString(MathUtil.PI))
        assertEquals("-3.141", MathUtil.toString(-MathUtil.PI))

        MathUtil.setToStringDecimalPlaces(5)
        assertEquals("+1.0", MathUtil.toString(1f))
        assertEquals("-1.0", MathUtil.toString(-1f))
        assertEquals("+1.1", MathUtil.toString(1.1f))
        assertEquals("-1.1", MathUtil.toString(-1.1f))
        assertEquals("+3.14159", MathUtil.toString(MathUtil.PI))
        assertEquals("-3.14159", MathUtil.toString(-MathUtil.PI))

        MathUtil.setToStringDecimalPlaces(3) // restore the default
    }

    companion object {
        val PI = MathUtil.PI
        val PI2 = MathUtil.PI / 2
        val PI4 = MathUtil.PI / 4
        val PI8 = MathUtil.PI / 8
    }
}
