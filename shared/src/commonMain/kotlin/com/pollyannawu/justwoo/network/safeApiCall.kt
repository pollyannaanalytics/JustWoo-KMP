package com.pollyannawu.justwoo.network

import com.pollyannawu.justwoo.model.ApiResult
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException

suspend inline fun <T> safeApiCall(block: () -> T): ApiResult<T> =
    try {
        ApiResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: ClientRequestException) {
        ApiResult.Error(e)
    } catch (e: ServerResponseException) {
        ApiResult.Error(e)
    } catch (e: HttpRequestTimeoutException) {
        ApiResult.Error(e)
    } catch (e: Throwable) {
        ApiResult.Error(e)
    }
