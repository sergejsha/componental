@file:OptIn(ExperimentalMaterial3Api::class)

package de.halfbit.componental.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
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
public fun <C : Any> ModalBottomSheetSlot(
    slot: Slot<C>,
    onSlotDismissed: () -> Unit,
    content: @Composable (child: C) -> Unit,
) {
    var current by remember { mutableStateOf<BottomSheetSlotState<C>?>(null) }
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

private fun <C : Any> createBottomSheetSlotState(
    active: RouteNode<C>,
    density: Density,
    onSlotDismissed: () -> Unit,
    content: @Composable (child: C) -> Unit,
): BottomSheetSlotState<C> {
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
                content = { content(active.child) }
            )
        }
    )
}

private class BottomSheetSlotState<out C : Any>(
    val component: RouteNode<C>,
    val sheetState: SheetState,
    val content: @Composable () -> Unit,
)
