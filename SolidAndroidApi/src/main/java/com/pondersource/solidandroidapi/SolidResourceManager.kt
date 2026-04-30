package com.pondersource.solidandroidapi

import android.content.Context
import com.pondersource.shared.domain.crud.N3Patch
import com.pondersource.shared.domain.network.SolidNetworkResponse
import com.pondersource.shared.domain.resource.Resource
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
interface SolidResourceManager {

    companion object {
        /**
         * Returns the application-scoped singleton [SolidResourceManager].
         * @param context Any [Context]; the application context is used internally.
         */
        fun getInstance(context: Context): SolidResourceManager =
            SolidResourceManagerImplementation.getInstance(context)
    }

    /**
     * Reads a resource from the pod.
     * @param webid The WebID of the authenticated user making the request.
     * @param resource The URI of the resource to read.
     * @param clazz The expected resource type (e.g. [com.pondersource.shared.domain.resource.RDFResource]).
     * @return [SolidNetworkResponse.Success] with the resource, or an error/exception variant.
     */
    suspend fun <T : Resource> read(
        webid: String,
        resource: URI,
        clazz: Class<T>,
    ): SolidNetworkResponse<T>

    /**
     * Creates a new resource on the pod.
     * @param webid The WebID of the authenticated user making the request.
     * @param resource The resource to create; its identifier determines the target URI.
     * @return [SolidNetworkResponse.Success] with the created resource.
     */
    suspend fun <T : Resource> create(
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
     * Pass [ifMatch] (the ETag from a previous [read] call) to issue a conditional PUT
     * that fails with 412 Precondition Failed if the resource was modified in the meantime.
     * This is the safe update pattern for [com.pondersource.shared.domain.resource.SolidNonRDFResource].
     *
     * @param webid    The WebID of the authenticated user making the request.
     * @param newResource The updated resource; its identifier determines the target URI.
     * @param ifMatch  Optional ETag for a conditional PUT (prevents lost-update races).
     * @return [SolidNetworkResponse.Success] with the updated resource.
     */
    suspend fun <T : Resource> update(
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
     * Not applicable to [com.pondersource.shared.domain.resource.SolidNonRDFResource] —
     * use [update] for binary resources.
     *
     * @param webid The WebID of the authenticated user making the request.
     * @param uri   The URI of the RDF resource to patch.
     * @param patch The patch to apply.
     * @return [SolidNetworkResponse.Success] with [Unit] on success.
     */
    suspend fun patch(
        webid: String,
        uri: URI,
        patch: N3Patch,
    ): SolidNetworkResponse<Unit>

    /**
     * Applies a pre-built N3 Patch body to an RDF resource on the pod via HTTP PATCH.
     *
     * Use this overload when the patch document has already been serialised to a `text/n3`
     * string (e.g. when transporting a patch over AIDL IPC and reconstructing it on the
     * service side).  Prefer [patch] with a typed [N3Patch] when building patches in-process.
     *
     * @param webid     The WebID of the authenticated user making the request.
     * @param uri       The URI of the RDF resource to patch.
     * @param n3Body    The full `text/n3` patch document body.
     * @return [SolidNetworkResponse.Success] with [Unit] on success.
     */
    suspend fun patchRaw(
        webid: String,
        uri: URI,
        n3Body: String,
    ): SolidNetworkResponse<Unit>

    /**
     * Deletes a resource from the pod.
     * @param webid The WebID of the authenticated user making the request.
     * @param resource The resource to delete.
     * @return [SolidNetworkResponse.Success] with the deleted resource.
     */
    suspend fun <T : Resource> delete(
        webid: String,
        resource: T,
    ): SolidNetworkResponse<T>

    /**
     * Deletes a resource from the pod.
     * @param webid The WebID of the authenticated user making the request.
     * @param resourceUri The uri of the resource to delete.
     * @return [SolidNetworkResponse.Success]
     */
    suspend fun delete(
        webid: String,
        resourceUri: URI,
    ): SolidNetworkResponse<Boolean>

    /**
     * Recursively deletes a container and all of its contents.
     * @param webid The WebID of the authenticated user making the request.
     * @param containerUri The URI of the LDP container to delete.
     * @return [SolidNetworkResponse.Success] with `true` on success.
     */
    suspend fun deleteContainer(
        webid: String,
        containerUri: URI
    ): SolidNetworkResponse<Boolean>
}
