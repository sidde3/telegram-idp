package org.demo.provider;

import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.demo.Constraint;
import org.demo.util.SignatureUtil;
import org.keycloak.broker.provider.*;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.*;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.net.URI;

@JBossLog
public class TelegramIdentityProvider extends AbstractIdentityProvider{

    public TelegramIdentityProvider(KeycloakSession session, TelegramIdentityProviderConfig config){
        super(session,config);
    }
    @Override
    public Response performLogin(AuthenticationRequest request) {
        try {
        AuthenticationSessionModel authSession = request.getAuthenticationSession();
        String secretKey = getConfig().getConfig().get(Constraint.BOT_TOKEN);
        /*RealmModel realm = request.getRealm();
        ClientSessionCode<AuthenticationSessionModel> clientSessionCode = new ClientSessionCode<>(
                session, realm, authSession
        );
        clientSessionCode.setAction(ClientSessionCode.ActionType.LOGIN.name());*/
        //String loginCode = clientSessionCode.getOrGenerateCode();
        //log.infof("Login Code: %s, Client: %s", loginCode, authSession.getClient());
        //String sig = SignatureUtil.generateSecureSignature(loginCode, secretKey);
        String rootSessionId = authSession.getParentSession().getId();
        String tabId = authSession.getTabId();
        //String redirectUri = request.getRedirectUri();
        String sessionData = rootSessionId + "_" + tabId;
        String sig = SignatureUtil.generateSecureSignature(sessionData, secretKey);
        authSession.setAuthNote("signature", sig);
        String tgParam =  sessionData;
        log.debugf("Signature: %s, Session Data: %s, Token: %s", sig, sessionData, secretKey);
        String botUsername = getConfig().getConfig().get(Constraint.BOT_USER);
        String redirectUrl = String.format(getConfig().getConfig().get(Constraint.TELEGRAM_URL), botUsername, tgParam);
        log.debugf("Telegram Url: %s", redirectUrl);
        return Response.seeOther(URI.create(redirectUrl)).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Response retrieveToken(KeycloakSession session, FederatedIdentityModel identity) {
        return null;
    }

    public Object  callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new TelegramLoginEndpoint(session, this, callback, event);
    }

}
