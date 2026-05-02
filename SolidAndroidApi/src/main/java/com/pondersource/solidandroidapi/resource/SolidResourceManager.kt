package com.pondersource.solidandroidapi.resource

import android.content.Context
import com.pondersource.shared.domain.crud.N3Patch
import com.pondersource.shared.domain.network.SolidNetworkResponse
import com.pondersource.shared.domain.resource.Resource
import com.pondersource.shared.domain.resource.SolidMetadata
import com.pondersource.solidandroidapi.auth.Authenticator
import com.pondersource.solidandroidapi.resource.implementation.SolidResourceManagerImplementation
import java.net.URI

/**
 * Performs authenticated CRUD operations on Solid pod resources on behalf of a specific user.
 *
 * All operations require the user identified by `webid` to have an active, authorized
 * [Authenticator] session.  Results are wrapped in [SolidNetworkResponse] so callers can
 * distinguish HTTP errors from unexpected exceptions without catching throwables.
 *
 * Obtain an instance via [SolidResourceManager.getInstance].
 */
public interface SolidResourceManager {

    public companion object {
        /**
         * Returns the application-scoped singleton [SolidResourceManager].
         * @param authenticator Any [Authenticator]; The one used for authentication.
         */
        public fun getInstance(authenticator: Authenticator): SolidResourceManager =
            SolidResourceManagerImplementation.getInstance(authenticator)
    }

    /**
     * Fetches only the HTTP headers for the resource at [uri] via HTTP HEAD.
     *
     * Returns [SolidMetadata] with all Solid-relevant response headers: ETag, Content-Type,
     * Content-Length, WAC-Allow, Allow, all Link relations (acl, describedby, type,
     * storageDescription), Accept-Patch/Post/Put, Last-Modified, and WWW-Authenticate.
     *
     * No response body is transferred. Ideal for caching checks, permission discovery,
     * and auxiliary resource IRI resolution before committing to a full GET.
     *
     * @param webid The WebID of the authenticated user making the request.
     * @param uri   The URI of the resource to HEAD.
     * @return [SolidNetworkResponse.Success] with [SolidMetadata], or an error/exception variant.
     */
    public suspend fun head(
        webid: String,
        uri: URI,
    ): SolidNetworkResponse<SolidMetadata>

    /**
     * Reads a resource from the pod.
     * @param webid The WebID of the authenticated user making the request.
     * @param resource The URI of the resource to read.
     * @param clazz The expected resource type (e.g. [com.pondersource.shared.domain.resource.RDFResource]).
     * @return [SolidNetworkResponse.Success] with the resource, or an error/exception variant.
     */
    public suspend fun <T : Resource> read(
        webid: String,
        resource: URI,
        clazz: Class<T>,
    ): SolidNetworkResponse<T>

    /**
     * Creates a new resource on the pod via conditional PUT (`If-None-Match: *`).
     *
     * Fails with 409 Conflict if a resource already exists at the target URI.
     *
     * @param webid The WebID of the authenticated user making the request.
     * @param resource The resource to create; its identifier determines the target URI.
     * @return [SolidNetworkResponse.Success] with the created resource.
     */
    public suspend fun <T : Resource> create(
        webid: String,
        resource: T
    ): SolidNetworkResponse<T>

    /**
     * Replaces an existing resource on the pod via HTTP PUT.
     *
     * For RDF resources, prefer [patch] when only a subset of triples changes — it is
     * atomic and avoids a full read-modify-write cycle. Use [update] when you have the
     * complete new representation (e.g. uploading a new photo or rewriting a full document).
     *
     * When [ifMatch] is null, a conditional `If-Match: *` is used to ensure the resource
     * exists before writing. Pass the ETag from a previous [read] or [head] call to get
     * full optimistic-concurrency protection (412 Precondition Failed on version mismatch).
     *
     * @param webid    The WebID of the authenticated user making the request.
     * @param newResource The updated resource; its identifier determines the target URI.
     * @param ifMatch  Optional ETag for a conditional PUT (prevents lost-update races).
     * @return [SolidNetworkResponse.Success] with the updated resource.
     */
    public suspend fun <T : Resource> update(
        webid: String,
        newResource: T,
        ifMatch: String? = null,
    ): SolidNetworkResponse<T>

    /**
     * Applies an N3 Patch to an RDF resource on the pod via HTTP PATCH.
     *
     * This is the preferred method for partial updates to RDF resources — it is atomic
     * and does not require reading the full resource first. Use [N3Patch.build] or
     * [N3Patch.fromDiff] to construct the patch without writing raw N3 strings.
     *
     * Not applicable to [com.pondersource.shared.domain.resource.SolidNonRDFSource] —
     * use [update] for binary resources.
     *
     * Pass [ifMatch] (the ETag from a previous [read] or [head] call) to issue a conditional
     * PATCH that fails with 412 if the resource was modified in the meantime.
     *
     * @param webid   The WebID of the authenticated user making the request.
     * @param uri     The URI of the RDF resource to patch.
     * @param patch   The patch to apply.
     * @param ifMatch Optional ETag for a conditional PATCH.
     * @return [SolidNetworkResponse.Success] with [Unit] on success.
     */
    public suspend fun patch(
        webid: String,
        uri: URI,
        patch: N3Patch,
        ifMatch: String? = null,
    ): SolidNetworkResponse<Unit>

    /**
     * Applies a pre-built N3 Patch body to an RDF resource on the pod via HTTP PATCH.
     *
     * Use this overload when the patch document has already been serialised to a `text/n3`
     * string (e.g. when transporting a patch over AIDL IPC and reconstructing it on the
     * service side). Prefer [patch] with a typed [N3Patch] when building patches in-process.
     *
     * Pass [ifMatch] (the ETag from a previous [read] or [head] call) to issue a conditional
     * PATCH that fails with 412 if the resource was modified in the meantime.
     *
     * @param webid     The WebID of the authenticated user making the request.
     * @param uri       The URI of the RDF resource to patch.
     * @param n3Body    The full `text/n3` patch document body.
     * @param ifMatch   Optional ETag for a conditional PATCH.
     * @return [SolidNetworkResponse.Success] with [Unit] on success.
     */
    public suspend fun patchRaw(
        webid: String,
        uri: URI,
        n3Body: String,
        ifMatch: String? = null,
    ): SolidNetworkResponse<Unit>

    /**
     * Deletes a resource or container from the pod.
     *
     * When [resource] is a container (or its URI ends with `/`), all contained resources
     * are deleted recursively before the container itself is removed.
     *
     * @param webid The WebID of the authenticated user making the request.
     * @param resource The resource to delete.
     * @return [SolidNetworkResponse.Success] with the deleted resource.
     */
    public suspend fun <T : Resource> delete(
        webid: String,
        resource: T,
    ): SolidNetworkResponse<T>

    /**
     * Deletes a resource or container from the pod by URI.
     *
     * When [resourceUri] ends with `/`, the target is treated as a container and all
     * contained resources are deleted recursively before the container itself is removed.
     *
     * @param webid The WebID of the authenticated user making the request.
     * @param resourceUri The URI of the resource or container to delete.
     * @return [SolidNetworkResponse.Success] with `true` on success.
     */
    public suspend fun delete(
        webid: String,
        resourceUri: URI,
    ): SolidNetworkResponse<Boolean>
}
