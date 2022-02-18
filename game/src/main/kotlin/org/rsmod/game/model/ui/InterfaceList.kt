package org.rsmod.game.model.ui

data class InterfaceList(
    val topLevel: MutableSet<UserInterface> = mutableSetOf(),
    var overlays: MutableMap<Component, UserInterface> = mutableMapOf(),
    val modals: MutableMap<Component, UserInterface> = mutableMapOf(),
    val properties: MutableMap<Component, ComponentProperty> = mutableMapOf()
)
