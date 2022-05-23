#!/usr/bin/python

import json
import os

if __name__ == "__main__":
    filepath = "220513_CatenaX_Testdata_v1.1.json"

    # Opening JSON file
    f = open(filepath)
    data = json.load(f)
    f.close()

    testdata = data["https://catenax.io/schema/TestDataContainer/1.0.0"]

    dict_cxId_bpn = {}

    list_bpn = []
    dict_serial_parts = {}
    dict_assemply_parts = {}

    counter = 0

    for tmp_data in testdata:
        tmp_keys = tmp_data.keys()

        for tmp_key in tmp_keys:
            if tmp_key == "https://catenax.io/schema/SerialPartTypization/1.0.0":
                serial_part = tmp_data[tmp_key][0]

                part_bpn = ""
                for local_identifier in serial_part["localIdentifiers"]:
                    if local_identifier["key"] == "ManufacturerID":
                        part_bpn = local_identifier["value"]

                if part_bpn == "":
                    print("!!! ERROR: NO BPN FOUND !!!")

                dict_cxId_bpn[serial_part["catenaXId"]] = part_bpn

                if dict_serial_parts.get(part_bpn) == None:
                    dict_serial_parts[part_bpn] = []

                dict_serial_parts[part_bpn].append(serial_part)

                if part_bpn not in list_bpn:
                    list_bpn.append(part_bpn)

            elif tmp_key == "https://catenax.io/schema/AssemblyPartRelationship/1.0.0":
                assemply_part = tmp_data[tmp_key][0]
                counter += 1

                part_bpn = ""
                if dict_cxId_bpn.get(assemply_part["catenaXId"]) != None:
                    part_bpn = dict_cxId_bpn[assemply_part["catenaXId"]]

                if part_bpn == "":
                    print("!!! ERROR: NO BPN FOUND !!!")

                if dict_assemply_parts.get(part_bpn) == None:
                    dict_assemply_parts[part_bpn] = []

                dict_assemply_parts[part_bpn].append(assemply_part)

    print(list_bpn)

    print("\n")

    dict_uuid_local_identifier = {}

    for tmp_bpn in list_bpn:
        folder_bpn = "%s"%tmp_bpn

        if not os.path.isdir(folder_bpn):
            os.mkdir(folder_bpn)

        ############################
        ## SERIAL PART TYPIZATION ##
        if dict_serial_parts.get(tmp_bpn) == None:
            print("!!! ERROR: No parts for BPN %s"%tmp_bpn)
        else:
            print(tmp_bpn, len(dict_serial_parts[tmp_bpn]))

            f = open(folder_bpn+"/serialPartTypization.csv", "w+")
            f.write("UUID;part_instance_id;manufacturing_date;manufacturing_country;manufacturer_part_id;customer_part_id;classification;name_at_manufacturer;name_at_customer;optional_identifier_key;optional_identifier_value\n")

            for tmp_json in dict_serial_parts[tmp_bpn]:
                UUID = tmp_json["catenaXId"]

                part_instance_id = ""
                optional_identifier_key = ""
                optional_identifier_value = ""
                for local_identifier in tmp_json["localIdentifiers"]:
                    if local_identifier["key"] == "PartInstanceID":
                        part_instance_id = local_identifier["value"]
                    elif local_identifier["key"] == "VAN":
                        optional_identifier_key = "VAN"
                        optional_identifier_value = local_identifier["value"]
                    #elif BATCH

                manufacturing_date = tmp_json["manufacturingInformation"]["date"]
                manufacturing_country = tmp_json["manufacturingInformation"]["country"]
                manufacturer_part_id = tmp_json["partTypeInformation"]["manufacturerPartID"]
                
                customer_part_id = ""
                if tmp_json["partTypeInformation"].get("customerPartId") != None:
                    customer_part_id = tmp_json["partTypeInformation"]["customerPartId"]

                classification = tmp_json["partTypeInformation"]["classification"]
                name_at_manufacturer = tmp_json["partTypeInformation"]["nameAtManufacturer"]

                name_at_customer = ""
                if tmp_json["partTypeInformation"].get("nameAtCustomer") != None:
                    name_at_customer = tmp_json["partTypeInformation"]["nameAtCustomer"]

                dict_uuid_local_identifier[UUID] = [part_instance_id, manufacturer_part_id, optional_identifier_key, optional_identifier_value]

                f.write("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s\n"%(UUID,part_instance_id,manufacturing_date,manufacturing_country,manufacturer_part_id,customer_part_id,classification,name_at_manufacturer,name_at_customer,optional_identifier_key,optional_identifier_value))
            f.close()

    print("\n")

    for tmp_bpn in list_bpn:
        folder_bpn = "%s"%tmp_bpn

        if not os.path.isdir(folder_bpn):
            os.mkdir(folder_bpn)

        #################################
        ## ASSEMBLY PARTS RELATIONSHIP ##
        if dict_assemply_parts.get(tmp_bpn) == None:
            print("!!! ERROR: No assemply_part for BPN %s"%tmp_bpn)
        else:
            print(tmp_bpn, len(dict_assemply_parts[tmp_bpn]))

            f = open(folder_bpn+"/assemblyPartRelationship.csv", "w+")
            f.write("parent_UUID;parent_part_instance_id;parent_manufacturer_part_id;parent_optional_identifier_key;parent_optional_identifier_value;UUID;part_instance_id;manufacturer_part_id;optional_identifier_key;optional_identifier_value;lifecycle_context;quantity_number;measurement_unit_lexical_value;datatype_URI;assembled_on\n")

            for tmp_json in dict_assemply_parts[tmp_bpn]:
                parent_UUID = tmp_json["catenaXId"]

                parent_part_instance_id = dict_uuid_local_identifier[parent_UUID][0]
                parent_manufacturer_part_id = dict_uuid_local_identifier[parent_UUID][1]
                parent_optional_identifier_key = dict_uuid_local_identifier[parent_UUID][2]
                parent_optional_identifier_value = dict_uuid_local_identifier[parent_UUID][3]

                if tmp_json.get("childParts") != None:
                    children_parts = tmp_json["childParts"]

                    for tmp_child in children_parts:
                        UUID = tmp_child["childCatenaXId"]

                        part_instance_id = ""
                        manufacturer_part_id = ""
                        optional_identifier_key = ""
                        optional_identifier_value = ""
                        if dict_uuid_local_identifier.get(UUID) != None:
                            part_instance_id = dict_uuid_local_identifier[UUID][0]
                            manufacturer_part_id = dict_uuid_local_identifier[UUID][1]
                            optional_identifier_key = dict_uuid_local_identifier[UUID][2]
                            optional_identifier_value = dict_uuid_local_identifier[UUID][3]

                        lifecycle_context = tmp_child["lifecycleContext"]
                        quantity_number = tmp_child["quantity"]["quantityNumber"]
                        measurement_unit_lexical_value = tmp_child["quantity"]["measurementUnit"]["lexicalValue"]
                        datatype_URI = tmp_child["quantity"]["measurementUnit"]["datatypeURI"]
                        assembled_on = tmp_child["assembledOn"]


                        f.write("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s\n"%(parent_UUID,parent_part_instance_id,parent_manufacturer_part_id,parent_optional_identifier_key,parent_optional_identifier_value,UUID,part_instance_id,manufacturer_part_id,optional_identifier_key,optional_identifier_value,lifecycle_context,quantity_number,measurement_unit_lexical_value,datatype_URI,assembled_on))
            f.close()