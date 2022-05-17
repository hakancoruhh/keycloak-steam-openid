package org.keycloak.social.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.logging.Logger;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.*;
import org.keycloak.sessions.AuthenticationSessionModel;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.keycloak.broker.provider.*;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import java.net.URI;
import org.keycloak.common.ClientConnection;
import org.keycloak.util.JsonSerialization;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.keycloak.events.EventType;
import org.keycloak.events.Errors;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.messages.Messages;
import javax.ws.rs.WebApplicationException;



public class SteamIdentityProvider extends AbstractIdentityProvider<SteamIdentityProviderConfig>
        implements SocialIdentityProvider<SteamIdentityProviderConfig> {

    private static final Logger log = Logger.getLogger(SteamIdentityProvider.class);
    public static final String AUTH_URL = "https://steamcommunity.com/openid/login";
    public static final String OPENID_NS = "http://specs.openid.net/auth/2.0";

    public static final String OPENID_IDENTITY = "http://specs.openid.net/auth/2.0/identifier_select";
    public static final String OPENID_CLAIMED_ID = "http://specs.openid.net/auth/2.0/identifier_select";

    public static final String PROFILE_URL = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/";

    public SteamIdentityProvider(KeycloakSession session, SteamIdentityProviderConfig config) {
        super(session, config);
        config.setAuthorizationUrl(AUTH_URL);
        config.setUserInfoUrl(PROFILE_URL);
    }

    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new Endpoint(callback, realm, event);
    }

    @Override
    public Response retrieveToken(KeycloakSession keycloakSession, FederatedIdentityModel federatedIdentityModel) {
        return null;
    }

    public Response keycloakInitiatedBrowserLogout(KeycloakSession session, UserSessionModel userSession, UriInfo uriInfo, RealmModel realm) {
        return null;
    }

    public Response performLogin(AuthenticationRequest request) {    
        URI uri = UriBuilder.fromUri(AUTH_URL)
                .scheme("https")
                .queryParam("openid.ns", OPENID_NS)
                .queryParam("openid.assoc_handle", request.getState().getEncoded())
                .queryParam("openid.mode", "checkid_setup")
                .queryParam("openid.return_to", request.getRedirectUri() + "?state=" + request.getState().getEncoded())
                .queryParam("openid.realm", getConfig().getRealmName())
                .queryParam("openid.identity", OPENID_IDENTITY)
                .queryParam("openid.claimed_id", OPENID_CLAIMED_ID)
                .build();
        return Response.seeOther(uri).build();
    }

    protected class Endpoint {
        protected AuthenticationCallback callback;
        protected RealmModel realm;
        protected EventBuilder event;

        @Context
        protected KeycloakSession session;

        @Context
        protected ClientConnection clientConnection;

        @Context
        protected HttpHeaders headers;

        public Endpoint(AuthenticationCallback callback, RealmModel realm, EventBuilder event) {
            this.callback = callback;
            this.realm = realm;
            this.event = event;
        }

        private Response errorIdentityProviderLogin(String message) {
            event.event(EventType.IDENTITY_PROVIDER_LOGIN);
            event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
            return ErrorPage.error(session, null, Response.Status.BAD_GATEWAY, message);
        }

        @GET
        public Response authResponse(@QueryParam("state") String state,
                                     @QueryParam("openid.assoc_handle") String handle,
                                     @QueryParam("openid.signed") String signed,
                                     @QueryParam("openid.sig") String sig,
                                     @QueryParam("openid.mode") String mode,
                                     @QueryParam("openid.op_endpoint") String endpoint,
                                     @QueryParam("openid.claimed_id") String claimedId,
                                     @QueryParam("openid.identity") String identity,
                                     @QueryParam("openid.return_to") String returnTo,
                                     @QueryParam("openid.response_nonce") String responseNonce,
                                     @QueryParam("openid.invalidate_handle") String invalidateHandle) {

            if (state == null) {
                return errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_MISSING_STATE_ERROR);
            }

            try {
                AuthenticationSessionModel authSession = this.callback.getAndVerifyAuthenticationSession(state);
                session.getContext().setAuthenticationSession(authSession);

                Pattern p = Pattern.compile("https?://steamcommunity.com/openid/id/([0-9]{17,25})");
                Matcher matcher = p.matcher(identity);
                String steamId;
                if (matcher.matches()) {
                    steamId = matcher.group(1);
                } else {
                    return callback.error("could not determine SteamID");
                }

                JsonNode userJson = SimpleHttp.doGet(PROFILE_URL, session)
                .param("key", getConfig().getSteamApiKey())
                .param("steamids", steamId)
                .asJson();
                String userJsonString = userJson.get("response").get("players").get(0).toString();
                User user = JsonSerialization.readValue(userJsonString, User.class);
            
                BrokeredIdentityContext federatedIdentity = new  BrokeredIdentityContext(steamId) ;
                federatedIdentity.setIdpConfig(getConfig());
                federatedIdentity.setIdp(SteamIdentityProvider.this);
                federatedIdentity.setAuthenticationSession(authSession);
                federatedIdentity.setBrokerUserId(user.steamid);
                federatedIdentity.setUsername(user.personaname);
                return callback.authenticated(federatedIdentity);

            } catch (WebApplicationException e) {
                return e.getResponse();
            } catch (Exception e) {
                log.error("Failed to make identity provider oauth callback", e);
            }
            return errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
            
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class User {
        public String steamid;
        public Integer communityvisibilitystate;
        public Integer profilestate;
        public String personaname;
        public String profileurl;
        public String avatar;
        public String avatarmedium;
        public String avatarfull;
        public String avatarhash;
        public Integer lastlogoff;
        public Integer personastate;
        public String primaryclanid;
        public Integer timecreated;
        public Integer personastateflags;
    }
}



