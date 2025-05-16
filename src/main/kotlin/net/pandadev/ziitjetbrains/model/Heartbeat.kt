package net.pandadev.ziitjetbrains.model

data class Heartbeat(
    val timestamp: String,
    val project: String? = null,
    val language: String? = null,
    val file: String? = null,
    val branch: String? = null,
    val editor: String,
    val os: String
) 