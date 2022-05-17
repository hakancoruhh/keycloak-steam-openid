package org.keycloak.social.steam;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;

public class SteamUserAttributeMapper extends AbstractJsonUserAttributeMapper {

	private static final String[] cp = new String[] { SteamIdentityProviderFactory.PROVIDER_ID };

	@Override
	public String[] getCompatibleProviders() {
		return cp;
	}

	@Override
	public String getId() {
		return "steam-user-attribute-mapper";
	}

}
