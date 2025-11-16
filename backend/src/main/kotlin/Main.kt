import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import routes.apiRoutes
import service.FirebaseInit

fun main() {
    // TODO("Ask Mailles for service account key")
    // FirebaseInit.init() // Commented out - not needed for weather endpoint
    mu.KotlinLogging.logger("Starting server on Kotlin port 8080...")

    embeddedServer(
        Netty, port = 8080, module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    install(CORS) {
        allowHost("localhost:9002")
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
    }
    apiRoutes()
}
