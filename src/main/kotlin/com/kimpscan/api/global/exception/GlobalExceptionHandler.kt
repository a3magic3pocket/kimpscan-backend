package com.kimpscan.api.global.exception

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.kimpscan.api.global.dto.ErrorResDto
import com.kimpscan.api.global.dto.FieldErrorDto
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.Date

@ControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseBody
    fun handleHttpMessageNotReadableException(
        httpServletRequest: HttpServletRequest,
        httpMessageNotReadableException: HttpMessageNotReadableException
    ): ResponseEntity<ErrorResDto> {
        val unknownFieldName = "unknown"
        val cause = httpMessageNotReadableException.cause
        val fieldErrors = mutableListOf<FieldErrorDto>()
        val code = "400"
        val status = HttpStatus.BAD_REQUEST
        var message = "some fields are invalid"

        when (cause) {
            is InvalidFormatException -> {
                val fieldName = cause.path.joinToString(" -> ") {
                    if (it.index < 0) {
                        it.fieldName ?: unknownFieldName
                    } else {
                        it.index.toString()
                    }
                }
                val invalidValue = cause.value
                var expectedType = cause.targetType.simpleName

                if (cause.targetType != null && cause.targetType.isEnum) {
                    val enumValues = cause.targetType.enumConstants?.joinToString(", ") { it.toString() }
                    expectedType = "(Enum: $enumValues)"
                }

                fieldErrors.add(
                    FieldErrorDto(
                        field = fieldName,
                        message = "The value of '$fieldName' is invalid: '$invalidValue' (expected: $expectedType)"
                    )
                )
            }

            is MismatchedInputException -> {
                message = "some fields are missing"
                val fieldName = cause.path.joinToString(" -> ") {
                    if (it.index < 0) {
                        it.fieldName ?: unknownFieldName
                    } else {
                        it.index.toString()
                    }
                }
                fieldErrors.add(
                    FieldErrorDto(
                        field = fieldName,
                        message = "$fieldName is required"
                    )
                )
            }

            else -> {
                logger.info("Unknown httpMessageNotReadableException")
            }
        }

        val errorRes = ErrorResDto(
            code = code,
            timestamp = Date(),
            path = httpServletRequest.requestURI,
            message = message,
            fieldErrorDtos = fieldErrors
        )

        return ResponseEntity(errorRes, status)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseBody
    fun handleMethodArgumentNotValidException(
        httpServletRequest: HttpServletRequest,
        methodArgumentNotValidException: MethodArgumentNotValidException,
    ): ErrorResDto {
        val code = "400"
        val message = "some fields are invalid"
        val fieldErrors = methodArgumentNotValidException.bindingResult.fieldErrors.map { fieldError ->
            FieldErrorDto(
                field = fieldError.field,
                message = fieldError.defaultMessage ?: "Invalid value"
            )
        }

        return ErrorResDto(
            code = code,
            timestamp = Date(),
            path = httpServletRequest.requestURI,
            message = message,
            fieldErrorDtos = fieldErrors
        )
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateFieldException::class)
    @ResponseBody
    fun handleDuplicateFieldException(
        httpServletRequest: HttpServletRequest,
        exception: DuplicateFieldException,
    ): ErrorResDto {
        val duplicateFieldExceptionMessage = "duplicate data exists"

        return ErrorResDto(
            code = exception.code,
            timestamp = Date(),
            path = httpServletRequest.requestURI,
            message = duplicateFieldExceptionMessage,
            fieldErrorDtos = listOf(
                FieldErrorDto(
                    field = exception.field,
                    message = exception.message ?: duplicateFieldExceptionMessage
                )
            )
        )
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidFieldException::class)
    @ResponseBody
    fun handleInvalidFieldException(
        httpServletRequest: HttpServletRequest,
        exception: InvalidFieldException,
    ): ErrorResDto {
        val invalidFieldExceptionMessage = "invalid data exists"

        return ErrorResDto(
            code = exception.code,
            timestamp = Date(),
            path = httpServletRequest.requestURI,
            message = invalidFieldExceptionMessage,
            fieldErrorDtos = listOf(
                FieldErrorDto(
                    field = exception.field,
                    message = exception.message ?: invalidFieldExceptionMessage
                )
            )
        )
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException::class)
    @ResponseBody
    fun handleNotFoundException(
        httpServletRequest: HttpServletRequest,
        exception: NotFoundException,
    ): ErrorResDto {
        val notFoundExceptionMessage = "not found"

        return ErrorResDto(
            code = exception.code,
            timestamp = Date(),
            path = httpServletRequest.requestURI,
            message = notFoundExceptionMessage,
            fieldErrorDtos = listOf(
                FieldErrorDto(
                    field = exception.field,
                    message = exception.message ?: notFoundExceptionMessage
                )
            )
        )
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ServiceException::class)
    @ResponseBody
    fun handleServiceException(
        httpServletRequest: HttpServletRequest,
        exception: ServiceException,
    ): ErrorResDto {
        exception.printStackTrace()
        val exceptionMessage = "internal server error occurred"

        return ErrorResDto(
            code = "500",
            timestamp = Date(),
            path = httpServletRequest.requestURI,
            message = exceptionMessage,
            fieldErrorDtos = listOf()
        )
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException::class)
    @ResponseBody
    fun handleMissingServletRequestParameterException(
        httpServletRequest: HttpServletRequest,
        exception: MissingServletRequestParameterException,
    ): ErrorResDto {
        val fieldName = exception.parameterName
        val message = "some fields are missing"

        val fieldErrors = listOf(
            FieldErrorDto(
                field = fieldName,
                message = "$fieldName is required"
            )
        )

        return ErrorResDto(
            code = "400",
            timestamp = Date(),
            path = httpServletRequest.requestURI,
            message = message,
            fieldErrorDtos = fieldErrors
        )
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    @ResponseBody
    fun handleException(
        httpServletRequest: HttpServletRequest,
        exception: Exception,
    ): ErrorResDto {
        val exceptionMessage = "internal server error occurred"
        exception.printStackTrace()

        return ErrorResDto(
            code = "500",
            timestamp = Date(),
            path = httpServletRequest.requestURI,
            message = exceptionMessage,
            fieldErrorDtos = listOf()
        )
    }

}
