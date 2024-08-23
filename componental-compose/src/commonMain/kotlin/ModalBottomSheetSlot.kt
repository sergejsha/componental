@file:OptIn(ExperimentalMaterial3Api::class)

package de.halfbit.componental.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import de.halfbit.componental.router.RouteNode
import de.halfbit.componental.router.slot.Slot

@Composable
public fun <I : Any, C : Any> ModalBottomSheetSlot(
    slot: Slot<I, C>,
    onSlotDismissed: () -> Unit,
    content: @Composable (id: I, child: C) -> Unit,
) {
    var current by remember { mutableStateOf<BottomSheetSlotState<I, C>?>(null) }
    val density = LocalDensity.current
    val active = slot.active

    LaunchedEffect(active) {
        if (active == null) {
            current?.let {
                it.sheetState.hide()
                current = null
            }
        } else {
            if (current?.component?.route != active.route) {
                current?.sheetState?.hide()
                current = null
            }

            val currentSlot = current
            if (currentSlot == null) {
                current = createBottomSheetSlotState(
                    active = active,
                    density = density,
                    onSlotDismissed = onSlotDismissed,
                    content = content,
                )
                // intentionally not calling .show() in initial, seems like a but in material3
            } else {
                currentSlot.sheetState.show()
            }
        }
    }

    current?.content?.invoke()
}

private fun <I : Any, C : Any> createBottomSheetSlotState(
    active: RouteNode<I, C>,
    density: Density,
    onSlotDismissed: () -> Unit,
    content: @Composable (id: I, child: C) -> Unit,
): BottomSheetSlotState<I, C> {
    val sheetState = SheetState(
        skipPartiallyExpanded = true,
        density = density,
        initialValue = SheetValue.Hidden,
    )

    return BottomSheetSlotState(
        component = active,
        sheetState = sheetState,
        content = {
            val isDarkTheme = isSystemInDarkTheme()
            val scrimAlpha = if (isDarkTheme) .62f else .86f

            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = onSlotDismissed,
                dragHandle = {}, // disable drag handle
                scrimColor = MaterialTheme.colorScheme.scrim.copy(scrimAlpha),
                content = { content(active.route, active.child) }
            )
        }
    )
}

private class BottomSheetSlotState<out I : Any, out C : Any>(
    val component: RouteNode<I, C>,
    val sheetState: SheetState,
    val content: @Composable () -> Unit,
)
