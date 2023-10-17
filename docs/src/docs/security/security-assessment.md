# Security Assessment

**Assumption**: There is no central IRS catena-x service - IRS is meant to be run within one company. This assumption is crucial for this security assessment, because it reduces the attack surface significantly.

```mermaid
C4Context
title Item Relationship Service

Person_Ext(user, "Other services (User)")

System_Ext(DAPS, "DAPS / SSI MIW")
System_Ext(EDC, "EDC")
System_Ext(EDC-DS, "EDC Discovery Service")
System_Ext(DF, "Discovery Finder")
System_Ext(DTR, "Digital Twin Registry")
System_Ext(KC, "Keycloak")
System_Ext(BPDM, "BPDM")
System_Ext(SH, "Semantic Hub")

System_Ext(V, "Vault")

Boundary(IRSBoundary, "IRS") {
    System(IC, "Ingress Controller")
    System(IRS-API, "IRS-API", "REST")
    System(IRS-App, "IRS App")
    SystemDb(Min, "MinIO")
    SystemDb(ConfigH, "Config", "Helm")
    System(G, "Grafana")
    SystemDb(P, "Prometheus")
}

BiRel(IC, IRS-API, "https")
Rel(ConfigH, IRS-API, "")
Rel(ConfigH, Min, "")
Rel(IRS-App, Min, "")
BiRel(IRS-API, IRS-App, "")
Rel(IRS-App, P, "Performance logging")
Rel(G, P, "Get logs")
Rel(ConfigH, G, "")
Rel(ConfigH, P, "")

Rel(IRS-App, V, "Get secrets")

Rel(user, IC, "https, access token,request parameters, trigger events")
Rel(IRS-App, EDC, "https, access token")
Rel(IRS-App, EDC-DS, "Find decentral DTs, https, access token")
Rel(IRS-App, DF, "Get EDC Discovery Service URL, https, access token")
Rel(IRS-App, DTR, "https, access token")
Rel(IRS-App, KC, "https, clientID, clientSecret, Get tokens to access DTR")
Rel(IRS-App, BPDM, "https, access token, Get BPN")
Rel(IRS-App, SH, "Get schemas to validate response from EDC, https, access token")

Rel(DAPS, EDC, "")
```
