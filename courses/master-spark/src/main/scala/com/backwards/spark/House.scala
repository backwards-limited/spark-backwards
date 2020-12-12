package com.backwards.spark

import java.util.Date
import org.apache.spark.sql.{Encoder, Encoders}

final case class House(id: Int, address: String, sqft: Int, price: Double, vacantBy: Date)

object House {
  implicit val houseEncoder: Encoder[House] =
    Encoders.bean(classOf[House])
}