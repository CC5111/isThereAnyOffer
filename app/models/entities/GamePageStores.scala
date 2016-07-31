package models.entities

case class G2aStore(id: Long,
                    idGame: Long,
                    idStore: String) extends BaseEntity

case class GogStore(id: Long,
                    idGame: Long,
                    idStore: String) extends BaseEntity

case class PsStore(id: Long,
                   idGame: Long,
                   idStore: String) extends BaseEntity

case class SteamStore(id: Long,
                      idGame: Long,
                      idStore: String) extends BaseEntity

case class XboxStore(id: Long,
                     idGame: Long,
                     idStore: String) extends BaseEntity
