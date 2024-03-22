package net.buj.chadusted

private val splashes = arrayOf(
    "<3",
    "Shrek is love",
    "Things happen",
    "Help me I ran out of ideas",
    "cleidianrahui",
)

fun getSplash(): String {
    return splashes.random()
}
