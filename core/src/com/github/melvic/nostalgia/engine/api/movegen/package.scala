package com.github.melvic.nostalgia.engine.api

package object movegen extends File.implicits with Rank.implicits with Location.implicits {
  type Move = Move.Move

  lazy val Files: List[File] = File.Files
  lazy val Ranks: List[Rank] = Rank.Ranks
}
