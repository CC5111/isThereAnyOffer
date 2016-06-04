package controllers

import play.api.mvc.{Action, Controller}

/**
  * Created by Pablo on 04-06-16.
  */
class TestControllers extends Controller{

    def index () = Action{ implicit  request =>
        Ok(views.html.home(title = "Is there any offers"))
    }

}
