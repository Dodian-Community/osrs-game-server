package org.rsmod.plugins.api.protocol.packet.server

import org.rsmod.game.message.ServerPacket

data class IfOpenTop(val interfaceId: Int) : ServerPacket
data class IfOpenSub(val interfaceId: Int, val targetComponent: Int, val clickMode: Int) : ServerPacket
data class IfMoveSub(val from: Int, val to: Int) : ServerPacket {
    override fun toString(): String {
        return "IfMoveSub(from=$from (parentId=${from shr 16}, child=${from and 0xffff}), to=$to (parentId=${to shr 16}, child=${to and 0xffff}))"
    }
}
data class IfCloseSub(val component: Int) : ServerPacket
data class IfSetText(val component: Int, val text: String) : ServerPacket
data class IfSetHide(val component: Int, val hidden: Boolean) : ServerPacket
data class IfSetEvents(val component: Int, val dynamic: IntRange, val events: Int) : ServerPacket
data class IfSetNpcHead(val component: Int, val npc: Int) : ServerPacket
data class IfSetPlayerHead(val component: Int) : ServerPacket
data class IfSetObject(val component: Int, val item: Int, val amountOrZoom: Int) : ServerPacket
data class IfSetAnim(val component: Int, val anim: Int) : ServerPacket

class RunClientScript(val id: Int, vararg val args: Any) : ServerPacket
