package models.entities

case class User(id: Long,
                username: String,
                name: String,
                email: String,
                birthdate: java.sql.Timestamp,
                password: String,
                photo: String) extends BaseEntity

case class WatchListItem(id: Long,
                         idUser: Long,
                         idGame: Long,
                         creationDate: java.sql.Timestamp,
                         threshold: Int) extends BaseEntity

case class Offer(id: Long,
                 link: String,
                 idGame: Long,
                 idPlatform: Long,
                 fromDate: java.sql.Timestamp,
                 untilDate: java.sql.Timestamp,
                 normalPrice: Double,
                 offerPrice: Double) extends BaseEntity {
  def discount: Int = (100 - (offerPrice/normalPrice*100)).toInt
}

case class Game(id: Long,
                name: String,
                cover: String,
                publisher: String,
                rating: String,
                releaseDate: java.sql.Timestamp,
                typeGame: String) extends BaseEntity

case class GameCategory(id: Long,
                        idGame: Long,
                        idCategory: Long) extends BaseEntity

case class Category(id: Long,
                    name: String) extends BaseEntity {
  override def toString: String = name
}

case class GameGenre(id: Long,
                     idGame: Long,
                     idGenre: Long) extends BaseEntity

case class Genre(id: Long,
                 name: String) extends BaseEntity {
  override def toString: String = name
}

case class GamePlatform(id: Long,
                        idGame: Long,
                        idPlatform: Long) extends BaseEntity

case class Platform(id: Long,
                    name: String) extends BaseEntity {
  override def toString: String = name
}
