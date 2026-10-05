package com.majidbahmani.calypso.nfc

/**
 * A scripted card: answers each command (hex) with the given response (hex, incl. status word).
 * Unknown commands get [defaultResponse]. Records every command sent, in order.
 */
class FakeCardTransport(
    private val responses: Map<String, String>,
    private val defaultResponse: String = "6A82",
) : CardTransport {

    val sentCommands = mutableListOf<String>()

    override suspend fun transceive(command: ByteArray): ByteArray {
        val hex = command.toHex()
        sentCommands += hex
        return (responses[hex] ?: defaultResponse).hexToBytes()
    }
}
