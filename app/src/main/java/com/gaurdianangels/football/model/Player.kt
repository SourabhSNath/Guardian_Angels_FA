package com.gaurdianangels.football.model


data class Player(
    val playerName: String,
    val playerType: String,
    var remoteUri: String? = null // Points to the image in firebase storage
)