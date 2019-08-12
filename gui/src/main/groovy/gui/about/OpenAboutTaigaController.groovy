package gui.about

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import org.viewaframework.controller.AbstractOpenerController

class OpenAboutTuesmonController extends AbstractOpenerController<ActionListener,ActionEvent> {

    OpenAboutTuesmonController() {
       super(new AboutTuesmonView())
    }

    Class<ActionListener> getSupportedClass() {
        return ActionListener
    }

}
