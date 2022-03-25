package dev.bitspittle.racketeer.console.view

import dev.bitspittle.racketeer.console.view.views.BeginTurnView

class ViewManager {
    private val _views = mutableListOf<View>(BeginTurnView)
    val views: List<View> = _views

    val currentView get() = views.last()

    fun pushView(view: View) {
        _views.add(view)
    }

    fun popView() {
        check(_views.size > 1)
        _views.removeLast()
    }

    fun replaceView(view: View) {
        _views.removeLast()
        pushView(view)
    }
}