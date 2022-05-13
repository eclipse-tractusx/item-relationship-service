#!/usr/bin/python

#
#  Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
#
#  See the AUTHORS file(s) distributed with this work for additional
#  information regarding authorship.
#
#  See the LICENSE file(s) distributed with this work for
#  additional information regarding license terms.
#
import json
import os

if __name__ == "__main__":
    filepath = "OEMA_HYBRID_VEHICLES_10_fix18.json"

    submodel_server_1_folder = "BPNL00000003B0Q0"
    submodel_server_1_address = "http://localhost:1111"

    submodel_server_2_folder = "BPNL00000003AXS3"
    submodel_server_2_address = "http://localhost:2222"

    submodel_server_3_address = "http://localhost:3333"

    replaceURL = "http://provider.connector:port"

    # Opening JSON file
    f = open(filepath)
    data = json.load(f)
    f.close()

    testdata = data["https://catenax.io/schema/TestDataContainer/1.0.0"]

    dict_cxId_bpn = {}

    list_bpn = []
    dict_serial_parts = {}
    dict_assembly_parts = {}
    dict_aas = {}

    for tmp_data in testdata:
        tmp_keys = tmp_data.keys()

        for tmp_key in tmp_keys:
            if tmp_key == "https://catenax.io/schema/BPDM/1.1.0":
                list_bpn.append(tmp_data[tmp_key][0]["bpn"])

            elif tmp_key == "https://catenax.io/schema/SerialPartTypization/1.0.0":
                serial_part = tmp_data[tmp_key][0]

                part_bpn = ""
                for local_identifier in serial_part["localIdentifiers"]:
                    if local_identifier["key"] == "ManufacturerID":
                        part_bpn = local_identifier["value"]

                if part_bpn == "":
                    print("!!! ERROR: NO BPN FOUND !!!")

                dict_cxId_bpn[serial_part["catenaXId"]] = part_bpn

                if dict_serial_parts.get(part_bpn) is None:
                    dict_serial_parts[part_bpn] = []

                dict_serial_parts[part_bpn].append(serial_part)

            elif tmp_key == "https://catenax.io/schema/AssemblyPartRelationship/1.0.0":
                assembly_part = tmp_data[tmp_key][0]

                part_bpn = ""
                if dict_cxId_bpn.get(assembly_part["catenaXId"]) is not None:
                    part_bpn = dict_cxId_bpn[assembly_part["catenaXId"]]

                if part_bpn == "":
                    print("!!! ERROR: NO BPN FOUND !!!")

                if dict_assembly_parts.get(part_bpn) is None:
                    dict_assembly_parts[part_bpn] = []

                dict_assembly_parts[part_bpn].append(assembly_part)

            elif tmp_key == "https://catenax.io/schema/AAS/3.0":
                aas = tmp_data[tmp_key][0]

                part_bpn = ""
                if dict_cxId_bpn.get(aas.get("globalAssetId").get("value")[0]) is not None:
                    part_bpn = dict_cxId_bpn[aas.get("globalAssetId").get("value")[0]]

                if part_bpn == "":
                    print("!!! ERROR: NO BPN FOUND !!!")

                if dict_aas.get(part_bpn) is None:
                    dict_aas[part_bpn] = []

                dict_aas[part_bpn].append(aas)

    print(list_bpn)

    print("\n")

    dict_uuid_local_identifier = {}

    for tmp_bpn in list_bpn:
        folder_bpn = "%s" % tmp_bpn

        if not os.path.isdir(folder_bpn):
            os.mkdir(folder_bpn)

        ##########################
        # SERIAL PART TYPIZATION #
        if dict_serial_parts.get(tmp_bpn) is None:
            print("!!! ERROR: No parts for BPN %s" % tmp_bpn)
        else:
            print(tmp_bpn, len(dict_serial_parts[tmp_bpn]))

            f = open(folder_bpn + "/serialPartTypization.json", "w+")
            f.write("[\n")
            result = ",\n"
            json_list = []
            for tmp_json in dict_serial_parts[tmp_bpn]:
                json_list.append(json.dumps(tmp_json))
            f.write(result.join(json_list))
            f.write("\n]")
            f.close()

    print("\n")

    for tmp_bpn in list_bpn:
        folder_bpn = "%s" % tmp_bpn

        if not os.path.isdir(folder_bpn):
            os.mkdir(folder_bpn)

        ###############################
        # ASSEMBLY PARTS RELATIONSHIP #
        if dict_assembly_parts.get(tmp_bpn) is None:
            print("!!! ERROR: No assembly_part for BPN %s" % tmp_bpn)
        else:
            print(tmp_bpn, len(dict_assembly_parts[tmp_bpn]))

            f = open(folder_bpn + "/assemblyPartRelationship.json", "w+")
            f.write("[\n")
            result = ",\n"
            json_list = []
            for tmp_json in dict_assembly_parts[tmp_bpn]:
                json_list.append(json.dumps(tmp_json))
            f.write(result.join(json_list))
            f.write("\n]")
            f.close()

    print("\n")

    for tmp_bpn in list_bpn:
        folder_bpn = "%s" % tmp_bpn

        if not os.path.isdir(folder_bpn):
            os.mkdir(folder_bpn)

        ##############################
        # ASSET ADMINISTRATION SHELL #
        if dict_aas.get(tmp_bpn) is None:
            print("!!! ERROR: No aas for BPN %s" % tmp_bpn)
        else:
            print(tmp_bpn, len(dict_aas[tmp_bpn]))
            f = open(folder_bpn + "/aas.json", "w+")
            f.write("[\n")
            result = ",\n"
            json_list = []

            for tmp_json in dict_aas[tmp_bpn]:
                for descriptor in tmp_json["submodelDescriptors"]:
                    for endpoint in descriptor["endpoints"]:
                        address_ = endpoint["protocolInformation"]["endpointAddress"]
                        if tmp_bpn == submodel_server_1_folder:
                            endpoint["protocolInformation"]["endpointAddress"] = address_.replace(replaceURL, submodel_server_1_address)
                        elif tmp_bpn == submodel_server_2_folder:
                            endpoint["protocolInformation"]["endpointAddress"] = address_.replace(replaceURL, submodel_server_2_address)
                        else:
                            endpoint["protocolInformation"]["endpointAddress"] = address_.replace(replaceURL, submodel_server_3_address)
                json_list.append(json.dumps(tmp_json))
            f.write(result.join(json_list))
            f.write("\n]")
            f.close()

