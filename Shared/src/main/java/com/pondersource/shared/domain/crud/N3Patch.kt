package com.pondersource.shared.domain.crud

import com.pondersource.shared.domain.network.HTTPAcceptType
import com.pondersource.shared.domain.resource.RDFResource
import com.pondersource.shared.domain.resource.RdfQuad
import java.io.InputStream

/**
 * Represents a Solid N3 Patch document for use with HTTP PATCH requests.
 *
 * A valid N3 Patch document declares `rdf:type solid:InsertDeletePatch` and
 * may contain `solid:deletes`, `solid:inserts`, and `solid:where` formulae.
 *
 * ## Building a patch
 *
 * **DSL builder** — no raw N3 strings required:
 * ```kotlin
 * // Simple insert
 * val patch = N3Patch.build {
 *     insertLiteral(contactUri, VCARD.FN, "Alice")
 * }
 *
 * // Safe replace — bind the old value via where, then swap it
 * val patch = N3Patch.build {
 *     where(contactUri, VCARD.FN, variable = "oldName")
 *     deleteVar(contactUri, VCARD.FN, variable = "oldName")
 *     insertLiteral(contactUri, VCARD.FN, "Alice")
 * }
 * ```
 *
 * **Diff factory** — automatically derives the patch from two resource states:
 * ```kotlin
 * val patch = N3Patch.fromDiff(originalResource, modifiedResource)
 * ```
 *
 * Content-Type: text/n3
 * Spec: https://solidproject.org/TR/protocol#n3-patch
 */
data class N3Patch(
    val deletes: String? = null,
    val inserts: String? = null,
    val where: String? = null,
) {
    init {
        require(deletes != null || inserts != null) {
            "N3Patch must contain at least one of 'deletes' or 'inserts'."
        }
    }

    val contentType: String = HTTPAcceptType.N3

    fun toN3String(): String = buildString {
        appendLine("@prefix solid: <http://www.w3.org/ns/solid/terms#> .")
        appendLine("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .")
        appendLine()
        appendLine("<> a solid:InsertDeletePatch ;")

        val clauses = mutableListOf<String>()
        if (deletes != null) clauses.add("  solid:deletes { $deletes }")
        if (inserts != null) clauses.add("  solid:inserts { $inserts }")
        if (where != null) clauses.add("  solid:where   { $where }")

        append(clauses.joinToString(" ;\n"))
        appendLine(" .")
    }

    fun toInputStream(): InputStream = toN3String().byteInputStream()

    companion object {
        fun insert(triples: String): N3Patch = N3Patch(inserts = triples)

        fun delete(triples: String): N3Patch = N3Patch(deletes = triples)

        fun replace(inserts: String, where: String): N3Patch =
            N3Patch(inserts = inserts, where = where)

        // ---- DSL builder ----

        fun build(block: N3PatchBuilder.() -> Unit): N3Patch =
            N3PatchBuilder().apply(block).build()

        fun fromDiff(original: RDFResource, updated: RDFResource): N3Patch {
            val originalQuads = original.getAllQuads().filter { it.graph == null }.toSet()
            val updatedQuads = updated.getAllQuads().filter { it.graph == null }.toSet()

            val toDelete = originalQuads - updatedQuads
            val toInsert = updatedQuads - originalQuads

            require(toDelete.isNotEmpty() || toInsert.isNotEmpty()) {
                "No difference between original and updated resources — patch would be empty."
            }

            return N3Patch(
                deletes = toDelete.takeIf { it.isNotEmpty() }
                    ?.joinToString("\n    ") { it.toN3Triple() },
                inserts = toInsert.takeIf { it.isNotEmpty() }
                    ?.joinToString("\n    ") { it.toN3Triple() },
            )
        }
    }
}

/**
 * Kotlin DSL builder for [N3Patch].
 *
 * Produces properly escaped N3 triple strings — callers work entirely with
 * subject/predicate IRI strings and typed object values; no raw N3 syntax needed.
 *
 * Variable names passed to [where], [deleteVar], and [insertVar] must not include
 * the leading `?` — the builder adds it automatically.
 */
class N3PatchBuilder {

    private val insertTriples = mutableListOf<String>()
    private val deleteTriples = mutableListOf<String>()
    private val whereTriples = mutableListOf<String>()

    fun insert(subject: String, predicate: String, iriObject: String): N3PatchBuilder {
        insertTriples += triple(subject, predicate, iriObject.asN3IriTerm())
        return this
    }

    fun insertLiteral(
        subject: String,
        predicate: String,
        value: String,
        datatype: String? = null,
        language: String? = null,
    ): N3PatchBuilder {
        insertTriples += triple(subject, predicate, literalTerm(value, datatype, language))
        return this
    }

    fun insertVar(subject: String, predicate: String, variable: String): N3PatchBuilder {
        insertTriples += triple(subject, predicate, "?$variable")
        return this
    }

    fun delete(subject: String, predicate: String, iriObject: String): N3PatchBuilder {
        deleteTriples += triple(subject, predicate, iriObject.asN3IriTerm())
        return this
    }

    fun deleteLiteral(
        subject: String,
        predicate: String,
        value: String,
        datatype: String? = null,
        language: String? = null,
    ): N3PatchBuilder {
        deleteTriples += triple(subject, predicate, literalTerm(value, datatype, language))
        return this
    }

    fun deleteVar(subject: String, predicate: String, variable: String): N3PatchBuilder {
        deleteTriples += triple(subject, predicate, "?$variable")
        return this
    }

    fun where(subject: String, predicate: String, variable: String): N3PatchBuilder {
        whereTriples += triple(subject, predicate, "?$variable")
        return this
    }

    fun build(): N3Patch {
        val insertsStr = insertTriples.takeIf { it.isNotEmpty() }?.joinToString("\n    ")
        val deletesStr = deleteTriples.takeIf { it.isNotEmpty() }?.joinToString("\n    ")
        val whereStr = whereTriples.takeIf { it.isNotEmpty() }?.joinToString("\n    ")
        return N3Patch(inserts = insertsStr, deletes = deletesStr, where = whereStr)
    }

    private fun String.asN3Subject(): String = if (startsWith("_:")) this else "<$this>"
    private fun String.asN3IriTerm(): String = if (startsWith("_:")) this else "<$this>"

    private fun literalTerm(value: String, datatype: String?, language: String?): String {
        val escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        return when {
            language != null -> "\"$escaped\"@$language"
            datatype != null -> "\"$escaped\"^^<$datatype>"
            else -> "\"$escaped\""
        }
    }

    private fun triple(subject: String, predicate: String, objectTerm: String): String =
        "${subject.asN3Subject()} <$predicate> $objectTerm ."
}

internal fun RdfQuad.toN3Triple(): String {
    val s = if (subject.startsWith("_:")) subject else "<$subject>"
    val p = "<$predicate>"
    val o = when {
        isBlankObject -> `object`
        isLiteralObject -> {
            val escaped = `object`
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
            when {
                language != null -> "\"$escaped\"@$language"
                datatype != null -> "\"$escaped\"^^<$datatype>"
                else -> "\"$escaped\""
            }
        }

        else -> "<${`object`}>"
    }
    return "$s $p $o ."
}
