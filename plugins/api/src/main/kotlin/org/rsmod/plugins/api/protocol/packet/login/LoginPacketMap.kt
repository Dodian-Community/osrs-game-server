package org.rsmod.plugins.api.protocol.packet.login

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import kotlin.reflect.KClass

class LoginPacketHandler<T : LoginPacket>(
    val read: ByteBuf.() -> T
)

class LoginPacketMap(
    val packets: MutableMap<KClass<out LoginPacket>, LoginPacketHandler<*>> = mutableMapOf()
) {

    inline fun <reified T : LoginPacket> register(noinline read: ByteBuf.() -> T) {
        if (packets.containsKey(T::class)) {
            error("Login packet type already has a handler (packet=${T::class.simpleName}).")
        }
        val handler = LoginPacketHandler(read)
        logger.trace { "Register login packet handler (type=${T::class.simpleName})" }
        packets[T::class] = handler
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : LoginPacket> get(): LoginPacketHandler<T>? {
        return packets[T::class] as? LoginPacketHandler<T>
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : LoginPacket> getValue(): LoginPacketHandler<T> {
        return packets[T::class] as LoginPacketHandler<T>
    }

    companion object {

        val logger = InlineLogger()
    }
}
