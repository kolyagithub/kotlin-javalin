package blog

import java.util.*
import java.util.concurrent.atomic.AtomicLong


class Repository<T> {


    val dataMap = HashMap<Long, T>()
    val atomicCounter = AtomicLong()

    fun create(t: T): Long {
        val id = atomicCounter.incrementAndGet()
        dataMap[id] = t
        return id
    }

    fun update(t: T, id: Long) {
        dataMap[id] = t
    }

    fun delete(id: Long) {
        dataMap.remove(id)
    }

    fun list(): MutableCollection<T> {
        return dataMap.values
    }

    fun get(id: Long): T? {
        return dataMap[id]
    }

}