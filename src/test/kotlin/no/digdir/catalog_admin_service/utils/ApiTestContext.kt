package no.digdir.catalog_admin_service.utils

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

abstract class ApiTestContext {

    @LocalServerPort
    var port = 0

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                "spring.data.mongodb.uri=mongodb://$MONGO_USER:$MONGO_PASSWORD@localhost:${mongoContainer.getMappedPort(MONGO_PORT)}/$MONGO_COLLECTION?authSource=admin&authMechanism=SCRAM-SHA-1"
            ).applyTo(configurableApplicationContext.environment)
        }
    }

    companion object {
        var mongoContainer: KGenericContainer

        init {
            mongoContainer = KGenericContainer("mongo:latest")
                .withEnv(MONGO_ENV_VALUES)
                .withExposedPorts(MONGO_PORT)
                .waitingFor(Wait.forListeningPort())

            mongoContainer.start()

            resetDB()
        }
    }
}

// Hack needed because testcontainers use of generics confuses Kotlin
class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)