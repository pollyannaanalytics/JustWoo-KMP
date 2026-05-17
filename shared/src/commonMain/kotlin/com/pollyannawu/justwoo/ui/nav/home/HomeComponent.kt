package com.pollyannawu.justwoo.ui.nav.home

import com.arkivanov.decompose.ComponentContext

/**
 * Home 目前所有的 click action（"+", Task Space, Profile, Calendar, Menu）
 * 都是 chrome 等級的全域動作，由 RootComponent 處理、Screen 透過 LocalAppActions 拿。
 * 所以 HomeComponent 目前是 marker — 之後若 Home 自己要持有 state（例如 unread badge）再擴。
 */
interface HomeComponent

class DefaultHomeComponent(
    componentContext: ComponentContext,
) : HomeComponent, ComponentContext by componentContext
