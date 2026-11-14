package com.sizzlesense.ai.di.qualifier

import javax.inject.Qualifier

/**
 * Marks whether internal evaluation tooling is enabled in the active build variant.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DeveloperEvalsEnabled
