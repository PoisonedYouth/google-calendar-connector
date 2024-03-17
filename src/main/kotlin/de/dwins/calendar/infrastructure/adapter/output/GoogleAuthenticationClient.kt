package de.dwins.calendar.infrastructure.adapter.output

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl
import com.google.api.client.auth.oauth2.BearerToken
import com.google.api.client.auth.oauth2.ClientParametersAuthentication
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.CalendarScopes
import de.dwins.calendar.application.port.output.UserOutputPort
import de.dwins.calendar.infrastructure.configuration.CalendarConnectionProperties
import java.util.*

class GoogleAuthenticationClient(
        private val calendarConnectionProperties: CalendarConnectionProperties,
        private val userOutputPort: UserOutputPort,
) : GoogleAuthenticationConnector {

    private val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private val redirectionUrl: String = "${calendarConnectionProperties.redirectBaseUri}/google-calendar/authorization/callback"

    private val authorizationCodeFlow: GoogleAuthorizationCodeFlow by lazy {
        GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                jsonFactory,
                calendarConnectionProperties.clientId,
                calendarConnectionProperties.clientSecret,
                // Scopes that are necessary for accessing calendar and get personal information like email and googleId
                listOf(CalendarScopes.CALENDAR, "openid", "profile", "email"),
        )
                // This is necessary to get a refresh token for regularly updating the access token
                .setAccessType("offline")
                .build()
    }

    override fun getAuthorizationUrl(): String {
        val authorizationUrl: AuthorizationCodeRequestUrl =
                authorizationCodeFlow
                        .newAuthorizationUrl()
                        .setRedirectUri(redirectionUrl)
        return authorizationUrl.build()
    }

    override fun getAuthenticatedUser(code: String): User {
        val response: GoogleTokenResponse = authorizationCodeFlow.newTokenRequest(
                code
        ).setRedirectUri(redirectionUrl)
                .execute()
        val verifier = GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
                .setAudience(Collections.singletonList(calendarConnectionProperties.clientId)).build()

        val parsedToken = verifier.verify(response.idToken)
        return User(
                googleId = parsedToken.payload.subject,
                primaryMail = parsedToken.payload.email,
                refreshToken = response.refreshToken,
                webhookRegistered = null,
        )
    }

    override fun initialzeAccessCredentials(user: User): Credential {
        val credentialBuilder = Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setTokenServerEncodedUrl(GenericUrl(GoogleOAuthConstants.TOKEN_SERVER_URL).build())
                .setClientAuthentication(
                        ClientParametersAuthentication(
                                calendarConnectionProperties.clientId,
                                calendarConnectionProperties.clientSecret,
                        ),
                )
        val credential = credentialBuilder.build()
        credential.setRefreshToken(user.refreshToken)

        handleUpdateAccessToken(credential, user)
        return credential
    }


    private fun handleUpdateAccessToken(
            credential: Credential,
            user: User,
    ) {
        val successfulTokenUpdate = credential.refreshToken()
        if (!successfulTokenUpdate) {
            error("Failed to update access token for consultant with googleId '${user.googleId}'")
        } else {
            userOutputPort.update(
                    user.copy(
                            refreshToken = credential.refreshToken
                                    ?: user.refreshToken,
                    ),
            )
        }
    }
}