package com.xcstasy.r.larva.core.functional.type

//From http://kategory.io

typealias Right<A, B> = Either.Right<A, B>
typealias Left<A, B> = Either.Left<A, B>

/**
 * Port of https://github.com/scala/scala/blob/v2.12.1/src/library/scala/util/Either.scala
 *
 * Represents a value of one of two possible types (a disjoint union.)
 * An instance of Either is either an instance of [Left] or [Right].
 */
sealed class Either<out A, out B> {
    /**
     * Returns `true` if this is a [Right], `false` otherwise.
     * Used only for performance instead of fold.
     */
    abstract val isRight: Boolean

    /**
     * Returns `true` if this is a [Left], `false` otherwise.
     * Used only for performance instead of fold.
     */
    abstract val isLeft: Boolean

    /**
     * Applies `fa` if this is a [Left] or `fb` if this is a [Right].
     *
     * Example:
     * ```
     * val result: Either<Exception, Value> = possiblyFailingOperation()
     * result.fold(
     *      { log("operation failed with $it") },
     *      { log("operation succeeded with $it") }
     * )
     * ```
     *
     * @param fa the function to apply if this is a [Left]
     * @param fb the function to apply if this is a [Right]
     * @return the results of applying the function
     */
    inline fun <C> fold(crossinline fa: (A) -> C, crossinline fb: (B) -> C): C = when (this) {
        is Right<A, B> -> fb(b)
        is Left<A, B> -> fa(a)
    }

    /**
     * If this is a `Left`, then return the left value in `Right` or vice versa.
     *
     * Example:
     * ```
     * Left("left").swap()   // Result: Right("left")
     * Right("right").swap() // Result: Left("right")
     * ```
     */
    fun swap(): Either<B, A> = fold({ Right(it) }, { Left(it) })

    /**
     * The given function is applied if this is a `Right`.
     *
     * Example:
     * ```
     * Right(12).map { "flower" } // Result: Right("flower")
     * Left(12).map { "flower" }  // Result: Left(12)
     * ```
     */
    inline fun <C> map(crossinline f: (B) -> C): Either<A, C> = fold({ Left(it) }, { Right(f(it)) })

    /**
     * Map over Left and Right of this Either
     */
    inline fun <C, D> biMap(crossinline fa: (A) -> C,
                            crossinline fb: (B) -> D): Either<C, D> = fold({ Left(fa(it)) }, { Right(fb(it)) })

    /**
     * Returns `false` if [Left] or returns the result of the application of
     * the given predicate to the [Right] value.
     *
     * Example:
     * ```
     * Right(12).exists { it > 10 } // Result: true
     * Right(7).exists { it > 10 }  // Result: false
     *
     * val left: Either<Int, Int> = Left(12)
     * left.exists { it > 10 }      // Result: false
     * ```
     */
    inline fun exists(crossinline predicate: (B) -> Boolean): Boolean = fold({ false }, { predicate(it) })

    /**
     * The left side of the disjoint union, as opposed to the [Right] side.
     */
    data class Left<out A, out B>(val a: A, private val dummy: Unit) : Either<A, B>() {
        override val isLeft = true
        override val isRight = false

        companion object {
            inline operator fun <A> invoke(a: A): Either<A, Nothing> = Left(a, Unit)
        }
    }

    /**
     * The right side of the disjoint union, as opposed to the [Left] side.
     */
    data class Right<out A, out B>(val b: B, private val dummy: Unit) : Either<A, B>() {
        override val isLeft = false
        override val isRight = true

        companion object {
            inline operator fun <B> invoke(b: B): Either<Nothing, B> = Right(b, Unit)
        }
    }

}

/**
 * Binds the given function across [Either.Right].
 *
 * @param f The function to bind across [Either.Right].
 */
inline fun <A, B, C> Either<A, B>.flatMap(crossinline f: (B) -> Either<A, C>): Either<A, C> = fold({ Left(it) }, { f(it) })

/**
 * Returns the value from this [Either.Right] or the given argument if this is a [Either.Left].
 *
 * Example:
 * ```
 * Right(12).rightOrElse(17) // Result: 12
 * Left(12).rightOrElse(17)  // Result: 17
 * ```
 */
inline fun <B> Either<*, B>.rightOrElse(crossinline default: () -> B): B = fold({ default() }, { it })

/**
 * Returns the value from this [Either.Left] or the given argument if this is a [Either.Right].
 *
 * Example:
 * ```
 * Right(12).leftOrElse(17) // Result: 17
 * Left(12).leftOrElse(17)  // Result: 12
 * ```
 */
inline fun <A> Either<A, *>.leftOrElse(crossinline default: () -> A): A = fold({ it }, { default() })


/**
 * * Returns [Either.Right] with the existing value of [Either.Right] if this is a [Either.Right] and the given predicate
 * holds for the right value.
 * * Returns `Left(default)` if this is a [Either.Right] and the given predicate does not
 * hold for the right value.
 * * Returns [Either.Left] with the existing value of [Either.Left] if this is a [Either.Left].
 *
 * Example:
 * ```
 * Right(12).filterOrElse({ it > 10 }, { -1 }) // Result: Right(12)
 * Right(7).filterOrElse({ it > 10 }, { -1 })  // Result: Left(-1)
 *
 * val left: Either<Int, Int> = Left(12)
 * left.filterOrElse({ it > 10 }, { -1 })      // Result: Left(12)
 * ```
 */
inline fun <A, B> Either<A, B>.filterOrElse(crossinline predicate: (B) -> Boolean, crossinline default: () -> A): Either<A, B> =
        fold({ Left(it) }, { if (predicate(it)) Right(it) else Left(default()) })

/**
 * Returns `true` if this is a [Either.Right] and its value is equal to `elem` (as determined by `==`),
 * returns `false` otherwise.
 *
 * Example:
 * ```
 * Right("something").contains { "something" } // Result: true
 * Right("something").contains { "anything" }  // Result: false
 * Left("something").contains { "something" }  // Result: false
 *  ```
 *
 * @param elem the element to test.
 * @return `true` if the option has an element that is equal (as determined by `==`) to `elem`, `false` otherwise.
 */
fun <A, B> Either<A, B>.contains(elem: B): Boolean = fold({ false }, { it == elem })

fun <A> A.left(): Either<A, Nothing> = Left(this)

fun <A> A.right(): Either<Nothing, A> = Right(this)

/**
 * Map over Left and Right of this Either
 */
inline fun <A, B> Either<A, B>.unitBiMap(crossinline fa: (A) -> Unit = {},
                                         crossinline fb: (B) -> Unit = {}) = biMap(fa, fb)