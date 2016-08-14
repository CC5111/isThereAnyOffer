// para que te ande este ejemplo recuerda hacer tickear make aqui arriba xD
// NOTA: Recuerda eliminar este archivo si vas a mergear esta rama c:

import scraping.SteamScraping

//dar link
val link = "https://steamdb.info/sales/?merged=true"
//cargar url
val crawler = SteamScraping(link)

//obtener ofertas
val offers = crawler.offersWithDiscount()

//ejemplo de una oferta
val offer = offers.head

//datos locos
offer.id
offer.link
offer.normalPrice // calculado con el descuento y el precio de oferta
offer.offerPrice
offer.discount
offer.fromDate  // muy muy similar a como lo hiciste con las fechas en psActor
offer.untilDate // muy muy similar a como lo hiciste con las fechas en psActor
