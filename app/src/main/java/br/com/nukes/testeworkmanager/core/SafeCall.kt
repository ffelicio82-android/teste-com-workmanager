package br.com.nukes.testeworkmanager.core

import android.database.SQLException
import android.database.sqlite.SQLiteException
import br.com.nukes.testeworkmanager.core.NetworkException.BadGatewayException
import br.com.nukes.testeworkmanager.core.NetworkException.BadRequestException
import br.com.nukes.testeworkmanager.core.NetworkException.ConflictException
import br.com.nukes.testeworkmanager.core.NetworkException.ForbiddenException
import br.com.nukes.testeworkmanager.core.NetworkException.GatewayTimeoutException
import br.com.nukes.testeworkmanager.core.NetworkException.HttpFailureException
import br.com.nukes.testeworkmanager.core.NetworkException.InternalServerErrorException
import br.com.nukes.testeworkmanager.core.NetworkException.NetworkUnavailableException
import br.com.nukes.testeworkmanager.core.NetworkException.NotFoundException
import br.com.nukes.testeworkmanager.core.NetworkException.ServiceUnavailableException
import br.com.nukes.testeworkmanager.core.NetworkException.UnauthorizedException
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.IOException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

suspend inline fun <T> safeCall(crossinline block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e // Propagate cancellation exceptions
    } catch (e: Throwable) {
        Result.failure(mapToDomain(e))
    }
}

suspend inline fun <T> safeIo(
    dispatcher: CoroutineDispatcher,
    crossinline block: suspend () -> T
): Result<T> = safeCall {
    withContext(dispatcher) {
        block()
    }
}

fun mapToDomain(t: Throwable): Throwable = when (t) {
    is HttpException -> when (t.code()) {
        400 -> BadRequestException(message = t.message(), cause = t)
        401 -> UnauthorizedException(message = t.message(), cause = t)
        403 -> ForbiddenException(message = t.message(), cause = t)
        404 -> NotFoundException(message = t.message(), cause = t)
        408 -> NetworkException.TimeoutException(message = t.message(), cause = t)
        409 -> ConflictException(message = t.message(), cause = t)
        500 -> InternalServerErrorException(message = t.message(), cause = t)
        502 -> BadGatewayException(message = t.message(), cause = t)
        503 -> ServiceUnavailableException(message = t.message(), cause = t)
        504 -> GatewayTimeoutException(message = t.message(), cause = t)
        else -> HttpFailureException(message = t.message(), cause = t)
    }
    is SocketTimeoutException -> NetworkException.TimeoutException(message = "", cause = t)
    is UnknownHostException, is ConnectException -> NetworkUnavailableException(t.message, t)
    is IOException -> t
    is JsonDataException -> ParseException(t.message, t)
    is PreferencesException -> t
    is SQLException, is SQLiteException -> DatabaseException(t.message, t)
    else -> UnknownException(t.message, t)
}