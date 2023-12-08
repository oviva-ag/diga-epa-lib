<img align="right" width="200" height="37" src="Gematik_Logo_Flag_With_Background.png" alt="Gematik Logo"/> <br/>

# Release notes ePA-PS-Sim

## Release 1.2.2

### fixed
- Updated releaseChain.jenkinsfile - use variable TAGNAME for publishing

## Release 1.2.1

### fixed
- Missing copyright header

## Release 1.2.0

### added
- Additional operation configureKonnektor
  - Change the configuration relevant for the communication with a Konnektor at runtime
- Additional operation readVSD
- Feature unlock SMC-B:
  - Automatically triggered SMC-B unlock during the start of the simulation
- Add folder code DIGA

### changed
- Moved the Apache CXF generation of the client-side 
  Konnektor Webservices implementation from the epa-ps-sim-app module to the epa-ps-sim-lib module
- Updated PHRManagementService on 2.5 Version

## Release 1.1.1

### fixed
- Wrong version information for lib-ihe-xds in pom

## Release 1.1.0

### added
- Additional operations:
  - replaceDocuments
  - permissionHcpo (a.k.a. requestFacilityAuthorization)
  - getAuthorizationState

## Release 1.0.0
- Initial version (internal only)
- Available operations:
  - putDocuments
  - getDocuments
  - find
  - deleteObjects

