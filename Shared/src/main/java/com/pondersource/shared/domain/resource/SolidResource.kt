package com.pondersource.shared.domain.resource

/**
 * Marker interface for resources retrieved from a Solid server.
 *
 * All Solid resources return [SolidMetadata] populated from the HTTP response headers
 * (ACL URL, WAC-Allow, allowed methods, resource types, ETag, etc.).
 */
interface SolidResource : Resource {

    fun getMetadata(): SolidMetadata
}