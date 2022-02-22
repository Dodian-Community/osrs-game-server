package net.dodian.central.modules

import com.google.inject.Provider
import com.google.inject.Scope
import dev.misfitlabs.kotlinguice4.KotlinModule
import io.guthix.js5.Js5Cache
import io.guthix.js5.container.disk.Js5DiskStore
import net.dodian.central.config.ServerConfig
import org.rsmod.game.cache.GameCache
import org.rsmod.game.config.GameConfig
import java.nio.file.Files
import javax.inject.Inject

class CentralCacheModule(private val scope: Scope) : KotlinModule() {

    override fun configure() {
        bind<GameCache>()
            .toProvider<GameCacheProvider>()
            .`in`(scope)
    }
}

private class GameCacheProvider @Inject constructor(
    private val serverConfig: ServerConfig
) : Provider<GameCache> {

    override fun get(): GameCache {
        val path = serverConfig.cachePath.resolve(PACKED_FOLDER)
        if (!Files.isDirectory(path)) {
            error("Cache directory does not exist: ${path.toAbsolutePath()}")
        }
        val diskStore = Js5DiskStore.open(path)
        val cache = Js5Cache(diskStore)
        return GameCache(path, diskStore, cache)
    }

    companion object {
        private const val PACKED_FOLDER = "packed"
    }
}
