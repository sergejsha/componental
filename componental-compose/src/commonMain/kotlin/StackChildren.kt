package de.halfbit.componental.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import de.halfbit.componental.router.stack.Stack

@Composable
public inline fun <R : Any, C : Any> StackChildren(
    stack: Stack<R, C>,
    crossinline content: @Composable (active: C) -> Unit,
) {
    AnimatedContent(
        targetState = stack.active,
        label = "StackChildren",
        contentKey = { it.route },
        transitionSpec = {
            fadeIn(
                animationSpec = tween(220, delayMillis = 70),
                initialAlpha = 0f,
            ) togetherWith fadeOut(
                animationSpec = tween(220),
                targetAlpha = 1f,
            )
        },
    ) { active ->
        content(active.child)
    }
}
