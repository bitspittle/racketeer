package dev.bitspittle.racketeer.console.view

interface ViewStack {
    val canGoBack: Boolean
    val currentView: View

    fun pushView(view: View)
    fun popView(): Boolean
    fun replaceView(view: View)
}

fun ViewStack.popAllViewsAndRefresh() {
    @Suppress("ControlFlowWithEmptyBody") // popView's side effect is all we need
    while (this.popView()) {}
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
}