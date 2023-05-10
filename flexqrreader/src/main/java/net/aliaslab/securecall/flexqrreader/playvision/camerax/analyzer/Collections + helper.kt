package net.aliaslab.securecall.flexqrreader.playvision.camerax

fun <T> Collection<T>.firstOrNull(): T? {
    return try {
        first()
    } catch (exc: NoSuchElementException) {
        null
    }
}

fun <T> Collection<T>.firstPredicateOrNull(predicate: (T) -> Boolean): T? {
    return try {
        first { predicate(it) }
    } catch (exc: NoSuchElementException) {
        null
    }
}