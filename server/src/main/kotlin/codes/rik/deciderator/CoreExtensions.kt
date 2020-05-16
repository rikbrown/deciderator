package codes.rik.deciderator

import kotlin.contracts.contract

/**
 * `!` operator for nullable Booleans, where `null` is considered `false`.
 */
operator fun Boolean?.not() = !(this ?: false)

/**
 * Returns true if the second element of this pair is not null
 */
inline fun <reified T, reified P: Pair<T?, T>> Pair<T?, T?>.isSecondNotNull(): Boolean {
  contract {
    returns(true) implies (this@isSecondNotNull is P)
  }
  return second != null
}
