package no.digdir.catalog_admin_service.service

import no.digdir.catalog_admin_service.model.*
import no.digdir.catalog_admin_service.repository.EditableFieldsRepository
import no.digdir.catalog_admin_service.repository.InternalFieldsRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

private val logger = LoggerFactory.getLogger(FieldsService::class.java)

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

    fun updateEditableFields(catalogId: String, operations: List<JsonPatchOperation>): EditableFields =
        try {
            patchOriginal(getCatalogEditableFields(catalogId), operations)
                .let { editableFieldsRepository.save(it) }
        } catch (ex: Exception) {
            logger.error("Failed to update editable field for catalog $catalogId", ex)
            throw ex
        }

    fun createInternalField(data: FieldToBeCreated, catalogId: String): Field =
        try {
            Field(
                id = UUID.randomUUID().toString(),
                catalogId = catalogId,
                label = data.label,
                description = data.description ?: MultiLanguageTexts(null, null, null),
                type = data.type ?: FieldType.TEXT_SHORT,
                location = data.location ?: FieldLocation.MAIN_COLUMN,
                codeListId = data.codeListId
            ).let { internalFieldsRepository.insert(it) }
        } catch (ex: Exception) {
            logger.error("Failed to create internal field for catalog $catalogId", ex)
            throw ex
        }

    fun getInternalField(fieldId: String, catalogId: String): Field? =
        internalFieldsRepository.findByIdAndCatalogId(fieldId, catalogId)

    fun deleteInternalField(fieldId: String, catalogId: String): Unit =
        try {
            internalFieldsRepository.findByIdAndCatalogId(fieldId, catalogId)
                ?.run { internalFieldsRepository.delete(this) }
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        } catch (ex: Exception) {
            logger.error("Failed to delete internal field with id $fieldId in catalog $catalogId", ex)
            throw ex
        }

    fun updateInternalField(fieldId: String, catalogId: String, operations: List<JsonPatchOperation>): Field? =
        try {
            internalFieldsRepository.findByIdAndCatalogId(fieldId, catalogId)
                ?.let { dbField -> patchOriginal(dbField, operations) }
                ?.let { internalFieldsRepository.save(it) }
        } catch (ex: Exception) {
            logger.error("Failed to update internal field with id $fieldId in catalog $catalogId", ex)
            throw ex
        }

}
