package com.hiusers.questengine.test

object Test {

    /*@Awake(LifeCycle.ENABLE)
    fun cmd() {
        simpleCommand("ababab") { sender, args ->
            sender.castSafely<Player>()?.let { p ->
                p.spigot().sendMessage(ChatMessageType., *args.map { it.toComponent() }.toTypedArray())
            }
        }
    }*/

    /*@SubscribeEvent
    fun actionbar(ev: PacketSendEvent) {
        if (ev.packet.nameInSpigot == "ClientboundSystemChatPacket") {
            info("overlay " + ev.packet.read("overlay"))
        }
    }*/

    /*    @SubscribeEvent
        fun onPacket(e: PacketSendEvent) {
            if (e.packet.nameInSpigot == "ClientboundSetActionBarTextPacket") {
    //            info("e.packet.source ${e.packet.source}")
    //            info("text " + e.packet.read("text"))
                e.isCancelled = true
            }
        }*/

}