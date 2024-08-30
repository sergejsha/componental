package de.halfbit.componental.restorator

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.protobuf.ProtoBuf

public fun Restorator(bytes: ByteArray?): Restorator =
    DefaultRestorator(bytes)

public interface Restorator {
    public fun restoreRoute(): ByteArray?
    public fun storeRoute(block: () -> ByteArray?)
    public fun storeAll(): ByteArray
}

@OptIn(ExperimentalSerializationApi::class)
private class DefaultRestorator(
    bytes: ByteArray?,
) : Restorator {
    private val storableRouters = mutableListOf<() -> ByteArray?>()
    private val consumable: ConsumableList = ConsumableList(
        bytes?.let {
            ProtoBuf
                .decodeFromByteArray(routersSerializer, bytes)
                .map { if (it.isEmpty()) null else it }
                .toMutableList()
        }
    )

    override fun restoreRoute(): ByteArray? =
        consumable.consume()

    override fun storeRoute(block: () -> ByteArray?) {
        storableRouters.add(block)
    }

    override fun storeAll(): ByteArray {
        val routers: List<ByteArray?> = storableRouters.map { it() }
        return ProtoBuf.encodeToByteArray(
            routersSerializer,
            routers.map { it ?: emptyByteArray }
        )
    }
}

private val emptyByteArray = byteArrayOf()
private val routersSerializer = ListSerializer(ByteArraySerializer())

private class ConsumableList(
    private val bytes: MutableList<ByteArray?>? = null
) {
    private var position: Int = 0
    fun consume(): ByteArray? {
        val bytes = bytes ?: return null
        if (position >= bytes.size) {
            throw IllegalStateException(
                "Restore for a not stored child requested." +
                        " Stored children count: ${bytes.size}," +
                        " requested position: $position"
            )
        }
        return bytes[position].also {
            bytes[position] = null
            position++
        }
    }
}
