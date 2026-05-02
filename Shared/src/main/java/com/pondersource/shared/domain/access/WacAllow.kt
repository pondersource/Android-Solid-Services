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
public data class WacAllow(
    val userModes: Set<String>,
    val publicModes: Set<String>
) : Parcelable {
    public fun canRead(): Boolean = userModes.contains("read")
    public fun canWrite(): Boolean = userModes.contains("write")
    public fun canAppend(): Boolean = userModes.contains("append") || canWrite()
    public fun canControl(): Boolean = userModes.contains("control")

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeStringList(userModes.toList())
        dest.writeStringList(publicModes.toList())
    }

    public companion object {
        @JvmField
        public val CREATOR: Parcelable.Creator<WacAllow> = object : Parcelable.Creator<WacAllow> {
            override fun createFromParcel(parcel: Parcel): WacAllow = WacAllow(
                userModes = parcel.createStringArrayList()!!.toSet(),
                publicModes = parcel.createStringArrayList()!!.toSet(),
            )

            override fun newArray(size: Int): Array<WacAllow?> = arrayOfNulls(size)
        }

        public fun parse(headerValue: String?): WacAllow? {
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
