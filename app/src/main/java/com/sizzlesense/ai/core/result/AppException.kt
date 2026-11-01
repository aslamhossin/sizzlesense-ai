package com.sizzlesense.ai.core.result

class AppException(val appError: AppError) : Exception(appError.readableMessage)
