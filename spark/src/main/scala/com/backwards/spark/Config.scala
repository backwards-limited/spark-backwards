package com.backwards.spark

import monocle.Lens
import monocle.macros.GenLens

final case class Config(input: String = "", employees: String = "", format: String = "", output: String = "")

object Config {
  val inputL: Lens[Config, String] =
    GenLens[Config](_.input)

  val employeesL: Lens[Config, String] =
    GenLens[Config](_.employees)

  val formatL: Lens[Config, String] =
    GenLens[Config](_.format)

  val outputL: Lens[Config, String] =
    GenLens[Config](_.output)
}