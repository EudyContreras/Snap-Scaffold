package com.eudycontreras.snapscaffold

internal const val MAX_OFFSET = 1f
internal const val MIN_OFFSET = 0f

val Int.f: Float
    get() = this.toFloat()

fun Float.coerceFraction() = this.coerceIn(MIN_OFFSET, MAX_OFFSET)

/**
 * Maps the given value from the specified minimum to the specified
 * minimum and from the specified maximum to the specified maximum
 * value. Ex:
 * ```
 *  var value = 40f
 *
 *  var fromMin = 0f
 *  var fromMax = 100f
 *
 *  var toMin = 0f
 *  var toMax = 1f
 *
 *  var result = 0.4f
 * ```
 * @param value the value to be transformed
 * @param fromMin the minimum value to map from
 * @param fromMax the maximum value to map from
 * @param toMin the minimum value to map to
 * @param toMax the maximum value to map to
 */
fun mapRange(
    value: Float,
    fromMin: Float = MIN_OFFSET,
    fromMax: Float = MAX_OFFSET,
    toMin: Float = MIN_OFFSET,
    toMax: Float = MAX_OFFSET
): Float {
    return ((value - fromMin) * (toMax - toMin) / (fromMax - fromMin) + toMin)
}

/**
 * Maps the given value from the specified minimum to the specified
 * minimum and from the specified maximum to the specified maximum using
 * clamping.
 * value. Ex:
 * ```
 *  var value = 40f
 *
 *  var fromMin = 0f
 *  var fromMax = 100f
 *
 *  var toMin = 0f
 *  var toMax = 1f
 *
 *  var result = 0.4f
 * ```
 * @param value the value to be transformed
 * @param fromMin the minimum value to map from
 * @param fromMax the maximum value to map from
 * @param toMin the minimum value to map to
 * @param toMax the maximum value to map to
 * @param clampMin the minimum value that the function can return
 * @param clampMax the maximum value that the function can return
 */
fun mapRangeBounded(
    value: Float,
    fromMin: Float = MIN_OFFSET,
    fromMax: Float = MAX_OFFSET,
    toMin: Float = MIN_OFFSET,
    toMax: Float = MAX_OFFSET,
    clampMin: Float = toMin,
    clampMax: Float = toMax
): Float {
    val fraction = mapRange(value, fromMin, fromMax, toMin, toMax).coerceIn(
        minimumValue = clampMin,
        maximumValue = clampMax
    )
    return if (value.isNaN()) toMin else fraction
}