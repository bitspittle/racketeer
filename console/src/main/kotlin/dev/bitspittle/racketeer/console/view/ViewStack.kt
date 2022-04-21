package dev.bitspittle.racketeer.console.view

interface ViewStack {
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
    while (this.popView() && !pred(this.currentView)) {}
}

fun ViewStack.popUntilAndRefresh(pred: (View) -> Boolean) {
    popUntil(pred)
    currentView.refreshCommands()
}

/** Keep popping screens until we hit bottom. */
fun ViewStack.popAll() {
    popUntil { false }
}

fun ViewStack.popAllAndRefresh() {
    popAll()
    currentView.refreshCommands()
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
    popPast(pred)
    currentView.refreshCommands()
}

class ViewStackImpl : ViewStack {
    private val _views = mutableListOf<View>()
    val views: List<View> = _views

    override val canGoBack get() = _views.size > 1
    override val currentView get() = views.last()

    override fun pushView(view: View) {
        _views.add(view)
    }

    override fun popView(): Boolean {
        return if (canGoBack) {
            _views.removeLast()
            true
        }
        else {
            false
        }
    }

    override fun replaceView(view: View) {
        _views.removeLast()
        pushView(view)
    }

    override fun contains(pred: (View) -> Boolean): Boolean {
        return _views.indexOfFirst(pred) >= 0
    }
}