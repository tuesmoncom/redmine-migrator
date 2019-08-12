package gui.tuesmon

import groovy.transform.InheritConstructors

import org.viewaframework.annotation.Controller
import org.viewaframework.annotation.Controllers
import org.viewaframework.annotation.Listener
import org.viewaframework.annotation.Listeners
import org.viewaframework.widget.controller.ExitActionController

import gui.warning.WarningView
import gui.controller.GlassPaneAwareListener

@Controllers([
    @Controller(type=ExitActionController,pattern='cancelButton'),
    @Controller(type=DeleteTuesmonProjectController, pattern='acceptButton')
])
@Listeners([
    @Listener(id="deleteWarningListenerID", type=GlassPaneAwareListener)
])
@InheritConstructors
class DeleteTuesmonProjectWarningView extends WarningView { }
