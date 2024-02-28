**STATUS**: Draft

# ePA POC
Simple client library for ePA derived from the Gematik [epa-ps-sim](https://github.com/gematik/epa-ps-sim).

## Getting Started
1. Copy the required keystore
    ```shell
    cp ~/workspaces/ocs_configuration_ti_pta/RISE_Oviva001/vKon_Client_172.026.002.035.p12 ~/workspaces/epa-exporter/service/src/main/resources/keys
    ```
2. Setup the hardcoded config in [KonnektorServiceTest](./epa-poc/src/test/java/com/oviva/poc/KonnektorServiceTest.java)
3. Port-forward connectivity to the TI Konnektor and the RISE TI Client
4. Run the [KonnektorServiceTest](./epa-poc/src/test/java/com/oviva/poc/KonnektorServiceTest.java)

## Tests
Execute tests in the [KonnektorServiceTest](./epa-poc/src/test/java/com/oviva/poc/KonnektorServiceTest.java) class.

## Normative Documents
- [gematik/api-telematik](https://github.com/gematik/api-telematik/tree/OPB5)
- [gematik/api-ePA](https://github.com/gematik/api-ePA/tree/ePA-2.6)

## Wishlist
- read author institution from SMC-B, see [SmbInformationProvider](https://github.com/gematik/epa-ps-sim/blob/main/epa-ps-sim-lib/src/main/java/de/gematik/epa/konnektor/SmbInformationProvider.java)