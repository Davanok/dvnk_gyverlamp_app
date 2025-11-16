package com.davanok.firelamp.data.implementations

import android.util.Log
import com.davanok.firelamp.data.model.LampAddress
import com.davanok.firelamp.data.model.adapters.parseDiscoverResponse
import com.davanok.firelamp.data.repositories.FireLampRepository
import com.davanok.firelamp.data.repositories.LampsFinderRepository
import com.davanok.firelamp.data.utils.runLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration

class LampsFinderRepositoryImpl(
    private val repository: FireLampRepository
) : LampsFinderRepository {
    override fun findLamps(port: Int, timeout: Duration): Flow<Pair<Float, List<LampAddress>>> = channelFlow {
        val localInet = NetworkInterface.getNetworkInterfaces()
            .toList()
            .flatMap { ni -> ni.inetAddresses.toList() }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { !it.isLoopbackAddress }
            ?: InetAddress.getByName("127.0.0.1") as? Inet4Address


        Log.d("findLamps", "localhost: ${localInet?.hostAddress}")
        if (localInet?.hostAddress == null) {
            close()
            return@channelFlow
        }

        val prefix = localInet.hostAddress!!.substringBeforeLast('.')
        val found = CopyOnWriteArrayList<LampAddress>()

        val total = 255
        val completed = java.util.concurrent.atomic.AtomicInteger(0)

        val concurrency = 50
        val sem = Semaphore(concurrency)
        val jobs = mutableListOf<Job>()

        for (i in 0..total) {
            val ip = "$prefix.$i"

            val job = launch(Dispatchers.IO) {
                sem.withPermit {
                    Log.d("findLamps", "check $ip")
                    runCatching {
                        val probeAddress = LampAddress(hostname = ip, port = port)

                        val raw = repository
                            .sendCommand(probeAddress, "DISCOVER", timeout)
                            .getOrNull()

                        if (raw == null) Log.d("findLamps", "not found lamp on $ip")
                        else {
                            val response = parseDiscoverResponse(raw)
                            found.add(
                                LampAddress(
                                    hostname = response.ipAddress,
                                    port = response.port,
                                    name = response.apName ?: ""
                                )
                            )
                            Log.d("findLamps", "found lamp on $ip")
                        }
                    }
                    val done = completed.incrementAndGet()
                    val progress = done.toFloat() / total.toFloat()

                    send(progress to found)
                }
            }
            jobs += job
        }

        jobs.joinAll()
        send(1.0f to found)
        close()
    }

    override suspend fun checkConnection(
        address: LampAddress,
        timeout: Duration
    ): Result<LampAddress?> = runLogging("checkConnection") {
        repository
            .sendCommand(address, "DISCOVER", timeout)
            .getOrNull()
            ?.let { parseDiscoverResponse(it).toLampAddress() }
    }
}