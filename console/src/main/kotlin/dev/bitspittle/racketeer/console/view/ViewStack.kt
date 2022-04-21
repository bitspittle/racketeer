package dev.bitspittle.racketeer.console.view

import dev.bitspittle.racketeer.console.view.views.game.GameView

interface ViewStack {
    val canGoBack: Boolean
    val currentView: GameView

    fun pushView(view: GameView)
    fun popView(): Boolean
    fun replaceView(view: GameView)

    fun contains(pred: (GameView) -> Boolean): Boolean
}

/** Keep popping screens until the passed in predicate is true (or we hit bottom) */
fun ViewStack.popUntil(pred: (GameView) -> Boolean) {
    @Suppress("ControlFlowWithEmptyBody") // popView's side effect is all we need
    while (this.popView() && !pred(this.currentView)) {}
}

fun ViewStack.popUntilAndRefresh(pred: (GameView) -> Boolean) {
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
fun ViewStack.popPast(pred: (GameView) -> Boolean) {
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

fun ViewStack.popPastAndRefresh(pred: (GameView) -> Boolean) {
    popPast(pred)
    currentView.refreshCommands()
}

class ViewStackImpl : ViewStack {
    private val _views = mutableListOf<GameView>()
    val views: List<GameView> = _views

    override val canGoBack get() = _views.size > 1
    override val currentView get() = views.last()

    override fun pushView(view: GameView) {
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

    override fun replaceView(view: GameView) {
        _views.removeLast()
        pushView(view)
    }

    override fun contains(pred: (GameView) -> Boolean): Boolean {
        return _views.indexOfFirst(pred) >= 0
    }
}