package com.pollyannawu.justwoo.data.network

import com.pollyannawu.justwoo.data.network.log.ApiLogger
import com.pollyannawu.justwoo.data.network.log.d
import com.pollyannawu.justwoo.data.network.log.defaultApiLogger
import com.pollyannawu.justwoo.data.network.log.w
import com.pollyannawu.justwoo.model.ApiResult
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException

/**
 * Wrap a network call so the result becomes an [ApiResult] instead of a
 * thrown exception, and emit a one-line log per outcome so service-level
 * intent (which method? success or which kind of failure?) is visible
 * separately from Ktor's wire-level dump.
 *
 * Callers should pass a descriptive [tag] like `"AuthApi.login"`. The
 * back-compat overload (no tag) keeps existing call sites compiling while
 * we migrate; new code should always pass a tag.
 */
suspend inline fun <T> safeApiCall(
    tag: String,
    logger: ApiLogger = ServiceLogger.instance,
    block: () -> T,
): ApiResult<T> {
    logger.d(SERVICE_TAG, "→ $tag")
    return try {
        val value = block()
        logger.d(SERVICE_TAG, "← $tag : success")
        ApiResult.Success(value)
    } catch (e: CancellationException) {
        throw e
    } catch (e: ClientRequestException) {
        logger.w(SERVICE_TAG, "← $tag : client ${e.response.status}", e)
        ApiResult.Error(e.withServerMessage())
    } catch (e: ServerResponseException) {
        logger.w(SERVICE_TAG, "← $tag : server ${e.response.status}", e)
        ApiResult.Error(e.withServerMessage())
    } catch (e: HttpRequestTimeoutException) {
        logger.w(SERVICE_TAG, "← $tag : timeout", e)
        ApiResult.Error(e)
    } catch (e: Throwable) {
        logger.w(SERVICE_TAG, "← $tag : ${e::class.simpleName}", e)
        ApiResult.Error(e)
    }
}

/** Back-compat overload — same as the tagged version but with an anonymous tag. */
suspend inline fun <T> safeApiCall(block: () -> T): ApiResult<T> =
    safeApiCall(tag = "unlabeled", block = block)

@PublishedApi
internal const val SERVICE_TAG: String = "JustWooApi"

/** Lazy holder so `safeApiCall` can stay `inline` without leaking [defaultApiLogger] into call sites. */
@PublishedApi
internal object ServiceLogger {
    val instance: ApiLogger by lazy { defaultApiLogger() }
}

/**
 * Wraps this exception in a new [Exception] whose message is the `"error"` field extracted
 * from the JSON body embedded in Ktor's HTTP error message. Returns `this` unchanged when no
 * such field is found (e.g. network timeouts, non-JSON bodies).
 */
@PublishedApi
internal fun Throwable.withServerMessage(): Throwable {
    val clean = message?.let { parseServerErrorMessage(it) } ?: return this
    return Exception(clean, this)
}

/** Extracts the value of `"error"` from `{"error":"..."}` embedded in a Ktor exception message. */
internal fun parseServerErrorMessage(raw: String): String? {
    val start = raw.indexOf("Text: \"").takeIf { it >= 0 } ?: return null
    val json = raw.substring(start + 7, raw.lastIndexOf('"'))
    return Regex(""""error"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.get(1)
}
