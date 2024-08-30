package de.halfbit.componental.compose

import androidx.compose.runtime.Composable
import de.halfbit.componental.router.slot.Slot

@Composable
public inline fun <I : Any, C : Any> DialogSlot(
    slot: Slot<I, C>,
    content: @Composable (child: C) -> Unit,
) {
    val active = slot.active
    if (active != null) {
        content(active.child)
    }
}
