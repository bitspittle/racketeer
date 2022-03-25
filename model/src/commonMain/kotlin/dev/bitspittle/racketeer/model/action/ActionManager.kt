//package dev.bitspittle.racketeer.model.action
//
//import dev.bitspittle.racketeer.model.action.groups.BeginTurnActionGroup
//
//class ActionManager {
//    private val _actionGroups = mutableListOf<ActionGroup>(BeginTurnActionGroup)
//    val actionGroups: List<ActionGroup> = _actionGroups
//
//    val currentActionGroup get() = _actionGroups.last()
//
//    fun pushGroup(actionGroup: ActionGroup) {
//        _actionGroups.add(actionGroup)
//    }
//
//    fun popGroup() {
//        _actionGroups.removeLast()
//    }
//}