package gui.tuesmon

import static org.viewaframework.util.ComponentFinder.find

import org.jdesktop.swingx.JXList

import java.awt.event.ActionEvent

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

import gui.swingx.JXBusyFeedbackLabel
import gui.tuesmon.TuesmonProjectListView
import gui.settings.SettingsService
import gui.migration.MigrationProgress
import gui.migration.MigrationProgressView
import gui.controller.DefaultActionViewControllerWorker

import groovy.util.logging.Log4j

import net.kaleidos.domain.Project
import net.kaleidos.tuesmon.TuesmonClient
import net.kaleidos.tuesmon.builder.ProjectBuilder

@Log4j
class DeleteTuesmonProjectController extends DefaultActionViewControllerWorker<Project> {

    @Override
    void preHandlingView(ViewContainer view, ActionEvent event) {
        viewManager.removeView(view)
        viewManager.addView(new MigrationProgressView())
    }

    @Override
    void handleView(ViewContainer view, ActionEvent event) {
        def model = locate(TuesmonProjectListView.ID).model
        def selectedProjectList = model.selectedObjects

        log.debug('Deleting selected projects')
        def total = selectedProjectList?.size()
        def service = new SettingsService()
        def settings = service.loadSettings()
        def tuesmonClient =
            new TuesmonClient(settings.tuesmonUrl)
                .authenticate(
                    settings.tuesmonUsername,
                    settings.tuesmonPassword)

        try {
            selectedProjectList.eachWithIndex { Project p, index ->
                log.debug("deleting ${p.name}")
                publish(
                    new MigrationProgress(
                        projectName: p.name,
                        progress: index.div(total)
                    )
                )
                tuesmonClient.deleteProject(new Project(id:p.id, name:p.name))
                log.debug("project ${p.name} deleted")
            }
        } catch (Throwable th) {
            publish(
                new MigrationProgress(
                    exception: th,
                    progress: 1.0
                )
            )
        }

        // This is a workaround
        // TODO move to tuesmon client.
        def userId = tuesmonClient.doGet('/api/v1/users/me').id
        def filteredProjectList =
             tuesmonClient
                .doGet("/api/v1/projects?member=$userId&page_size=500")
                .collect { new ProjectBuilder().build(it, null) }

        log.debug("Updating list")
        model.addAll(filteredProjectList)
        log.debug("All selected projects deleted")

        publish(new MigrationProgress(progress:1.0))
    }

    @Override
    void handleViewPublising(ViewContainer view, ActionEvent event, List<MigrationProgress> chunks) {
        def migrationProgress = chunks.first()
        def progressView = locate(MigrationProgressView).named(MigrationProgressView.ID)

        def progressBar = find(JProgressBar).in(progressView).named('migrationProgressBar')
        def loggingProgress = find(JLabel).in(progressView).named('loggingProgress')
        def closeButton = find(JButton).in(progressView).named('closeButton')
        def busyLabel = find(JXBusyFeedbackLabel).in(progressView).named('outputIconLabel')

        if (migrationProgress.exception) {
            log.error("Exception while migrating: ${migrationProgress.exception.message}")
            closeButton.enabled = true
            loggingProgress.text = "Migration Failed!!! Please check log"
            progressBar.setValue(100)
            progressBar.setBackground(java.awt.Color.RED)
            busyLabel.setFailure()
            setDeleteButtonEnabled(tuesmonProjectListView.model.rowCount > 0)
            return
        }

        if (migrationProgress.progress.intValue() == 1) {
            closeButton.enabled = true
            loggingProgress.text = "Task Finished!!!"
            progressBar.setValue(100)
            busyLabel.setSuccess()
            setDeleteButtonEnabled(tuesmonProjectListView.model.rowCount > 0)
            return
        }

        loggingProgress.text = "Deleting project: ${migrationProgress.projectName}"
        progressBar.setValue((migrationProgress.progress * 100).intValue())

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

}
