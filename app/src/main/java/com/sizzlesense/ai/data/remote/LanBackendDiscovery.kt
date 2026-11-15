package com.sizzlesense.ai.data.remote

import android.os.Build
import com.sizzlesense.ai.BuildConfig
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/**
 * Discovers candidate SizzleSense FastAPI servers listening on port `8000` within the device's
 * active Wi-Fi LAN subnets when known/cached backend IPs change over DHCP.
 */
internal fun interface LanBackendScanner {
    suspend fun scanPort8000OnLocalSubnet(): List<String>
}

internal object AndroidDeviceProfile {
    private const val EMULATOR_HOST_URL = "http://10.0.2.2:8000"

    fun isPhysicalDevice(): Boolean {
        // Plain JVM unit tests leave Build.FINGERPRINT null; preserve emulator/default failover there.
        val fingerprint = Build.FINGERPRINT ?: return false
        val hardware = Build.HARDWARE.orEmpty()
        val product = Build.PRODUCT.orEmpty()
        val model = Build.MODEL.orEmpty()
        val isEmulator = fingerprint.startsWith("generic") ||
            fingerprint.startsWith("unknown") ||
            fingerprint.contains("emulator") ||
            hardware.contains("goldfish") ||
            hardware.contains("ranchu") ||
            product.contains("sdk_gphone") ||
            product.contains("emulator") ||
            model.contains("Emulator") ||
            model.contains("Android SDK built for")
        return !isEmulator
    }

    fun preferredDefaultBackendUrl(
        persistedUrl: String? = null,
        isPhysical: Boolean = isPhysicalDevice()
    ): String {
        val cleaned = persistedUrl?.trim()?.trimEnd('/').orEmpty()
        val lanUrl = BuildConfig.DEFAULT_LAN_BACKEND_URL.trim().trimEnd('/')
        if (isPhysical && lanUrl.isNotBlank() && (cleaned.isBlank() || cleaned == EMULATOR_HOST_URL)) {
            return lanUrl
        }
        return cleaned.ifBlank { BuildConfig.DEFAULT_BACKEND_URL }
    }
}

internal class SocketLanBackendScanner(
    private val ioDispatcher: CoroutineDispatcher
) : LanBackendScanner {

    override suspend fun scanPort8000OnLocalSubnet(): List<String> = withContext(ioDispatcher) {
        val localIps = findSiteLocalIpv4Addresses()
            .filterNot { it.startsWith(EMULATOR_NAT_PREFIX) }
        if (localIps.isEmpty()) return@withContext emptyList()

        val prefixes = localIps
            .map { ip -> ip.substringBeforeLast('.') }
            .distinct()
            .take(MAX_SUBNETS_TO_SCAN)
        val selfSet = localIps.toSet()

        val candidates = prefixes.flatMap { prefix ->
            (1..254)
                .map { host -> "$prefix.$host" }
                .filterNot { it in selfSet }
        }

        val semaphore = Semaphore(MAX_CONCURRENT_PROBES)
        coroutineScope {
            candidates.map { ip ->
                async {
                    semaphore.withPermit {
                        if (isTcpPortOpen(ip, BACKEND_PORT, PROBE_TIMEOUT_MS)) {
                            "http://$ip:$BACKEND_PORT"
                        } else {
                            null
                        }
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    private fun findSiteLocalIpv4Addresses(): List<String> = runCatching {
        NetworkInterface.getNetworkInterfaces()
            ?.toList()
            .orEmpty()
            .asSequence()
            .filter { iface -> iface.isUp && !iface.isLoopback && !iface.isVirtual }
            .flatMap { iface -> iface.inetAddresses.toList().asSequence() }
            .filterIsInstance<Inet4Address>()
            .filter { addr -> !addr.isLoopbackAddress && addr.isSiteLocalAddress }
            .mapNotNull { addr -> addr.hostAddress }
            .distinct()
            .toList()
    }.getOrDefault(emptyList())

    private fun isTcpPortOpen(host: String, port: Int, timeoutMs: Int): Boolean = runCatching {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(host, port), timeoutMs)
            true
        }
    }.getOrDefault(false)

    companion object {
        private const val BACKEND_PORT = 8000
        private const val PROBE_TIMEOUT_MS = 220
        private const val MAX_CONCURRENT_PROBES = 32
        private const val MAX_SUBNETS_TO_SCAN = 2
        private const val EMULATOR_NAT_PREFIX = "10.0.2."
    }
}
