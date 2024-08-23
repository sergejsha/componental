package de.halfbit.componental.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.halfbit.componental.router.slot.Slot

@Composable
public fun <I : Any, C : Any> BoxScope.ToastSlot(
    slot: Slot<I, C>,
    content: @Composable (id: I, child: C) -> Unit,
) {
    val active = slot.active
    if (active != null) {
        ToastSurface {
            content(active.route, active.child)
        }
    }
}

@Composable
public fun BoxScope.ToastSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier
            .padding(8.dp)
            .align(Alignment.BottomCenter),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp,
        content = content,
    )
}

@Composable
public fun ToastError(
    text: String,
    actionText: String,
    onSlotDismissed: () -> Unit,
) {
    Toast(
        text = text,
        buttonText = actionText,
        color = MaterialTheme.colorScheme.onErrorContainer,
        backgroundColor = MaterialTheme.colorScheme.errorContainer,
        buttonColor = MaterialTheme.colorScheme.onErrorContainer,
        onSlotDismissed = onSlotDismissed,
    )
}

@Composable
public fun ToastInfo(
    text: String,
    actionText: String,
    onSlotDismissed: () -> Unit,
) {
    Toast(
        text = text,
        buttonText = actionText,
        color = MaterialTheme.colorScheme.inverseOnSurface,
        backgroundColor = MaterialTheme.colorScheme.inverseSurface,
        buttonColor = MaterialTheme.colorScheme.inversePrimary,
        onSlotDismissed = onSlotDismissed,
    )
}

@Composable
private fun Toast(
    text: String,
    buttonText: String,
    color: Color,
    backgroundColor: Color,
    buttonColor: Color,
    onSlotDismissed: () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(backgroundColor)
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
        )
        Spacer(modifier = Modifier.width(4.dp))
        TextButton(
            onClick = onSlotDismissed,
            colors = ButtonDefaults.textButtonColors(
                contentColor = buttonColor,
            ),
        ) {
            Text(
                text = buttonText,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
