package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.CardTemplate

fun CardTemplate.shouldMask(ctx: GameContext) = ctx.settings.maskCards && !ctx.cardStats.contains(this.name)

/**
 * A no-op command used when read-only viewing a card template
 */
class ViewCardTemplateCommand(
    ctx: GameContext, card: CardTemplate, extra: String? = null
) : Command(ctx) {
    private val shouldMaskCard = card.shouldMask(ctx)

    override val type = if (shouldMaskCard) Type.Disabled else Type.Normal
    override val title = if (shouldMaskCard) "?".repeat(card.name.length) else ctx.describer.describeCard(card, concise = true)
    override val extra = if (shouldMaskCard) null else extra
    override val description = if (shouldMaskCard) "You must own this card at least once to see its details." else ctx.describer.describeCard(card)
}
