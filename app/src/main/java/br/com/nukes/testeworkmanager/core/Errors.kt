package br.com.nukes.testeworkmanager.core

sealed class NetworkException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    data class BadRequestException(val code: Int = 400, override val message: String? = null, override val cause: Throwable? = null) : NetworkException(message, cause)
    data class UnauthorizedException(val code: Int = 401, override val message: String? = null, override val cause: Throwable? = null) : NetworkException(message, cause)
    data class ForbiddenException(val code: Int = 403, override val message: String? = null, override val cause: Throwable? = null) : NetworkException(message, cause)
    data class NotFoundException(val code: Int = 404, override val message: String? = null, override val cause: Throwable? = null) : NetworkException(message, cause)
    data class TimeoutException(val code: Int = 408, override val message: String? = null, override val cause: Throwable? = null) : NetworkException(message, cause)
    data class ConflictException(val code: Int = 409, override val message: String? = null, override val cause: Throwable? = null) : NetworkException(message, cause)
    data class InternalServerErrorException(val code: Int = 500, override val message: String? = null, override val cause: Throwable? = null) : NetworkException(message, cause)
    data class BadGatewayException(val code: Int = 502, override val message: String? = null, override val cause: Throwable? = null) : NetworkException(message, cause)
    data class ServiceUnavailableException(val code: Int = 503, override val message: String? = null, override val cause: Throwable? = null) : NetworkException(message, cause)
    data class GatewayTimeoutException(val code: Int = 504, override val message: String? = null, override val cause: Throwable? = null) : NetworkException(message, cause)

    data class NetworkUnavailableException(override val message: String? = null, override val cause: Throwable? = null) : NetworkException(message, cause)
}

sealed class PreferencesException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    data class EditException(override val message: String? = null, override val cause: Throwable? = null) : PreferencesException(message, cause)
    data class FetchDataException(override val message: String? = null, override val cause: Throwable? = null) : PreferencesException(message, cause)
    data class ClearDataException(override val message: String? = null, override val cause: Throwable? = null) : PreferencesException(message, cause)
}

data class DatabaseException(override val message: String? = null, override val cause: Throwable? = null) : Exception(message, cause)

data class UnknownException(override val message: String? = null, override val cause: Throwable? = null) : Exception(message, cause)

data class ParseException(override val message: String? = null, override val cause: Throwable? = null) : Exception(message, cause)