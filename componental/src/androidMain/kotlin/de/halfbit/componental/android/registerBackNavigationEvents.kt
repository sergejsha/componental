package de.halfbit.componental.android

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import de.halfbit.componental.back.BackNavigation

public fun OnBackPressedDispatcher.registerBackNavigationEvents() {
    addCallback(
        object : OnBackPressedCallback(enabled = true) {
            override fun handleOnBackPressed() {
                BackNavigation.dispatchOnNavigateBack()
            }
        }
    )
}
