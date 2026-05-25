package com.pollyannawu.justwoo.android.ui.nav

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.pollyannawu.justwoo.android.ui.auth.RegisterScreen
import com.pollyannawu.justwoo.android.ui.auth.SignInScreen
import com.pollyannawu.justwoo.ui.nav.auth.AuthComponent

@Composable
fun AuthContent(component: AuthComponent) {
    Children(
        stack = component.stack,
        animation = stackAnimation(fade() + slide()),
    ) { created ->
        when (val child = created.instance) {
            is AuthComponent.Child.SignIn -> SignInScreen(
                onSignInSuccess = {},
                onNavigateToRegister = child.component::onSwitchToRegister,
            )
            is AuthComponent.Child.Register -> RegisterScreen(
                onRegisterSuccess = {},
                onNavigateToSignIn = child.component::onSwitchToSignIn,
            )
        }
    }
}
