package org.keycloak.social.steam;

import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.Config;

import org.keycloak.models.KeycloakSessionFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class SteamIdentityProviderFactory implements SocialIdentityProviderFactory<SteamIdentityProvider>{

    public static final String PROVIDER_ID = "steam";

    @Override
    public String getName() {
        return "Steam";
    }

    @Override
    public SteamIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new SteamIdentityProvider(session, new SteamIdentityProviderConfig(model));
    }

    @SuppressWarnings("unchecked")
	@Override
	public SteamIdentityProviderConfig createConfig() {
		return new SteamIdentityProviderConfig();
	}
    @Override
    public Map<String, String> parseConfig(KeycloakSession keycloakSession, InputStream inputStream) {
        return new HashMap<>();
    }
    @Override
    public SteamIdentityProvider create(KeycloakSession keycloakSession) {
        return null;
    }

    @Override
    public void init(Config.Scope scope) {
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {
    }


    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}