package br.com.nukes.testeworkmanager.core

import br.com.nukes.testeworkmanager.core.NetworkException.BadGatewayException
import br.com.nukes.testeworkmanager.core.NetworkException.BadRequestException
import br.com.nukes.testeworkmanager.core.NetworkException.ConflictException
import br.com.nukes.testeworkmanager.core.NetworkException.ForbiddenException
import br.com.nukes.testeworkmanager.core.NetworkException.GatewayTimeoutException
import br.com.nukes.testeworkmanager.core.NetworkException.InternalServerErrorException
import br.com.nukes.testeworkmanager.core.NetworkException.NetworkUnavailableException
import br.com.nukes.testeworkmanager.core.NetworkException.NotFoundException
import br.com.nukes.testeworkmanager.core.NetworkException.ServiceUnavailableException
import br.com.nukes.testeworkmanager.core.NetworkException.UnauthorizedException
import com.squareup.moshi.JsonDataException
import okio.IOException
import retrofit2.HttpException
import java.util.concurrent.TimeoutException

suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
    return try {
        val result = block()
        Result.success(result)
    } catch (e: HttpException) {
        val networkException = when (e.code()) {
            400 -> BadRequestException(message = e.message(), cause = e)
            401 -> UnauthorizedException(message = e.message(), cause = e)
            403 -> ForbiddenException(message = e.message(), cause = e)
            404 -> NotFoundException(message = e.message(), cause = e)
            408 -> NetworkException.TimeoutException(message = e.message(), cause = e)
            409 -> ConflictException(message = e.message(), cause = e)
            500 -> InternalServerErrorException(message = e.message(), cause = e)
            502 -> BadGatewayException(message = e.message(), cause = e)
            503 -> ServiceUnavailableException(message = e.message(), cause = e)
            504 -> GatewayTimeoutException(message = e.message(), cause = e)
            else -> NetworkUnavailableException(message = e.message(), cause = e)
        }

        Result.failure(networkException)
    } catch (e: TimeoutException) {
        Result.failure(NetworkException.TimeoutException(message = e.message, cause = e))
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: JsonDataException) {
        Result.failure(ParseException(message = e.message, cause = e))
    } catch (e: PreferencesException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(UnknownException(message = e.message, cause = e))
    }
}