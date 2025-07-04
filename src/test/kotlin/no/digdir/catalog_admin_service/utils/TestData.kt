package no.digdir.catalog_admin_service.utils

import no.digdir.catalog_admin_service.model.Code
import no.digdir.catalog_admin_service.model.CodeList
import no.digdir.catalog_admin_service.model.DesignDTO
import no.digdir.catalog_admin_service.model.DesignDBO
import no.digdir.catalog_admin_service.model.CodeListToBeCreated
import no.digdir.catalog_admin_service.model.EditableFields
import no.digdir.catalog_admin_service.model.Field
import no.digdir.catalog_admin_service.model.FieldLocation
import no.digdir.catalog_admin_service.model.FieldType
import no.digdir.catalog_admin_service.model.Logo
import no.digdir.catalog_admin_service.model.MultiLanguageTexts
import no.digdir.catalog_admin_service.model.User
import no.digdir.catalog_admin_service.model.UserToBeCreated
import org.bson.Document
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap

const val MONGO_USER = "testuser"
const val MONGO_PASSWORD = "testpassword"
const val MONGO_PORT = 27017
const val MONGO_DATABASE = "catalogAdmin"
const val MONGO_CODELIST_COLLECTION = "codeLists"
const val MONGO_DESIGN_COLLECTION = "catalogDesigns"
const val MONGO_LOGO_COLLECTION = "catalogLogos"
const val MONGO_USER_COLLECTION = "catalogUsers"
const val INTERNAL_FIELDS_COLLECTION = "internalFields"
const val EDITABLE_COLLECTIONS_COLLECTION = "editableFields"

val MONGO_ENV_VALUES: Map<String, String> = ImmutableMap.of(
    "MONGO_INITDB_ROOT_USERNAME", MONGO_USER,
    "MONGO_INITDB_ROOT_PASSWORD", MONGO_PASSWORD
)

val NAME: MultiLanguageTexts = MultiLanguageTexts(en = "codeName", nb = null, nn = null)
val CODE: Code = Code(id = "555", name = NAME, parentID = null)
val CODES: List<Code> = listOf(CODE)
val CODE_LIST_0 =
    CodeList(id = "123", name = "name", description = "description", codes = CODES, catalogId = "910244132")
val CODES_1 = listOf(
    Code("1", MultiLanguageTexts("nb 1", "nn 1", "en 1"), null),
    Code("2", MultiLanguageTexts("nb 2", "nn 2", "en 2"), "1"),
    Code("3", MultiLanguageTexts("nb 3", "nn 3", null), "1")
)
val CODE_LIST_1 = CodeList(id = "321", name = "code list 1", description = "description of code list 1", codes = CODES_1, catalogId = "123456789")
val CODE_LIST_2 = CodeList(id = "456", name = "code list 2", description = "description of code list 2", codes = CODES_1, catalogId = "910244132")
val CODE_LIST_3 = CodeList(id = "678", name = "code list 3", description = "description of code list 3", codes = CODES_1, catalogId = "910244132")

val CODE_LIST_TO_BE_CREATED_0 = CodeListToBeCreated(name = "name", description = "description", codes = CODES)

val DESIGN_DTO = DesignDTO(backgroundColor = "#FFFFFF", fontColor = "#CCCFFF", logoDescription = "FDK Logo", hasLogo = false)
val DESIGN_DBO =
    DesignDBO(backgroundColor = "#FFFFFF", fontColor = "#CCCFFF", logoDescription = "FDK Logo", catalogId = "910244132")

val USER =
    User(name = "Test User", id = "123", catalogId = "910244132", email = "test@mail.com", telephoneNumber = "12345678")
val USER_TO_BE_CREATED = UserToBeCreated(name = "Test User", email = "test@mail.com", telephoneNumber = "12345678")

val LOGO = Logo(
    base64Logo = "PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/Pgo8IURPQ1RZUEUgc3ZnIFBVQkxJQyAiLS8vVzNDLy9EVEQgU1ZHIDEuMS8vRU4iICJodHRwOi8vd3d3LnczLm9yZy9HcmFwaGljcy9TVkcvMS4xL0RURC9zdmcxMS5kdGQiPgo8c3ZnIHZlcnNpb249IjEuMSIgYmFzZVByb2ZpbGU9ImZ1bGwiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CiAgICA8cG9seWdvbiBpZD0idHJpYW5nbGUiIHBvaW50cz0iMCwwIDAsNTAgNTAsMCIgZmlsbD0iIzAwOTkwMCIgc3Ryb2tlPSIjMDA0NDAwIi8+Cjwvc3ZnPg==",
    contentType = "image/svg+xml",
    catalogId = "910244132",
    filename = "test.svg"
)

val FIELD_0 = Field(
    id="field-0",
    catalogId = "910244132",
    label = NAME,
    description = NAME,
    type = FieldType.CODE_LIST,
    location = FieldLocation.RIGHT_COLUMN,
    codeListId = "678",
    enableFilter = null
)

val LIST_OF_CODE_LISTS_TO_BE_CREATED: List<CodeListToBeCreated> = listOf(
    CodeListToBeCreated(name = "name 1", description = "description", codes = CODES),
    CodeListToBeCreated(name = "name 2", description = "description", codes = CODES),
    CodeListToBeCreated(name = "name 3", description = "description", codes = CODES)
)

fun codeListPopulation(): List<Document> =
listOf(CODE_LIST_0, CODE_LIST_1, CODE_LIST_2, CODE_LIST_3)
    .map { it.mapDBO() }

fun internalFieldsPopulation(): List<Document> =
    listOf(FIELD_0)
        .map { it.mapDBO() }

fun editableFieldsPopulation(): List<Document> =
    listOf(
        EditableFields(catalogId = CODE_LIST_0.catalogId, domainCodeListId = CODE_LIST_0.id),
        EditableFields(catalogId = CODE_LIST_1.catalogId, domainCodeListId = CODE_LIST_1.id)
        ).map { it.mapDBO() }

private fun EditableFields.mapDBO(): Document =
    Document()
        .append("_id", catalogId)
        .append("domainCodeListId", domainCodeListId)

private fun CodeList.mapDBO(): Document =
    Document()
        .append("_id", id)
        .append("name", name)
        .append("catalogId", catalogId)
        .append("description", description)
        .append("codes", codes)

private fun Field.mapDBO(): Document =
    Document()
        .append("_id", id)
        .append("catalogId", catalogId)
        .append("label", label)
        .append("description", description)
        .append("type", type)
        .append("location", location)
        .append("codeListId", codeListId)

fun designPopulation(): List<Document> =
    listOf(DESIGN_DBO)
        .map { it.mapDBO() }
private fun DesignDBO.mapDBO(): Document =
    Document()
        .append("_id", catalogId)
        .append("backgroundColor", backgroundColor)
        .append("fontColor", fontColor)
        .append("logoDescription", logoDescription)
        .append("hasLogo", hasLogo)

fun logoPopulation(): List<Document> =
    listOf(LOGO)
        .map { it.mapDBO() }
private fun Logo.mapDBO(): Document =
    Document()
        .append("_id", catalogId)
        .append("contentType", contentType)
        .append("base64Logo", base64Logo)
        .append("filename", filename)

fun userPopulation(): List<Document> =
    listOf(USER)
        .map { it.mapDBO() }

private fun User.mapDBO(): Document =
    Document()
        .append("_id", id)
        .append("name", name)
        .append("catalogId", catalogId)
        .append("email", email)
        .append("telephoneNumber", telephoneNumber)

