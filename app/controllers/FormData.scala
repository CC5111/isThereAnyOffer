package controllers

import org.joda.time.DateTime
case class GameData(name: String, publisher: String, developer: String, link: String, description: String,
                    rating: String, releaseDate: DateTime, gameType: String, video: String, score: String,
                    platform: PlatformData, category: CategoryData, store_ids: StoreData)
case class PlatformData(ps3: Boolean, ps4: Boolean, x360: Boolean, xone: Boolean, pc: Boolean)
case class CategoryData(single: Boolean, coop: Boolean, multi: Boolean)
case class StoreData(psStore: String, xboxStore: String, steam: String, gog: String)