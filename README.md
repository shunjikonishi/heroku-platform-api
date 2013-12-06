# Heroku Platform API

## Overview
Java wrapper of Heroku Platform API.

https://devcenter.heroku.com/articles/platform-api-reference

## Implemented features
- RateLimitRating
- Account
- AccountFeature
- Addon
- AddonService
- App
- AppFeature
- AppTransfer
- Collaborator
- Config
- Domain
- Dyno
- Formation
- Key
- LogDrain
- LogSession
- OAuthAuthorization
- OAuthClient
- OAuthToken
- Plan
- Region
- Release
- SSLEndpoint
- Stack

## Not implemented features
None(2013-12-06)

## Usage
[JavaDoc](http://oss.flect.co.jp/apidocs/heroku-platform-api/index.html)

    //Get the url of Heroku authentication page.
    String oauthUrl = PlatformApi.getOAuthUrl("&lt;YOUR_CLIENT_ID>", Scope.Global);
    
    //... Authenticate and get code.
    
    PlatformApi api = PlatformApi.fromOAuth("&lt;YOUR_CLIENT_SECRET>", code);
    
    //Get application list
    List<App> appList = api.getAppList();
    App app = appList.get(0);
    
    //heroku ps:scale web=5
    Formation api.updateFormation(app.getName(), "web", 5, 1);
    
    //heroku ps:restart
    api.restart(app.getName());

## License
MIT