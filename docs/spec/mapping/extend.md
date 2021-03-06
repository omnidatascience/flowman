---
layout: page
title: Flowman Extend Mapping
permalink: /spec/mapping/extend.html
---
# Extend Mapping

The `extend` mapping will add new columns derived from the existing ones (or with constant
values) to a mapping. All incoming columns will be kept.

## Example
```
mappings:
  facts_with_ids:
    kind: extend
    input: facts
    columns:
      placement_id: "CAST(placement AS INT)"
      lineitem_id: "CAST(SUBSTR(lineitem,5) AS INT)"
```

## Fields
* `kind` **(mandatory)** *(type: string)*: `extend`

* `broadcast` **(optional)** *(type: boolean)* *(default: false)*: 
Hint for broadcasting the result of this mapping for map-side joins.

* `cache` **(optional)** *(type: string)* *(default: NONE)*:
Cache mode for the results of this mapping. Supported values are
  * NONE
  * DISK_ONLY
  * MEMORY_ONLY
  * MEMORY_ONLY_SER
  * MEMORY_AND_DISK
  * MEMORY_AND_DISK_SER

* `input` **(mandatory)** *(type: string)*: 
Specifies the name of the input mapping.

* `columns` **(optional)** *(type: map:string)*:
Specifies a map of new column names with an expression for calculating them.

## Description
Essentially an `extend` mapping works like a simple SQL `SELECT` statement with all incoming
columns select by `*` and additional named columns. For example the mapping above would be
expressed in SQL as
```
SELECT
    *,
    CAST(placement AS INT) AS placement_id,
    CAST(SUBSTR(lineitem,5) AS INT) AS lineitem_id
FROM facts    
```
