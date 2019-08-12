package gui.tuesmon

import static org.viewaframework.util.ComponentFinder.find

import org.jdesktop.swingx.JXList

import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import javax.swing.JLabel
import javax.swing.JButton
import javax.swing.JTextField
import javax.swing.JProgressBar
import javax.swing.DefaultListModel

import org.viewaframework.view.*
import org.viewaframework.util.*
import org.viewaframework.controller.*
import org.viewaframework.view.perspective.*
import org.viewaframework.widget.view.*

import net.kaleidos.domain.Project
import net.kaleidos.tuesmon.TuesmonClient
import net.kaleidos.tuesmon.builder.ProjectBuilder

import gui.settings.SettingsService
import gui.controller.DefaultViewControllerWorker

class TuesmonProjectListController extends
    DefaultViewControllerWorker<ActionListener, ActionEvent, String, Project> {

    @Override
    Class<ActionListener> getSupportedClass() {
        return ActionListener
    }

    @Override
    void preHandlingView(ViewContainer view, ActionEvent event) {
        updateStatus("Loading tuesmon list...", 50)
        def projectsView = getProjectListView()

        if (!projectsView) {
            projectsView = new TuesmonProjectListView()
            viewManager.addView(projectsView , PerspectiveConstraint.RIGHT)
        }

        projectsView.model.clear()
        locateRootView().rootPane.glassPane.visible = true
    }

    ViewContainer getProjectListView() {
        return locate(TuesmonProjectListView).named(TuesmonProjectListView.ID)
    }

    @Override
    void handleView(ViewContainer view, ActionEvent event) {
        def service = new SettingsService()
        def settings = service.loadSettings()

        def tuesmonClient =
            new TuesmonClient(settings.tuesmonUrl)
            .authenticate(
                settings.tuesmonUsername,
                settings.tuesmonPassword
            )

        // This is a workaround
        // TODO move to tuesmon client.
        def userId = tuesmonClient.doGet('/api/v1/users/me').id
        def filteredProjectList =
             tuesmonClient
                .doGet("/api/v1/projects?member=$userId&page_size=500")
                .collect { new ProjectBuilder().build(it, null) }

        publish(filteredProjectList)

    }

    @Override
    void handleViewPublising(ViewContainer view, ActionEvent event, List<Project> chunks) {
        locate(TuesmonProjectListView)
            .named(TuesmonProjectListView.ID)
            .model
            .addAll(chunks.flatten())

    }

    @Override
    void postHandlingView(ViewContainer viewContainer, ActionEvent event) {
        def rows = tuesmonProjectListView.model.rowCount
        locateRootView().rootPane.glassPane.visible = false

        if (!rows) {
            updateStatus("No Tuesmon projects found", 0)
            setDeleteButtonEnabled(false)
            return
        }

        setDeleteButtonEnabled(true)
        updateStatus("Showing $rows Tuesmon projects ", 0)
    }

    void setDeleteButtonEnabled(boolean enabled) {
        find(JButton)
            .in(tuesmonProjectListView)
            .named('deleteSelected')
            .setEnabled(enabled)
    }

    TuesmonProjectListView getTuesmonProjectListView() {
        locate(TuesmonProjectListView).named(TuesmonProjectListView.ID)
    }

    void updateStatus(String message, Integer progress) {
        def progressBar = find(JProgressBar).in(viewManager.rootView).named(StatusBar.STATUS_BAR_NAME)
        def label = find(JLabel).in(viewManager.rootView).named(StatusBar.LEFT_PANEL_LABEL)

        progressBar.value = progress
        label.text = message
    }

}
