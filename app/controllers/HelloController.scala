package controllers

import javax.inject.Singleton

import play.api.mvc.{Action, Controller}

/**
  * Created by Pablo on 30-05-16.
  */

@Singleton
class HelloController extends Controller {

    def index(name: String) = Action { implicit request =>
        Ok(views.html.base(name))
    }
}
