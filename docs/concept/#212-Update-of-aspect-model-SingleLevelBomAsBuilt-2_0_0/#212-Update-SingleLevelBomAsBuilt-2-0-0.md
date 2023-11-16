# Overview

# Problem Statement
SingleLevelBomAsBuilt 2.0.0 introduces a new property with boolean values called hasAlternatives.
The model now allows to reference items that are not available as AsBuilt Twins - e.g. Serial items or batches) but as AsPlanned twins.
Sometimes, however, it is not known what exact AsPlanned twin is needed to build the BoM asBuilt because there are different alternative twins to choose from.
In that case, all potential alternatives have to be referenced. To indicate that ambiguity the property "hasAlternative" has been added to the aspect model.
Property hasAlternative: Expresses weather the part is built-in or weather it is one of several options.

If the value is false, it can be assumed this exact part is built-in.
If the value is true, it is unknown weather this or an alternative part is built-in.

This is the case when, e.g. the same item is supplied by two suppliers, the item is only tracked by a customer part ID during assembly.
Thus, these items can not be differentiated from each other.

manufacturerId (BPN) of child items is now mandatory
Description of the main property 'SingleLevelBomAsBuild' was adapted to the new requirements of the model.


# Specification

| Artefact              | Version | Specification                                                                                                                                                                                                                                                                                                                                                                                                                               |
|-----------------------|---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Use Case              |         | Traceability                                                                                                                                                                                                                                                                                                                                                                                                                                |
| BomLifecycle          |         | asBuilt                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| Use Cases             |          | CE, DPP, Trace-X (Traceability)                                                                                                                                                                                                                                                                                                                                                                                                             |
| SingleLevelBomAsBuilt | 2.0.0   | [SingleLevelBomAsBuilt2.0.0](https://github.com/eclipse-tractusx/sldt-semantic-models/blob/main/io.catenax.single_level_bom_as_built/2.0.0/gen/SingleLevelBomAsBuilt.json)                                                                                                                                                                                                                                                                  |
| Propery hasAlternatives| boolean | Expresses wether the part is built-in or wether it is one of several options. If the value is false, it can be assumend this exact item is built-in. If the value is true, it is unknown wether this or an alternative item is built-in. This is the case when, e.g. the same item is supplied by two suppliers, the item is only tracked by a customer part ID during assembly. Thus, these items can not be differentiated from each other. |

|Precondition | Condition  | 
|---|---|
|  hasAlternative = true| Unkown if part is built-in OR alternative part is built-in |
|  hasAlternative = false| exact part is built-in |


# Glossary

| Term                   | Description                                                  |
|------------------------|--------------------------------------------------------------|
| (Part) Instance  | Part Instance (twin) a concrete instance of a part - asBuilt |    
| (Part) Type            | Specification of a part (twin) - asPlanned                   |                   

# Assumption 
* Property hasAlternatives is only added to SingleLevelBomAsBuilt aspect. Upstream relation will not be extended with property hasAlternatives.
* The property is only for the asBuilt Lifecycle and for no other lifecycles. 
* The switch of BoMLifecyle is only unidirectional from asBuilt to asPlanned (no way back)
* Business apps and use cases do not have the requirement to continue traversing the asPlanned structure after switching. The return of the semantic models such as (PartAsPlanned) is sufficient.  

# Requirements
* [ ] IRS validates the childItems array in SingleLevelBomAsBuilt2.0.0 and reports tombstone in case the array does contain plausible entries.
* [ ] IRS has the capability to switch bomLifeCyle (only for asBuilt) to provide asPlanned parts  in a mixed stucture.
* [ ] 

# Non functional requirements

# Concept

## SingleLevelBomAsBuilt2 childItems contains asBuilt aspects hasAlternatives=false

1. Normal case as IRS works as designed. 
1. Traversal Aspect: SingleLevelBomAsBuilt
1. Semantic Aspects: Provisioning of bomLifeCycle asBuilt related semantic models (SerialPart, JustInSequence) according to job params "aspects":[]

````mermaid
---
title: SingleLevelBomAsBuilt childItems contains asBuilt aspects hasAlternatives=false 
---
%%{init: { 'fontFamily': 'Architects daughter', 'theme': 'dark' , 'curve' : 'linear'} }%%
flowchart LR
    
    s1((AAS C-X 1 
    hasAlternatives=false))
    SingleLevelBomAsBuilt[SingleLevelBomAsBuilt2.0.0]
    
    
aas((AAS OEM)) --> 
SingleLevelBomAsBuilt --> s1
s1 --> SerialPart
s1 --> SingleLevelBomAsBuilt2
SingleLevelBomAsBuilt2 --> s4((AAS C-X 2
hasAlternatives=false))

````

## SingleLevelBomAsBuilt2 childItems contains asPlanned aspects hasAlternatives=false

1. IRS traversal using  SingleLevelBomAsBuilt
2. Traversal Aspect: SingleLevelBomAsBuilt
3. In case all items of array childItems of aspect SingleLevelBomAsBuilt are set to hasAlternatives=true:
   Provisioning of bomLifeCycle asBuilt related semantic models (SerialPart, JustInSequence) according to job params "aspects":[]
   Provisioning of bomLifeCycle asPlanned related semantic models (PartAsPlanned, PartSiteInformation) according to job params "aspects":[]
4. IRS stops traversal on asPlanned (SingleLevelBomAsPlanned)
5. IRS continue traversal on asBuilt (SingleLevelBomAsBuilt)
6. Create a tombstone: SingleLevelBomAsBuilt for "catenaXId" : "urn:uuid:*" with invalid state. ChildItems contains a mixture of part instance and part type. What is not permitted.

Despite the inadmissible state, the IRS returns the results. The business app is made aware of the situation by means of the tombstone. However, IRS job processing is not affected here - no changes are made to the status model.


````mermaid
---
title:  SingleLevelBomAsBuilt2 childItems contains asPlanned aspects hasAlternatives=false  
---
%%{init: { 'fontFamily': 'Architects daughter', 'theme': 'dark' , 'curve' : 'linear'} }%%
flowchart LR
    
    s1((AAS C-X 1
    hasAlternatives=true))
    s2((AAS C-X 2
    hasAlternatives=true))
    s3((AAS C-X 3
    hasAlternatives=true))
    SingleLevelBomAsBuilt[SingleLevelBomAsBuilt2.0.0]
    
    
aas((AAS OEM)) --> 
SingleLevelBomAsBuilt --> s1
SingleLevelBomAsBuilt --> s2 
SingleLevelBomAsBuilt --> s3

s2 --> stop((stop))
s3 --> stop((stop))

s1 --> SingleLevelBomAsBuilt3[SingleLevelBomAsBuilt]
SingleLevelBomAsBuilt3 -->  s4((AAS C-X 4))

classDef asPlanned fill:#9f6,stroke:#333,stroke-width:2px;
class s2,s3 asPlanned

classDef stop fill:#fff,stroke:#333,stroke-width:2px;


````

== SingleLevelBomAsBuilt childItems contains combination of hasAlternatives=false &=true

1. IRS traversal using  SingleLevelBomAsBuilt
2. Traversal Aspect: SingleLevelBomAsBuilt
3. In case all items of array childItems of aspect SingleLevelBomAsBuilt are set to hasAlternatives=true or false:
   Provisioning of bomLifeCycle asBuilt related semantic models (SerialPart, JustInSequence) according to job params "aspects":[]
   Provisioning of bomLifeCycle asPlanned related semantic models (PartAsPlanned, PartSiteInformation) according to job params "aspects":[]
4. Create a tombstone: SingleLevelBomAsBuilt for "catenaXId" : "urn:uuid:*" with invalid state. At least one ChildItems contains an incorrect state for property "hasAlternatives".  

Despite the inadmissible state, the IRS returns the results. The business app is made aware of the situation by means of the tombstone. However, IRS job processing is not affected here - no changes are made to the status model.

````mermaid
---
title:  SingleLevelBomAsBuilt2 childItems contains combination of hasAlternatives=false &=true  
---
%%{init: { 'fontFamily': 'Architects daughter', 'theme': 'dark' , 'curve' : 'linear'} }%%
flowchart LR
    
    s1((AAS C-X 1
    hasAlternatives=false))
    s2((AAS C-X 2
    hasAlternatives=true))
    s3((AAS C-X 3
    hasAlternatives=true))
    SingleLevelBomAsBuilt[SingleLevelBomAsBuilt2.0.0]
    
    
aas((AAS OEM)) --> 
SingleLevelBomAsBuilt --> s1
SingleLevelBomAsBuilt --> s2
SingleLevelBomAsBuilt --> s3
s1 --> SingleLevelBomAsBuilt3[SingleLevelBomAsBuilt]
s2 --> SingleLevelBomAsBuilt4[SingleLevelBomAsBuilt]
s3 --> SingleLevelBomAsBuilt5[SingleLevelBomAsBuilt]




````

# LOP
* [ ] Should the endpoint POST irs/jobs be extended by the default aspect PartAsPlanned? So that the interface can be called without specifying a parameter for "aspects"?

# Decision
 


