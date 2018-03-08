package com.dimajix.flowman.spec.schema

import org.apache.spark.sql.types.DataType


case object BooleanType extends FieldType {
    override def sparkType : DataType = org.apache.spark.sql.types.BooleanType

    override def parse(value:String) : Any = value.toBoolean
    override def interpolate(value: FieldValue, granularity:String) : Iterable[Any] = {
        value match {
            case SingleValue(v) => Seq(parse(v))
            case ArrayValue(values) => values.map(parse)
            case RangeValue(start,end) => ???
        }
    }
}
