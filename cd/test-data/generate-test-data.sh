#!/bin/bash

set -euo pipefail

env=$1
partition=$2

cd $env

sample_oneids="'BMW MUC', 'SCHAEFFLER', 'BOSCH', 'ZF'"
list_partitions_to_keep() {
   echo "("
   jq -r '.partitions[] | select (.key=="'$partition$'") | .OneIDs[] | "\'"+.+"\',"' ../../dataspace-partitions.json
   echo "$sample_oneids);"
}

# Purge data from PRS database
echo "DELETE from public.part_relationship WHERE oneidmanufacturer IN ('CAXLAPHGVJJFHZZZ', 'CAXLBRHHQAJAIOZZ','CAXLCPOZSGFCTJZZ','CAXLHNJURNRLPCZZ','CAXLJXFARPBQZQZZ','CAXLNDDMHMMNCOZZ','CAXLTHAJNAHZXGZZ','CAXLXCLMNDAAWEZZ','CAXLXZZDURIFEUZZ','CAXLYSHKEZTCKAZZ','CAXLZJVJEBYWYYZZ','CAXSJRTGOPVESVZZ','CAXSPGQORIGHFAZZ','CAXSWPFTJQEVZNZZ','CAXSZJVJEBYWYYZZ') OR parent_oneidmanufacturer IN ('CAXLAPHGVJJFHZZZ', 'CAXLBRHHQAJAIOZZ','CAXLCPOZSGFCTJZZ','CAXLHNJURNRLPCZZ','CAXLJXFARPBQZQZZ','CAXLNDDMHMMNCOZZ','CAXLTHAJNAHZXGZZ','CAXLXCLMNDAAWEZZ','CAXLXZZDURIFEUZZ','CAXLYSHKEZTCKAZZ','CAXLZJVJEBYWYYZZ','CAXSJRTGOPVESVZZ','CAXSPGQORIGHFAZZ','CAXSWPFTJQEVZNZZ','CAXSZJVJEBYWYYZZ');"
echo "DELETE from public.part_aspect WHERE public.part_aspect.oneidmanufacturer IN ('CAXLAPHGVJJFHZZZ', 'CAXLBRHHQAJAIOZZ','CAXLCPOZSGFCTJZZ','CAXLHNJURNRLPCZZ','CAXLJXFARPBQZQZZ','CAXLNDDMHMMNCOZZ','CAXLTHAJNAHZXGZZ','CAXLXCLMNDAAWEZZ','CAXLXZZDURIFEUZZ','CAXLYSHKEZTCKAZZ','CAXLZJVJEBYWYYZZ','CAXSJRTGOPVESVZZ','CAXSPGQORIGHFAZZ','CAXSWPFTJQEVZNZZ','CAXSZJVJEBYWYYZZ');"
echo "DELETE from public.part_attribute WHERE oneidmanufacturer IN ('CAXLAPHGVJJFHZZZ', 'CAXLBRHHQAJAIOZZ','CAXLCPOZSGFCTJZZ','CAXLHNJURNRLPCZZ','CAXLJXFARPBQZQZZ','CAXLNDDMHMMNCOZZ','CAXLTHAJNAHZXGZZ','CAXLXCLMNDAAWEZZ','CAXLXZZDURIFEUZZ','CAXLYSHKEZTCKAZZ','CAXLZJVJEBYWYYZZ','CAXSJRTGOPVESVZZ','CAXSPGQORIGHFAZZ','CAXSWPFTJQEVZNZZ','CAXSZJVJEBYWYYZZ');"

# Generate SQL to load part_relationship data (parent-child relationships)
echo 'COPY public.part_relationship (oneidmanufacturer, objectidmanufacturer, parent_oneidmanufacturer, parent_objectidmanufacturer, part_relationship_list_id, upload_date_time) FROM stdin CSV;'
jq -r '(now | strftime("%Y-%m-%dT%H:%M:%S%z")) as $n | .[].relationships | .[].relationship | [.child.oneIDManufacturer, .child.objectIDManufacturer, .parent
.oneIDManufacturer, .parent.objectIDManufacturer, "78F4BB1B-2EBB-418C-9C16-3E74BACCBEAC", $n] | @csv' "PartRelationshipUpdateList.json" | sort | uniq
echo '\.'
echo 'DELETE FROM public.part_relationship WHERE parent_oneidmanufacturer NOT IN'
list_partitions_to_keep

# Generate SQL to load part_aspect data (aspect URLs)
echo 'COPY public.part_aspect (name, oneidmanufacturer, objectidmanufacturer, url, effect_time, last_modified_time) FROM stdin CSV;'
jq -r '(now | strftime("%Y-%m-%dT%H:%M:%S%z")) as $n | .[] | .part as $p | .aspects[] | [.name, $p.oneIDManufacturer, $p.objectIDManufacturer, .url, $n, $n]
| @csv' "PartAspectUpdate.json" | sort | uniq
echo '\.'
echo 'DELETE FROM public.part_aspect WHERE oneidmanufacturer NOT IN'
list_partitions_to_keep

# Generate SQL to load part_attribute data (partTypeName field)
echo 'COPY public.part_attribute (attribute, oneidmanufacturer, objectidmanufacturer, value, effect_time, last_modified_time) FROM stdin CSV;'
jq -r '(now | strftime("%Y-%m-%dT%H:%M:%S%z")) as $n | .[] | .part as $p | ["partTypeName", $p.oneIDManufacturer, $p.objectIDManufacturer, .partTypeName, $n,
 $n] | @csv' "PartTypeNameUpdate.json" | sort | uniq
echo '\.'
echo 'DELETE FROM public.part_attribute WHERE oneidmanufacturer NOT IN'
list_partitions_to_keep
