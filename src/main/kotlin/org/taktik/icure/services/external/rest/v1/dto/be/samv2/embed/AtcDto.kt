package org.taktik.icure.services.external.rest.v1.dto.be.samv2.embed

class AtcDto(var code: String? = null, var description: String? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AtcDto

        if (code != other.code) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code?.hashCode() ?: 0
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }
}
