package gui.tuesmon

import gui.controller.*
import net.kaleidos.domain.Project

import org.viewaframework.annotation.*
import org.viewaframework.widget.view.*
import org.viewaframework.widget.view.ui.*
import org.viewaframework.view.DefaultViewContainer

@Controllers([
    @Controller(type=TuesmonProjectDeletionWarningController, pattern='deleteSelected'),
    @Controller(type=TuesmonProjectListController, pattern='refresh'),
])
class TuesmonProjectListView extends MasterViewEditor<Project> {

    static final ID = 'tuesmonProjectListViewID'

    TuesmonProjectListView() {
        super(ID, [
            new MasterViewColumn("id", 10),
            new MasterViewColumn("slug", 200),
            new MasterViewColumn("name", 200),
            new MasterViewColumn("description", 600),
        ])
    }

    Class<Project> getMasterType() {
        return Project
    }

}
