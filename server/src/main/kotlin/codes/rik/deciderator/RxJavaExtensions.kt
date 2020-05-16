package codes.rik.deciderator

import io.reactivex.rxjava3.core.Observable
import java.util.Optional
import kotlin.contracts.ExperimentalContracts

/**
 * Returns an [Observable] with elements in a sliding window of 2
 * @see [http://www.zerobugbuild.com/?p=213](.NET inspiration)
 */
fun <T> Observable<T>.slidingPairs(): Observable<Pair<T?, T?>> = scan(null to null) { acc: Pair<T?, T?>, cur: T? -> acc.second to cur }

/**
 * Returns an [Observable] which only emits the next element if it was distinct to the previous one
 */
inline fun <reified T> Observable<T>.distinctWithPrevious() = slidingPairs()
  .filter { (prev, next) -> prev != next }
  .nonNull()
  .map { (_, next) -> next }

/**
 * Maps the [Observable]s value and emits only if the result is non-null
 */
fun <T, R> Observable<T>.mapNotNull(fn: (T) -> R?): Observable<R> = mapOptional { Optional.ofNullable(fn(it)) }

inline fun <reified T> Observable<Pair<T?, T?>>.nonNull(): Observable<Pair<T?, T>> = this
  .mapNotNull {
    if (it.isSecondNotNull()) it
    else null
  }

