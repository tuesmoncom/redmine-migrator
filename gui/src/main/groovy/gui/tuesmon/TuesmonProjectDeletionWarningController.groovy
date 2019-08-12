package gui.tuesmon

import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import gui.controller.*
import org.viewaframework.controller.*

class TuesmonProjectDeletionWarningController extends AbstractOpenerController<ActionListener,ActionEvent> {

    TuesmonProjectDeletionWarningController() {
       super(new DeleteTuesmonProjectWarningView())
    }

    Class<ActionListener> getSupportedClass() {
        return ActionListener
    }

}
