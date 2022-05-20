package dev.bitspittle.racketeer.scripting.methods.card

import dev.bitspittle.racketeer.model.card.UpgradeType
import dev.bitspittle.racketeer.model.card.isInternal

fun UpgradeType.assertNotInternal() {
    if (this.isInternal()) { error("Script should not reference internal upgrade type $this") }
}