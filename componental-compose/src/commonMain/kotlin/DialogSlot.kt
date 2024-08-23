package de.halfbit.componental.compose

import androidx.compose.runtime.Composable
import de.halfbit.componental.router.slot.Slot

@Composable
public fun <I : Any, C : Any> DialogSlot(
    slot: Slot<I, C>,
    content: @Composable (route: I, child: C) -> Unit,
) {
    val active = slot.active
    if (active != null) {
        content(active.route, active.child)
    }
}
