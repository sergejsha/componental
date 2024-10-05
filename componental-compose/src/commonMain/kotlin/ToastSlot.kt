package de.halfbit.componental.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.halfbit.componental.router.slot.Slot

@Composable
public inline fun <C : Any> BoxScope.ToastSlot(
    slot: Slot<C>,
    modifier: Modifier = Modifier,
    crossinline content: @Composable (child: C) -> Unit,
) {
    val active = slot.active
    if (active != null) {
        ToastSurface(modifier = modifier) {
            content(active.child)
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
            .padding(horizontal = 32.dp)
            .padding(bottom = 24.dp)
            .align(Alignment.BottomCenter),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 6.dp,
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
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp),
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
