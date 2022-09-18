package domain

sealed abstract case class Word private (word: String) {
  val length: Int = word.length
  def contains(char: Char): Boolean = word.contains(char)
  def toList: List[Char] = word.toList
  def toSet: Set[Char] = word.toSet
}

object Word {
  def make(word: String): Option[Word] =
    if (word.nonEmpty && word.forall(_.isLetter)) Some(new Word(word.toLowerCase) {}) else None
}
