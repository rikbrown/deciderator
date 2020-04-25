package codes.rik.deciderator

fun <T> Collection<T>.replace(predicate: (T) -> Boolean, replacer: (T) -> T) =
  map {
    if (predicate(it)) replacer(it)
    else (it)
  }
