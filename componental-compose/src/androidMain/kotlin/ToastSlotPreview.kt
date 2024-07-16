package de.halfbit.componental.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview


@Preview
@Composable
internal fun ToastSlotPreview() {
    //GamineForAgentsTheme {
    Box(modifier = Modifier.fillMaxSize()) {
        ToastInfo(
            text = "Thanks for your orders!" +
                "\nWe will come back to you as soon as we can.",
            onSlotDismissed = {},
        )
    }
    //}
}
