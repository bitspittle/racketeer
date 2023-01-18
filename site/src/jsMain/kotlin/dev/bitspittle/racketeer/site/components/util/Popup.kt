package dev.bitspittle.racketeer.site.components.util

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.dom.ElementTarget
import com.varabyte.kobweb.compose.foundation.layout.BoxScope
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.overlay.Popup
import com.varabyte.kobweb.silk.components.overlay.PopupPlacement
import dev.bitspittle.limp.types.Logger
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.methods.collection.FormattedItem
import dev.bitspittle.racketeer.site.components.layouts.FirebaseData
import dev.bitspittle.racketeer.site.components.widgets.Card
import dev.bitspittle.racketeer.site.components.widgets.toCardSpec
import dev.bitspittle.racketeer.site.model.*
import dev.bitspittle.racketeer.site.model.account.Account
import dev.bitspittle.racketeer.site.model.user.UserData
import dev.bitspittle.racketeer.site.model.user.UserData.Settings
import org.jetbrains.compose.web.css.*

class PopupParams(
    val firebase: FirebaseData,
    val data: GameData,
    val events: Events,
    val account: Account,
    val settings: Settings,
    val userStats: UserData.Stats,
    val logger: Logger,
    val describer: Describer,
    val tooltipParser: TooltipParser
)

fun GameContext.toPopupParams() =
    PopupParams(firebase, data, events, account, settings, userStats, logger, describer, tooltipParser)

fun ChoiceContext.toPopupParams() =
    PopupParams(firebase, data, events, account, settings, userStats, logger, describer, tooltipParser)

@Composable
fun installPopup(params: PopupParams, item: Any) {
    @Composable
    fun RightPopup(content: @Composable BoxScope.() -> Unit) {
        Popup(
            ElementTarget.PreviousSibling, placement = PopupPlacement.Right, content = content,
            modifier = Modifier
                .backgroundColor(Colors.WhiteSmoke)
                .padding(5.px)
                .borderRadius(5.px)
                .outline(1.px, LineStyle.Solid, Colors.Black)
        )
    }

    with(params) {
        when (item) {
            is Blueprint -> RightPopup {
                Card(
                    data,
                    userStats,
                    describer,
                    tooltipParser,
                    item.toCardSpec(userStats, describer)
                )
            }

            is Building -> RightPopup { Card(data, userStats, describer, tooltipParser, item.toCardSpec(describer)) }
            is Card -> RightPopup { Card(data, userStats, describer, tooltipParser, item.toCardSpec(data, userStats)) }
            is CardTemplate -> RightPopup {
                Card(
                    data,
                    userStats,
                    describer,
                    tooltipParser,
                    item.toCardSpec(data, userStats)
                )
            }

            is FormattedItem -> installPopup(params, item.wrapped)
            is List<*> -> @Suppress("UNCHECKED_CAST") when (item.first()) {
                is Card -> RightPopup {
                    Card(data, userStats, describer, tooltipParser, (item as List<Card>).toCardSpec(data))
                }
            }
        }
    }
}

@Composable
fun installPopup(ctx: GameContext, item: Any) = installPopup(ctx.toPopupParams(), item)

@Composable
fun installPopup(ctx: ChoiceContext, item: Any) = installPopup(ctx.toPopupParams(), item)
