# IRS configuration space
⚠️ This folder contains configuration for specific environments ⚠️

⚠️ Do not use for your own deployment ⚠️

## Structure
```
charts  
├── connector  
│   ├── aasregistry             -> helm chart for Digital Twin Registry
│   ├── bdrs-mock               -> helm chart for a BDRS mock
│   ├── discovery               -> helm chart for Discovery Finder and EDC Discovery mock
│   ├── edc-provider            -> helm chart for EDC with simple data backend
│   └── vault                   -> helm chart for hashicorp vault
├── irs-environments            -> helm charts for different environments
└── item-relationship-service   -> IRS helm chart (use main instead of this one)
```
