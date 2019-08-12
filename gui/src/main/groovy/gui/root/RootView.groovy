package gui.root

import gui.about.OpenAboutTuesmonController
import gui.redmine.RedmineProjectListController
import gui.tuesmon.TuesmonProjectListController

import org.viewaframework.annotation.Controller
import org.viewaframework.annotation.Controllers
import org.viewaframework.annotation.Listener
import org.viewaframework.annotation.Listeners
import org.viewaframework.view.DefaultViewContainerFrame
import org.viewaframework.widget.controller.ExitActionController

@Controllers([
    @Controller(type=OpenExitWarningController,pattern=ExitActionController.EXIT_PATTERN),
    @Controller(type=OpenSettingsController, pattern = 'settings'),
    @Controller(type=RedmineProjectListController, pattern = 'redmine'),
    @Controller(type=TuesmonProjectListController, pattern = 'tuesmon'),
    @Controller(type=OpenAboutTuesmonController, pattern = 'aboutTuesmon'),
])
@Listeners([
    @Listener(id='translucent',type=RootViewListener)
])
class RootView extends DefaultViewContainerFrame { }
