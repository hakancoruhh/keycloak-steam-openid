package org.keycloak.social.steam;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;


public class SteamIdentityProviderConfig extends OAuth2IdentityProviderConfig {

    public SteamIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    public SteamIdentityProviderConfig() {
    }


    public String getSteamApiKey() {
        return getConfig().get("steamApiKey");
    }

    public void setSteamApiKey(String key) {
        getConfig().put("steamApiKey", key);
    }

    public String getRealmName() {
        return getConfig().get("realmName");
    }

    public void setRealmName(String key) {
        getConfig().put("realmName", key);
    }

   
}

