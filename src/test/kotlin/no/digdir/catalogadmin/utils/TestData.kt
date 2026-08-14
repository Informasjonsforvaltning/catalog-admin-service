package no.digdir.catalogadmin.utils

import no.digdir.catalogadmin.model.Code
import no.digdir.catalogadmin.model.CodeList
import no.digdir.catalogadmin.model.CodeListToBeCreated
import no.digdir.catalogadmin.model.DesignDBO
import no.digdir.catalogadmin.model.DesignDTO
import no.digdir.catalogadmin.model.EditableFields
import no.digdir.catalogadmin.model.Field
import no.digdir.catalogadmin.model.FieldLocation
import no.digdir.catalogadmin.model.FieldType
import no.digdir.catalogadmin.model.Logo
import no.digdir.catalogadmin.model.MultiLanguageTexts
import no.digdir.catalogadmin.model.User
import no.digdir.catalogadmin.model.UserToBeCreated

const val DB_USER = "testuser"
const val DB_PASSWORD = "testpassword"
const val DB_NAME = "catalog_admin"

val NAME: MultiLanguageTexts = MultiLanguageTexts(en = "codeName", nb = null, nn = null)
val CODE: Code = Code(id = "555", name = NAME, parentID = null)
val CODES: List<Code> = listOf(CODE)
val CODE_LIST_0 =
    CodeList(id = "123", name = "name", description = "description", codes = CODES, catalogId = "910244132")
val CODES_1 =
    listOf(
        Code("1", MultiLanguageTexts("nb 1", "nn 1", "en 1"), null),
        Code("2", MultiLanguageTexts("nb 2", "nn 2", "en 2"), "1"),
        Code("3", MultiLanguageTexts("nb 3", "nn 3", null), "1"),
    )
val CODE_LIST_1 =
    CodeList(id = "321", name = "code list 1", description = "description of code list 1", codes = CODES_1, catalogId = "123456789")
val CODE_LIST_2 =
    CodeList(id = "456", name = "code list 2", description = "description of code list 2", codes = CODES_1, catalogId = "910244132")
val CODE_LIST_3 =
    CodeList(id = "678", name = "code list 3", description = "description of code list 3", codes = CODES_1, catalogId = "910244132")

val CODE_LIST_TO_BE_CREATED_0 = CodeListToBeCreated(name = "name", description = "description", codes = CODES)

val DESIGN_DTO = DesignDTO(backgroundColor = "#FFFFFF", fontColor = "#CCCFFF", logoDescription = "FDK Logo", hasLogo = false)
val DESIGN_DBO =
    DesignDBO(backgroundColor = "#FFFFFF", fontColor = "#CCCFFF", logoDescription = "FDK Logo", catalogId = "910244132", hasLogo = false)

val USER =
    User(name = "Test User", id = "123", catalogId = "910244132", email = "test@mail.com", telephoneNumber = "12345678")
val USER_TO_BE_CREATED = UserToBeCreated(name = "Test User", email = "test@mail.com", telephoneNumber = "12345678")

// The base64 payload is an opaque blob that cannot be wrapped.
@Suppress("ktlint:standard:max-line-length")
val LOGO =
    Logo(
        base64Logo = "PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/Pgo8IURPQ1RZUEUgc3ZnIFBVQkxJQyAiLS8vVzNDLy9EVEQgU1ZHIDEuMS8vRU4iICJodHRwOi8vd3d3LnczLm9yZy9HcmFwaGljcy9TVkcvMS4xL0RURC9zdmcxMS5kdGQiPgo8c3ZnIHZlcnNpb249IjEuMSIgYmFzZVByb2ZpbGU9ImZ1bGwiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CiAgICA8cG9seWdvbiBpZD0idHJpYW5nbGUiIHBvaW50cz0iMCwwIDAsNTAgNTAsMCIgZmlsbD0iIzAwOTkwMCIgc3Ryb2tlPSIjMDA0NDAwIi8+Cjwvc3ZnPg==",
        contentType = "image/svg+xml",
        catalogId = "910244132",
        filename = "test.svg",
    )

val FIELD_0 =
    Field(
        id = "field-0",
        catalogId = "910244132",
        label = NAME,
        description = NAME,
        type = FieldType.CODE_LIST,
        location = FieldLocation.RIGHT_COLUMN,
        codeListId = "678",
        enableFilter = null,
    )

val LIST_OF_CODE_LISTS_TO_BE_CREATED: List<CodeListToBeCreated> =
    listOf(
        CodeListToBeCreated(name = "name 1", description = "description", codes = CODES),
        CodeListToBeCreated(name = "name 2", description = "description", codes = CODES),
        CodeListToBeCreated(name = "name 3", description = "description", codes = CODES),
    )
