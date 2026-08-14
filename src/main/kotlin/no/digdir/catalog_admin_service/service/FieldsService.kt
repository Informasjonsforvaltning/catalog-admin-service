package no.digdir.catalog_admin_service.service

import jakarta.persistence.EntityManager
import no.digdir.catalog_admin_service.model.EditableFields
import no.digdir.catalog_admin_service.model.Field
import no.digdir.catalog_admin_service.model.FieldLocation
import no.digdir.catalog_admin_service.model.FieldToBeCreated
import no.digdir.catalog_admin_service.model.FieldType
import no.digdir.catalog_admin_service.model.Fields
import no.digdir.catalog_admin_service.model.JsonPatchOperation
import no.digdir.catalog_admin_service.model.MultiLanguageTexts
import no.digdir.catalog_admin_service.repository.EditableFieldsRepository
import no.digdir.catalog_admin_service.repository.InternalFieldsRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

private val logger = LoggerFactory.getLogger(FieldsService::class.java)

@Service
class FieldsService(
    private val editableFieldsRepository: EditableFieldsRepository,
    private val internalFieldsRepository: InternalFieldsRepository,
    private val entityManager: EntityManager,
) {
    fun getCatalogFields(catalogId: String): Fields = Fields(
        editable = getCatalogEditableFields(catalogId),
        internal = getCatalogInternalFields(catalogId),
    )

    private fun getCatalogEditableFields(catalogId: String): EditableFields = editableFieldsRepository.findById(catalogId).orElse(null)
        ?: EditableFields(catalogId = catalogId, domainCodeListId = null)

    private fun getCatalogInternalFields(catalogId: String): List<Field> = internalFieldsRepository.findByCatalogId(catalogId)

    fun updateEditableFields(catalogId: String, operations: List<JsonPatchOperation>): EditableFields = try {
        patchOriginal(getCatalogEditableFields(catalogId), operations)
            .let { editableFieldsRepository.save(it) }
    } catch (ex: Exception) {
        logger.error("Failed to update editable field for catalog $catalogId", ex)
        throw ex
    }

    @Transactional
    fun createInternalField(data: FieldToBeCreated, catalogId: String): Field = try {
        Field(
            id = UUID.randomUUID().toString(),
            catalogId = catalogId,
            label = data.label,
            description = data.description ?: MultiLanguageTexts(null, null, null),
            type = data.type ?: FieldType.TEXT_SHORT,
            location = data.location ?: FieldLocation.MAIN_COLUMN,
            codeListId = data.codeListId,
            enableFilter = data.enableFilter,
        ).also { entityManager.persist(it) }
    } catch (ex: Exception) {
        logger.error("Failed to create internal field for catalog $catalogId", ex)
        throw ex
    }

    fun getInternalField(fieldId: String, catalogId: String): Field? = internalFieldsRepository.findByIdAndCatalogId(fieldId, catalogId)

    fun deleteInternalField(fieldId: String, catalogId: String): Unit = try {
        internalFieldsRepository
            .findByIdAndCatalogId(fieldId, catalogId)
            ?.run { internalFieldsRepository.delete(this) }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    } catch (ex: Exception) {
        logger.error("Failed to delete internal field with id $fieldId in catalog $catalogId", ex)
        throw ex
    }

    fun updateInternalField(fieldId: String, catalogId: String, operations: List<JsonPatchOperation>): Field? = try {
        internalFieldsRepository
            .findByIdAndCatalogId(fieldId, catalogId)
            ?.let { dbField -> patchOriginal(dbField, operations) }
            ?.let { internalFieldsRepository.save(it) }
    } catch (ex: Exception) {
        logger.error("Failed to update internal field with id $fieldId in catalog $catalogId", ex)
        throw ex
    }
}
