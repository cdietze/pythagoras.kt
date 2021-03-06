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

/**
 * Provides read-only access to a [Matrix4].
 */
interface IMatrix4 {
    /** Returns the (0,0)th component of the matrix.  */
    val m00: Float

    /** Returns the (1,0)th component of the matrix.  */
    val m10: Float

    /** Returns the (2,0)th component of the matrix.  */
    val m20: Float

    /** Returns the (3,0)th component of the matrix.  */
    val m30: Float

    /** Returns the (0,1)th component of the matrix.  */
    val m01: Float

    /** Returns the (1,1)th component of the matrix.  */
    val m11: Float

    /** Returns the (2,1)th component of the matrix.  */
    val m21: Float

    /** Returns the (3,1)th component of the matrix.  */
    val m31: Float

    /** Returns the (0,2)th component of the matrix.  */
    val m02: Float

    /** Returns the (1,2)th component of the matrix.  */
    val m12: Float

    /** Returns the (2,2)th component of the matrix.  */
    val m22: Float

    /** Returns the (3,2)th component of the matrix.  */
    val m32: Float

    /** Returns the (0,3)th component of the matrix.  */
    val m03: Float

    /** Returns the (1,3)th component of the matrix.  */
    val m13: Float

    /** Returns the (2,3)th component of the matrix.  */
    val m23: Float

    /** Returns the (3,3)th component of the matrix.  */
    val m33: Float

    /**
     * Transposes this matrix.
     * @return a new matrix containing the result.
     */
    fun transpose(): Matrix4

    /**
     * Transposes this matrix, storing the result in the provided object.
     * @return the result matrix, for chaining.
     */
    fun transpose(result: Matrix4): Matrix4

    /**
     * Multiplies this matrix by another.
     * @return a new matrix containing the result.
     */
    fun mult(other: IMatrix4): Matrix4

    /**
     * Multiplies this matrix by another and stores the result in the object provided.
     * @return a reference to the result matrix, for chaining.
     */
    fun mult(other: IMatrix4, result: Matrix4): Matrix4

    /**
     * Determines whether this matrix represents an affine transformation.
     */
    val isAffine: Boolean

    /**
     * Determines whether the matrix is mirrored.
     */
    val isMirrored: Boolean

    /**
     * Multiplies this matrix by another, treating the matrices as affine.
     * @return a new matrix containing the result.
     */
    fun multAffine(other: IMatrix4): Matrix4

    /**
     * Multiplies this matrix by another, treating the matrices as affine, and stores the result
     * in the object provided.
     * @return a reference to the result matrix, for chaining.
     */
    fun multAffine(other: IMatrix4, result: Matrix4): Matrix4

    /**
     * Inverts this matrix.
     * @return a new matrix containing the result.
     */
    fun invert(): Matrix4

    /**
     * Inverts this matrix and places the result in the given object.
     * @return a reference to the result matrix, for chaining.
     */
    fun invert(result: Matrix4): Matrix4

    /**
     * Inverts this matrix as an affine matrix.
     * @return a new matrix containing the result.
     */
    fun invertAffine(): Matrix4

    /**
     * Inverts this matrix as an affine matrix and places the result in the given object.
     * @return a reference to the result matrix, for chaining.
     */
    fun invertAffine(result: Matrix4): Matrix4

    /**
     * Linearly interpolates between this and the specified other matrix.
     * @return a new matrix containing the result.
     */
    fun lerp(other: IMatrix4, t: Float): Matrix4

    /**
     * Linearly interpolates between this and the specified other matrix, placing the result in
     * the object provided.
     * @return a reference to the result object, for chaining.
     */
    fun lerp(other: IMatrix4, t: Float, result: Matrix4): Matrix4

    /**
     * Linearly interpolates between this and the specified other matrix, treating the matrices as
     * affine.
     * @return a new matrix containing the result.
     */
    fun lerpAffine(other: IMatrix4, t: Float): Matrix4

    /**
     * Linearly interpolates between this and the specified other matrix (treating the matrices as
     * affine), placing the result in the object provided.
     * @return a reference to the result object, for chaining.
     */
    fun lerpAffine(other: IMatrix4, t: Float, result: Matrix4): Matrix4

    /**
     * Projects the supplied point in-place using this matrix.
     * @return a reference to the point, for chaining.
     */
    fun projectPointLocal(point: Vector3): Vector3

    /**
     * Projects the supplied point using this matrix.
     * @return a new vector containing the result.
     */
    fun projectPoint(point: IVector3): Vector3

    /**
     * Projects the supplied point using this matrix and places the result in the object supplied.
     * @return a reference to the result vector, for chaining.
     */
    fun projectPoint(point: IVector3, result: Vector3): Vector3

    /**
     * Transforms a point in-place by this matrix.
     * @return a reference to the point, for chaining.
     */
    fun transformPointLocal(point: Vector3): Vector3

    /**
     * Transforms a point by this matrix.
     * @return a new vector containing the result.
     */
    fun transformPoint(point: IVector3): Vector3

    /**
     * Transforms a point by this matrix and places the result in the object provided.
     * @return a reference to the result, for chaining.
     */
    fun transformPoint(point: IVector3, result: Vector3): Vector3

    /**
     * Transforms a point by this matrix and returns the resulting z coordinate.
     */
    fun transformPointZ(point: IVector3): Float

    /**
     * Transforms a vector in-place by the inner 3x3 part of this matrix.
     * @return a reference to the vector, for chaining.
     */
    fun transformVectorLocal(vector: Vector3): Vector3

    /**
     * Transforms a vector by this inner 3x3 part of this matrix.
     * @return a new vector containing the result.
     */
    fun transformVector(vector: IVector3): Vector3

    /**
     * Transforms a vector by the inner 3x3 part of this matrix and places the result in the object
     * provided.
     * @return a reference to the result, for chaining.
     */
    fun transformVector(vector: IVector3, result: Vector3): Vector3

    /**
     * Transforms `vector` by this matrix (M * V).
     * @return a new vector containing the result.
     */
    fun transform(vector: IVector4): Vector4

    /**
     * Transforms `vector` by this matrix (M * V) and stores the result in the object
     * provided.
     * @return a reference to the result vector, for chaining.
     */
    fun transform(vector: IVector4, result: Vector4): Vector4

    /**
     * Extracts the rotation component of the matrix.
     * @return a new quaternion containing the result.
     */
    fun extractRotation(): Quaternion

    /**
     * Extracts the rotation component of the matrix and places it in the provided result
     * quaternion.
     * @return a reference to the result quaternion, for chaining.
     */
    fun extractRotation(result: Quaternion): Quaternion

    /**
     * Extracts the rotation and scale components and places them in the provided result.
     * @return a reference to `result`, for chaining.
     */
    fun extractRotationScale(result: Matrix3): Matrix3

    /**
     * Extracts the scale component of the matrix.
     * @return a new vector containing the result.
     */
    fun extractScale(): Vector3

    /**
     * Extracts the scale component of the matrix and places it in the provided result vector.
     * @return a reference to the result vector, for chaining.
     */
    fun extractScale(result: Vector3): Vector3

    /**
     * Returns an approximation of the uniform scale for this matrix (the cube root of the
     * signed volume of the parallelepiped spanned by the axis vectors).
     */
    fun approximateUniformScale(): Float

    /**
     * Compares this matrix to another with the provided epsilon.
     */
    fun epsilonEquals(other: IMatrix4, epsilon: Float): Boolean
}
