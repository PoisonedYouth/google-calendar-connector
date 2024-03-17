package de.dwins.calendar.infrastructure.adapter.input

import de.dwins.calendar.domain.usecase.GoogleCalendarUsecase
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class GoogleCalendarController(
    private val googleCalendarUsecase: GoogleCalendarUsecase
) {
    @GetMapping("/google-calendar/authorization/request")
    fun register(): ResponseEntity<String> {
        return ResponseEntity.ok(googleCalendarUsecase.getAuthorizationUrlForGoogleCalendarAccess())
    }

    @GetMapping("/google-calendar/authorization/callback")
    fun registerCallback(@RequestParam("code") code: String): ResponseEntity<String> {
        googleCalendarUsecase.authorizeCalendarSyncForUser(code)
        return ResponseEntity.ok("Successfully authorized.")
    }


    @PostMapping("/google-calendar/{googleId}/events/update")
    fun receiveUpdateEvent(@PathVariable("googleId") googleId: String, request: HttpServletRequest) {
        googleCalendarUsecase.synchronizeGoogleCalendarEvents(googleId)
    }
}

