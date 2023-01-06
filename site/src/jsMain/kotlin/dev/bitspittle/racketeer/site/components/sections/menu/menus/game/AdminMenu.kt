package dev.bitspittle.racketeer.site.components.sections.menu.menus.game

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.varabyte.kobweb.compose.foundation.layout.RowScope
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.silk.components.forms.Button
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.UpgradeType
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.game.allPiles
import dev.bitspittle.racketeer.model.game.recordChanges
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import dev.bitspittle.racketeer.site.KeyScope
import dev.bitspittle.racketeer.site.components.sections.menu.Menu
import dev.bitspittle.racketeer.site.components.sections.menu.MenuActions
import dev.bitspittle.racketeer.site.components.sections.menu.MenuButton
import dev.bitspittle.racketeer.site.components.util.Data
import dev.bitspittle.racketeer.site.components.util.downloadSnapshotToDisk
import dev.bitspittle.racketeer.site.components.util.installPopup
import dev.bitspittle.racketeer.site.components.util.loadSnapshotFromDisk
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

class AdminMenu(private val params: GameMenuParams) : Menu {
    override val title = "Admin"

    @Composable
    override fun renderContent(actions: MenuActions) {
        MenuButton(actions, CreateCard(params))
        MenuButton(actions, BuildBuilding(params))
        MenuButton(actions, MoveCards.FromPile(params))
        MenuButton(actions, UpgradeCard.FromPile(params))
        MenuButton(actions, Snapshot(params))
        Button(
            onClick = {
                window.open(
                    "https://docs.google.com/spreadsheets/d/1iG38W0xl2UzRHhQX_GvJWg3zZndqY-UkKAVaWzNiLKg/edit#gid=200941839",
                    target = "_blank"
                )
                actions.close()
            },
        ) { Text("Open API Sheet") }
    }

    class CreateCard(private val params: GameMenuParams) : Menu {
        override val title = "Create Card"

        @Composable
        override fun renderContent(actions: MenuActions) {
            with(params) {
                ctx.data.cards.sortedBy { it.name }.forEach { card ->
                    Button(onClick = {
                        updater.runStateChangingAction {
                            ctx.state.addChange(
                                GameStateChange.MoveCard(
                                    ctx.state,
                                    card.instantiate(),
                                    ctx.state.hand,
                                    ListStrategy.FRONT
                                )
                            )
                        }
                        actions.close()
                    }) { Text(card.name) }
                    installPopup(ctx, card)
                }
            }
        }
    }

    class BuildBuilding(private val params: GameMenuParams) : Menu {
        override val title = "Build Building"

        @Composable
        override fun renderContent(actions: MenuActions) {
            with(params) {
                ctx.data.blueprints.sortedBy { it.name }.forEach { blueprint ->
                    Button(
                        onClick = {
                            // Run this command in two separate state changing actions; you need to own the blueprint before you can
                            // build it.
                            updater.runStateChangingActions(
                                {
                                    if (!ctx.state.blueprints.contains(blueprint)) {
                                        ctx.state.addChange(GameStateChange.AddBlueprint(blueprint))
                                    }
                                },
                                {
                                    check(ctx.state.blueprints.indexOf(blueprint) >= 0)
                                    ctx.state.addChange(GameStateChange.Build(blueprint, free = true))
                                },
                            )
                            actions.close()
                        },
                        enabled = ctx.state.buildings.none { it.blueprint === blueprint }
                    ) { Text(blueprint.name) }
                    installPopup(params.ctx, blueprint)
                }
            }
        }
    }

    object UpgradeCard {
        private val cardUpgradesToTypeIds = mapOf(
            UpgradeType.CASH to "thief",
            UpgradeType.INFLUENCE to "spy",
            UpgradeType.LUCK to "trickster",
            UpgradeType.VETERAN to "cop",
        )

        private fun Card.canUpgrade(vararg anyOfTypes: UpgradeType): Boolean {
            return (anyOfTypes.takeIf { it.isNotEmpty() } ?: UpgradeType.values()).any { type ->
                !upgrades.contains(type) && template.types.contains(cardUpgradesToTypeIds[type])
            }
        }

        class FromPile(private val params: GameMenuParams) : Menu {
            override val title = "Upgrade Card"

            @Composable
            override fun renderContent(actions: MenuActions) {
                with(params) {
                    ctx.state.allPiles.forEach { pile ->
                        Button(
                            onClick = {
                                actions.visit(ChooseCard(params, pile.cards))
                            },
                            enabled = pile.cards.any { card -> card.canUpgrade() }
                        ) { Text(ctx.describer.describePileTitle(ctx.state, pile, withSize = true)) }
                    }
                }
            }
        }

        class ChooseCard(private val params: GameMenuParams, private val cards: List<Card>) : Menu {
            override val title = "Choose"

            @Composable
            override fun renderContent(actions: MenuActions) {
                with(params) {
                    cards.forEach { card ->
                        Button(
                            onClick = {
                                scope.launch {
                                    ctx.state.recordChanges {
                                        if (card.canUpgrade(UpgradeType.CASH)
                                        ) {
                                            ctx.state.addChange(GameStateChange.UpgradeCard(card, UpgradeType.CASH))
                                        }
                                        if (card.canUpgrade(UpgradeType.INFLUENCE)
                                        ) {
                                            ctx.state.addChange(GameStateChange.UpgradeCard(card, UpgradeType.INFLUENCE))
                                        }
                                        if (card.canUpgrade(UpgradeType.LUCK)
                                        ) {
                                            ctx.state.addChange(GameStateChange.UpgradeCard(card, UpgradeType.LUCK))
                                        }
                                        if (card.canUpgrade(UpgradeType.VETERAN)
                                        ) {
                                            ctx.state.addChange(GameStateChange.UpgradeCard(card, UpgradeType.VETERAN))
                                        }
                                    }

                                    actions.close()
                                }
                            },
                            enabled = card.canUpgrade()
                        ) { Text(card.template.name) }
                        installPopup(ctx, card)
                    }
                }
            }
        }
    }

    object MoveCards {
        class FromPile(private val params: GameMenuParams) : Menu {
            override val title = "Move Cards"

            @Composable
            override fun renderContent(actions: MenuActions) {
                with(params) {
                    ctx.state.allPiles.forEach { pile ->
                        Button(
                            onClick = {
                                actions.visit(ChooseCards(params, pile))
                            },
                            enabled = pile.cards.isNotEmpty(),
                        ) { Text(ctx.describer.describePileTitle(ctx.state, pile, withSize = true)) }
                    }
                }
            }
        }

        class ChooseCards(private val params: GameMenuParams, private val pile: Pile) : Menu {
            private val ctx = params.ctx
            override val title = "From ${ctx.describer.describePileTitle(ctx.state, pile)}"

            private lateinit var selected: SnapshotStateMap<Card, Unit>

            override fun KeyScope.handleKey(): Boolean {
                return if (code == "KeyA") {
                    if (selected.count() < pile.cards.size) {
                        pile.cards.forEach { card -> selected[card] = Unit }
                    } else {
                        selected.clear()
                    }
                    true
                } else false
            }

            @Composable
            override fun renderContent(actions: MenuActions) {
                selected = remember { mutableStateMapOf() }

                with(params) {
                    pile.cards.forEach { card ->
                        Button(
                            onClick = {
                                if (selected.remove(card) == null) {
                                    selected[card] = Unit
                                }
                            },
                            Modifier.thenIf(selected.contains(card)) {
                                Modifier.outline(1.px, LineStyle.Solid, Colors.Black)
                            }
                        ) { Text(card.template.name) }
                        installPopup(ctx, card)
                    }
                }
            }

            @Composable
            override fun RowScope.renderExtraButtons(actions: MenuActions) {
                Button(
                    onClick = {
                        actions.visit(ToPile(params, cards = selected.keys.toList(), excludedPile = pile))
                    },
                    enabled = selected.isNotEmpty(),
                ) { Text("Continue") }
            }
        }

        class ToPile(private val params: GameMenuParams, private val cards: List<Card>, private val excludedPile: Pile) : Menu {
            override val title = "To Pile"

            @Composable
            override fun renderContent(actions: MenuActions) {
                with(params) {
                    ctx.state.allPiles.forEach { pile ->
                        Button(
                            onClick = {
                                updater.runStateChangingAction {
                                    ctx.state.addChange(
                                        GameStateChange.MoveCards(
                                            ctx.state,
                                            cards,
                                            pile,
                                            ListStrategy.FRONT
                                        )
                                    )
                                }
                                actions.close()
                            },
                            enabled = pile !== excludedPile,
                        ) { Text(ctx.describer.describePileTitle(ctx.state, pile, withSize = true)) }
                    }
                }
            }
        }
    }

    class Snapshot(private val params: GameMenuParams) : Menu {
        override val title = "Snapshot"

        @Composable
        override fun renderContent(actions: MenuActions) {
            Button(
                onClick = {
                    with(params) {
                        Data.save(Data.Keys.Quicksave, GameSnapshot.from(ctx.describer, ctx.state))
                        ctx.logger.debug("Game saved.")
                        actions.close()
                    }
                },
            ) { Text("Save Now") }

            Button(
                onClick = {
                    params.ctx.downloadSnapshotToDisk()
                    actions.close()
                },
            ) { Text("Download Snapshot") }

            Button(
                onClick = {
                    with(params) {
                        ctx.loadSnapshotFromDisk(scope) {
                            actions.close()
                        }
                    }
                },
            ) { Text("Load Snapshot") }
        }
    }
}
