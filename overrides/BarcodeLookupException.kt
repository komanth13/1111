package com.example.domain.model

/** A missing product is null; transport failures and incomplete labels are distinct. */
class BarcodeLookupException(
    val reason: Reason,
    val productName: String? = null,
    cause: Throwable? = null
) : Exception(reason.name, cause) {
    enum class Reason { NETWORK, SERVICE, INCOMPLETE_NUTRITION, INVALID_RESPONSE }
}
