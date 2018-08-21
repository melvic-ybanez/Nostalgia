package engine.utils

/**
  * Created by melvic on 8/8/18.
  */
object Masks {
  val NotAFile = 0xfefefefefefefefeL
  val NotHFile = 0x7f7f7f7f7f7f7f7fL

  lazy val Files = Array(
    0x0101010101010101L,
    0x0202020202020202L,
    0x0404040404040404L,
    0x0808080808080808L,
    0x1010101010101010L,
    0x2020202020202020L,
    0x4040404040404040L,
    0x8080808080808080L)

  lazy val Ranks = Array(
    0x00000000000000ffL,
    0x000000000000ff00L,
    0x0000000000ff0000L,
    0x00000000ff000000L,
    0x000000ff00000000L,
    0x0000ff0000000000L,
    0x00ff000000000000L,
    0xff00000000000000L)
}
