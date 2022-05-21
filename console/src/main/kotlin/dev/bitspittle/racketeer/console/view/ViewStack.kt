package dev.bitspittle.racketeer.console.view

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * The current stack of screens.
 *
 * The user will always see the top most screen, but the stack exists so they can request going
 * back.
 *
 * Note: This class is accessed from multiple threads, so it's implementation must be designed to be thread safe.
 * Callers can use [lock] if they need to ensure that the view stack won't change while they're doing something.
 */
interface ViewStack {
    val lock: ReentrantReadWriteLock

    val canGoBack: Boolean
    val currentView: View

    fun pushView(view: View)
    fun popView(): Boolean
    fun replaceView(view: View)

    fun contains(pred: (View) -> Boolean): Boolean
}

/** Keep popping screens until the passed in predicate is true (or we hit bottom) */
fun ViewStack.popUntil(pred: (View) -> Boolean) {
    @Suppress("ControlFlowWithEmptyBody") // popView's side effect is all we need

    lock.write {
        while (this.popView() && !pred(this.currentView)) { }
    }
}

fun ViewStack.popUntilAndRefresh(pred: (View) -> Boolean) {
    lock.write {
        popUntil(pred)
        currentView.refreshCommands()
    }
}

/** Keep popping screens until we hit bottom. */
fun ViewStack.popAll() {
    popUntil { false }
}

fun ViewStack.popAllAndRefresh() {
    lock.write {
        popAll()
        currentView.refreshCommands()
    }
}

/** Keep popping screens until one AFTER we match the predicate, useful for going on past some root screen. */
fun ViewStack.popPast(pred: (View) -> Boolean) {
    var abortNext = false
    popUntil { view ->
        if (abortNext) {
            true
        } else {
            abortNext = pred(view)
            false
        }
    }
}

fun ViewStack.popPastAndRefresh(pred: (View) -> Boolean) {
    lock.write {
        popPast(pred)
        currentView.refreshCommands()
    }
}

class ViewStackImpl : ViewStack {
    override val lock = ReentrantReadWriteLock()

    private val views = mutableListOf<View>()

    override val canGoBack get() = lock.read { views.size > 1 }
    override val currentView get() = lock.read { views.last() }

    override fun pushView(view: View) {
        lock.write {
            views.add(view)
        }
    }

    override fun popView(): Boolean {
        return lock.write {
            if (canGoBack) {
                views.removeLast()
                true
            } else {
                false
            }
        }
    }

    override fun replaceView(view: View) {
        lock.write {
            views.removeLast()
            pushView(view)
        }
    }

    override fun contains(pred: (View) -> Boolean): Boolean {
        return lock.read {
            views.indexOfFirst(pred) >= 0
        }
    }
}