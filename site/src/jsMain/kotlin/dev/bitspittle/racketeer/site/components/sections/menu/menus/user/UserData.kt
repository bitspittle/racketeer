package dev.bitspittle.racketeer.site.components.sections.menu.menus.user

import androidx.compose.runtime.*
import com.varabyte.kobweb.silk.components.style.*
import dev.bitspittle.racketeer.site.components.sections.ReadOnlyStyle
import dev.bitspittle.racketeer.site.components.sections.menu.Menu
import dev.bitspittle.racketeer.site.components.sections.menu.MenuActions
import dev.bitspittle.racketeer.site.components.sections.menu.MenuButton
import dev.bitspittle.racketeer.site.components.util.PopupParams
import dev.bitspittle.racketeer.site.components.util.installPopup
import org.jetbrains.compose.web.dom.*

class UserDataMenu(private val params: PopupParams) : Menu {
    override val title = "User Data"

    @Composable
    override fun renderContent(actions: MenuActions) {
        MenuButton(actions, CardListMenu(params))
        MenuButton(actions, BuildingListMenu(params))
    }
}
