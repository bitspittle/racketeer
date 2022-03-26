package dev.bitspittle.racketeer.console.view

import dev.bitspittle.racketeer.console.view.views.RootView

interface ViewStack {
    fun pushView(view: View)
    fun popView()
    fun replaceView(view: View)
}

class ViewStackImpl : ViewStack {
    private val _views = mutableListOf<View>(RootView)
    val views: List<View> = _views

    val currentView get() = views.last()

    override fun pushView(view: View) {
        _views.add(view)
    }

    override fun popView() {
        check(_views.size > 1)
        _views.removeLast()
    }

    override fun replaceView(view: View) {
        _views.removeLast()
        pushView(view)
    }
}