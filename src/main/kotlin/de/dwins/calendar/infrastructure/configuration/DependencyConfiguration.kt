package de.dwins.calendar.infrastructure.configuration

import de.dwins.calendar.application.port.input.GoogleCalendarService
import de.dwins.calendar.application.port.output.GoogleCalendarEventOutputPort
import de.dwins.calendar.application.port.output.UserOutputPort
import de.dwins.calendar.domain.usecase.GoogleCalendarUsecase
import de.dwins.calendar.infrastructure.adapter.output.*
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class DependencyConfiguration(
        @Value("\${clientId}") private val clientId: String,
        @Value("\${clientSecret}") private val clientSecret: String,
        @Value("\${redirectBaseUri}") private val redirectBaseUri: String,

        ) {

    @Bean
    fun googleCalendarEventOutputPort(entityManager: EntityManager): GoogleCalendarEventOutputPort {
        return GoogleCalendarEventRepository(entityManager)
    }

    @Bean
    fun userOutputPort(entityManager: EntityManager): UserOutputPort {
        return UserRepository(entityManager)
    }

    @Bean
    fun googleAuthenticationConnector(
            storedUserOutputPort: UserOutputPort,
    ): GoogleAuthenticationConnector {
        return GoogleAuthenticationClient(
                userOutputPort = storedUserOutputPort,
                calendarConnectionProperties = CalendarConnectionProperties(
                        clientId = clientId,
                        clientSecret = clientSecret,
                        redirectBaseUri = redirectBaseUri
                )
        )
    }

    @Bean
    fun googleCalendarConntector(authenticationConnector: GoogleAuthenticationConnector, storedUserOutputPort: UserOutputPort): GoogleCalendarConntector {
        return GoogleCalendarClient(googleAuthenticationConnector = authenticationConnector,
                userOutputPort = storedUserOutputPort,
                calendarConnectionProperties = CalendarConnectionProperties(
                        clientId = clientId,
                        clientSecret = clientSecret,
                        redirectBaseUri = redirectBaseUri
                )
        )
    }

    @Bean
    fun GoogleCalendarUsecase(
            googleCalendarConntector: GoogleCalendarConntector,
            googleAuthenticationConnector: GoogleAuthenticationConnector,
            userOutputPort: UserOutputPort,
            calendarEventOutputPort: GoogleCalendarEventOutputPort
    ): GoogleCalendarUsecase {
        return GoogleCalendarService(
                googleCalendarConntector = googleCalendarConntector,
                googleAuthenticationConnector = googleAuthenticationConnector,
                userOutputPort = userOutputPort,
                calendarEventOutputPort = calendarEventOutputPort
        )
    }
}

data class CalendarConnectionProperties(
        val clientId: String,
        val clientSecret: String,
        val redirectBaseUri: String
)