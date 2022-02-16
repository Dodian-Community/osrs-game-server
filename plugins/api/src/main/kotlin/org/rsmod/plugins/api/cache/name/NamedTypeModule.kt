package org.rsmod.plugins.api.cache.name

import com.google.inject.Provider
import com.google.inject.Scope
import dev.misfitlabs.kotlinguice4.KotlinModule
import org.rsmod.game.name.NamedTypeLoaderList
import org.rsmod.plugins.api.cache.name.cs2.Cs2NameLoader
import org.rsmod.plugins.api.cache.name.cs2.Cs2NameMap
import org.rsmod.plugins.api.cache.name.enum.EnumNameLoader
import org.rsmod.plugins.api.cache.name.enum.EnumNameMap
import org.rsmod.plugins.api.cache.name.item.ItemNameLoader
import org.rsmod.plugins.api.cache.name.item.ItemNameMap
import org.rsmod.plugins.api.cache.name.npc.NpcNameLoader
import org.rsmod.plugins.api.cache.name.npc.NpcNameMap
import org.rsmod.plugins.api.cache.name.obj.ObjectNameLoader
import org.rsmod.plugins.api.cache.name.obj.ObjectNameMap
import org.rsmod.plugins.api.cache.name.ui.ComponentNameMap
import org.rsmod.plugins.api.cache.name.ui.UserInterfaceNameLoader
import org.rsmod.plugins.api.cache.name.ui.UserInterfaceNameMap
import org.rsmod.plugins.api.cache.name.vars.VarbitNameLoader
import org.rsmod.plugins.api.cache.name.vars.VarbitNamedMap
import org.rsmod.plugins.api.cache.name.vars.VarpNameLoader
import org.rsmod.plugins.api.cache.name.vars.VarpNameMap
import javax.inject.Inject

class NamedTypeModule(private val scope: Scope) : KotlinModule() {

    override fun configure() {
        bind<NamedTypeLoaderList>()
            .toProvider<NamedTypeLoaderListProvider>()
            .`in`(scope)
        bind<NpcNameMap>().`in`(scope)
        bind<ItemNameMap>().`in`(scope)
        bind<ObjectNameMap>().`in`(scope)
        bind<UserInterfaceNameMap>().`in`(scope)
        bind<ComponentNameMap>().`in`(scope)
        bind<VarpNameMap>().`in`(scope)
        bind<VarbitNamedMap>().`in`(scope)
        bind<Cs2NameMap>().`in`(scope)
        bind<EnumNameMap>().`in`(scope)
    }
}

private class NamedTypeLoaderListProvider @Inject constructor(
    private val itemList: ItemNameLoader,
    private val npcList: NpcNameLoader,
    private val objList: ObjectNameLoader,
    private val interfaceList: UserInterfaceNameLoader,
    private val varpList: VarpNameLoader,
    private val varbitList: VarbitNameLoader,
    private val scriptList: Cs2NameLoader,
    private val enumList: EnumNameLoader,
) : Provider<NamedTypeLoaderList> {

    override fun get() = NamedTypeLoaderList().apply {
        register(itemList)
        register(npcList)
        register(objList)
        register(interfaceList)
        register(varpList)
        register(varbitList)
        register(scriptList)
        register(enumList)
    }
}
