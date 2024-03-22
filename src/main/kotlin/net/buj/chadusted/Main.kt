package net.buj.chadusted

import arc.Events
import arc.util.CommandHandler
import arc.util.Log
import mindustry.Vars
import mindustry.core.*
import mindustry.game.EventType.*
import mindustry.gen.*
import mindustry.mod.*
import java.util.HashMap

val colors = arrayOf("clear", "black", "white", "lightgray", "gray", "darkgray", "blue",
    "navy", "royal", "slate", "sky", "cyan", "teal", "green", "acid", "lime", "forest",
    "olive", "yellow", "gold", "goldenrod", "orange", "brown", "tan", "brick", "red",
    "scarlet", "coral", "salmon", "pink", "magenta", "purple", "violet", "maroon")
val colorRegex = Regex("(\\[(#[0-9a-f]{1,8}|${colors.joinToString("|")})]).*", RegexOption.IGNORE_CASE)

class Main: Plugin() {
    private val playerMetrics = HashMap<Int, Interface>()

    class ChatFormatterImpl(private val self: Main) : NetServer.ChatFormatter {
        override fun format(player: Player?, text: String): String {
            return if (player == null) text else self.applyFormat(player, text)
        }
    }

    init {
        Events.on(ServerLoadEvent::class.java) {
            Vars.netServer.chatFormatter = ChatFormatterImpl(this)

            Events.on(PlayerConnect::class.java) {
                playerMetrics[it.player.id] = interfaceFor(it.player)
            }

            Events.on(PlayerLeave::class.java) {
                playerMetrics.remove(it.player.id)
            }
        }
    }

    override fun registerServerCommands(handler: CommandHandler) {
        handler.register("messageformatof", "<player> <format...>", "Set message format") { args ->
            val target = args[0].toIntOrNull().run {
                if (this == null) {
                    Log.err("Target is an invalid ID")
                    return@register
                }
                val value = playerMetrics.values.find { it.id() == this }
                if (value != null) return@run value
                interfaceForId(this)
            }
            if (target == null) {
                Log.err("Player could not be found")
                return@register
            }

            val format = args[1]

            if (!format.contains("{name}")) {
                Log.warn("Message format must include player name ({name})")
            }
            if (!format.contains("{message}")) {
                Log.warn("Message format must include player message ({message})")
            }
            if (!format.contains("{rank}")) {
                Log.warn("Warning! Message format does not include player rank ({rank})")
            }

            target.setFormatString(format)
        }
    }

    override fun registerClientCommands(handler: CommandHandler) {
        handler.register<Player>("messageformat", "<format...>", "Set message format") { args, player ->
            val format = args[0]

            if (!format.contains("{name}")) {
                player.sendMessage("[red]Message format must include player name ({name})")
                return@register
            }
            if (!format.contains("{message}")) {
                player.sendMessage("[red]Message format must include player message ({message})")
                return@register
            }
            if (!format.contains("{rank}")) {
                player.sendMessage("[yellow]Warning! Message format does not include player rank ({rank})")
            }

            val metrics = if (playerMetrics[player.id] != null) playerMetrics[player.id] as Interface else {
                val inter = interfaceFor(player)
                playerMetrics[player.id] = inter
                inter
            }
            metrics.setFormatString(format)
        }

        handler.register<Player>("messageformatof", "<player> <format...>", "Set message format") { args, player ->
            if (!player.admin()) {
                player.sendMessage("[red]This command is only accessible to administrators")
                return@register
            }

            val target = args[0].toIntOrNull().run {
                if (this == null) {
                    player.sendMessage("[red]Target is an invalid ID")
                    return@register
                }
                val value = playerMetrics.values.find { it.id() == this }
                if (value != null) return@run value
                interfaceForId(this)
            }
            if (target == null) {
                player.sendMessage("[red]Player could not be found")
                return@register
            }

            val format = args[1]

            if (!format.contains("{name}")) {
                player.sendMessage("[red]Message format must include player name ({name})")
                return@register
            }
            if (!format.contains("{message}")) {
                player.sendMessage("[red]Message format must include player message ({message})")
                return@register
            }
            if (!format.contains("{rank}")) {
                player.sendMessage("[yellow]Warning! Message format does not include player rank ({rank})")
            }

            target.setFormatString(format)
        }
    }

    private fun replaceRevertColor(text: String, from: String, to: String): String {
        var replace = to

        var count = 0 // just undo colors N amount of times amr?
        var i = 0
        while (true) {
            i = replace.indexOf('[', i)
            if (i == -1) break

            if (replace.slice(i..<replace.length).matches(colorRegex)) {
                count++
            }
            else if (replace.slice(i..<replace.length).startsWith("[]")) {
                if (count > 0) count--
                else {
                    replace = replace.removeRange(i.rangeTo(i + 1))
                    i--
                }
            }
            i++
        }

        if (replace.endsWith("[")) replace += "["
        replace += "[]".repeat(count)

        var replaced = text
        while (true) {
            i = replaced.indexOf(from)
            if (i == -1) break

            if (i > 0) {
                var index = replaced.lastIndexOf('[', i - 1);
                if (index != -1) {
                    val match = colorRegex.matchAt(replaced, index)
                    if (index == i - 1 || (match != null && index + match.groups[1]!!.value.length >= i)) {
                        var count = 0
                        while (index != -1 && replaced[index] == '[') {
                            index--
                            count++
                        }
                        if (count % 2 == 1) {
                            index++
                            i++
                            replaced = replaced.replaceRange((index - 1)..<index, "[[")
                        }
                    }
                }
            }
            replaced = replaced.replaceRange(i..<(from.length + i), replace)
            i += replace.length
        }

        return replaced
    }

    private fun escapeString(text: String): String {
        return text.replace("[", "[[") // totally won't break
    }

    private fun formatPlaytime(time: Int): String {
        var time = time
        var res = ""
        if (time > 60 * 60 * 24) {
            res += "${time / 60 * 60 * 24}d "
            time %= 60 * 60 * 24
        }
        if (time > 60 * 60) {
            res += "${time / 60 * 60}h "
            time %= 60 * 60
        }
        if (time > 60) {
            res += "${time / 60}m "
            time %= 60
        }
        if (time > 0 || res.isEmpty()) res += "${time}s"
        return res.trim()
    }

    private fun applyFormat(player: Player, text: String): String {
        val metrics = if (playerMetrics[player.id] != null) playerMetrics[player.id] as Interface else {
            val inter = interfaceFor(player)
            playerMetrics[player.id] = inter
            inter
        }

        return metrics.formatString()
            .run { replaceRevertColor(this, "{name}", player.coloredName()) }
            .run { replaceRevertColor(this, "{message}", "[white]$text") }
            .run { replaceRevertColor(this, "{rmessage}", "[white]" + escapeString(text)) }
            .run { replaceRevertColor(this, "{rank}", if (metrics.rank() != null) "<${metrics.rank()}>" else "") }
            .run { replaceRevertColor(this, "{wins}", metrics.wins().toString()) }
            .run { replaceRevertColor(this, "{plays}", metrics.plays().toString()) }
            .run { replaceRevertColor(this, "{playtime}", formatPlaytime(metrics.playtimeSeconds())) }
            .run { replaceRevertColor(this, "{id}", metrics.id().toString()) }
            .run { replaceRevertColor(this, "{e}", getSplash()) }
            .run { this.trim() }
    }
}
