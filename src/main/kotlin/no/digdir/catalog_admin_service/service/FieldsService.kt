package no.digdir.catalog_admin_service.service

import no.digdir.catalog_admin_service.model.*
import no.digdir.catalog_admin_service.repository.EditableFieldsRepository
import no.digdir.catalog_admin_service.repository.InternalFieldsRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class FieldsService(
    private val editableFieldsRepository: EditableFieldsRepository,
    private val internalFieldsRepository: InternalFieldsRepository
) {

    fun getCatalogFields(catalogId: String): Fields =
        Fields(
            editable = getCatalogEditableFields(catalogId),
            internal = getCatalogInternalFields(catalogId)
        )

    private fun getCatalogEditableFields(catalogId: String): EditableFields =
        editableFieldsRepository.findByIdOrNull(catalogId)
            ?: EditableFields(catalogId = catalogId, domainCodeListId = null)

    private fun getCatalogInternalFields(catalogId: String): List<Field> =
        internalFieldsRepository.findByCatalogId(catalogId)
            .sortedBy { it.id }

    fun updateEditableFields(editableFields: EditableFields): EditableFields =
        editableFieldsRepository.save(editableFields)

    fun createInternalField(data: FieldToBeCreated, catalogId: String): Field =
        Field(
            id = UUID.randomUUID().toString(),
            catalogId = catalogId,
            label = data.label,
            description = data.description ?: MultiLanguageTexts(null, null, null),
            type = data.type ?: FieldType.TEXT_SHORT,
            location = data.location ?: FieldLocation.MAIN_COLUMN,
            codeListId = data.codeListId
        ).let { internalFieldsRepository.insert(it) }

    fun getInternalField(fieldId: String, catalogId: String): Field? =
        internalFieldsRepository.findByIdAndCatalogId(fieldId, catalogId)

    fun deleteInternalField(fieldId: String, catalogId: String): Unit =
        internalFieldsRepository.findByIdAndCatalogId(fieldId, catalogId)
            ?.run { internalFieldsRepository.delete(this) }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    fun updateInternalField(fieldId: String, catalogId: String, operations: List<JsonPatchOperation>): Field? =
        internalFieldsRepository.findByIdAndCatalogId(fieldId, catalogId)
            ?.let { dbField -> patchOriginal(dbField, operations) }
            ?.let { internalFieldsRepository.save(it) }

}
