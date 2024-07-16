package de.halfbit.componental.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import de.halfbit.componental.router.stack.Stack

@Composable
public fun <K : Any, C : Any> StackChildren(
    stack: Stack<K, C>,
    content: @Composable (key: K, child: C) -> Unit,
) {
    AnimatedContent(
        targetState = stack.active,
        label = "StackChildren",
        contentKey = { child -> child.id },
        transitionSpec = {
            fadeIn(
                animationSpec = tween(220, delayMillis = 50),
                initialAlpha = 0f,
            ) togetherWith fadeOut(
                animationSpec = tween(220),
                targetAlpha = 1f,
            )
        },
    ) { child ->
        content(child.id, child.child)
    }
}
