package gui.settings

import static org.viewaframework.util.ComponentFinder.find

import java.awt.event.ActionEvent

import javax.swing.JButton
import javax.swing.JSpinner
import javax.swing.JMenuItem
import javax.swing.JTextField
import javax.swing.JPasswordField

import org.viewaframework.view.ViewContainer
import org.viewaframework.controller.AbstractActionController

class SettingsController extends AbstractActionController {

    @Override
    void handleView(ViewContainer view, ActionEvent event) {
        if (event.source.name == 'settingsCancelButton') {
            return
        }

        def textFieldFinder = find(JTextField).in(view)
        def spinnerFinder = find(JSpinner).in(view)
        def passwordFinder  = find(JPasswordField).in(view)

        def redmineUrl = textFieldFinder.named('redmineUrl')
        def redmineApiKey= textFieldFinder.named('redmineApiKey')
        def redmineTimeout = spinnerFinder.named('redmineTimeout')
        def tuesmonUrl = textFieldFinder.named('tuesmonUrl')
        def tuesmonUsername = textFieldFinder.named('tuesmonUsername')
        def tuesmonPassword =  passwordFinder.named('tuesmonPassword')

        def settings = new Settings(
            redmineUrl: redmineUrl.text,
            redmineApiKey: redmineApiKey.text,
            redmineTimeout: redmineTimeout.value,
            tuesmonUrl: tuesmonUrl.text,
            tuesmonUsername: tuesmonUsername.text,
            tuesmonPassword: new String(tuesmonPassword?.password)
        )

        new SettingsService().saveSettings(settings)

    }

    @Override
    void postHandlingView(ViewContainer view, ActionEvent event) {
        // removing settings view
        viewManager.removeView(view)
    }

}
