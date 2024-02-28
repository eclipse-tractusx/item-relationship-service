# #333 Validation of Policies with PolicyTemplates provided by PolicyHub  

| Key           | Value                                                                            |
|---------------|----------------------------------------------------------------------------------|
| Creation date | 22.02.2024                                                                       |
| Ticket Id     | [#333](https://github.com/eclipse-tractusx/item-relationship-service/issues/333) |    
| State         | WIP                                                                              | 

## Table of Contents

1. [Overview](#overview)
2. [Summary](#summary)
3. [Problem Statement](#problem-statement)
4. [Requirements](#requirements)
5. [NFR](#nfr)
6. [Out of scope](#out-of-scope)
7. [Assumptions](#assumptions)
8. [Concept](#concept)
9. [Glossary](#glossary)
10. [References](#references)

## Overview
The policy store must offer a level of security and reliability to ensure trust in this Policy Store component.   For this reason, the reliability of the Policy Store data must be improved and misuse or abuse must be prevented.

## Summary
Policy Store MUST only support permitted and compliant policies and, in the event of registration of a non-compliant policy, should provide the caller with assistance and instructions on how this can be corrected

## Problem Statement
In its current form, the Policy Store accepts any form of policy definition. There is no validation with regard to semantic or syntactic correctness.
This can lead to policies being misused or created incorrectly. This can permanently damage trust in the data chain.

## Requirements
- [ ] The policies accepted by the Policy Store must comply with the specifications defined by the Policy Hub.
- [ ] Each policy must be evaluated against a policy template and MUST only be accepted in case the entire policy meets the requirements of the template.

## NFR
- [ ] Performance: A user who wants to register a policy with a maximum of 10 constraints in the Policy Store will receive feedback on whether the policy is valid in less than a second.
- [ ] If a user registers one or more non-compliant policies, he receives a meaningful error message with instructions on how to correct the policy.
- [ ] OpenAPI 3.0 endpoints contain a sufficient and meaningful description, including instructions how to correct policy.
- [ ] Fail fast strategy for api endpoints is implemented. If several policies are registered and at least one policy is not valid, all policies are not accepted.

## Out of scope
- Fixing bugs in PolicyHub 
- Alignment with PolicyHub developers

## Assumptions
- The PolicyHub could be fully used to proof the conformity of any policy in the C-X network.

## Concept

### POST /irs/policies 
Creation of one or more policies for one or more BPNLs. 

````mermaid

sequenceDiagram
    %%{init: {'theme': 'dark', 'themeVariables': { 'fontSize': '15px'}}}%%
    autonumber

    actor TraceX
    participant PolicyStore
    participant PolicyValidator
    participant PolicyHub

    TraceX ->> PolicyStore : POST /irs/policies RequestBody policies
    PolicyStore ->> PolicyValidator : validate policies
    loop policies
        PolicyValidator --> PolicyValidator : Transform external policy to PolicyContentDefinition
        PolicyValidator ->>  PolicyHub  : POST  /api/policy-hub/policy-content with PolicyContentDefinition
        PolicyHub -->> PolicyValidator : response (PolicyTemplate)
        alt (valid response)
            PolicyValidator ->> PolicyValidator : Validate registered Policy with PolicyContentTemplate
            alt (policy valid)
                PolicyValidator -->> PolicyValidator : set policy state to valid
            else (policy invalid)
                PolicyValidator -->> PolicyValidator : set policy state to invalid + add message
            end
        else
           PolicyValidator -->> PolicyValidator : set policy state to invalid + add message 
        end
    end 
       alt (policies state is valid) 
         loop (policies)
            PolicyStore ->> PolicyStore: persist registered Policies
         end
          PolicyStore -->> TraceX : 201  Created Policy registration succeed.
       else
          PolicyStore -->> TraceX : 400 Policy registration failed. Response contains detailed error message.   
       end

````
1. Business app registers policy definition for BPNL
2. Validation for registered policies 
3. PolicyValidator extracts policy constraints from registered policy and creates PolicyContentDefinition   (Policy Content definition is the request body for POST /api/policy-hub/policy-content)
   31. Detect PolicyType (Usage)
   32. Detect Constraint Operand (And / Or)
   33. Detect Constraints with Key/Operator/Value combination
4. POST /api/policy-hub/policy-content with PolicyContentDefinition to receive PolicyContentTemplate 
5. Receive HTTP Status code 200 with PolicyTemplate or 400 in case of PolicyContentDefinition mismatch
6Response from  
6. Validate requested Policy with PolicyTemplate received from PolicyHub 
7. Set state of policy to valid in case of positive validation 
8. Set state of policy to valid in case of negative validation and generate error message with details 
9. Set state of policy to valid in case PolicyHub does not respond with 200 and generate error message with details
11. Store all policies with valid state 
12. Return 201 http code to caller
13. Return 400 http code  to caller with detailed error message for policyID

### Policy Validation 

| PolicyContentDefinition Request        | PolicyTemplate  Response | Policy              | Description                                                                                                                                               | 
|----------------------------------------|--------------------------|---------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------| 
| PolicyType (Usage/Access)              | action(use, access)      | action(use, access) | Mappring of correct type                                                                                                                                  |  
| ConstraintOperand (And/Or)             | odrl:and/:or             |odrl:and/:or| Logical Operand to use AND and OR is supported                                                                                                            |
| Constraints.Key                        | leftOperand              | leftOperand         |                     |                                                                                                                                      |
| Constraints.Operator (Equals, In, ...) | operator(eq,in,neq, ...) | operator  (eq,in,neq, ...)          |    |                                                                                                                                                       |  
| Constraints.Value Static               | rightOperand             | rightOperand        | For static value check right operand for dynamic value check if Policy.rightOperand.value is in PolicyTemplate.rightOperand.atrributes.key.possibleValues | 
| Constraints.Value Attributes           | rightOperand @attributes | rightOperand        | Dynamic value check if Policy.rightOperand.value is in PolicyTemplate.rightOperand.atrributes.key.possibleValues                                          | 

### Detailed error message 

#### 400 HTTP Error Code 

All policies with valid and invalid state are returned in case of any validation error. 

|Field|Type| Description                     | Example                                                                                 |
|---|---|---------------------------------|-----------------------------------------------------------------------------------------|
| message| String | Message with error cause        | "Cannot find representation of target resource."                                        | 
| statusCode| number | Http Status Code                | 400                                                                                     | 
| error | String | Policy validation error message | Policy for type Usage and technicalKeys FrameworkAgreement.traceabiliee does not exists |
| details | List<String> | Detailed information |                                                                                         |


##### Error case: 
- policyId: uuid of policy 
- message: Message containing detailed cause why policy is not valid 
- statusCode: Http Status Code 400
- error: Detailed error message 
- details: details provided by PolicyHub

##### Successful case:
- policyId: uuid of policy
- message:"Policy validation was successful but policy was not created."
- statusCode: Http Status Code 200 (ok but not CREATED)
- error: null
- details: empty array

````agsl
[
    {
         "policyId" : "e5392bb7-9f7b-4eaf-8324-a388d1ab15cf", 
         "messages": ["Policy for type Usage and technicalKeys FrameworkAgreement.traceabiliee does not exists"],
         "statusCode": 400,
         "error": "Cannot find representation of target resource.",
         "details": []
    },
    {
         "policyId" : "e5392bb7-9f7b-4eaf-8324-a388d1ab15cf", 
         "message": "Policy validation was successful but policy was not created.",
         "statusCode": 200,
         "error": "{}"
         "details": []
    }
    
]

````

## PolicyContentDefinition

### Example 1

`````
POST {{baseUrl}}/api/policy-hub/policy-content

{
"PolicyType": "Usage",
"ConstraintOperand": "And",
"Constraints": [
    {
        "Key": "FrameworkAgreement.traceability",
        "Operator": "Equals"
    },
    {
         "Key": "purpose",
         "Operator": "Equals",
         "Value": "ID 3.1 Trace"
    },
    {
        "Key": "BusinessPartnerNumber",
        "Operator": "Equals",
        "Value": "BPNL00000003CRHK"
    },
    {
        "Key": "Membership",
        "Operator": "Equals",
        "Value": "active"
    }
    ]
}
`````



### Example 2 

`````
{
   "PolicyType": "Usage",
   "ConstraintOperand": "And",
   "Constraints": [
    {
      "Key": "FrameworkAgreement.traceability",
      "Operator": "Equals",
      "Value" : "active:1.1"
    },
    {
      "Key": "companyRole.dismantler",
      "Operator": "In"
    },
    {
       "Key": "BusinessPartnerNumber",
       "Operator": "Equals",
       "Value": "BPNL00000003CRHK"
    },
    {
      "Key": "purpose",
      "Operator": "Equals"
    },
    {
       "Key": "Membership",
       "Operator": "Equals"
    }
   ]
}
`````

#### Policies 


* Type
  * Access
  * Usage
  * Purpose


* UseCase
  * Traceability
  * Quality
  * PCF
  * Behavioraltwin
  * Sustainability


## Glossary

| Abbreviation | Name                    | Description                                             |
|--------------|-------------------------|---------------------------------------------------------|
|              | PolicyTemplate          | Response of endpoint POST /api/plicy-hub/policy-content | 
|              | PolicyContentDefinition | DSL introduced by PolicyHub| 

## References
* [Swagger Policy Hub](https://policy-hub.dev.demo.catena-x.net/api/policy-hub/swagger/index.html)
* [API Endpoint usage](https://github.com/eclipse-tractusx/policy-hub/blob/feature/request-docu/docs/developer/Technical-Documentation/requests/example-requests.md)