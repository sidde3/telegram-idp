package org.demo.provider;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.*;
import lombok.extern.jbosslog.JBossLog;
import org.demo.AuthenticationException;
import org.demo.Constraint;
import org.demo.util.SignatureUtil;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider.AuthenticationCallback;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

@JBossLog
public class TelegramLoginEndpoint {
    private final KeycloakSession session;
    private final AuthenticationCallback callback;
    private final TelegramIdentityProvider provider;
    private final EventBuilder event;
    private final String botToken;
    public TelegramLoginEndpoint(KeycloakSession session, TelegramIdentityProvider telegramIdentityProvider, AuthenticationCallback callback, EventBuilder event) {
        this.session = session;
        this.provider = telegramIdentityProvider;
        this.callback = callback;
        this.event = event;
        this.botToken = provider.getConfig().getConfig().get(Constraint.BOT_TOKEN);
    }
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authResponse(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        log.debugf("Query Parameter: %s", queryParams);
        event.event(EventType.LOGIN);
        try {
            String sessionId = queryParams.getFirst("t");
            AuthenticationSessionModel authSession = restoreAuthenticationSession(sessionId);
            String sig = authSession.getAuthNote("signature");
            log.debugf("Authentication session Note: %s, Client: %s", sig, authSession.getClient());
            String sessionData = SignatureUtil.getInstance().validateSignature(sessionId,sig, botToken);
            SignatureUtil.getInstance().validateTelegramDataHash(queryParams, botToken);
            BrokeredIdentityContext identityContext = createBrokeredIdentityContext(queryParams);
            identityContext.setAuthenticationSession(authSession);
            return callback.authenticated(identityContext);
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }

    }

    private AuthenticationSessionModel restoreAuthenticationSession(String sessionData) throws AuthenticationException {
        RealmModel realm = this.session.getContext().getRealm();
        if (realm == null) {
            throw new AuthenticationException("Realm context not found.", "internal_error", "An internal server error occurred.");
        }
        String[] parts = sessionData.split("_");

        String rootSessionId = parts[0];
        String tabId = parts[1];

        RootAuthenticationSessionModel rootAuthSession = this.session.authenticationSessions().getRootAuthenticationSession(realm, rootSessionId);
        if (rootAuthSession == null) {
            throw new AuthenticationException("Root authentication session not found.", "session_not_found", "Your login session has expired or is invalid. Please try again.");
        }

        AuthenticationSessionModel authSession = rootAuthSession.getAuthenticationSessions().get(tabId);
        if (authSession == null) {
            throw new AuthenticationException("Authentication session not found.", "session_not_found", "Your login session could not be found. Please try again.");
        }

        return authSession;
    }

    private BrokeredIdentityContext createBrokeredIdentityContext(MultivaluedMap<String, String> queryParams) {
        String id = queryParams.getFirst("id");
        String username = queryParams.getFirst("username");
        String firstName = queryParams.getFirst("first_name");
        String lastName = queryParams.getFirst("last_name");
        String photoUrl = queryParams.getFirst("photo_url");

        BrokeredIdentityContext context = getBrokeredIdentityContext(id, firstName, lastName);

        // Make the original, human-readable username available for mappers
        if (username != null && !username.isBlank()) {
            context.setUserAttribute("username", username);
        }
        if (photoUrl != null && !photoUrl.isBlank()) {
            context.setUserAttribute("picture", photoUrl);
        }

        return context;
    }

    private BrokeredIdentityContext getBrokeredIdentityContext(String id, String firstName, String lastName) {
        String prefixedId = Constraint.ID_PREFIX + id;
        BrokeredIdentityContext context = new BrokeredIdentityContext(prefixedId, this.provider.getConfig());

        // Use the unique prefixed ID as the Keycloak username to prevent linking
        context.setUsername(prefixedId);
        context.setModelUsername(prefixedId);

        context.setFirstName(firstName);
        context.setLastName(lastName);
        context.setEmail(null);
        context.setBrokerUserId(prefixedId);
        context.setIdp(provider);
        return context;
    }
}
