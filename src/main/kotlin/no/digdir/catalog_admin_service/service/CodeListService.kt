package no.digdir.catalog_admin_service.service

import no.digdir.catalog_admin_service.configuration.ApplicationProperties
import no.digdir.catalog_admin_service.model.Code
import java.util.*
import no.digdir.catalog_admin_service.model.CodeList
import no.digdir.catalog_admin_service.model.CodeListToBeCreated
import no.digdir.catalog_admin_service.model.CodeLists
import no.digdir.catalog_admin_service.model.FieldType
import no.digdir.catalog_admin_service.repository.CodeListRepository
import org.springframework.stereotype.Service
import no.digdir.catalog_admin_service.model.JsonPatchOperation
import no.digdir.catalog_admin_service.model.MultiLanguageTexts
import no.digdir.catalog_admin_service.rdf.UNESKOS
import no.digdir.catalog_admin_service.rdf.turtleResponse
import no.digdir.catalog_admin_service.repository.EditableFieldsRepository
import no.digdir.catalog_admin_service.repository.InternalFieldsRepository
import org.apache.jena.datatypes.xsd.impl.XSDDateType
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.SKOS
import org.apache.jena.vocabulary.XSD
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

private val logger = LoggerFactory.getLogger(CodeListService::class.java)

@Service
class CodeListService(
    private val codeListRepository: CodeListRepository,
    private val editableFieldsRepository: EditableFieldsRepository,
    private val applicationProperties: ApplicationProperties,
    private val internalFieldsRepository: InternalFieldsRepository,
) {
    private fun CodeList.subjectsURI() = "${applicationProperties.adminServiceUri}/$catalogId/concepts/subjects"
    private fun createCodeURI(codeListUri: String, codeId: String) = "$codeListUri#$codeId"
    private fun publisherURI(publisherId: String) = "https://data.brreg.no/enhetsregisteret/api/enheter/$publisherId"

    fun getCodeLists(catalogId: String): CodeLists =
        CodeLists(codeLists = codeListRepository.findCodeListsByCatalogId(catalogId).sortedBy { it.name })

    fun getCodeListById(catalogId: String, codeListId: String): CodeList? =
        codeListRepository.findCodeListByIdAndCatalogId(codeListId, catalogId)

    fun deleteCodeListById(catalogId: String, codeListId: String) {
        val codeListsInInternalFields = internalFieldsRepository.findByCatalogIdAndTypeAndCodeListId(catalogId, FieldType.CODE_LIST, codeListId)
        val domainCodeListInEditableField = editableFieldsRepository.findByIdOrNull(catalogId)?.domainCodeListId

        when {
            codeListsInInternalFields.isNotEmpty() -> {
                logger.error("Cannot delete a code list that is in use in internal fields.")
                throw ResponseStatusException(HttpStatus.BAD_REQUEST)
            }
            domainCodeListInEditableField == codeListId -> {
                logger.error("Cannot delete a code list that is in use in editable fields.")
                throw ResponseStatusException(HttpStatus.BAD_REQUEST)
            }
            else -> {
                try {
                    codeListRepository.deleteById(codeListId)
                } catch (ex: Exception) {
                    logger.error("Failed to delete code list with id $codeListId", ex)
                    throw ex
                }
            }
        }
    }

    private fun CodeListToBeCreated.mapCodeListToBeCreatedToCodeList(catalogId: String): CodeList =
        CodeList(
            id = UUID.randomUUID().toString(),
            name = name,
            catalogId = catalogId,
            description = description,
            codes = codes
        )


    fun createCodeList(data: CodeListToBeCreated, catalogId: String): CodeList =
        try {
            data.mapCodeListToBeCreatedToCodeList(catalogId)
                .let { codeListRepository.insert(it) }
        } catch (ex: Exception) {
            logger.error("Failed to create code-list for catalog $catalogId", ex)
            throw ex
        }

    fun createListOfCodeLists(codeListsToBeCreated: List<CodeListToBeCreated>, catalogId: String) {
        codeListsToBeCreated
            .map { it.mapCodeListToBeCreatedToCodeList(catalogId) }
            .run { codeListRepository.saveAll(this) }
    }

    fun updateCodeList(codeListId: String, catalogId: String, operations: List<JsonPatchOperation>): CodeList? =
        try {
            codeListRepository.findCodeListByIdAndCatalogId(codeListId, catalogId)
                ?.let { dbCodeList -> patchOriginal(dbCodeList, operations) }
                ?.let { codeListRepository.save(it) }
        } catch (ex: Exception) {
            logger.error("Failed to update code-list with id $codeListId in catalog $catalogId", ex)
            throw ex
        }

    fun getAllConceptSubjectCodeLists(): List<CodeList> =
        editableFieldsRepository.findAll()
            .mapNotNull { it.domainCodeListId }
            .mapNotNull { codeListRepository.findByIdOrNull(it) }

    fun getAllConceptSubjectCodeListsRDF(): String {
        val allConceptSubjects = ModelFactory.createDefaultModel()
        getAllConceptSubjectCodeLists()
            .forEach { allConceptSubjects.add(it.createModel()) }

        return allConceptSubjects.addDefaultCodeListPrefixes().turtleResponse()
    }

    fun getConceptSubjectsForCatalog(catalogId: String): CodeList? =
        editableFieldsRepository.findByIdOrNull(catalogId)
            ?.domainCodeListId
            ?.let { codeListRepository.findByIdOrNull(it) }

    fun getConceptSubjectsForCatalogRDF(catalogId: String): String? =
        getConceptSubjectsForCatalog(catalogId)
            ?.createModel()
            ?.addDefaultCodeListPrefixes()
            ?.turtleResponse()

    private fun Model.addDefaultCodeListPrefixes(): Model {
        setNsPrefix("dct", DCTerms.NS)
        setNsPrefix("skos", SKOS.uri)
        setNsPrefix("uneskos", UNESKOS.uri)
        setNsPrefix("xsd", XSD.NS)
        return this
    }

    private fun CodeList.createModel(): Model {
        val uri = subjectsURI()
        val codeListModel = ModelFactory.createDefaultModel()
        codeListModel.createResource(uri, SKOS.ConceptScheme)
            .addProperty(DCTerms.identifier, codeListModel.createTypedLiteral(uri, XSDDateType.XSDanyURI))
            .addProperty(DCTerms.title, name)
            .addProperty(DCTerms.description, description)
            .addProperty(DCTerms.publisher, codeListModel.createResource(publisherURI(catalogId)))
            .addCodes(codes)

        return codeListModel
    }

    private fun Resource.addCodes(codes: List<Code>): Resource {
        codes.map { code -> createCodeResource(code, codes.filter { it.parentID == code.id }.map { it.id }) }
            .forEach { addProperty(UNESKOS.contains, it) }

        return this
    }

    private fun Resource.createCodeResource(code: Code, childrenIds: List<String>): Resource {
        val codeURI = createCodeURI(uri, code.id)
        return model.createResource(codeURI, SKOS.Concept)
            .addProperty(DCTerms.identifier, model.createTypedLiteral(codeURI, XSDDateType.XSDanyURI))
            .addProperty(SKOS.inScheme, this)
            .addBroader(uri, code.parentID)
            .addNarrower(uri, childrenIds)
            .addCodePrefLabel(code.name)
    }

    private fun Resource.addCodePrefLabel(name: MultiLanguageTexts): Resource {
        if (name.nb != null) addProperty(SKOS.prefLabel, name.nb, "nb")
        if (name.nn != null) addProperty(SKOS.prefLabel, name.nn, "nn")
        if (name.en != null) addProperty(SKOS.prefLabel, name.en, "en")
        return this
    }

    private fun Resource.addBroader(codeListUri: String, parentId: String?): Resource {
        if (parentId != null) addProperty(SKOS.broader, model.getResource(createCodeURI(codeListUri, parentId)))
        return this
    }

    private fun Resource.addNarrower(codeListUri: String, childrenIds: List<String>): Resource {
        childrenIds.forEach { addProperty(SKOS.narrower, model.getResource(createCodeURI(codeListUri, it))) }
        return this
    }
}
