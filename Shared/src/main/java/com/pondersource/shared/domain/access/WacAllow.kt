package com.pondersource.shared.domain.access

import android.os.Parcel
import android.os.Parcelable

/**
 * Parsed representation of the `WAC-Allow` HTTP response header.
 *
 * Format: `WAC-Allow: user="read write", public="read"`
 *
 * Spec: https://solidproject.org/TR/wac — WAC-Allow header
 */
data class WacAllow(
    val userModes: Set<String>,
    val publicModes: Set<String>
) : Parcelable {
    fun canRead() = userModes.contains("read")
    fun canWrite() = userModes.contains("write")
    fun canAppend() = userModes.contains("append") || canWrite()
    fun canControl() = userModes.contains("control")

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeStringList(userModes.toList())
        dest.writeStringList(publicModes.toList())
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<WacAllow> {
            override fun createFromParcel(parcel: Parcel): WacAllow = WacAllow(
                userModes = parcel.createStringArrayList()!!.toSet(),
                publicModes = parcel.createStringArrayList()!!.toSet(),
            )

            override fun newArray(size: Int): Array<WacAllow?> = arrayOfNulls(size)
        }

        fun parse(headerValue: String?): WacAllow? {
            headerValue ?: return null
            val groups = mutableMapOf<String, Set<String>>()
            val regex = Regex("""(\w+)\s*=\s*"([^"]*)"""")
            regex.findAll(headerValue).forEach { match ->
                val group = match.groupValues[1]
                val modes = match.groupValues[2]
                    .split(" ")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet()
                groups[group] = modes
            }
            return WacAllow(
                userModes = groups["user"] ?: emptySet(),
                publicModes = groups["public"] ?: emptySet()
            )
        }
    }
}
