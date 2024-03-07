**STATUS**: Draft

# ePA POC
Simple client library for ePA derived from the Gematik [epa-ps-sim](https://github.com/gematik/epa-ps-sim).

## Getting Started
1. Copy the required keystore
    ```shell
    cp ~/workspaces/ocs_configuration_ti_pta/RISE_Oviva001/vKon_Client_172.026.002.035.p12 ~/workspaces/diga-epa-lib/diga-epa-client/src/main/resources/keys
    ```
2. Run the tests in the [KonnektorServiceTest](diga-epa-client/src/test/java/com/oviva/epa/client/KonnektorServiceTest.java) class
   * You should be able to write an example file in the test environment (RU).

## Normative Documents
- [gematik/api-telematik](https://github.com/gematik/api-telematik/tree/OPB5)
- [gematik/api-ePA](https://github.com/gematik/api-ePA/tree/ePA-2.6)

## Wishlist
- remove the endpoint re-mapping hacks in the [KonnektorConnectionFactory](https://github.com/oviva-ag/diga-epa-lib/blob/main/diga-epa-client/src/main/java/com/oviva/poc/konn/KonnektorConnectionFactory.java#L334)
- `health` check on `KonnektorConnection`
- bonus: automatically monitor and re-connect the card-terminal and pin verification when necessary
