package net.buj.chadusted

import mindustry.gen.Player

class Interface { // Custom implementations are to be provided
    private var fmtStr = "{rank} [coral][[{name}]:[white] {message}"
    private val began = System.currentTimeMillis() / 1000;

    fun id(): Int {
        return 0
    }

    fun rank(): String? {
        return null
    }

    fun formatString(): String {
        return fmtStr
    }

    fun setFormatString(newString: String) {
        fmtStr = newString
    }

    fun wins(): Int {
        return 0;
    }

    fun plays(): Int {
        return 0;
    }

    fun playtimeSeconds(): Int {
        return (System.currentTimeMillis() / 1000 - began).toInt()
    }
}

fun interfaceFor(player: Player): Interface {
    player.sendMessage("[yellow]Chadasted has not yet been configured on this server. Your settings will not be saved.")
    return Interface()
}

fun interfaceForId(id: Int): Interface? {
    return null
}
