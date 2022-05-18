# keycloak-steam-openid

Keycloak Social Login extension for Steam.


## Install

Download `keycloak-steam-openid-<version>.jar` from Master Release.
Then deploy it into `$KEYCLOAK_HOME/providers` directory.


### Steam
Access to [Steam Developer Portal](https://steamcommunity.com/dev/apikey) assign your domain name (aka realm name).
Also your domain must be base of redirect Redirect URI
> :warning: **Keycloak realm name is different thing**
Then you can get Web API Key.

### Steam


## Source Build

Clone this repository and run `mvn package`.

You can see `keycloak-steam-openid-<version>.jar` under `target` directory.

## Licence

[MIT licensed](./LICENSE).

## Author

- [Hiroyuki Wada](https://github.com/hakancoruhh)
