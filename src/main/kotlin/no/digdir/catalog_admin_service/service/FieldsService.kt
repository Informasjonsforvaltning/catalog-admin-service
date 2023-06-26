package no.digdir.catalog_admin_service.service

import no.digdir.catalog_admin_service.model.EditableFields
import no.digdir.catalog_admin_service.model.Field
import no.digdir.catalog_admin_service.model.Fields
import no.digdir.catalog_admin_service.repository.EditableFieldsRepository
import no.digdir.catalog_admin_service.repository.InternalFieldsRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

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
        internalFieldsRepository.findFieldsByCatalogId(catalogId)
            .sortedBy { it.id }

}
