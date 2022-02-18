package org.rsmod.game

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.rsmod.game.config.InternalConfig
import org.rsmod.game.coroutine.GameCoroutineScope
import org.rsmod.game.dispatch.GameJobDispatcher
import org.rsmod.game.event.EventBus
import org.rsmod.game.event.impl.NpcTimerTrigger
import org.rsmod.game.event.impl.PlayerTimerTrigger
import org.rsmod.game.model.client.Client
import org.rsmod.game.model.client.ClientList
import org.rsmod.game.model.mob.Mob
import org.rsmod.game.model.mob.Npc
import org.rsmod.game.model.mob.NpcList
import org.rsmod.game.model.mob.Player
import org.rsmod.game.model.mob.PlayerList
import org.rsmod.game.model.world.World
import org.rsmod.game.update.task.UpdateTaskList
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.system.measureNanoTime

private val logger = InlineLogger()

sealed class GameState {
    object Inactive : GameState()
    object Active : GameState()
    object ShutDown : GameState()
}

class Game @Inject private constructor(
    private val config: InternalConfig,
    private val coroutineScope: GameCoroutineScope,
    private val jobDispatcher: GameJobDispatcher,
    private val updateTaskList: UpdateTaskList,
    private val playerList: PlayerList,
    private val clientList: ClientList,
    private val npcList: NpcList,
    private val world: World,
    private val eventBus: EventBus
) {

    var state: GameState = GameState.Inactive

    private var excessCycleNanos = 0L

    fun start() {
        if (state != GameState.Inactive) {
            error("::start has already been called.")
        }
        val delay = config.gameTickDelay
        state = GameState.Active
        coroutineScope.start(delay.toLong())
    }

    private fun CoroutineScope.start(delay: Long) = launch {
        while (state != GameState.ShutDown && isActive) {
            val elapsedNanos = measureNanoTime { gameLogic() } + excessCycleNanos
            val elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos)
            val overdue = elapsedMillis > delay
            val sleepTime = if (overdue) {
                val elapsedCycleCount = elapsedMillis / delay
                val upcomingCycleDelay = (elapsedCycleCount + 1) * delay
                upcomingCycleDelay - elapsedMillis
            } else {
                delay - elapsedMillis
            }
            if (overdue) logger.error { "Cycle took too long (elapsed=${elapsedMillis}ms, sleep=${sleepTime}ms)" }
            excessCycleNanos = elapsedNanos - TimeUnit.MILLISECONDS.toNanos(elapsedMillis)
            delay(sleepTime)
        }
    }

    private suspend fun gameLogic() {
        clientList.forEach { it.pollActions(config.actionsPerCycle) }
        playerList.forEach { it?.cycle() }
        npcList.forEach { it?.cycle(eventBus) }
        world.queueList.cycle()
        jobDispatcher.executeAll()
        updateTaskList.forEach { it.execute() }
        playerList.forEach { it?.flush() }
    }
}

private fun Client.pollActions(iterations: Int) {
    for (i in 0 until iterations) {
        val message = pendingPackets.poll() ?: break
        val handler = message.handler
        val packet = message.packet
        try {
            handler.handle(this, player, packet)
        } catch (t: Throwable) {
            logger.error(t) {
                "Action handler process error (packet=${packet::class.simpleName}, " +
                    "handler=${handler::class.simpleName}, player=$player)"
            }
        }
    }
}

private suspend fun Player.cycle() {
    queueCycle()
    timerCycle()
}

private suspend fun Npc.cycle(eventBus: EventBus) {
    queueCycle()
    timerCycle(eventBus)
}

private suspend fun Mob.queueCycle() {
    /* flag whether a new queue should be polled this cycle */
    val pollQueue = queueStack.idle
    try {
        queueStack.processCurrent()
    } catch (t: Throwable) {
        queueStack.discardCurrent()
        logger.error(t) { "Queue process error ($this)" }
    }
    if (pollQueue) {
        try {
            queueStack.pollPending()
        } catch (t: Throwable) {
            logger.error(t) { "Queue poll error ($this)" }
        }
    }
}

private fun Player.timerCycle() {
    if (timers.isEmpty()) return
    val iterator = timers.iterator()
    while (iterator.hasNext()) {
        val entry = iterator.next()
        val key = entry.key
        val cycles = entry.value
        if (cycles > 0) {
            timers.decrement(key)
            continue
        }
        try {
            val event = PlayerTimerTrigger(this, key)
            eventBus.publish(event)
            /* if the timer was not re-set after event we remove it */
            if (timers.isNotActive(key)) {
                iterator.remove()
            }
        } catch (t: Throwable) {
            iterator.remove()
            logger.error(t) { "Timer event error ($this)" }
        }
    }
}

private fun Npc.timerCycle(eventBus: EventBus) {
    if (timers.isEmpty()) return
    val iterator = timers.iterator()
    while (iterator.hasNext()) {
        val entry = iterator.next()
        val key = entry.key
        val cycles = entry.value
        if (cycles > 0) {
            timers.decrement(key)
            continue
        }
        try {
            val event = NpcTimerTrigger(this, key)
            eventBus.publish(event)
            /* if the timer was not re-set after event we remove it */
            if (timers.isNotActive(key)) {
                iterator.remove()
            }
        } catch (t: Throwable) {
            iterator.remove()
            logger.error(t) { "Timer event error ($this)" }
        }
    }
}
