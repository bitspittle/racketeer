package dev.bitspittle.racketeer.console.view

interface ViewStack {
    val canGoBack: Boolean

    fun pushView(view: View)
    fun popView(): Boolean
    fun replaceView(view: View)
}

class ViewStackImpl : ViewStack {
    private val _views = mutableListOf<View>()
    val views: List<View> = _views

    val currentView get() = views.last()


    override val canGoBack get() = _views.size > 1

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