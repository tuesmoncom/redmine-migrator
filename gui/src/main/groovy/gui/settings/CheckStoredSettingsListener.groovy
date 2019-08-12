package gui.settings

import static org.viewaframework.util.ComponentFinder.find

import javax.swing.JSpinner
import javax.swing.JTextField
import javax.swing.JPasswordField

import gui.controller.*
import org.viewaframework.view.event.ViewContainerEvent
import org.viewaframework.view.event.DefaultViewContainerEventController

class CheckStoredSettingsListener extends GlassPaneAwareListener {

    @Override
    public void onViewInitBackActions(ViewContainerEvent event) {

        def textFieldFinder = find(JTextField).in(event.source)
        def passwordFinder = find(JPasswordField).in(event.source)
        def spinnerFinder = find(JSpinner).in(event.source)

        def addressField = textFieldFinder.named('redmineUrl')
        def apiKeyField = textFieldFinder.named('redmineApiKey')
        def timeoutField = spinnerFinder.named('redmineTimeout')
        def tuesmonUrl = textFieldFinder.named('tuesmonUrl')
        def tuesmonUsername = textFieldFinder.named('tuesmonUsername')
        def tuesmonPassword = passwordFinder.named('tuesmonPassword')

        def settings = new SettingsService().loadSettings()

        addressField.text = settings.redmineUrl
        apiKeyField.text = settings.redmineApiKey
        timeoutField.value = settings.redmineTimeout
        tuesmonUrl.text = settings.tuesmonUrl
        tuesmonUsername.text = settings.tuesmonUsername
        tuesmonPassword.text = settings.tuesmonPassword

    }

}
