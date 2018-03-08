package com.dimajix.flowman.spec.schema

import org.apache.spark.sql.types.DataType


case object BinaryType extends FieldType {
    override def sparkType : DataType = org.apache.spark.sql.types.BinaryType

    override def parse(value:String) : Any = ???
    override def interpolate(value: FieldValue, granularity:String) : Iterable[Any] = ???
}
