package no.digdir.catalog_admin_service.utils

import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.net.HttpURLConnection
import java.net.URL
import org.junit.jupiter.api.BeforeEach

abstract class ApiTestContext {

    @LocalServerPort
    var port = 0

    @BeforeEach
    fun resetDatabase() {
        resetDB()
    }

    companion object {
        @JvmStatic
        val mongoContainer: KGenericContainer = KGenericContainer("mongo:latest")
            .withEnv(MONGO_ENV_VALUES)
            .withExposedPorts(MONGO_PORT)
            .waitingFor(Wait.forListeningPort())

        init {
            startMockServer()
            mongoContainer.start()


            resetDB()

            try {
                val con = URL("http://localhost:5050/ping").openConnection() as HttpURLConnection
                con.connect()
                if (con.responseCode != 200) {
                    stopMockServer()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                stopMockServer()
            }
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.mongodb.uri") {
                "mongodb://$MONGO_USER:$MONGO_PASSWORD@localhost:${mongoContainer.getMappedPort(MONGO_PORT)}/$MONGO_DATABASE?authSource=admin"
            }
        }
    }
}

// Hack needed because test containers use of generics confuses Kotlin
class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)
